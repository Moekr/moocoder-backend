package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.UserVO;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.UserDTO;
import com.moekr.aes.web.dto.form.ChangePasswordForm;
import com.moekr.aes.web.dto.form.StudentRegisterForm;
import com.moekr.aes.web.dto.form.TeacherRegisterForm;

public interface UserService {
	UserVO create(UserDTO userDTO) throws ServiceException;

	void register(StudentRegisterForm form) throws ServiceException;

	void register(TeacherRegisterForm form) throws ServiceException;

	void changePassword(String username, ChangePasswordForm form) throws ServiceException;

	void delete(int userId) throws ServiceException;
}
