package com.nklcbdty.batch.nklcbdty.batch.crawler.run;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import lombok.extern.slf4j.Slf4j;
import com.nklcbdty.batch.nklcbdty.batch.crawler.dto.CrawledData;

@Slf4j
public class CrawlerDataWriter implements ItemWriter<CrawledData> {

    @Override
    public void write(Chunk<? extends CrawledData> items) throws Exception {
        for (CrawledData data : items) {
            log.info("Saving crawled data: URL={}, Title={}", data.getUrl(), data.getTitle());
        }
        log.info("Successfully wrote {} crawled items.", items.size());

    }
}
