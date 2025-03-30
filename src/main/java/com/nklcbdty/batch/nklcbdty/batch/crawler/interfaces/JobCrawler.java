package com.nklcbdty.batch.nklcbdty.batch.crawler.interfaces;

import java.util.List;

import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;

public interface JobCrawler {
    List<Job_mst> crawlJobs();
}
