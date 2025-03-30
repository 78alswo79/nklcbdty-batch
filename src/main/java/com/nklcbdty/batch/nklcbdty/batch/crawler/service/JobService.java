package com.nklcbdty.batch.nklcbdty.batch.crawler.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nklcbdty.batch.nklcbdty.batch.crawler.repository.JobRepository;
import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;

@Service
public class JobService {
    private final JobRepository jobRepository2;

    @Autowired
    public JobService(JobRepository jobRepository2) {
        this.jobRepository2 = jobRepository2;
    }

    public List<Job_mst> list(String company) {
        List<Job_mst> items;
        if ("ALL".equals(company)) {
            items = jobRepository2.findAll();
        } else {
            items = jobRepository2.findAllByCompanyCd(company);
        }


        return items;
    }
}
