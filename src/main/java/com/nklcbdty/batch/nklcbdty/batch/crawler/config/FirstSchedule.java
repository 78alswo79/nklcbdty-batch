package com.nklcbdty.batch.nklcbdty.batch.crawler.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FirstSchedule {

	private final JobLauncher jobLauncher;
	
	public FirstSchedule(JobLauncher jobLauncher) {
		this.jobLauncher = jobLauncher;
	}
	
	@Autowired
	private Job batchMstCRUDJob;
	
	@Autowired
	private Job requestCrawlerJob;
	
	@Scheduled(cron = "* * 18 * * *", zone = "Asia/Seoul")
	public void runFirstJob() throws Exception {
		
		
		 // 현재 날짜와 시간 가져오기
        LocalDateTime now = LocalDateTime.now();
        // 포맷 정의
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
        String todayDate = now.format(dateFormat);
		
		JobParameters jobParameters = new JobParametersBuilder()
				.addString("date", todayDate)
				.toJobParameters();
		
		// requestCrawler Job 실행
        JobExecution requestCrawlerExecution = jobLauncher.run(requestCrawlerJob, jobParameters);

        // requestCrawler Job이 성공적으로 완료되었는지 확인
        if (requestCrawlerExecution.getStatus().isUnsuccessful()) {
            // 실패한 경우 로그를 남기거나 예외 처리
        	log.info("requestCrawlerJob 실행 실패: {}", requestCrawlerExecution.getAllFailureExceptions());
//            System.out.println("requestCrawlerJob 실행 실패: " + requestCrawlerExecution.getAllFailureExceptions());
            return; // 첫 번째 Job을 실행하지 않음
        }

        // firstJob 실행
        JobExecution firstJobExecution = jobLauncher.run(batchMstCRUDJob, jobParameters);
        log.info("batchMstCRUDJob 실행 완료: {}", firstJobExecution.getStatus());
//        System.out.println("batchMstCRUDJob 실행 완료: " + firstJobExecution.getStatus());
	}
	
	
}
