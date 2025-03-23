package com.nklcbdty.batch.nklcbdty.batch.crawler.batchConfig;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class BatchJobSchedule {
	private final JobLauncher jobLauncher;
    private final BatchConfiguration batchConfiguration;

    public BatchJobSchedule(JobLauncher jobLauncher, BatchConfiguration batchConfiguration) {
        this.jobLauncher = jobLauncher;
        this.batchConfiguration = batchConfiguration;
    }

    @Scheduled(cron = "* * 18 * * ?") // 매일 오후 3시
    public void runJob() {
    	try {
    		JobParameters jobParameters = new JobParametersBuilder()
    				.addString("param", "value")
    				.addLong("time", System.currentTimeMillis()) // 현재 시간 추가
    				.toJobParameters();
    		jobLauncher.run(batchConfiguration.testBatch(), jobParameters);
    	} catch(JobExecutionAlreadyRunningException e) {
    		 e.printStackTrace();
    	} catch (JobParametersInvalidException e) {
            e.printStackTrace();
            // 잘못된 작업 매개변수에 대한 처리
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
            // 작업 인스턴스가 이미 완료된 경우에 대한 처리
        } catch (JobRestartException e) {
            e.printStackTrace();
            // 작업 재시작에 대한 처리
        }
    }
}
