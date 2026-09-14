package com.nklcbdty.batch.nklcbdty.batch.linkvalidator.run;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// 상세페이지 HTML 에 공고명이 있는지 보는 판정. 공고명에 & 가 들어가면 HTML 에는 &amp; 로
// 이스케이프돼 있어서, 원문 그대로 비교하면 살아있는 공고가 매일 종료로 찍힌다.
class LinkValidatorProcessorTest {

    private final LinkValidatorProcessor processor = new LinkValidatorProcessor(null, List.of());

    @Test
    @DisplayName("공고명이 원문 그대로 있으면 살아있는 것으로 본다")
    void containsSubject_plainMatch() {
        String html = "<h1>백엔드 개발자(내비 서비스)</h1>";

        assertThat(processor.containsSubject(html, "백엔드 개발자(내비 서비스)")).isTrue();
    }

    @Test
    @DisplayName("HTML 에서 & 가 이스케이프된 공고명도 살아있는 것으로 본다")
    void containsSubject_escapedAmpersand() {
        String html = "<h1>SLAM research scientist (R&amp;D)</h1>";

        assertThat(processor.containsSubject(html, "SLAM research scientist (R&D)")).isTrue();
    }

    @Test
    @DisplayName("공고명이 아예 없으면 종료로 본다")
    void containsSubject_missing() {
        String html = "<h1>다른 공고</h1>";

        assertThat(processor.containsSubject(html, "SLAM research scientist (R&D)")).isFalse();
    }
}
