package com.nklcbdty.batch.nklcbdty.batch.linkvalidator.liveness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.nklcbdty.common.vo.Job_mst;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 카카오 영입(careers.kakao.com) 공고 생존 판정.
 *
 * <p>상세페이지(careers.kakao.com/jobs/{realId})는 클라이언트 렌더링이라 원본 HTML 이 1.5KB 짜리
 * 셸이고 공고명이 없다. 그래서 HTML 문자열 매칭으로는 살아있는 공고도 전부 종료로 찍혔다 —
 * 2026-09 기준 careers.kakao.com 출처 220행이 매일 전멸했고, 그 중 33건은 실제로 열려 있었다.
 * 대신 크롤러가 쓰는 것과 같은 채용 API 에서 열려 있는 공고 목록을 받아 판정한다.</p>
 *
 * <p>대조 키는 {@code annoId}(jobOfferId) 가 아니라 {@code realId}("P-14324"/"S-4752") 다.
 * 두 가지 이유가 있다. 본사(P)와 공동체(S)의 jobOfferId 는 서로 다른 번호 체계라 숫자만으로는
 * 같은 값이 양쪽에 있을 수 있고, {@code company=ALL} 로 받으면 응답의 {@code jobOfferId} 가
 * 0 으로 내려와 쓸 수가 없다. realId 는 크롤러가 만들어 둔 상세 링크에 그대로 들어 있다.</p>
 *
 * <p>목록은 직군(part) 전체를 훑는다. 크롤러는 TECHNOLOGY 만 적재하지만, 공고가 직군을 옮겨도
 * 살아있는 건 살아있는 것이므로 넓게 받아야 오탐(살아있는데 종료 처리)이 없다.</p>
 */
@Component
@Slf4j
public class KakaoLivenessChecker implements CompanyLivenessChecker {

    static final String CAREER_HOST = "careers.kakao.com";

    /** company=ALL 이면 본사(P)·공동체(S)가 한 번에 내려온다. page 는 1부터. */
    private static final String JOB_LIST_URL_FORMAT =
        "https://careers.kakao.com/public/api/job-list"
            + "?skillSet=&part=%s&company=ALL&keyword=&employeeType=&page=%d";

    /** 직군 코드 목록. 하드코딩해 두면 카카오가 직군을 늘렸을 때 그 직군 공고가 통째로 종료 처리된다. */
    private static final String JOBS_ATTRIBUTE_URL = "https://careers.kakao.com/public/api/jobs-attribute";

    /** 직군 목록을 못 받았을 때 쓰는 2026-09 기준 값. 목록 조회까지 같이 실패하면 어차피 UNKNOWN 이다. */
    private static final List<String> FALLBACK_JOB_TYPES =
        List.of("TECHNOLOGY", "BUSINESS_SERVICES", "DESIGN", "STAFF");

    /** 상세 링크에서 realId 를 뽑는다. 크롤러가 "/jobs/{P|S}-{jobOfferId}?..." 형태로 만들어 둔다. */
    private static final Pattern REAL_ID_PATTERN = Pattern.compile("/jobs/([PS]-\\d+)");

    private static final int MAX_PAGES = 20;

    private static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /** 배치 한 번 도는 동안은 같은 스냅샷을 쓴다. 공고 건수만큼 API 를 때리지 않기 위함. */
    private static final long SNAPSHOT_TTL_MS = TimeUnit.MINUTES.toMillis(10);

    private final Function<String, String> fetcher;

    private Set<String> liveRealIdSnapshot;
    private long snapshotTakenAt;

    public KakaoLivenessChecker() {
        this(defaultFetcher());
    }

    // 테스트에서 HTTP 를 걷어내기 위한 생성자.
    KakaoLivenessChecker(Function<String, String> fetcher) {
        this.fetcher = fetcher;
    }

    private static Function<String, String> defaultFetcher() {
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

        return url -> {
            Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json, text/plain, */*")
                .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.warn("카카오 채용 API 응답 실패 status={} url={}", response.code(), url);
                    return null;
                }
                ResponseBody body = response.body();
                return body == null ? null : body.string();
            } catch (Exception e) {
                log.warn("카카오 채용 API 호출 실패 url={} - {}", url, e.getMessage());
                return null;
            }
        };
    }

    /**
     * careers.kakao.com 에서 받아온 공고만 본다.
     *
     * <p>company_cd 로 잡으면 안 된다. 카카오뱅크·카카오게임즈·카카오헬스케어·카카오페이증권·
     * 카카오모빌리티는 같은 {@code KAKAO} 코드를 쓰지만 채용 사이트가 각자 따로이고, 그쪽
     * 상세페이지는 서버 렌더링이라 지금의 HTML 매칭으로 잘 판정된다.</p>
     *
     * <p>링크 모양이 달라 realId 를 못 뽑는 행도 일단 받는다. 이 호스트에서는 HTML 매칭이
     * 틀린다는 것을 알고 있으니, 그대로 흘려보내 종료로 찍히게 두는 것보다 UNKNOWN 이 낫다.</p>
     */
    @Override
    public boolean supports(Job_mst job) {
        if (job == null) {
            return false;
        }
        String link = job.getJobDetailLink();
        return link != null && link.contains(CAREER_HOST);
    }

    @Override
    public Liveness check(Job_mst job) {
        String realId = job == null ? null : realIdOf(job.getJobDetailLink());
        if (realId == null) {
            // 대조할 키가 없으면 판정하지 않는다.
            return Liveness.UNKNOWN;
        }

        Set<String> liveRealIds = liveRealIds();
        if (liveRealIds.isEmpty()) {
            // 통신 실패거나 응답이 통째로 비었다. 진짜 0건일 수도 있지만 그 경우와 장애를
            // 구분할 수 없으므로 종료 처리하지 않는다. (전멸 방지)
            return Liveness.UNKNOWN;
        }

        return liveRealIds.contains(realId) ? Liveness.ALIVE : Liveness.CLOSED;
    }

    private String realIdOf(String jobDetailLink) {
        if (jobDetailLink == null) {
            return null;
        }
        Matcher matcher = REAL_ID_PATTERN.matcher(jobDetailLink);
        return matcher.find() ? matcher.group(1) : null;
    }

    private synchronized Set<String> liveRealIds() {
        long now = System.currentTimeMillis();
        if (liveRealIdSnapshot != null && now - snapshotTakenAt < SNAPSHOT_TTL_MS) {
            return liveRealIdSnapshot;
        }

        Set<String> collected = fetchAllLiveRealIds();
        if (collected.isEmpty()) {
            // 실패한 스냅샷은 캐시하지 않는다. 다음 공고에서 다시 시도할 수 있어야 한다.
            return Collections.emptySet();
        }

        liveRealIdSnapshot = Collections.unmodifiableSet(collected);
        snapshotTakenAt = now;
        log.info("카카오 채용 API 스냅샷 갱신 — 열려 있는 공고 {}건", liveRealIdSnapshot.size());
        return liveRealIdSnapshot;
    }

    private Set<String> fetchAllLiveRealIds() {
        Set<String> realIds = new HashSet<>();

        for (String jobType : jobTypes()) {
            int totalPages = 1;
            for (int page = 1; page <= totalPages && page <= MAX_PAGES; page++) {
                String rawJson = fetcher.apply(String.format(JOB_LIST_URL_FORMAT, jobType, page));
                if (rawJson == null || rawJson.isBlank()) {
                    // 일부만 받은 목록으로 판정하면 못 받은 쪽 공고가 전부 종료 처리된다.
                    return Collections.emptySet();
                }

                JSONObject response;
                JSONArray jobList;
                try {
                    response = new JSONObject(rawJson);
                    jobList = response.optJSONArray("jobList");
                } catch (Exception e) {
                    log.warn("카카오 채용 API 응답 파싱 실패 (part={}, page={}): {}", jobType, page, e.getMessage());
                    return Collections.emptySet();
                }
                if (jobList == null) {
                    log.warn("카카오 채용 API 응답에 jobList 없음 (part={}, page={})", jobType, page);
                    return Collections.emptySet();
                }

                for (int i = 0; i < jobList.length(); i++) {
                    JSONObject item = jobList.optJSONObject(i);
                    String realId = item == null ? null : item.optString("realId", null);
                    if (realId != null && !realId.isBlank()) {
                        realIds.add(realId);
                    }
                }

                totalPages = response.optInt("totalPage", 1);
            }
        }

        return realIds;
    }

    /** 직군 코드. 못 받으면 알려진 값으로 돈다 — 목록 자체가 안 되면 어차피 빈 스냅샷이 된다. */
    private List<String> jobTypes() {
        String rawJson = fetcher.apply(JOBS_ATTRIBUTE_URL);
        if (rawJson == null || rawJson.isBlank()) {
            return FALLBACK_JOB_TYPES;
        }

        try {
            JSONArray jobTypeList = new JSONObject(rawJson).optJSONArray("jobTypeList");
            if (jobTypeList == null) {
                return FALLBACK_JOB_TYPES;
            }

            List<String> jobTypes = new ArrayList<>();
            for (int i = 0; i < jobTypeList.length(); i++) {
                JSONObject item = jobTypeList.optJSONObject(i);
                if (item == null || !item.optBoolean("useFlag", true)) {
                    continue;
                }
                String jobType = item.optString("jobType", null);
                if (jobType != null && !jobType.isBlank()) {
                    jobTypes.add(jobType);
                }
            }
            return jobTypes.isEmpty() ? FALLBACK_JOB_TYPES : jobTypes;
        } catch (Exception e) {
            log.warn("카카오 직군 목록 조회 실패 — 알려진 값으로 진행: {}", e.getMessage());
            return FALLBACK_JOB_TYPES;
        }
    }
}
