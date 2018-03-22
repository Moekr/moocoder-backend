package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.UserDAO;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.logic.api.GitlabApi;
import com.moekr.aes.logic.service.MailService;
import com.moekr.aes.logic.service.UserService;
import com.moekr.aes.logic.vo.model.UserModel;
import com.moekr.aes.util.Asserts;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.enums.Role;
import com.moekr.aes.web.dto.form.ChangePasswordForm;
import com.moekr.aes.web.dto.form.StudentRegisterForm;
import com.moekr.aes.web.dto.form.TeacherRegisterForm;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
	private final UserDAO userDAO;
	private final MailService mailService;
	private final GitlabApi gitlabApi;

	@Autowired
	public UserServiceImpl(UserDAO userDAO, MailService mailService, GitlabApi gitlabApi) {
		this.userDAO = userDAO;
		this.mailService = mailService;
		this.gitlabApi = gitlabApi;
	}

	@Override
	public UserModel findByUsername(String username) {
		User user = userDAO.findByUsername(username);
		Asserts.isTrue(user != null);
		return new UserModel(user);
	}

	@Override
	public List<UserModel> findAllByRole(Role role) {
		return userDAO.findAllByRole(role).stream().map(UserModel::new).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void register(StudentRegisterForm form) {
		User user = userDAO.findByUsername(form.getUsername());
		Asserts.isTrue(user == null, "用户名已被使用！");
		user = userDAO.findByEmail(form.getEmail());
		Asserts.isTrue(user == null, "邮箱已被使用！");
		int userId = gitlabApi.createUser(form.getUsername(), form.getEmail(), form.getPassword());
		int namespaceId = gitlabApi.fetchNamespace(form.getUsername());
		String token = gitlabApi.createToken(userId);
		user = new User();
		BeanUtils.copyProperties(form, user, "password");
		user.setId(userId);
		user.setPassword(DigestUtils.sha256Hex(form.getPassword()));
		user.setNamespace(namespaceId);
		user.setToken(token);
		user.setRole(Role.STUDENT);
		user.setCreatedAt(LocalDateTime.now());
		userDAO.save(user);
		ModelMap model = new ModelMap();
		model.addAttribute("username", form.getUsername());
		model.addAttribute("password", form.getPassword());
		model.addAttribute("role", Role.STUDENT);
		mailService.send(form.getEmail(), form.getUsername(), "注册成功", new ModelAndView("mail/register", model));
	}

	@Override
	@Transactional
	public void register(TeacherRegisterForm form) {
		User user = userDAO.findByUsername(form.getUsername());
		Asserts.isTrue(user == null, "用户名已被使用！");
		user = userDAO.findByEmail(form.getEmail());
		Asserts.isTrue(user == null, "邮箱已被使用！");
		String password = ToolKit.randomPassword();
		int userId = gitlabApi.createUser(form.getUsername(), form.getEmail(), password);
		int namespaceId = gitlabApi.fetchNamespace(form.getUsername());
		String token = gitlabApi.createToken(userId);
		user = new User();
		BeanUtils.copyProperties(form, user);
		user.setId(userId);
		user.setPassword(DigestUtils.sha256Hex(password));
		user.setNamespace(namespaceId);
		user.setToken(token);
		user.setRole(Role.TEACHER);
		user.setCreatedAt(LocalDateTime.now());
		userDAO.save(user);
		ModelMap model = new ModelMap();
		model.addAttribute("username", form.getUsername());
		model.addAttribute("password", password);
		model.addAttribute("role", Role.TEACHER);
		mailService.send(form.getEmail(), form.getUsername(), "注册成功", new ModelAndView("mail/register", model));
	}

	@Override
	@Transactional
	public void changePassword(String username, ChangePasswordForm form) {
		User user = userDAO.findByUsername(username);
		Assert.notNull(user, "找不到用户");
		Asserts.isTrue(StringUtils.equals(DigestUtils.sha256Hex(form.getOrigin()), user.getPassword()), "密码不正确！");
		gitlabApi.changePassword(user.getId(), form.getPassword());
		user.setPassword(DigestUtils.sha256Hex(form.getPassword()));
		userDAO.save(user);
	}
}
