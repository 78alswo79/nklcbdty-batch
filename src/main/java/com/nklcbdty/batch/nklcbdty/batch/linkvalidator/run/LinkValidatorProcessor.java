package com.nklcbdty.batch.nklcbdty.batch.linkvalidator.run;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.nklcbdty.batch.nklcbdty.batch.crawler.vo.Job_mst;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class LinkValidatorProcessor implements ItemProcessor<Job_mst, Job_mst> {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final long MIN_INTERVAL_PER_DOMAIN_MS = 5_000L;
    private static final String ERROR_END_DATE = "2000-01-01 00:00";

    private final LinkValidatorReader linkValidatorReader;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    private final Map<String, Long> lastCallPerDomain = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public Job_mst process(Job_mst job) throws Exception {
        int current = counter.incrementAndGet();
        int total = linkValidatorReader.getTotal();
        log.info("📊 진행도: [{}/{}] id={}", current, total, job.getId());

        String url = job.getJobDetailLink();
        String annoSubject = job.getAnnoSubject();
        if (url == null || url.isEmpty() || annoSubject == null || annoSubject.isEmpty()) {
            log.warn("🚫 URL 또는 공고명이 비어 있어 검증을 건너뜁니다. id={}", job.getId());
            return null;
        }

        throttleByDomain(url);

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("⚠️ 응답 실패 (status={}): id={} url={}", response.code(), job.getId(), url);
                return markAsError(job);
            }
            ResponseBody body = response.body();
            if (body == null) {
                log.warn("⚠️ 응답 본문이 비어 있음. id={} url={}", job.getId(), url);
                return markAsError(job);
            }
            String content = body.string();
            if (content.contains(annoSubject)) {
                log.debug("✅ 공고 유효: id={}", job.getId());
                return null;
            }
            log.info("🚨 공고 종료 감지: id={} 공고명='{}'", job.getId(), annoSubject);
            String yesterday = LocalDate.now().minusDays(1).atTime(23, 59, 59).format(FORMATTER);
            job.setEndDate(yesterday);
            job.setUpdateDts(LocalDateTime.now());
            return job;
        } catch (IOException e) {
            log.error("😭 페이지 조회 중 오류 발생 id={} url={} - {}", job.getId(), url, e.getMessage());
            return markAsError(job);
        } finally {
            String domain = extractDomain(url);
            if (domain != null) {
                lastCallPerDomain.put(domain, System.currentTimeMillis());
            }
        }
    }

    private Job_mst markAsError(Job_mst job) {
        job.setEndDate(ERROR_END_DATE);
        job.setUpdateDts(LocalDateTime.now());
        return job;
    }

    private void throttleByDomain(String url) throws InterruptedException {
        String domain = extractDomain(url);
        if (domain == null) {
            return;
        }
        Long last = lastCallPerDomain.get(domain);
        if (last == null) {
            return;
        }
        long elapsed = System.currentTimeMillis() - last;
        if (elapsed < MIN_INTERVAL_PER_DOMAIN_MS) {
            long sleepMs = MIN_INTERVAL_PER_DOMAIN_MS - elapsed;
            log.debug("⏳ 도메인 [{}] 최소 간격 유지를 위해 {}ms 대기", domain, sleepMs);
            Thread.sleep(sleepMs);
        }
    }

    private String extractDomain(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return null;
        }
    }
}
