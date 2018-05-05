package com.moekr.moocoder.logic.service;

import com.moekr.moocoder.logic.vo.ExamVO;
import com.moekr.moocoder.util.enums.ExamStatus;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.dto.ExamDTO;
import org.springframework.data.domain.Page;

public interface ExamService {
	ExamVO create(int userId, ExamDTO examDTO) throws ServiceException;

	Page<ExamVO> retrievePage(int userId, int page, int limit, boolean joined, ExamStatus status) throws ServiceException;

	Page<ExamVO> retrievePage(int userId, int page, int limit, ExamStatus status) throws ServiceException;

	Page<ExamVO> retrievePage(int page, int limit, ExamStatus status) throws ServiceException;

	ExamVO retrieve(int userId, int examId) throws ServiceException;

	ExamVO retrieve(int examId) throws ServiceException;

	ExamVO update(int userId, int examId, ExamDTO examDTO) throws ServiceException;

	void delete(int userId, int examId) throws ServiceException;

	void join(int userId, int examId) throws ServiceException;
}
