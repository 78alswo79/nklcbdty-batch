package com.nklcbdty.batch.nklcbdty.batch.crawler.repository;

import java.util.List;
import java.util.Objects;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;
import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.QJob_mst;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@Repository
public class JobRepositoryInterfaceImpl extends QuerydslRepositorySupport implements JobRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public JobRepositoryInterfaceImpl(EntityManager entityManager) {
        super(Job_mst.class);
        this.queryFactory = new JPAQueryFactory(Objects.requireNonNull(entityManager));
    }

    @Override
    public List<Job_mst> findJobsByDetailedCriteria(
        List<String> companyCds,
        List<String> subJobCdNms,
        Long personalHistoryStart, // Long 타입
        Long personalHistoryEnd    // Long 타입
    ) {
        QJob_mst job_mst = QJob_mst.job_mst;

        return queryFactory
                .selectFrom(job_mst)
                .where(
                    companyCdIn(companyCds),
                    subJobCdNmIn(subJobCdNms),
                    personalHistoryRange(job_mst, personalHistoryStart, personalHistoryEnd) // Long 타입용 새 메소드
                )
                .orderBy(job_mst.endDate.desc())
                .fetch();
    }

    private BooleanExpression companyCdIn(List<String> companyCds) {
        // 리스트가 null이거나 비어있으면 조건을 적용하지 않음
        if (companyCds == null || companyCds.isEmpty()) {
            return null;
        }
        return QJob_mst.job_mst.companyCd.in(companyCds);
    }

    private BooleanExpression subJobCdNmIn(List<String> subJobCdNms) {
        if (subJobCdNms == null || subJobCdNms.isEmpty()) {
            return null;
        }
        return QJob_mst.job_mst.subJobCdNm.in(subJobCdNms);
    }

    private BooleanExpression personalHistoryRange(
        QJob_mst job,
        Long userFilterMinExp,
        Long userFilterMaxExp
    ) {
        // --- 1. 사용자가 '모든 경력'을 검색한 경우 (0L, 0L) ---
        // userFilterMinExp와 userFilterMaxExp가 null이 아니어야 비교 가능
        if (userFilterMinExp != null && userFilterMinExp == 0L &&
            userFilterMaxExp != null && userFilterMaxExp == 0L) {
            return null; // 조건 없음 -> 전체 조회
        }

        // --- 2. 사용자가 특정 경력 범위를 검색한 경우 (0L, 0L 이외) ---

        // '경력 무관' 공고 (job.personalHistory === 0 && job.personalHistoryEnd === 0)는
        // '0L,0L' 입력 시에만 노출되어야 하므로, 특정 경력 검색 시에는 제외되어야 한다.
        BooleanExpression excludeNoExperienceJob =
            job.personalHistory.gt(0L) // 최소 경력이 0 초과이거나
            .or(job.personalHistoryEnd.gt(0L)); // 최대 경력이 0 초과인 공고


        // --- 조건부 매칭 로직 ---
        // 1. 공고가 요구하는 최소 경력 (job.personalHistory)은 사용자의 최소 경력 (userFilterMinExp)보다 작거나 같아야 함.
        //    단, userFilterMinExp가 0L (0년차부터 시작)인 경우, job.personalHistory가 무엇이든 만족함.
        BooleanExpression condMinCareerMatches =
            job.personalHistory.loe(userFilterMinExp); // job.personalHistory <= userFilterMinExp

        // userFilterMinExp가 0L인지 체크하는 조건 (null 방지)
        BooleanExpression userMinExpIsZero;
        if (userFilterMinExp != null && userFilterMinExp == 0L) {
            userMinExpIsZero = Expressions.TRUE;
        } else {
            userMinExpIsZero = Expressions.FALSE;
        }
        condMinCareerMatches = condMinCareerMatches.or(userMinExpIsZero);


        // 2. 공고가 요구하는 최대 경력 (job.personalHistoryEnd)은 사용자의 최대 경력 (userFilterMaxExp)보다 크거나 같아야 함.
        //    단, userFilterMaxExp가 0L (무제한)인 경우, job.personalHistoryEnd가 무엇이든 만족함.
        //    또한, job.personalHistoryEnd가 0L (공고의 최대 경력이 무제한)인 경우도 포함
        BooleanExpression condMaxCareerMatches =
            // 공고의 최대 경력이 무제한 (job.personalHistoryEnd == 0L) 이거나
            job.personalHistoryEnd.eq(0L)
            .or(
                // job.personalHistoryEnd >= userFilterMaxExp
                job.personalHistoryEnd.goe(userFilterMaxExp)
            );

        // userFilterMaxExp가 0L인지 체크하는 조건 (null 방지)
        BooleanExpression userMaxExpIsZero;
        if (userFilterMaxExp != null && userFilterMaxExp == 0L) {
            userMaxExpIsZero = Expressions.TRUE;
        } else {
            userMaxExpIsZero = Expressions.FALSE;
        }
        condMaxCareerMatches = condMaxCareerMatches.or(userMaxExpIsZero);


        // 최종 조건:
        // '경력 무관 공고는 제외' AND '공고 요구 경력이 사용자 경력 범위에 부합'
        return excludeNoExperienceJob
                .and(condMinCareerMatches)
                .and(condMaxCareerMatches);
    }
}

