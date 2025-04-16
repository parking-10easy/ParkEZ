package com.parkez.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Profile("!test") // test 환경에서는 스케줄링 비활성화
public class SchedulingConfig {
}
