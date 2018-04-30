package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.UserVO;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.UserDTO;
import com.moekr.aes.web.dto.form.StudentRegisterForm;
import org.springframework.data.domain.Page;

public interface UserService {
	UserVO create(UserDTO userDTO) throws ServiceException;

	Page<UserVO> retrievePage(int page, int limit, String search) throws ServiceException;

	UserVO retrieve(int userId) throws ServiceException;

	void delete(int userId) throws ServiceException;

	void register(StudentRegisterForm form) throws ServiceException;
}
