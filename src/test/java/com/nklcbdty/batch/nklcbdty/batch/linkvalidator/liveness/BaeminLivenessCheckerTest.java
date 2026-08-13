package com.nklcbdty.batch.nklcbdty.batch.linkvalidator.liveness;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.nklcbdty.common.vo.Job_mst;

// 배민 상세페이지는 Next.js 클라이언트 렌더링이라 원본 HTML 에 공고명이 없다.
// 그래서 HTML 문자열 매칭 대신 채용 API 의 recruitSeq 목록으로 생존을 판정한다.
class BaeminLivenessCheckerTest {

    private Job_mst baeminJob(String annoId) {
        Job_mst job = new Job_mst();
        job.setAnnoId(annoId);
        job.setCompanyCd("BAEMIN");
        job.setAnnoSubject("QA Engineer(자동화 및 품질 전략)");
        job.setJobDetailLink("https://career.woowahan.com/recruitment/R2603042/detail?jobCodes=");
        return job;
    }

    private String pageJson(int totalPageNumber, String... seqs) {
        StringBuilder items = new StringBuilder();
        for (int i = 0; i < seqs.length; i++) {
            if (i > 0) {
                items.append(',');
            }
            items.append("{\"recruitSeq\":").append(seqs[i]).append(",\"recruitName\":\"공고").append(i).append("\"}");
        }
        return "{\"code\":\"2000\",\"data\":{\"totalPageNumber\":" + totalPageNumber
            + ",\"list\":[" + items + "]}}";
    }

    @Test
    @DisplayName("채용 API 에 recruitSeq 가 있으면 ALIVE")
    void aliveWhenSeqPresent() {
        BaeminLivenessChecker checker =
            new BaeminLivenessChecker(url -> pageJson(1, "25575", "25562", "25497"));

        assertThat(checker.check(baeminJob("25575"))).isEqualTo(Liveness.ALIVE);
    }

    @Test
    @DisplayName("채용 API 에 없는 recruitSeq 는 CLOSED")
    void closedWhenSeqAbsent() {
        BaeminLivenessChecker checker =
            new BaeminLivenessChecker(url -> pageJson(1, "25575", "25562"));

        assertThat(checker.check(baeminJob("99999"))).isEqualTo(Liveness.CLOSED);
    }

    @Test
    @DisplayName("API 통신 실패(null 응답)는 CLOSED 가 아니라 UNKNOWN — 장애로 공고가 전멸하면 안 된다")
    void unknownWhenFetchFails() {
        BaeminLivenessChecker checker = new BaeminLivenessChecker(url -> null);

        assertThat(checker.check(baeminJob("25575"))).isEqualTo(Liveness.UNKNOWN);
    }

    @Test
    @DisplayName("응답 형식이 바뀌어 data.list 를 못 찾으면 UNKNOWN")
    void unknownWhenEnvelopeChanged() {
        BaeminLivenessChecker checker =
            new BaeminLivenessChecker(url -> "{\"code\":\"2000\",\"payload\":{}}");

        assertThat(checker.check(baeminJob("25575"))).isEqualTo(Liveness.UNKNOWN);
    }

    @Test
    @DisplayName("응답이 JSON 이 아니어도(차단 페이지 등) UNKNOWN")
    void unknownWhenNotJson() {
        BaeminLivenessChecker checker =
            new BaeminLivenessChecker(url -> "<html><body>Access Denied</body></html>");

        assertThat(checker.check(baeminJob("25575"))).isEqualTo(Liveness.UNKNOWN);
    }

    @Test
    @DisplayName("여러 페이지를 모두 모아서 판정한다")
    void followsPagination() {
        BaeminLivenessChecker checker = new BaeminLivenessChecker(url -> {
            if (url.contains("page=0")) {
                return pageJson(2, "111", "222");
            }
            return pageJson(2, "333", "444");
        });

        assertThat(checker.check(baeminJob("444"))).isEqualTo(Liveness.ALIVE);
    }

    @Test
    @DisplayName("페이지 일부를 못 받으면 부분 목록으로 판정하지 않고 UNKNOWN")
    void unknownWhenSomePageFails() {
        BaeminLivenessChecker checker = new BaeminLivenessChecker(url -> {
            if (url.contains("page=0")) {
                return pageJson(2, "111", "222");
            }
            return null; // 2페이지 실패
        });

        // 2페이지에 있었을 공고가 종료로 찍히면 안 된다.
        assertThat(checker.check(baeminJob("333"))).isEqualTo(Liveness.UNKNOWN);
    }

    @Test
    @DisplayName("공고 건수만큼 API 를 때리지 않는다 — 스냅샷을 재사용한다")
    void reusesSnapshotAcrossJobs() {
        List<String> calls = new ArrayList<>();
        Function<String, String> counting = url -> {
            calls.add(url);
            return pageJson(1, "25575", "25562", "25497");
        };
        BaeminLivenessChecker checker = new BaeminLivenessChecker(counting);

        checker.check(baeminJob("25575"));
        checker.check(baeminJob("25562"));
        checker.check(baeminJob("25497"));

        assertThat(calls).hasSize(1);
    }

    @Test
    @DisplayName("annoId 가 없으면 판정하지 않는다")
    void unknownWhenNoAnnoId() {
        BaeminLivenessChecker checker =
            new BaeminLivenessChecker(url -> pageJson(1, "25575"));

        assertThat(checker.check(baeminJob(null))).isEqualTo(Liveness.UNKNOWN);
    }

    @Test
    @DisplayName("배민 공고만 맡는다 — company_cd 또는 career.woowahan.com 링크로 판별")
    void supportsOnlyBaemin() {
        BaeminLivenessChecker checker = new BaeminLivenessChecker(url -> null);

        Job_mst naver = new Job_mst();
        naver.setCompanyCd("NAVER");
        naver.setJobDetailLink("https://recruit.navercorp.com/rcrt/view.do?annoId=30005189");

        Job_mst legacyNoCompanyCd = new Job_mst();
        legacyNoCompanyCd.setJobDetailLink("https://career.woowahan.com/recruitment/R2603042/detail");

        assertThat(checker.supports(baeminJob("25575"))).isTrue();
        assertThat(checker.supports(legacyNoCompanyCd)).isTrue();
        assertThat(checker.supports(naver)).isFalse();
        assertThat(checker.supports(null)).isFalse();
    }
}
