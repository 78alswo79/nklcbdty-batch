package com.nklcbdty.batch.nklcbdty.batch.linkvalidator.liveness;

import com.nklcbdty.common.vo.Job_mst;

/**
 * 상세페이지 HTML 로는 생존 판정이 안 되는 회사를 채용 API 로 대신 판정한다.
 *
 * <p>기본 검증(LinkValidatorProcessor)은 상세페이지 원본 HTML 에 공고명이 들어있는지로
 * 살았는지를 본다. 상세페이지가 클라이언트 렌더링(SPA)인 회사는 응답이 200 이어도 셸 HTML 에
 * 공고명이 없어서 매번 종료로 찍힌다. 배민(career.woowahan.com)이 이 경우로, 매일 07:00 크롤이
 * 살려놓으면 09:30 링크 검증이 다시 죽이는 일이 반복됐다.</p>
 *
 * <p>구현체는 그 회사 채용 API 에서 "지금 열려 있는 공고 목록"을 받아 annoId 포함 여부로 판정한다.</p>
 */
public interface CompanyLivenessChecker {

    /** 이 공고를 이 체커가 판정할 수 있는지. */
    boolean supports(Job_mst job);

    /** 생존 여부. 판정 못 하면 {@link Liveness#UNKNOWN} 을 돌려 공고를 건드리지 않게 한다. */
    Liveness check(Job_mst job);
}
