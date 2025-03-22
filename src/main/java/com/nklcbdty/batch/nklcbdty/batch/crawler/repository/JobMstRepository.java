package com.nklcbdty.batch.nklcbdty.batch.crawler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;

@Repository
public interface JobMstRepository extends JpaRepository<Job_mst, Long>{

}
