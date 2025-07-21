package com.nklcbdty.batch.nklcbdty.batch.crawler.repository;

import java.util.List;
import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;

public interface JobRepositoryCustom {
    List<Job_mst> findJobsByDetailedCriteria(
        List<String> companyCds,
        List<String> subJobCdNms,
        Long personalHistoryStart,
        Long personalHistoryEnd
    );
}
