//package com.nklcbdty.batch.nklcbdty.batch.crawler.batchConfig;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
//import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
//import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
//import org.springframework.batch.core.launch.support.RunIdIncrementer;
//import org.springframework.batch.repeat.RepeatStatus;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.repository.BatchJobMstRepository;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.repository.JobMstRepository;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.service.HttpClientService;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Batch_output_job_mst;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;
//
//import lombok.RequiredArgsConstructor;
//
//@Configuration
//@EnableBatchProcessing
//@RequiredArgsConstructor
//public class ClawlerBatchConfiguration {
//	
//    @Autowired
//    public JobBuilderFactory jobBuilderFactory; // Job 빌더 팩토리 주입
//
//    @Autowired
//    public StepBuilderFactory stepBuilderFactory; // Step 빌더 팩토리 주입
//    
//    @Autowired
//    private JobMstRepository jobMstRepository; // 데이터베이스 접근을 위한 리포지토리 주입
//    
//    @Autowired
//    private BatchJobMstRepository batchJobMstRepository;
//      
//    @Autowired
//    private HttpClientService httpClientService;
//    
//    private List<Batch_output_job_mst> batchJobLists;
//    private List<Job_mst> jobLists;
//    
//    @Bean
//    public Job crawlerBatchMain() {
//        // Job을 정의하고 구성
//        return jobBuilderFactory.get("crawlerBatchMain") 	// Job 이름 설정
//                .incrementer(new RunIdIncrementer()) 		// Job 실행 시마다 ID를 증가시킴   
//                .start(jobMstDeleteStep())
//                .next(requestNklcbdtyService())
//                .build(); 									// Job 빌드
//    }
//    
//    @Bean
//    public Step jobMstDeleteStep() {
//    	jobLists = jobMstRepository.findAll();
//		// Step을 정의하고 구성
//	    return stepBuilderFactory.get("jobMstDeleteStep") // Step 이름 설정
//	    		.tasklet((contribution, chunkContext) -> {
//	    			if (jobLists.size() > 0) {
//	    	    		jobMstRepository.deleteAll();
//	    	    	}
//	                return RepeatStatus.FINISHED;
//	            })
//    			.build(); // Step 빌드
//	}
//    
//    @Bean
//    public Step requestNklcbdtyService() {
//		// Step을 정의하고 구성
//	    return stepBuilderFactory.get("requestNklcbdtyService") // Step 이름 설정
//	    		.tasklet((contribution, chunkContext) -> {
//	    			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//	                    httpClientService.callHttpClient();
//	                });
//	                future.join(); // 비동기 작업이 완료될 때까지 대기
//	                return RepeatStatus.FINISHED;
//	            })
//    			.build(); // Step 빌드
//	}
//}
