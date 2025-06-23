package com.nklcbdty.batch.nklcbdty.batch.crawler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;

@Repository
public interface JobRepositoryInterface extends JpaRepository<Job_mst, Long> {
    List<Job_mst> findAllByCompanyCdAndSubJobCdNmIsNotNullOrderByEndDateAsc(String company);
    List<Job_mst> findAllBySubJobCdNmIsNotNull();
    List<Job_mst> findAllByCompanyCdInAndSubJobCdNmInOrderByEndDateDesc(List<String> companyCds, List<String> subJobCdNms);

    @Modifying
    void deleteByCompanyCd(String companyCd);
}
