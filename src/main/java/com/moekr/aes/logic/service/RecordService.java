package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.RecordVO;
import com.moekr.aes.util.exceptions.ServiceException;

public interface RecordService {
	RecordVO retrieve(int userId, int recordId) throws ServiceException;

	RecordVO retrieve(int recordId) throws ServiceException;
}
