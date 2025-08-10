package com.nklcbdty.batch.nklcbdty.batch.crawler.run;

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
public class CrawlerBatchScheduler {
    private final JobLauncher jobLauncher;
    private final Job crawlerBatchJob;

    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Seoul")
    public void runEmailBatchJob() {
        try {
            log.info("⏰ 크롤링 배치 실행 요청!");

            // JobParameters는 매번 다른 값으로 넣어줘야 같은 Job이 여러 번 실행될 수 있어요.
            // 여기서는 현재 시간을 파라미터로 사용해서 매번 다른 Job 인스턴스를 생성합니다.
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(crawlerBatchJob, jobParameters);
            log.info("🎉 크롤링 배치 실행 완료!");
        } catch (Exception e) {
            log.error("😭 크롤링 배치 실행 중 오류 발생: {}", e.getMessage());
        }
    }
}
