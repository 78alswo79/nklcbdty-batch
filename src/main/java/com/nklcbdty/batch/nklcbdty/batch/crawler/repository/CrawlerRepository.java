package com.nklcbdty.batch.nklcbdty.batch.crawler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;

@Repository
public interface CrawlerRepository extends JpaRepository<Job_mst, Long> {
    boolean existsByAnnoId(String annoId); // annoId 존재 여부 확인
    Job_mst findByAnnoId(String annoId);   // annoId로 Job_mst 조회
    List<Job_mst> findAllByAnnoIdIn(List<Long> annoIds); // 모든 annoId 조회
}
