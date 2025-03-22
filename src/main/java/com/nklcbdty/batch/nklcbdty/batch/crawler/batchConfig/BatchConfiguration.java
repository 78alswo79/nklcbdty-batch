package com.nklcbdty.batch.nklcbdty.batch.crawler.batchConfig;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.nklcbdty.batch.nklcbdty.batch.crawler.repository.BatchJobMstRepository;
import com.nklcbdty.batch.nklcbdty.batch.crawler.repository.JobMstRepository;
import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Batch_output_job_mst;
import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;

@Configuration
@EnableBatchProcessing // Spring Batch 기능을 활성화
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory; // Job 빌더 팩토리 주입

    @Autowired
    public StepBuilderFactory stepBuilderFactory; // Step 빌더 팩토리 주입
    
    @Autowired
    private JobMstRepository jobMstRepository; // 데이터베이스 접근을 위한 리포지토리 주입
    
    @Autowired
    private BatchJobMstRepository batchJobMstRepository;
    
    private List<Batch_output_job_mst> batchJobLists;
    
    @Bean
    public Job testBatch() {
        // Job을 정의하고 구성
        return jobBuilderFactory.get("testBatch") // Job 이름 설정
                .incrementer(new RunIdIncrementer()) // Job 실행 시마다 ID를 증가시킴
                .flow(step1()) // Job의 흐름을 step1로 설정
                .end() // Job의 끝
                .build(); // Job 빌드
    }
    
    @Bean
    public Step step1() {
        // Step을 정의하고 구성
        return stepBuilderFactory.get("step1") // Step 이름 설정
                .<Job_mst, Job_mst>chunk(100) // 청크 기반 처리, 한 번에 10개 아이템 처리
                .reader(reader()) // ItemReader 설정
                .processor(processor()) // ItemProcessor 설정
                .writer(writer()) // ItemWriter 설정
                .build(); // Step 빌드
    }
    
    @Bean
    public ItemReader<Job_mst> reader() {
    	batchJobLists = batchJobMstRepository.findAll();
    	// ItemReader 구현
        return new ItemReader<Job_mst>() {
            private List<Job_mst> jobLists = jobMstRepository.findAll(); // 데이터베이스에서 모든 Job_mst 객체를 가져옴
            private int index = 0; // 현재 인덱스 초기화

            @Override
            public Job_mst read() {
                // 다음 Job_mst 객체를 읽어옴
                if (index < jobLists.size()) {
                	
                    return jobLists.get(index++); 	// 현재 인덱스의 객체 반환 후 인덱스 증가
                } else {
                    return null; 					// 더 이상 읽을 객체가 없으면 null 반환
                }
            }
        };
    }
   
    @Bean
    public ItemProcessor<Job_mst, Job_mst> processor() {
        // ItemProcessor<input객체, return객체>여야 함.
        return job -> {
        	// batchJobLists에서 job의 ID로 검색
        	Batch_output_job_mst existingJob = batchJobLists.stream()
                    .filter(b -> b.getAnnoId().equals(job.getAnnoId())) // ID 비교
                    .findFirst()
                    .orElse(null);

            // batchJobLists에 해당 job이 없으면 null 반환
            return existingJob == null ? job : null; // 삽입할 Job_mst 객체 반환, 없으면 null
        };
    }

    @Bean
    public ItemWriter<Job_mst> writer() {
    	// ItemWriter 구현

        return job_mstItem -> {
        	List<Batch_output_job_mst> batchList = new ArrayList<Batch_output_job_mst>();
        	try {
        		if (batchJobLists.size() > 0) {
        			batchJobMstRepository.deleteAll();        			
        		}
        		
        		// 삽입 로직
        		for (Job_mst job_mst : job_mstItem) {
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
        			batchList.add(batch);
        		}
        		batchJobMstRepository.saveAll(batchList);
        	} catch (Exception e) {
        		 e.printStackTrace(); // 예외를 출력
        	}
        };
    }
}
