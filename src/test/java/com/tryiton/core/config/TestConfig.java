package com.tryiton.core.config;

import io.awspring.cloud.s3.S3Template;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestConfig {
    
    @Bean
    @Primary
    public S3Template s3Template() {
        return mock(S3Template.class);
    }
}
