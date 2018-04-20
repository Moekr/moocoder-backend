package com.moekr.aes.data;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
@CommonsLog
public class TransactionWrapper {
	private final PlatformTransactionManager transactionManager;

	@Autowired
	public TransactionWrapper(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void wrap(Method method) throws Exception {
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
		try {
			method.invoke();
			transactionManager.commit(transactionStatus);
		} catch (Exception e) {
			transactionManager.rollback(transactionStatus);
			throw e;
		}
	}

	public void wrap(SafeMethod method) {
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
		try {
			method.invoke();
			transactionManager.commit(transactionStatus);
		} catch (TransactionException e) {
			transactionManager.rollback(transactionStatus);
			log.error(e.getMessage());
		}
	}

	@FunctionalInterface
	public interface Method {
		void invoke() throws Exception;
	}

	@FunctionalInterface
	public interface SafeMethod {
		void invoke();
	}
}
