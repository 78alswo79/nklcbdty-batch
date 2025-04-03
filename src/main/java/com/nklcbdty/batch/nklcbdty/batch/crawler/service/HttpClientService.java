package com.nklcbdty.batch.nklcbdty.batch.crawler.service;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class HttpClientService {
    private final OkHttpClient client = new OkHttpClient();
    private final List<String> whiteList = Arrays.asList(
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=naver",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=kakao",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=line",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=coupang",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=baemin",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=daangn",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=toss",
            "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=yanolja"
    );

    public void callHttpClient() {
        for (String url : whiteList) {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                // 응답 처리
                System.out.println(response.body().string());
            } catch (IOException e) {
                System.err.println("Error fetching data from: " + url + " - " + e.getMessage());
            }
        }
    }
}
