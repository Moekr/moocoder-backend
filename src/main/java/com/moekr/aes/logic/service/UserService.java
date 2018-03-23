package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.model.UserModel;
import com.moekr.aes.util.enums.Role;
import com.moekr.aes.web.dto.form.ChangePasswordForm;
import com.moekr.aes.web.dto.form.StudentRegisterForm;
import com.moekr.aes.web.dto.form.TeacherRegisterForm;

import java.util.List;

public interface UserService {
	UserModel findByUsername(String username);

	List<UserModel> findAllByRole(Role role);

	void register(StudentRegisterForm form);

	void register(TeacherRegisterForm form);

	void changePassword(String username, ChangePasswordForm form);

	void delete(int userId);
}
