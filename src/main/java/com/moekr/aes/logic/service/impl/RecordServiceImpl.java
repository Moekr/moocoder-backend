package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.RecordDAO;
import com.moekr.aes.data.entity.Record;
import com.moekr.aes.logic.service.RecordService;
import com.moekr.aes.logic.vo.RecordVO;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.Asserts;
import com.moekr.aes.util.exceptions.ServiceException;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Service;

@Service
@CommonsLog
public class RecordServiceImpl implements RecordService {
	private final RecordDAO recordDAO;

	public RecordServiceImpl(RecordDAO recordDAO) {
		this.recordDAO = recordDAO;
	}

	@Override
	public RecordVO retrieve(int userId, int recordId) throws ServiceException {
		Record record = recordDAO.findById(recordId);
		Asserts.notNull(record, "选择的提交记录不存在");
		if (record.getCommit().getResult().getOwner().getId() == userId) {
			return new RecordVO(record);
		} else if (record.getCommit().getResult().getExam().getCreator().getId() == userId) {
			return new RecordVO(record);
		}
		throw new AccessDeniedException();
	}

	@Override
	public RecordVO retrieve(int recordId) throws ServiceException {
		Record record = recordDAO.findById(recordId);
		Asserts.notNull(record, "选择的提交记录不存在");
		return new RecordVO(record);
	}
}
