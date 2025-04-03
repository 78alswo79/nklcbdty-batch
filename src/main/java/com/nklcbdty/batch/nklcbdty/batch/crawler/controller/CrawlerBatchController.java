package com.nklcbdty.batch.nklcbdty.batch.crawler.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nklcbdty.batch.nklcbdty.batch.crawler.service.BatchService;

@RestController
@RequestMapping("/api/batch")
public class CrawlerBatchController {
	
	@Autowired
	private BatchService batchService;
	
	@Autowired
	private JobLauncher jobLauncher;
    
	@Autowired
	private Job testBatch;
	
	@RequestMapping("/insert")
	public ResponseEntity<Map<String, Object>> insert() {
		Map<String, Object> resMap = new HashMap<>();
		
		try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // JobParameters에 현재 시간 추가
                    .toJobParameters();
            jobLauncher.run(testBatch, jobParameters);
            resMap.put("resMapMessage", "배치 작업이 시작되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            resMap.put("resMapMessage", "배치 작업이 실패했습니다.");
        }
		
		resMap.put("testMessage", "batch 인서트 시!!");
		System.out.println("batch 인서트 시작");
		return ResponseEntity.ok(resMap);
	}
}
