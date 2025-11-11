package com.idear.backend;

import com.idear.backend.config.EmbeddedRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(EmbeddedRedisConfig.class)
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
