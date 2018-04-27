package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.ExamVO;
import com.moekr.aes.util.enums.ExamStatus;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.ExamDTO;
import org.springframework.data.domain.Page;

public interface ExamService {
	ExamVO create(int userId, ExamDTO examDTO) throws ServiceException;

	Page<ExamVO> retrievePage(int userId, int page, int limit, boolean joined, ExamStatus status) throws ServiceException;

	Page<ExamVO> retrievePage(int userId, int page, int limit, ExamStatus status) throws ServiceException;

	ExamVO retrieve(int userId, int examinationId) throws ServiceException;

	ExamVO update(int userId, int examinationId, ExamDTO examDTO) throws ServiceException;

	void delete(int userId, int examinationId) throws ServiceException;

	void join(int userId, int examinationId) throws ServiceException;

	Page<ExamVO> retrievePage(int page, int limit, ExamStatus status) throws ServiceException;

	ExamVO retrieve(int examinationId) throws ServiceException;

	ExamVO update(int examinationId, ExamDTO examDTO) throws ServiceException;

	void delete(int examinationId) throws ServiceException;
}
