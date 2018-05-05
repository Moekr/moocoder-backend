package com.moekr.moocoder.logic.service;

import com.moekr.moocoder.logic.vo.UserVO;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.dto.UserDTO;
import com.moekr.moocoder.web.dto.form.RegisterForm;
import org.springframework.data.domain.Page;

public interface UserService {
	UserVO create(UserDTO userDTO) throws ServiceException;

	Page<UserVO> retrievePage(int page, int limit, String search) throws ServiceException;

	UserVO retrieve(int userId) throws ServiceException;

	void delete(int userId) throws ServiceException;

	void register(RegisterForm form) throws ServiceException;
}
