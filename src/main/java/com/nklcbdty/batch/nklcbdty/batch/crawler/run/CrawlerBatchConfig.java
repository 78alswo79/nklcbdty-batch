package com.nklcbdty.batch.nklcbdty.batch.crawler.run;

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
import org.springframework.web.client.RestTemplate;

import com.nklcbdty.batch.nklcbdty.batch.crawler.dto.CrawledData;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CrawlerBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    @Bean
    public RestTemplate restTemplate() {
        // 필요하다면 타임아웃 등 추가 설정 가능
        return new RestTemplate();
    }

    // --- ItemReader, ItemProcessor, ItemWriter 빈 정의 ---
    @Bean
    public ItemReader<String> crawlerDataReader() {
        return new CrawlerDataReader(); // 이전에 만든 ListItemReader를 반환
    }

    @Bean
    public ItemProcessor<String, CrawledData> crawlerDataProcessor(RestTemplate restTemplate) {
        // RestTemplate을 주입받아 사용
        return new CrawlerDataProcessor(restTemplate);
    }

    @Bean
    public ItemWriter<CrawledData> crawlerDataWriter() {
        // 실제 저장 로직에 따라 필요한 빈 주입
        return new CrawlerDataWriter();
    }

    // --- Step 정의 ---
    @Bean
    public Step crawlerProcessingStep(
            ItemReader<String> crawlerDataReader,
            ItemProcessor<String, CrawledData> crawlerDataProcessor,
            ItemWriter<CrawledData> crawlerDataWriter) {

        return new StepBuilder("crawlerProcessingStep", jobRepository)
                // !!! 가장 중요: chunk 사이즈를 1로 설정하여 각 Item마다 Processor가 호출되도록 함 !!!
                .<String, CrawledData>chunk(1, platformTransactionManager)
                .reader(crawlerDataReader)
                .processor(crawlerDataProcessor)
                .writer(crawlerDataWriter)
                .faultTolerant()
                    .retryLimit(3)
                    .retry(RuntimeException.class) // API 호출 실패 등 RuntimeException 재시도
                    .skipLimit(5) // 스킵할 수 있는 아이템 수
                    .skip(Exception.class) // 모든 Exception을 스킵 대상으로 지정 (좀 더 구체화 가능)
                .build();
    }

    // --- Job 정의 ---
    @Bean
    public Job crawlerBatchJob(Step crawlerProcessingStep) {
        return new JobBuilder("crawlerBatchJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(crawlerProcessingStep)
                .build();
    }
}
