package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.ProblemVO;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.ProblemDTO;

import java.util.List;

public interface ProblemService {
	ProblemVO create(int userId, byte[] content) throws ServiceException;

	List<ProblemVO> retrievePage(int userId, int page) throws ServiceException;

	ProblemVO retrieve(int userId, int problemId) throws ServiceException;

	ProblemVO update(int userId, int problemId, ProblemDTO problemDTO) throws ServiceException;

	void delete(int userId, int problemId) throws ServiceException;

	ProblemVO deprecate(int userId, int problemId) throws ServiceException;

	ProblemVO create(byte[] content) throws ServiceException;

	List<ProblemVO> retrievePage(int page) throws ServiceException;

	ProblemVO retrieve(int problemId) throws ServiceException;

	ProblemVO update(int problemId, ProblemDTO problemDTO) throws ServiceException;

	void delete(int problemId) throws ServiceException;

	ProblemVO deprecate(int problemId) throws ServiceException;
}
