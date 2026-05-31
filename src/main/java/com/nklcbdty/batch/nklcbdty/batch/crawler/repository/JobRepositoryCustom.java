package com.nklcbdty.batch.nklcbdty.batch.crawler.repository;

import java.util.List;
import com.nklcbdty.common.vo.Job_mst;

public interface JobRepositoryCustom {
    List<Job_mst> findJobsByDetailedCriteria(
        List<String> companyCds,
        List<String> subJobCdNms,
        Long personalHistoryStart,
        Long personalHistoryEnd
    );

    List<Job_mst> findActiveJobs();
}
