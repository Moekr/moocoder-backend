package com.moekr.aes.logic;

import com.moekr.aes.util.Method;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@CommonsLog
public class AsyncWrapper {

	@Async
	public void asyncInvoke(Method method) {
		try {
			method.invoke();
		} catch (Exception e) {
			log.error("进行异步调用时发生异常[" + e.getClass() + "]:" + e.getMessage());
		}
	}

	@Async
	public void asyncInvoke(Method method, long delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		asyncInvoke(method);
	}
}
