package com.nklcbdty.batch.nklcbdty.batch.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.nklcbdty.common.dto.JobPosting;
import com.nklcbdty.common.email.JobEmailContentBuilder;
import com.nklcbdty.common.email.JobMailOrdering;
import com.nklcbdty.common.vo.Job_mst;

// job_mst 에 같은 공고가 여러 행으로 남아 있어도 메일에는 한 번만 실려야 한다.
class JobMailOrderingDedupeTest {

    private Job_mst job(String annoId, String company, String subject, String subJob, String endDate) {
        Job_mst j = new Job_mst();
        j.setAnnoId(annoId);
        j.setCompanyCd(company);
        j.setAnnoSubject(subject);
        j.setSubJobCdNm(subJob);
        j.setEndDate(endDate);
        return j;
    }

    @Test
    @DisplayName("annoId 가 같은 행은 하나만 남고 먼저 나온 순서를 지킨다")
    void foldsRowsSharingAnnoId() {
        Job_mst first = job("30005174", "NAVER", "Frontend Developer", "Frontend", "2026-08-03 23:59:00");
        Job_mst second = job("30005174", "NAVER", "Frontend Developer", "Frontend", "2026-08-03 23:59:00");
        Job_mst other = job("30005187", "NAVER", "Backend Developer", "Backend", "2026-08-05 23:59:00");

        List<Job_mst> result = JobMailOrdering.dedupe(List.of(first, second, other));

        assertThat(result).containsExactly(first, other);
    }

    @Test
    @DisplayName("annoId 가 갈렸어도 메일에 똑같이 그려지는 행은 접는다")
    void foldsRowsThatRenderIdentically() {
        Job_mst first = job("30005174", "NAVER", "Frontend Developer", "Frontend", "2026-08-03 23:59:00");
        Job_mst reposted = job("30009999", "NAVER", "Frontend Developer", "Frontend", "2026-08-03 23:59:00");

        List<Job_mst> result = JobMailOrdering.dedupe(List.of(first, reposted));

        assertThat(result).containsExactly(first);
    }

    @Test
    @DisplayName("같은 회사·직무라도 제목이나 종료일이 다르면 서로 다른 공고로 남긴다")
    void keepsGenuinelyDifferentPostings() {
        Job_mst payServer = job("1", "LINE", "LINE Pay Server Engineer", "Backend", "2999-12-31 03:00:00");
        Job_mst payFront = job("2", "LINE", "LINE Pay Frontend Engineer", "FrontEnd", "2999-12-31 03:00:00");
        Job_mst sameTitleLaterDeadline = job("3", "LINE", "LINE Pay Server Engineer", "Backend", "2026-09-01 23:59:00");

        List<Job_mst> result = JobMailOrdering.dedupe(List.of(payServer, payFront, sameTitleLaterDeadline));

        assertThat(result).containsExactly(payServer, payFront, sameTitleLaterDeadline);
    }

    @Test
    @DisplayName("메일 본문에도 같은 공고가 한 번만 그려진다")
    void rendersEachPostingOnceInTheEmailBody() {
        // 실제로 사용자가 받은 메일에서 NAVER Frontend Developer 가 두 줄로 나온 상황.
        Job_mst first = job("30005174", "NAVER", "Frontend Developer", "Frontend", "2026-08-03 23:59:00");
        Job_mst duplicate = job("30005174", "NAVER", "Frontend Developer", "Frontend", "2026-08-03 23:59:00");

        List<Job_mst> deduped = JobMailOrdering.dedupe(List.of(first, duplicate));
        List<JobPosting> postings = deduped.stream().map(job -> {
            JobPosting p = new JobPosting();
            p.setTitle(job.getAnnoSubject());
            p.setCompany(job.getCompanyCd());
            p.setJobType(job.getSubJobCdNm());
            p.setEndDate(job.getEndDate());
            return p;
        }).collect(Collectors.toList());

        String html = JobEmailContentBuilder.generateHtml("backend", postings);

        assertThat(html.split("Frontend Developer", -1)).hasSize(2); // 등장 1회
    }

    @Test
    @DisplayName("회사가 다르면 annoId 가 같아도 서로 다른 공고다")
    void doesNotFoldAcrossCompaniesSharingAnnoId() {
        // annoId 는 크롤 원본마다 체계가 달라(LINE strapiId, TOSS id 등) 숫자가 겹칠 수 있다.
        Job_mst line = job("1234", "LINE", "Server Engineer", "Backend", "2026-09-01 23:59:00");
        Job_mst toss = job("1234", "TOSS", "Server Developer", "Backend", "2026-09-02 23:59:00");

        List<Job_mst> result = JobMailOrdering.dedupe(List.of(line, toss));

        assertThat(result).containsExactly(line, toss);
    }

    @Test
    @DisplayName("annoId 가 없는 행은 표기가 다르면 그대로 둔다")
    void keepsRowsWithoutAnnoId() {
        Job_mst noId = job(null, "KAKAO", "블록체인 서비스 백엔드 개발자", "Backend", "2026-08-06 23:59:59");
        Job_mst anotherNoId = job(null, "KAKAO", "FHIR/의료데이터 플랫폼 개발자", "FullStack", null);

        List<Job_mst> result = JobMailOrdering.dedupe(List.of(noId, anotherNoId));

        assertThat(result).containsExactly(noId, anotherNoId);
    }
}
