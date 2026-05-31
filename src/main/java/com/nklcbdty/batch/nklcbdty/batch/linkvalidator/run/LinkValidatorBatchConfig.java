package com.nklcbdty.batch.nklcbdty.batch.linkvalidator.run;

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

import com.nklcbdty.common.vo.Job_mst;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class LinkValidatorBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    @Bean
    public Step linkValidatorStep(
            ItemReader<Job_mst> linkValidatorReader,
            ItemProcessor<Job_mst, Job_mst> linkValidatorProcessor,
            ItemWriter<Job_mst> linkValidatorWriter) {

        return new StepBuilder("linkValidatorStep", jobRepository)
                .<Job_mst, Job_mst>chunk(10, platformTransactionManager)
                .reader(linkValidatorReader)
                .processor(linkValidatorProcessor)
                .writer(linkValidatorWriter)
                .faultTolerant()
                    .skipLimit(50)
                    .skip(Exception.class)
                .build();
    }

    @Bean
    public Job linkValidatorJob(Step linkValidatorStep) {
        return new JobBuilder("linkValidatorJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(linkValidatorStep)
                .build();
    }
}