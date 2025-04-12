//package com.nklcbdty.batch.nklcbdty.batch.crawler.batchConfig;
//
//import java.util.List;
//import java.util.Set;
//
//import org.springframework.batch.core.BatchStatus;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobExecution;
//import org.springframework.batch.core.JobParameters;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.JobParametersInvalidException;
//import org.springframework.batch.core.explore.JobExplorer;
//import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
//import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.repository.JobRestartException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class BatchJobSchedule {
//	private final JobLauncher jobLauncher;
//    private final Job crawlerBatchMain;
//        
//    @Autowired
//    private JobExplorer jobExplorer;
//    
//    private volatile boolean isJobRunning = false;	// 작업 실행 상태를 사타내는 플래그 추가
//    
////    public BatchJobSchedule(JobLauncher jobLauncher, ClawlerBatchConfiguration clawlerBatchConfiguration) {
////        this.jobLauncher = jobLauncher;
////        this.Job = clawlerBatchConfiguration;
////    }
//
//    @Scheduled(cron = "*/40 * * * * ?") // 매일 오후 3시
//    public void runJob() {
//    	// Job실행 상태 확인.
//    	if (isJobrunning()) {
//    		log.info("Job is already running!!시발");
//    		return;
//    	}
//    	
//    	try {
//    		isJobRunning = true;
//    		log.info(">>>>>>>>>>>>>>>>>>>>>>돼라");
//    		JobParameters jobParameters = new JobParametersBuilder()
//    				.addString("param", "value")
//    				.addLong("time", System.currentTimeMillis()) // 현재 시간 추가
//    				.toJobParameters();
//    		jobLauncher.run(crawlerBatchMain, jobParameters);
//    	} catch(JobExecutionAlreadyRunningException e) {
//    		 e.printStackTrace();
//    	} catch (JobParametersInvalidException e) {
//            e.printStackTrace();
//            // 잘못된 작업 매개변수에 대한 처리
//        } catch (JobInstanceAlreadyCompleteException e) {
//            e.printStackTrace();
//            // 작업 인스턴스가 이미 완료된 경우에 대한 처리
//        } catch (JobRestartException e) {
//            e.printStackTrace();
//            // 작업 재시작에 대한 처리
//        } finally {
//        	isJobRunning = false;
//        }
//    	// Job이 완료된 경우 재시작
//        restartJobIfCompleted();
//    }
//    
//    private boolean isJobrunning() {
//    	// 현재 실행 중인 Job의 상태를 확인
//        Set<JobExecution> runningJobs = jobExplorer.findRunningJobExecutions("crawlerBatchMain");
//        log.info("runningJobs은 실행중에 있나요??>>>>>>>>>>>>>>>>" + runningJobs.toString());
//        return !runningJobs.isEmpty(); // 실행 중인 Job이 있으면 true 반환
//    }
//    
//    public void restartJobIfCompleted() {
//        // Job 상태 확인
////        Set<JobExecution> completedJobs = jobExplorer.findJobExecutions("crawlerBatchMain");
//        Set<JobExecution> completedJobs = jobExplorer.findRunningJobExecutions("crawlerBatchMain");
//        for (JobExecution jobExecution : completedJobs) {
//            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
//                log.info("Job이 완료되었습니다. 재시작합니다.");
//                try {
//                    JobParameters jobParameters = new JobParametersBuilder()
//                            .addString("param", "value")
//                            .toJobParameters();
//                    jobLauncher.run(crawlerBatchMain, jobParameters);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//}
