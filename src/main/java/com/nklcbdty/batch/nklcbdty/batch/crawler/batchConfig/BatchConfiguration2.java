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
//import org.springframework.core.task.SimpleAsyncTaskExecutor;
//import org.springframework.core.task.TaskExecutor;
//
//import com.nklcbdty.batch.nklcbdty.batch.crawler.repository.BatchJobMstRepository;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.repository.JobMstRepository;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.service.BaeminJobCrawlerService;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.service.CoupangJobCrawlerService;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.service.DaangnJobCrawlerService;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.service.JobService;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.service.KakaoCrawlerService;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.service.LineJobCrawlerService;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.service.NaverJobCrawlerService;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.service.TossJobCrawlerService;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.service.YanoljaCralwerService;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Batch_output_job_mst;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;
//
//import lombok.RequiredArgsConstructor;
//
//@Configuration
//@EnableBatchProcessing
//@RequiredArgsConstructor
//public class BatchConfiguration2 {
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
//    private List<Batch_output_job_mst> batchJobLists;
//    private final NaverJobCrawlerService naverJobCrawlerService;
//    private final KakaoCrawlerService kakaoCrawlerService;
//    private final LineJobCrawlerService lineJobCrawlerService;
//    private final TossJobCrawlerService tossJobCrawlerService;
//    private final YanoljaCralwerService yanoljaCralwerService;
//    private final JobService jobService;
//    private final CoupangJobCrawlerService coupangJobCrawlerService;
//    private final BaeminJobCrawlerService baeminJobCrawlerService;
//    private final DaangnJobCrawlerService daangnJobCrawlerService;
//    
//    @Bean
//    public TaskExecutor taskExecutor() {
//        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
//        executor.setConcurrencyLimit(8); 			// 동시 실행할 스레드 수 설정
//        return executor;
//    }
//    
//    @Bean
//    public Job testBatch2() {
//        // Job을 정의하고 구성
//        return jobBuilderFactory.get("testBatch2") // Job 이름 설정
//                .incrementer(new RunIdIncrementer()) // Job 실행 시마다 ID를 증가시킴   
//                .start(crawlerStep())
//                .next(deleteStep2())
//                .next(insertStep2())
//                .build(); // Job 빌드
//    }
//    
//    @Bean
//    public Step crawlerStep() {
//		// Step을 정의하고 구성
//	    return stepBuilderFactory.get("crawlerStep") // Step 이름 설정
//	    		.tasklet((contribution, chunkContext) -> {
//	                // 각 크롤러를 병렬로 실행
//	                CompletableFuture<Void> naverFuture = CompletableFuture.runAsync(() -> naverJobCrawlerService.crawlJobs(), taskExecutor());
//	                CompletableFuture<Void> kakaoFuture = CompletableFuture.runAsync(() -> kakaoCrawlerService.crawlJobs(), taskExecutor());
//	                CompletableFuture<Void> lineFuture = CompletableFuture.runAsync(() -> lineJobCrawlerService.crawlJobs(), taskExecutor());
//	                CompletableFuture<Void> coupangFuture = CompletableFuture.runAsync(() -> coupangJobCrawlerService.crawlJobs(), taskExecutor());
//	                CompletableFuture<Void> baeminFuture = CompletableFuture.runAsync(() -> baeminJobCrawlerService.crawlJobs(), taskExecutor());
//	                CompletableFuture<Void> daangnFuture = CompletableFuture.runAsync(() -> daangnJobCrawlerService.crawlJobs(), taskExecutor());
//	                CompletableFuture<Void> tossFuture = CompletableFuture.runAsync(() -> tossJobCrawlerService.crawlJobs(), taskExecutor());
//	                CompletableFuture<Void> yanoljaFuture = CompletableFuture.runAsync(() -> yanoljaCralwerService.crawlJobs(), taskExecutor());
//
//	                // 모든 작업이 완료될 때까지 대기
//	                CompletableFuture<Void> allOf = CompletableFuture.allOf(naverFuture, kakaoFuture, lineFuture, coupangFuture, baeminFuture, daangnFuture, tossFuture, yanoljaFuture);
//	                allOf.join(); // 모든 작업이 완료될 때까지 대기
//
//	                return RepeatStatus.FINISHED;
//	            })
//	            .taskExecutor(taskExecutor()) // TaskExecutor 설정
//    			.build(); // Step 빌드
//	}
//    
//    
//	@Bean
//	public Step deleteStep2() {
//		// Step을 정의하고 구성
//	    return stepBuilderFactory.get("deleteStep2") // Step 이름 설정
//	    			.tasklet((contribution, chunkContext) -> {
//	    				batchJobLists = batchJobMstRepository.findAll();
//	    				if (batchJobLists.size() > 0) {
//	    					batchJobMstRepository.deleteAll();
//	    				}
//	    				return RepeatStatus.FINISHED; // Tasklet이 완료되었음을 나타냄
//	    			})
//	    			.build(); // Step 빌드
//	}
//	  
//	@Bean
//	public Step insertStep2() {
//		return stepBuilderFactory.get("insertStep2")
//					.tasklet((contribution, chunkContext) -> {
//						// step1이 끝난 후 호출할 메소드
//						executeNextMethod();
//						return RepeatStatus.FINISHED; // Tasklet이 완료되었음을 나타냄
//					})
//					.build();
//	}
//	  
//	public void executeNextMethod() {
//		List<Batch_output_job_mst> batchList = new ArrayList<Batch_output_job_mst>();
//		List<Job_mst> jobLists = jobMstRepository.findAll();
//		
//		String formattedDateTime = batchStartDate();
//		try {
//			// 삽입 로직
//			for (Job_mst job_mst : jobLists) {
//				// 새로운 Job_mst 객체를 데이터베이스에 저장
//		  		Batch_output_job_mst batch = new Batch_output_job_mst();
//		  		batch.setId(job_mst.getId());
//		  		batch.setCompanyCd(job_mst.getCompanyCd());
//		  		batch.setAnnoId(job_mst.getAnnoId());
//		  		batch.setClassCdNm(job_mst.getClassCdNm());
//		  		batch.setEmpTypeCdNm(job_mst.getEmpTypeCdNm());
//		  		batch.setAnnoSubject(job_mst.getAnnoSubject());
//		  			
//		  		batch.setSubJobCdNm(job_mst.getSubJobCdNm());
//		  		batch.setSysCompanyCdNm(job_mst.getSysCompanyCdNm());
//		  		batch.setJobDetailLink(job_mst.getJobDetailLink());
//		  		batch.setWorkplace(job_mst.getWorkplace());
//		  		
//		  		batch.setBatchDate(formattedDateTime);
//		  		batchList.add(batch);
//		  	}
//		  	batchJobMstRepository.saveAll(batchList);
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
//	private String batchStartDate() {
//		LocalDateTime now = LocalDateTime.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String formattedDateTime = now.format(formatter);
//        
//        return formattedDateTime;
//	}
//}
