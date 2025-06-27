package com.tryiton.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.config.import=",
		"spring.cloud.aws.secretsmanager.enabled=false",
		"spring.cloud.aws.region.static=us-east-1",
		"spring.cloud.aws.credentials.access-key=test",
		"spring.cloud.aws.credentials.secret-key=test"
})
class CoreApplicationTests {

	@Test
	void contextLoads() {
	}

}
