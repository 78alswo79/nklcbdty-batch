package com.nklcbdty.batch.nklcbdty.batch.linkvalidator.liveness;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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
 * 배민(우아한형제들) 공고 생존 판정.
 *
 * <p>상세페이지(career.woowahan.com/recruitment/{recruitNumber}/detail)는 Next.js 클라이언트
 * 렌더링이라 원본 HTML 에 공고명이 없다. 그래서 HTML 문자열 매칭으로는 살아있는 공고도 전부
 * 종료로 찍힌다. 대신 크롤러가 쓰는 것과 같은 채용 API 에서 열려 있는 공고 목록을 받아
 * {@code annoId}(= recruitSeq) 포함 여부로 판정한다.</p>
 *
 * <p>목록은 직군 필터 없이 전부 받는다. 크롤러는 개발 직군(BA005001)만 적재하지만, 공고가 직군을
 * 옮겨도 살아있는 건 살아있는 것이므로 넓게 받아야 오탐(살아있는데 종료 처리)이 없다.</p>
 */
@Component
@Slf4j
public class BaeminLivenessChecker implements CompanyLivenessChecker {

    static final String CAREER_HOST = "career.woowahan.com";
    private static final String COMPANY_CD = "BAEMIN";

    /** 직군 필터 없이 전체 공고. size=100 이면 현재(58건) 한 페이지에 들어오지만 페이징도 따라간다. */
    private static final String API_URL_FORMAT =
        "https://career.woowahan.com/w1/recruits?recruitCampaignSeq=0&page=%d&size=100";
    private static final int MAX_PAGES = 20;

    private static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /** 배치 한 번 도는 동안은 같은 스냅샷을 쓴다. 공고 건수만큼 API 를 때리지 않기 위함. */
    private static final long SNAPSHOT_TTL_MS = TimeUnit.MINUTES.toMillis(10);

    private final Function<String, String> fetcher;

    private Set<String> liveSeqSnapshot;
    private long snapshotTakenAt;

    public BaeminLivenessChecker() {
        this(defaultFetcher());
    }

    // 테스트에서 HTTP 를 걷어내기 위한 생성자.
    BaeminLivenessChecker(Function<String, String> fetcher) {
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
                    log.warn("배민 채용 API 응답 실패 status={} url={}", response.code(), url);
                    return null;
                }
                ResponseBody body = response.body();
                return body == null ? null : body.string();
            } catch (Exception e) {
                log.warn("배민 채용 API 호출 실패 url={} - {}", url, e.getMessage());
                return null;
            }
        };
    }

    @Override
    public boolean supports(Job_mst job) {
        if (job == null) {
            return false;
        }
        if (COMPANY_CD.equalsIgnoreCase(job.getCompanyCd())) {
            return true;
        }
        // company_cd 가 비어 있는 예전 행도 링크 도메인으로 잡아낸다.
        String link = job.getJobDetailLink();
        return link != null && link.contains(CAREER_HOST);
    }

    @Override
    public Liveness check(Job_mst job) {
        String annoId = job == null ? null : job.getAnnoId();
        if (annoId == null || annoId.isBlank()) {
            // 대조할 키가 없으면 판정하지 않는다.
            return Liveness.UNKNOWN;
        }

        Set<String> liveSeqs = liveSeqs();
        if (liveSeqs.isEmpty()) {
            // 통신 실패거나 응답이 통째로 비었다. 진짜 0건일 수도 있지만 그 경우와 장애를
            // 구분할 수 없으므로 종료 처리하지 않는다. (전멸 방지)
            return Liveness.UNKNOWN;
        }

        return liveSeqs.contains(annoId.trim()) ? Liveness.ALIVE : Liveness.CLOSED;
    }

    private synchronized Set<String> liveSeqs() {
        long now = System.currentTimeMillis();
        if (liveSeqSnapshot != null && now - snapshotTakenAt < SNAPSHOT_TTL_MS) {
            return liveSeqSnapshot;
        }

        Set<String> collected = fetchAllLiveSeqs();
        if (collected.isEmpty()) {
            // 실패한 스냅샷은 캐시하지 않는다. 다음 공고에서 다시 시도할 수 있어야 한다.
            return Collections.emptySet();
        }

        liveSeqSnapshot = Collections.unmodifiableSet(collected);
        snapshotTakenAt = now;
        log.info("배민 채용 API 스냅샷 갱신 — 열려 있는 공고 {}건", liveSeqSnapshot.size());
        return liveSeqSnapshot;
    }

    private Set<String> fetchAllLiveSeqs() {
        Set<String> seqs = new HashSet<>();

        for (int page = 0; page < MAX_PAGES; page++) {
            String rawJson = fetcher.apply(String.format(API_URL_FORMAT, page));
            if (rawJson == null || rawJson.isBlank()) {
                // 첫 페이지부터 실패면 판정 불가, 중간부터면 모은 데까지는 버린다.
                // 부분 목록으로 판정하면 못 받은 페이지의 공고가 전부 종료 처리된다.
                return Collections.emptySet();
            }

            JSONObject data;
            JSONArray list;
            try {
                data = new JSONObject(rawJson).optJSONObject("data");
                list = data == null ? null : data.optJSONArray("list");
            } catch (Exception e) {
                log.warn("배민 채용 API 응답 파싱 실패 (page={}): {}", page, e.getMessage());
                return Collections.emptySet();
            }
            if (list == null) {
                log.warn("배민 채용 API 응답에 data.list 없음 (page={})", page);
                return Collections.emptySet();
            }

            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.optJSONObject(i);
                Object seq = item == null ? null : item.opt("recruitSeq");
                if (seq != null) {
                    seqs.add(seq.toString());
                }
            }

            int totalPages = data.optInt("totalPageNumber", 1);
            if (page + 1 >= totalPages) {
                break;
            }
        }

        return seqs;
    }
}
