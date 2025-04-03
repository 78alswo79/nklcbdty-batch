//package com.nklcbdty.batch.nklcbdty.batch.crawler.service;
//
//import java.util.Arrays;
//import java.util.List;
//
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import lombok.RequiredArgsConstructor;
//import reactor.core.publisher.Flux;
//
//
//@Service
//public class HttpWebClientService {
//
//	private final WebClient webClient;
//	private final List<String> whiteList = Arrays.asList(
//			"https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=naver"
//			, "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=kakao"
//			, "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=line"
//			, "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=coupang"
//			, "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=baemin"
//			, "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=daangn"
//			, "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=toss"
//			, "https://port-0-nklcbdty-service-m6qh1fte0c037b76.sel4.cloudtype.app/api/crawler?company=yanolja"
//		);
//	
//	public HttpWebClientService(WebClient.Builder webClientBuilder) {
//		this.webClient = webClientBuilder.build();
//	}
//	
//	public void callHttpWebClient() {
//		// 화이트 리스트에 있는 URL로 비동기 요청을 보냅니다.
//        Flux<String> responses = Flux.fromIterable(whiteList)
//                .flatMap(url -> webClient.get()
//                        .uri(url)
//                        .retrieve()
//                        .bodyToMono(String.class)
//                        .onErrorReturn("Error fetching data from: " + url)); // 오류 처리
//        
//        // 모든 요청이 완료된 후 결과를 리스트로 반환합니다.
//        //return responses.collectList();
//
//	}
//	
//}
