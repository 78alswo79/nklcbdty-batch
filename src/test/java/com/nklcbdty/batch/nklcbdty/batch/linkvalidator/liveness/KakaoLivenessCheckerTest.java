package com.nklcbdty.batch.nklcbdty.batch.linkvalidator.liveness;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.nklcbdty.common.vo.Job_mst;

// careers.kakao.com 상세페이지는 클라이언트 렌더링이라 원본 HTML(1.5KB 셸)에 공고명이 없다.
// 그래서 HTML 문자열 매칭 대신 채용 API 의 realId 목록으로 생존을 판정한다.
class KakaoLivenessCheckerTest {

    private static final String JOBS_ATTRIBUTE = """
        {"jobTypeList":[
            {"jobType":"TECHNOLOGY","useFlag":true},
            {"jobType":"DESIGN","useFlag":true},
            {"jobType":"RETIRED_PART","useFlag":false}
        ]}
        """;

    private Job_mst kakaoJob(String realId) {
        Job_mst job = new Job_mst();
        job.setCompanyCd("KAKAO");
        job.setAnnoId(realId.substring(2));
        job.setAnnoSubject("Data Scientist (경력)");
        job.setJobDetailLink("https://careers.kakao.com/jobs/" + realId
            + "?skillSet=&part=TECHNOLOGY&company=KAKAO&keyword=&employeeType=&page=1");
        return job;
    }

    private String pageJson(int totalPage, String... realIds) {
        StringBuilder items = new StringBuilder();
        for (int i = 0; i < realIds.length; i++) {
            if (i > 0) {
                items.append(',');
            }
            // company=ALL 로 받으면 jobOfferId 는 0 으로 내려온다. realId 만 쓸 수 있다.
            items.append("{\"realId\":\"").append(realIds[i])
                .append("\",\"jobOfferId\":0,\"jobOfferTitle\":\"공고").append(i).append("\"}");
        }
        return "{\"totalPage\":" + totalPage + ",\"jobList\":[" + items + "]}";
    }

    /** jobs-attribute 는 직군 목록을, 그 외 URL 은 공고 목록을 준다. */
    private Function<String, String> api(Function<String, String> jobList) {
        return url -> url.contains("jobs-attribute") ? JOBS_ATTRIBUTE : jobList.apply(url);
    }

    @Test
    @DisplayName("채용 API 에 realId 가 있으면 ALIVE")
    void aliveWhenRealIdPresent() {
        KakaoLivenessChecker checker =
            new KakaoLivenessChecker(api(url -> pageJson(1, "P-14324", "S-4752")));

        assertThat(checker.check(kakaoJob("P-14324"))).isEqualTo(Liveness.ALIVE);
    }

    @Test
    @DisplayName("채용 API 에 없는 realId 는 CLOSED")
    void closedWhenRealIdAbsent() {
        KakaoLivenessChecker checker =
            new KakaoLivenessChecker(api(url -> pageJson(1, "P-14324")));

        assertThat(checker.check(kakaoJob("S-4306"))).isEqualTo(Liveness.CLOSED);
    }

    @Test
    @DisplayName("본사(P)와 공동체(S)는 번호 체계가 달라 realId 로 구분한다")
    void distinguishesPrefix() {
        KakaoLivenessChecker checker =
            new KakaoLivenessChecker(api(url -> pageJson(1, "P-4752")));

        // 숫자만 봤다면 살아있다고 잘못 판정했을 건이다.
        assertThat(checker.check(kakaoJob("S-4752"))).isEqualTo(Liveness.CLOSED);
    }

    @Test
    @DisplayName("API 통신 실패(null 응답)는 CLOSED 가 아니라 UNKNOWN — 장애로 공고가 전멸하면 안 된다")
    void unknownWhenFetchFails() {
        KakaoLivenessChecker checker = new KakaoLivenessChecker(url -> null);

        assertThat(checker.check(kakaoJob("P-14324"))).isEqualTo(Liveness.UNKNOWN);
    }

    @Test
    @DisplayName("응답 형식이 바뀌어 jobList 를 못 찾으면 UNKNOWN")
    void unknownWhenEnvelopeChanged() {
        KakaoLivenessChecker checker =
            new KakaoLivenessChecker(api(url -> "{\"totalPage\":1,\"items\":[]}"));

        assertThat(checker.check(kakaoJob("P-14324"))).isEqualTo(Liveness.UNKNOWN);
    }

    @Test
    @DisplayName("응답이 JSON 이 아니어도(차단 페이지 등) UNKNOWN")
    void unknownWhenNotJson() {
        KakaoLivenessChecker checker =
            new KakaoLivenessChecker(api(url -> "<html><body>Access Denied</body></html>"));

        assertThat(checker.check(kakaoJob("P-14324"))).isEqualTo(Liveness.UNKNOWN);
    }

    @Test
    @DisplayName("상세 링크에서 realId 를 못 뽑으면 종료로 찍지 않고 UNKNOWN")
    void unknownWhenLinkHasNoRealId() {
        KakaoLivenessChecker checker =
            new KakaoLivenessChecker(api(url -> pageJson(1, "P-14324")));

        Job_mst job = kakaoJob("P-14324");
        job.setJobDetailLink("https://careers.kakao.com/jobs");

        assertThat(checker.supports(job)).isTrue();
        assertThat(checker.check(job)).isEqualTo(Liveness.UNKNOWN);
    }

    @Test
    @DisplayName("직군(part)을 전부 훑는다 — 크롤러가 담지 않는 직군으로 옮겨간 공고도 살아있는 것")
    void coversEveryJobType() {
        KakaoLivenessChecker checker = new KakaoLivenessChecker(api(url -> {
            if (url.contains("part=DESIGN")) {
                return pageJson(1, "P-14545");
            }
            return pageJson(1, "P-14324");
        }));

        assertThat(checker.check(kakaoJob("P-14545"))).isEqualTo(Liveness.ALIVE);
    }

    @Test
    @DisplayName("useFlag=false 인 직군은 조회하지 않는다")
    void skipsDisabledJobTypes() {
        List<String> calls = new ArrayList<>();
        KakaoLivenessChecker checker = new KakaoLivenessChecker(api(url -> {
            calls.add(url);
            return pageJson(1, "P-14324");
        }));

        checker.check(kakaoJob("P-14324"));

        assertThat(calls).hasSize(2);
        assertThat(calls).anyMatch(url -> url.contains("part=TECHNOLOGY"));
        assertThat(calls).anyMatch(url -> url.contains("part=DESIGN"));
        assertThat(calls).noneMatch(url -> url.contains("part=RETIRED_PART"));
    }

    @Test
    @DisplayName("여러 페이지를 모두 모아서 판정한다")
    void followsPagination() {
        KakaoLivenessChecker checker = new KakaoLivenessChecker(api(url -> {
            if (url.contains("page=1")) {
                return pageJson(2, "P-111", "P-222");
            }
            return pageJson(2, "P-333", "P-444");
        }));

        assertThat(checker.check(kakaoJob("P-444"))).isEqualTo(Liveness.ALIVE);
    }

    @Test
    @DisplayName("페이지 일부를 못 받으면 부분 목록으로 판정하지 않고 UNKNOWN")
    void unknownWhenSomePageFails() {
        KakaoLivenessChecker checker = new KakaoLivenessChecker(api(url -> {
            if (url.contains("page=1")) {
                return pageJson(2, "P-111", "P-222");
            }
            return null; // 2페이지 실패
        }));

        // 2페이지에 있었을 공고가 종료로 찍히면 안 된다.
        assertThat(checker.check(kakaoJob("P-333"))).isEqualTo(Liveness.UNKNOWN);
    }

    @Test
    @DisplayName("직군 목록을 못 받아도 알려진 직군으로 판정을 이어간다")
    void fallsBackWhenJobTypesUnavailable() {
        KakaoLivenessChecker checker = new KakaoLivenessChecker(
            url -> url.contains("jobs-attribute") ? null : pageJson(1, "P-14324"));

        assertThat(checker.check(kakaoJob("P-14324"))).isEqualTo(Liveness.ALIVE);
    }

    @Test
    @DisplayName("공고 건수만큼 API 를 때리지 않는다 — 스냅샷을 재사용한다")
    void reusesSnapshotAcrossJobs() {
        List<String> calls = new ArrayList<>();
        KakaoLivenessChecker checker = new KakaoLivenessChecker(api(url -> {
            calls.add(url);
            return pageJson(1, "P-14324", "S-4752");
        }));

        checker.check(kakaoJob("P-14324"));
        int afterFirst = calls.size();
        checker.check(kakaoJob("S-4752"));
        checker.check(kakaoJob("S-9999"));

        assertThat(calls).hasSize(afterFirst);
    }

    @Test
    @DisplayName("careers.kakao.com 이 아닌 카카오 계열 공고는 이 체커가 잡지 않는다")
    void supportsOnlyCareersKakaoLinks() {
        KakaoLivenessChecker checker =
            new KakaoLivenessChecker(api(url -> pageJson(1, "P-14324")));

        Job_mst mobility = new Job_mst();
        mobility.setCompanyCd("KAKAO");
        mobility.setJobDetailLink("https://kakaomobility.career.greetinghr.com/ko/o/236686");

        Job_mst bank = new Job_mst();
        bank.setCompanyCd("KAKAO");
        bank.setJobDetailLink("https://kakaobank.recruiter.co.kr/app/jobnotice/view?jobnoticeSn=253256");

        assertThat(checker.supports(mobility)).isFalse();
        assertThat(checker.supports(bank)).isFalse();
    }
}
