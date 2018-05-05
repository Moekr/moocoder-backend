package com.moekr.moocoder.logic;

import com.moekr.moocoder.logic.service.impl.ClosedExamChecker;
import com.moekr.moocoder.util.Method;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@CommonsLog
public class TaskScheduler {
	private final ClosedExamChecker closedExamChecker;

	public TaskScheduler(ClosedExamChecker closedExamChecker) {
		this.closedExamChecker = closedExamChecker;
	}

	@Scheduled(cron = "5 * * * * *")
	protected void scheduledCheckClosedExam() {
		scheduledInvoke(closedExamChecker::check);
	}

	private void scheduledInvoke(Method method) {
		try {
			method.invoke();
		} catch (Exception e) {
			log.error("进行定时调用时发生异常[" + e.getClass() + "]:" + e.getMessage());
		}
	}
}
