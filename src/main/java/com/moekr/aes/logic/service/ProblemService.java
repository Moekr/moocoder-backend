package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.ProblemVO;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.ProblemDTO;
import org.springframework.data.domain.Page;

public interface ProblemService {
	ProblemVO create(int userId, byte[] content) throws ServiceException;

	Page<ProblemVO> retrievePage(int userId, int page, int limit) throws ServiceException;

	ProblemVO retrieve(int userId, int problemId) throws ServiceException;

	ProblemVO update(int userId, int problemId, ProblemDTO problemDTO) throws ServiceException;

	void delete(int userId, int problemId) throws ServiceException;

	ProblemVO deprecate(int userId, int problemId) throws ServiceException;

	ProblemVO create(byte[] content) throws ServiceException;

	Page<ProblemVO> retrievePage(int page, int limit) throws ServiceException;

	ProblemVO retrieve(int problemId) throws ServiceException;

	ProblemVO update(int problemId, ProblemDTO problemDTO) throws ServiceException;

	void delete(int problemId) throws ServiceException;

	ProblemVO deprecate(int problemId) throws ServiceException;
}
