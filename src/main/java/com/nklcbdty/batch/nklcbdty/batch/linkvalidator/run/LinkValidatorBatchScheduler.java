package com.nklcbdty.batch.nklcbdty.batch.linkvalidator.run;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class LinkValidatorBatchScheduler {
    private final JobLauncher jobLauncher;
    private final Job linkValidatorJob;

    /**
     * 매일 오전 7시 30분에 링크 유효성 검증 배치를 실행합니다.
     * (크롤링 배치(7시) 완료 후, 이메일 배치(8시) 시작 전에 실행되도록 배치)
     * cron = "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 30 9 * * *", zone = "Asia/Seoul")
    public void runLinkValidatorJob() {
        try {
            log.info("⏰ 링크 유효성 검증 배치 실행 요청!");
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(linkValidatorJob, jobParameters);
            log.info("🎉 링크 유효성 검증 배치 실행 완료!");
        } catch (Exception e) {
            log.error("😭 링크 유효성 검증 배치 실행 중 오류 발생: {}", e.getMessage());
        }
    }
}
