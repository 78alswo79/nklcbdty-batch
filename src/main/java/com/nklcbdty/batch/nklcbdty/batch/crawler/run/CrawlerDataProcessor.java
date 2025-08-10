package com.nklcbdty.batch.nklcbdty.batch.crawler.run;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.nklcbdty.batch.nklcbdty.batch.crawler.dto.CrawledData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CrawlerDataProcessor implements ItemProcessor<String, CrawledData> {
    private final RestTemplate restTemplate;

    @Override
    public CrawledData process(String itemUrl) throws Exception {
        // 5분 = 300초 = 300,000 밀리초 (ms)
        final long DELAY_MILLIS = 3 * 60 * 1000L;
        log.info("⏰ 다음 크롤링 작업을 위해 {}분 대기합니다...", DELAY_MILLIS / 60000);
        try {
            Thread.sleep(DELAY_MILLIS); // 5분 대기
        } catch (InterruptedException e) {
            log.warn("크롤링 배치 스레드 대기 중 인터럽트 발생!");
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
        }
        log.info("▶️ {} 호출 시작...", itemUrl);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(itemUrl, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("🎉 API 호출 성공: {} (Status: {})", itemUrl, response.getStatusCode());

                CrawledData crawledData = new CrawledData();
                crawledData.setUrl(itemUrl); // 호출한 URL 저장
                crawledData.setTitle("API Call Result for " + itemUrl);
                crawledData.setContent(response.getBody());

                return crawledData; // 다음 Writer로 전달
            } else {
                log.warn("❌ API 호출 실패: {} (Status: {})", itemUrl, response.getStatusCode());
                // 실패 시 null을 반환하면 해당 아이템은 건너뛰어지고, 예외를 던지면 재시도 또는 스킵될 수 있어.
                // 여기서는 에러를 throw해서 faultTolerant가 동작하도록 할게.
                throw new RuntimeException("API call failed: " + itemUrl + " with status " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("😭 API 호출 중 오류 발생: {}. URL: {}", e.getMessage(), itemUrl);
            throw e;
        }
    }
}
