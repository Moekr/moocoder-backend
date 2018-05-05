package com.moekr.moocoder.logic.service;

import com.moekr.moocoder.logic.vo.RecordVO;
import com.moekr.moocoder.util.exceptions.ServiceException;

public interface RecordService {
	RecordVO retrieve(int userId, int recordId) throws ServiceException;

	RecordVO retrieve(int recordId) throws ServiceException;
}
