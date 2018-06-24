package com.moekr.moocoder.logic.service;

import com.moekr.moocoder.logic.vo.ResultVO;
import com.moekr.moocoder.util.exceptions.ServiceException;

import java.util.List;

public interface ResultService {
	ResultVO retrieve(int userId, int resultId) throws ServiceException;

	List<ResultVO> retrieveByOwner(int userId) throws ServiceException;

	List<ResultVO> retrieveByExam(int userId, int examId) throws ServiceException;

	ResultVO retrieve(int resultId) throws ServiceException;

	List<ResultVO> retrieveByExam(int examId) throws ServiceException;
}
