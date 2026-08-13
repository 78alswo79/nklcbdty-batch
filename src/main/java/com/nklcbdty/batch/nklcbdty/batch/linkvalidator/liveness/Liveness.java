package com.nklcbdty.batch.nklcbdty.batch.linkvalidator.liveness;

/**
 * 공고 생존 판정 결과.
 *
 * <p>{@link #UNKNOWN} 이 따로 있는 이유: 판정을 못 했다는 것과 종료됐다는 것은 다르다.
 * 채용 API 가 잠깐 죽거나 응답 형식이 바뀌면 "모르겠다"가 나와야 하고, 그때는 공고를
 * 건드리지 않아야 한다. 이를 종료로 취급하면 API 장애 한 번에 그 회사 공고가 전멸한다.</p>
 */
public enum Liveness {
    /** 채용 중인 것이 확인됨 → end_date 를 건드리지 않는다. */
    ALIVE,
    /** 내려간 공고임이 확인됨 → 종료 처리한다. */
    CLOSED,
    /** 판정 불가(통신 실패·응답 형식 변경 등) → 아무것도 하지 않는다. */
    UNKNOWN
}
