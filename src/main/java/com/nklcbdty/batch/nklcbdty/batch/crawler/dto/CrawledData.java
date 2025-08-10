package com.nklcbdty.batch.nklcbdty.batch.crawler.dto;

import lombok.Data;

@Data
public class CrawledData {
    private String url;
    private String title;
    private String content;
}
