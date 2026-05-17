package com.nklcbdty.batch.nklcbdty.batch.linkvalidator.run;

import java.util.Iterator;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import com.nklcbdty.batch.nklcbdty.batch.crawler.repository.JobRepositoryInterface;
import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class LinkValidatorReader implements ItemReader<Job_mst> {
    private final JobRepositoryInterface jobRepositoryInterface;
    private Iterator<Job_mst> iterator;
    private boolean initialized = false;

    @Getter
    private int total = 0;

    @Override
    public Job_mst read() {
        if (!initialized) {
            List<Job_mst> activeJobs = jobRepositoryInterface.findActiveJobs();
            this.total = activeJobs.size();
            log.info("📋 링크 검증 대상 공고 수: {}", total);
            this.iterator = activeJobs.iterator();
            initialized = true;
        }
        if (iterator != null && iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }
}