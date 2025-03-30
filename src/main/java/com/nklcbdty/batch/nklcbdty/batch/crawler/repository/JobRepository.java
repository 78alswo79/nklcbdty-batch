package com.nklcbdty.batch.nklcbdty.batch.crawler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;

@Repository("jobRepository2")
public interface JobRepository extends JpaRepository<Job_mst, Long> {
    List<Job_mst> findAllByCompanyCd(String company);
}
