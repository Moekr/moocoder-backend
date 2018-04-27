package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.CommitVO;
import com.moekr.aes.util.exceptions.ServiceException;

public interface CommitService {
	CommitVO retrieve(int userId, int commitId) throws ServiceException;

	CommitVO retrieve(int commitId) throws ServiceException;
}
