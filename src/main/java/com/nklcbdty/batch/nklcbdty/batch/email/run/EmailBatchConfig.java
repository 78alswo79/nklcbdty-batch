package com.nklcbdty.batch.nklcbdty.batch.email.run;

import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.nklcbdty.batch.nklcbdty.batch.email.dto.EmailSendRequest;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class EmailBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    // Step 정의 (청크, 재시도, 건너뛰기 포함)
    @Bean
    public Step emailSendingStep(
            ItemReader<Map.Entry<String, String>> emailDataReader,
            ItemProcessor<Map.Entry<String, String>, EmailSendRequest> emailContentProcessor,
            ItemWriter<EmailSendRequest> emailSenderWriter) {

        // StepBuilder 생성자에 Step 이름과 JobRepository를 전달합니다.
        return new StepBuilder("emailSendingStep", jobRepository) // <-- 변경된 부분!
                // chunk() 메서드에 청크 사이즈와 PlatformTransactionManager를 전달합니다.
                .<Map.Entry<String, String>, EmailSendRequest>chunk(5, platformTransactionManager) // <-- 변경된 부분!
                .reader(emailDataReader)
                .processor(emailContentProcessor)
                .writer(emailSenderWriter)
                .faultTolerant() // 오류 허용 설정 시작
                    // 재시도 설정: RuntimeException 발생 시 최대 3번 재시도
                    .retryLimit(3)
                    .retry(RuntimeException.class) // DummyEmailService에서 던지는 RuntimeException을 재시도 대상으로 지정
                    // 건너뛰기 설정: IllegalArgumentException 발생 시 건너뛰기
                    .skipLimit(10) // 최대 10개의 아이템을 건너뛸 수 있음
                    .skip(IllegalArgumentException.class) // DummyEmailService에서 던지는 IllegalArgumentException을 건너뛰기 대상으로 지정
                .build();
    }

    // Job 정의 (재시작 가능성 및 메타데이터 관리)
    @Bean
    public Job emailBatchJob(Step emailSendingStep) {
        // JobBuilder 생성자에 Job 이름과 JobRepository를 전달합니다.
        return new JobBuilder("emailBatchJob", jobRepository) // <-- 변경된 부분!
                .incrementer(new RunIdIncrementer()) // JobParameters를 자동으로 증가시켜 매번 새로운 JobInstance 생성 (재시작 가능성 확보)
                .start(emailSendingStep)
                .build();
    }
}
