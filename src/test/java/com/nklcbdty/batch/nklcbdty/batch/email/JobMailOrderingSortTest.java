package com.nklcbdty.batch.nklcbdty.batch.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.nklcbdty.common.email.JobMailOrdering;
import com.nklcbdty.common.vo.Job_mst;

// 0.10.2 정렬 정책: 1년 이내 공고는 마감 임박순(오름차순), 종료일 없는(상시) 공고는 그 뒤,
// 2999-12-31 등 1년 초과(무기한) 공고는 맨 뒤.
class JobMailOrderingSortTest {

    private Job_mst job(String subject, String endDate) {
        Job_mst j = new Job_mst();
        j.setAnnoSubject(subject);
        j.setEndDate(endDate);
        return j;
    }

    @Test
    @DisplayName("1년 이내 공고는 마감 임박순, 상시는 그 뒤, 2999는 맨 뒤")
    void ordersByDeadlineAscending_alwaysAndFarFutureLast() {
        LocalDate today = LocalDate.now();
        Job_mst nextMonth = job("한 달 후 마감", today.plusMonths(1) + " 23:59:59");
        Job_mst thisWeek = job("이번 주 마감", today.plusDays(5) + " 23:59:59");
        Job_mst always = job("상시(영입종료시)", "영입종료시");
        Job_mst veryFar = job("2999 무기한", "2999-12-31 23:59:59");

        // 원본 순서는 뒤섞여 있어도 정렬되어야 한다.
        List<Job_mst> input = List.of(veryFar, nextMonth, always, thisWeek);

        List<Job_mst> result = JobMailOrdering.pushFarFutureEndDateToBottom(input);

        assertThat(result).extracting(Job_mst::getAnnoSubject)
            .containsExactly("이번 주 마감", "한 달 후 마감", "상시(영입종료시)", "2999 무기한");
    }
}
