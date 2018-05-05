package com.moekr.moocoder.logic.service;

import com.moekr.moocoder.logic.vo.CommitVO;
import com.moekr.moocoder.util.exceptions.ServiceException;

public interface CommitService {
	CommitVO retrieve(int userId, int commitId) throws ServiceException;

	CommitVO retrieve(int commitId) throws ServiceException;
}
