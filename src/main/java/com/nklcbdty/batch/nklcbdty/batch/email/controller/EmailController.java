package com.nklcbdty.batch.nklcbdty.batch.email.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/batch")
@Slf4j
@RequiredArgsConstructor
public class EmailController {
    private final JobLauncher jobLauncher;
    private final Job emailBatchJob;

    @GetMapping("/runEmailJob")
    public ResponseEntity<Map<String, Object>> runEmailBatchJobManually() {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("⏰ 수동 이메일 발송 배치 실행 요청!");

            // JobParameters는 매번 다른 값으로 넣어줘야 같은 Job이 여러 번 실행될 수 있어요.
            // 여기서는 현재 시간을 파라미터로 사용해서 매번 다른 Job 인스턴스를 생성합니다.
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(emailBatchJob, jobParameters);
            log.info("🎉 수동 이메일 발송 배치 실행 완료!");

            response.put("status", "success");
            response.put("message", "이메일 발송 배치 Job이 성공적으로 실행되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("😭 수동 이메일 발송 배치 실행 중 오류 발생: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "이메일 발송 배치 Job 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
