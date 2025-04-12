package com.nklcbdty.batch.nklcbdty.batch.crawler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Batch_output_job_mst;



@Repository
public interface BatchJobMstRepository extends JpaRepository<Batch_output_job_mst, Long>{
}
