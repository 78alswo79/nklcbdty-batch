package com.nklcbdty.batch.nklcbdty.batch.crawler.vo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Entity
@Table(name = "batch_output_job_mst")
@Data
@JsonIgnoreProperties
public class Batch_output_job_mst {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String companyCd; // 회사구분

    @Column(nullable = true)
    private Long annoId; // 공고 고유 번호

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
}
