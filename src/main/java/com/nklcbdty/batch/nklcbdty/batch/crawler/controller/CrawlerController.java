package com.nklcbdty.batch.nklcbdty.batch.crawler.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/batch")
@Slf4j
@RequiredArgsConstructor
public class CrawlerController {
    private final JobLauncher jobLauncher;
    private final Job crawlerBatchJob;

    @GetMapping("/runCrawlerJob") // API 엔드포인트 변경!
    public ResponseEntity<Map<String, Object>> runCrawlerBatchJobManually() { // 메서드 이름 변경!
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("⏰ 수동 크롤링 배치 실행 요청!"); // 로그 메시지 변경!

            // JobParameters는 매번 다른 값으로 넣어줘야 같은 Job이 여러 번 실행될 수 있어요.
            // 여기서는 현재 시간을 파라미터로 사용해서 매번 다른 Job 인스턴스를 생성합니다.
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            // 주입받은 crawlerBatchJob을 실행해야 해.
            jobLauncher.run(crawlerBatchJob, jobParameters); // 실행 Job 변경!
            log.info("🎉 수동 크롤링 배치 실행 완료!"); // 로그 메시지 변경!

            response.put("status", "success");
            response.put("message", "크롤링 배치 Job이 성공적으로 실행되었습니다."); // 응답 메시지 변경!
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("😭 수동 크롤링 배치 실행 중 오류 발생: {}", e.getMessage(), e); // 로그 메시지 변경!
            response.put("status", "error");
            response.put("message", "크롤링 배치 Job 실행 중 오류가 발생했습니다: " + e.getMessage()); // 응답 메시지 변경!
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
