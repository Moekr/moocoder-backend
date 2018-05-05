package com.moekr.moocoder.logic.service;

import com.moekr.moocoder.logic.vo.ProblemVO;
import com.moekr.moocoder.util.enums.ProblemType;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.dto.ProblemDTO;
import org.springframework.data.domain.Page;

public interface ProblemService {
	ProblemVO create(int userId, ProblemDTO problemDTO, byte[] content) throws ServiceException;

	Page<ProblemVO> retrievePage(int userId, int page, int limit, ProblemType type) throws ServiceException;

	Page<ProblemVO> retrievePage(int page, int limit, ProblemType type) throws ServiceException;

	ProblemVO retrieve(int userId, int problemId) throws ServiceException;

	void update(int userId, int problemId, String path, byte[] content) throws ServiceException;

	void delete(int userId, int problemId) throws ServiceException;
}
