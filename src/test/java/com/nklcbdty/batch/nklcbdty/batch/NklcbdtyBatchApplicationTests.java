package com.nklcbdty.batch.nklcbdty.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
// 앱 전체 컨텍스트를 띄우는 테스트라 운영 DB 등 자격증명이 환경변수로 있어야 통과한다.
// 그게 없는 환경(로컬·CI)에서는 항상 실패해서, 이 한 건 때문에 ./gradlew test 를
// CI 에 걸지 못하고 있었다. 자격증명이 있을 때만 돌린다.
@EnabledIfEnvironmentVariable(named = "DATASOURCE_URL", matches = ".+")
class NklcbdtyBatchApplicationTests {

	@Test
	void contextLoads() {
	}

}
