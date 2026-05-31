package com.nklcbdty.batch.nklcbdty.batch.linkvalidator.run;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.nklcbdty.common.crawler.repository.JobRepository;
import com.nklcbdty.common.vo.Job_mst;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class LinkValidatorWriter implements ItemWriter<Job_mst> {
    private final JobRepository jobRepositoryInterface;

    @Override
    public void write(Chunk<? extends Job_mst> items) {
        for (Job_mst job : items) {
            jobRepositoryInterface.save(job);
            log.info("📝 end_date 갱신 완료: id={} -> {}", job.getId(), job.getEndDate());
        }
    }
}