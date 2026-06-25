package com.run4you.dispatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 출동 관제 도메인 설정.
 * SSE 하트비트용 스케줄링을 활성화한다. (앱 전역에서 이미 @EnableScheduling 이면 중복 무해)
 */
@Configuration
@EnableScheduling
public class DispatchConfig {
}
