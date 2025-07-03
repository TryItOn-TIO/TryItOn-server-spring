package com.tryiton.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.auditing.enabled=false")
@Tag("integration")
class CoreApplicationTests {

	@Test
	void contextLoads() {
	}

}