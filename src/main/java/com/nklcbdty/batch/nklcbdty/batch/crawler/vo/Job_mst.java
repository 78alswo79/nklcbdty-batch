package com.nklcbdty.batch.nklcbdty.batch.crawler.vo;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Entity
@Table(name = "job_mst")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job_mst {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String companyCd; // 회사구분

    @Column(nullable = true)
    private String annoId; // 공고 고유 번호

    @Column(nullable = true)
    private String classCdNm; // Tech, Business, ...

    @Column(nullable = true)
    private String empTypeCdNm; // 정규, 비정규

    @Column(nullable = true)
    private String annoSubject; // 채용공고명

    @Column(nullable = true)
    private String subJobCdNm; // 직무 구분

    @Column(nullable = true)
    private String sysCompanyCdNm; // 회사 구분

    @Column(nullable = true)
    private String jobDetailLink; // 공고 URL
    
    @Column(nullable = true)
    private String workplace; // 근무지

    @Column(nullable = true)
    private String startDate;

    @Column(nullable = true)
    private String endDate;

    @Column(nullable = true)
    private Long personalHistory;

    @Column(nullable = true)
    private Long personalHistoryEnd;

    @CreationTimestamp
    private LocalDateTime insertDts;

    @UpdateTimestamp
    private LocalDateTime updateDts;
}
