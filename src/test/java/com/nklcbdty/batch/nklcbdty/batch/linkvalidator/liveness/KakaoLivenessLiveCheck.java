package com.nklcbdty.batch.nklcbdty.batch.linkvalidator.liveness;

import org.junit.jupiter.api.Test;

import com.nklcbdty.common.vo.Job_mst;

/**
 * 카카오 채용 API 를 실제로 불러 생존 판정이 도는지 눈으로 확인하는 용도.
 *
 * <p>단위 테스트가 아니다(외부 의존·비결정적). 응답 형식이 바뀌어 판정이 전부 UNKNOWN 이 되거나
 * 공고가 전멸할 때 {@code ./gradlew test --tests "*KakaoLivenessLiveCheck*"} 로 켜서 본다.</p>
 */
@org.junit.jupiter.api.Disabled("실제 채용 API 를 호출한다. 필요할 때 수동으로 켠다")
class KakaoLivenessLiveCheck {

    private Job_mst job(String realId) {
        Job_mst item = new Job_mst();
        item.setCompanyCd("KAKAO");
        item.setJobDetailLink("https://careers.kakao.com/jobs/" + realId + "?page=1");
        return item;
    }

    @Test
    void checkAgainstRealApi() {
        KakaoLivenessChecker checker = new KakaoLivenessChecker();

        // 실제로 열려 있는 공고 / 존재하지 않는 공고 / 상세 링크가 깨진 공고
        for (String realId : new String[] {"P-14324", "S-4752", "S-1"}) {
            System.out.println(realId + " -> " + checker.check(job(realId)));
        }
    }
}
