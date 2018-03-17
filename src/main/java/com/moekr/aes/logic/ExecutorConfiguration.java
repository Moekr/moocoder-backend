package com.moekr.aes.logic;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

@EnableScheduling
@EnableAsync
@Configuration
public class ExecutorConfiguration {
	private static final int PROCESSOR_THREAD = Runtime.getRuntime().availableProcessors();
	private static final int MAX_THREAD = 16;

	@Bean
	public ScheduledExecutorService scheduledExecutor() {
		return Executors.newScheduledThreadPool(Math.min(2 * PROCESSOR_THREAD, MAX_THREAD));
	}

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(Math.min(2 * PROCESSOR_THREAD, MAX_THREAD));
		taskExecutor.setMaxPoolSize(Math.min(4 * PROCESSOR_THREAD, MAX_THREAD));
		taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		taskExecutor.initialize();
		return taskExecutor;
	}
}
