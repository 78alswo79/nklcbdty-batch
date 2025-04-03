package com.nklcbdty.batch.nklcbdty.batch.crawler.batchConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.annotation.Transactional;

import com.nklcbdty.batch.nklcbdty.batch.crawler.repository.BatchJobMstRepository;
import com.nklcbdty.batch.nklcbdty.batch.crawler.repository.JobMstRepository;
import com.nklcbdty.batch.nklcbdty.batch.crawler.service.BaeminJobCrawlerService;
import com.nklcbdty.batch.nklcbdty.batch.crawler.service.CoupangJobCrawlerService;
import com.nklcbdty.batch.nklcbdty.batch.crawler.service.DaangnJobCrawlerService;
import com.nklcbdty.batch.nklcbdty.batch.crawler.service.HttpClientService;
//import com.nklcbdty.batch.nklcbdty.batch.crawler.service.HttpWebClientService;
import com.nklcbdty.batch.nklcbdty.batch.crawler.service.JobService;
import com.nklcbdty.batch.nklcbdty.batch.crawler.service.KakaoCrawlerService;
import com.nklcbdty.batch.nklcbdty.batch.crawler.service.LineJobCrawlerService;
import com.nklcbdty.batch.nklcbdty.batch.crawler.service.NaverJobCrawlerService;
import com.nklcbdty.batch.nklcbdty.batch.crawler.service.TossJobCrawlerService;
import com.nklcbdty.batch.nklcbdty.batch.crawler.service.YanoljaCralwerService;
import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Batch_output_job_mst;
import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfiguration3 {
	
    @Autowired
    public JobBuilderFactory jobBuilderFactory; // Job 빌더 팩토리 주입

    @Autowired
    public StepBuilderFactory stepBuilderFactory; // Step 빌더 팩토리 주입
    
    @Autowired
    private JobMstRepository jobMstRepository; // 데이터베이스 접근을 위한 리포지토리 주입
    
    @Autowired
    private BatchJobMstRepository batchJobMstRepository;
    
//    @Autowired
//    private HttpWebClientService httpWebClientService;
    
    @Autowired
    private HttpClientService httpClientService;
    
    private List<Batch_output_job_mst> batchJobLists;
    
    @Bean
    public Job testBatch3() {
        // Job을 정의하고 구성
        return jobBuilderFactory.get("testBatch3") // Job 이름 설정
                .incrementer(new RunIdIncrementer()) // Job 실행 시마다 ID를 증가시킴   
                .start(crawlerStep3())
                .next(deleteStep3())
                .next(insertStep3())
                .build(); // Job 빌드
    }
    
    @Bean
    public Step crawlerStep3() {
		// Step을 정의하고 구성
	    return stepBuilderFactory.get("crawlerStep3") // Step 이름 설정
	    		.tasklet((contribution, chunkContext) -> {
	                
//	    			httpWebClientService.callHttpWebClient();
	    			httpClientService.callHttpClient();
	                return RepeatStatus.FINISHED;
	            })
    			.build(); // Step 빌드
	}
    
    
	@Bean
	public Step deleteStep3() {
		// Step을 정의하고 구성
	    return stepBuilderFactory.get("deleteStep3") // Step 이름 설정
	    			.tasklet((contribution, chunkContext) -> {
	    				batchJobLists = batchJobMstRepository.findAll();
	    				if (batchJobLists.size() > 0) {
	    					batchJobMstRepository.deleteAll();
	    				}
	    				return RepeatStatus.FINISHED; // Tasklet이 완료되었음을 나타냄
	    			})
	    			.build(); // Step 빌드
	}
	  
	@Bean
	public Step insertStep3() {
		return stepBuilderFactory.get("insertStep3")
					.tasklet((contribution, chunkContext) -> {
						// step1이 끝난 후 호출할 메소드
						executeNextMethod();
						return RepeatStatus.FINISHED; // Tasklet이 완료되었음을 나타냄
					})
					.build();
	}
	  
	@Transactional
	public void executeNextMethod() {
		List<Batch_output_job_mst> batchList = new ArrayList<Batch_output_job_mst>();
		List<Job_mst> jobLists = jobMstRepository.findAll();
		
		String formattedDateTime = batchStartDate();
		try {
			// 삽입 로직
			for (Job_mst job_mst : jobLists) {
				// 새로운 Job_mst 객체를 데이터베이스에 저장
		  		Batch_output_job_mst batch = new Batch_output_job_mst();
		  		batch.setId(job_mst.getId());
		  		batch.setCompanyCd(job_mst.getCompanyCd());
		  		batch.setAnnoId(job_mst.getAnnoId());
		  		batch.setClassCdNm(job_mst.getClassCdNm());
		  		batch.setEmpTypeCdNm(job_mst.getEmpTypeCdNm());
		  		batch.setAnnoSubject(job_mst.getAnnoSubject());
		  			
		  		batch.setSubJobCdNm(job_mst.getSubJobCdNm());
		  		batch.setSysCompanyCdNm(job_mst.getSysCompanyCdNm());
		  		batch.setJobDetailLink(job_mst.getJobDetailLink());
		  		batch.setWorkplace(job_mst.getWorkplace());
		  		
		  		batch.setBatchDate(formattedDateTime);
		  		batchList.add(batch);
		  	}
		  	batchJobMstRepository.saveAll(batchList);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private String batchStartDate() {
		LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        
        return formattedDateTime;
	}
}
