package com.nklcbdty.batch.nklcbdty.batch.crawler.batchConfig;

import java.util.List;
import java.util.Set;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@Slf4j
public class BatchJobSchedule {
	private final JobLauncher jobLauncher;
    private final ClawlerBatchConfiguration clawlerBatchConfiguration;
    
    @Autowired
    JobExplorer jobExplorer;
    
    public BatchJobSchedule(JobLauncher jobLauncher, ClawlerBatchConfiguration clawlerBatchConfiguration) {
        this.jobLauncher = jobLauncher;
        this.clawlerBatchConfiguration = clawlerBatchConfiguration;
    }

    @Scheduled(cron = "* * 21 * * ?") // 매일 오후 3시
    public void runJob() {
    	try {
    		JobParameters jobParameters = new JobParametersBuilder()
    				.addString("param", "value")
    				.addLong("time", System.currentTimeMillis()) // 현재 시간 추가
    				.toJobParameters();
    		
    		// Job실행 상태 확인.
    		if (!isJobrunning()) {
    			jobLauncher.run(clawlerBatchConfiguration.crawlerBatchMain(), jobParameters);
    		} else {
    			log.debug("Job is already runnig!!!!");
    		}
    		
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
    
    private boolean isJobrunning() {
    	// 현재 실행 중인 Job의 상태를 확인
        Set<JobExecution> runningJobs = jobExplorer.findRunningJobExecutions("crawlerBatchMain");
        log.debug("runningJobs은 실행중에 있나요??>>>>>>>>>>>>>>>>" + runningJobs.toString());
        return !runningJobs.isEmpty(); // 실행 중인 Job이 있으면 true 반환
    }
}
