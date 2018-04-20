package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.ExaminationVO;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.ExaminationDTO;
import org.springframework.data.domain.Page;

public interface ExaminationService {
	ExaminationVO create(int userId, ExaminationDTO examinationDTO) throws ServiceException;

	Page<ExaminationVO> retrievePage(int userId, int page, int limit) throws ServiceException;

	ExaminationVO update(int userId, int examinationId, ExaminationDTO examinationDTO) throws ServiceException;

	void delete(int userId, int examinationId) throws ServiceException;

	void delete(int examinationId) throws ServiceException;

	void participate(int userId, int examinationId) throws ServiceException;

	Page<ExaminationVO> retrievePage(int page, int limit) throws ServiceException;
}
