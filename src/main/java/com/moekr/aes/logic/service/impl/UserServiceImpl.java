package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.RecordDAO;
import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.dao.UserDAO;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.logic.api.GitlabApi;
import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.logic.service.MailService;
import com.moekr.aes.logic.service.UserService;
import com.moekr.aes.logic.vo.UserVO;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.Asserts;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.enums.UserRole;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.UserDTO;
import com.moekr.aes.web.dto.form.ChangePasswordForm;
import com.moekr.aes.web.dto.form.StudentRegisterForm;
import com.moekr.aes.web.dto.form.TeacherRegisterForm;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
	private final UserDAO userDAO;
	private final ResultDAO resultDAO;
	private final RecordDAO recordDAO;
	private final MailService mailService;
	private final GitlabApi gitlabApi;
	private final JenkinsApi jenkinsApi;

	@Autowired
	public UserServiceImpl(UserDAO userDAO, ResultDAO resultDAO, RecordDAO recordDAO, MailService mailService, GitlabApi gitlabApi, JenkinsApi jenkinsApi) {
		this.userDAO = userDAO;
		this.resultDAO = resultDAO;
		this.recordDAO = recordDAO;
		this.mailService = mailService;
		this.gitlabApi = gitlabApi;
		this.jenkinsApi = jenkinsApi;
	}

	@Override
	public UserVO create(UserDTO userDTO) throws ServiceException {
		User user = userDAO.findByUsername(userDTO.getUsername());
		Asserts.isNull(user, "用户名已被使用！");
		user = userDAO.findByEmail(userDTO.getEmail());
		Asserts.isNull(user, "邮箱已被使用！");
		String password = ToolKit.randomPassword();
		int userId;
		try {
			userId = gitlabApi.createUser(userDTO.getUsername(), userDTO.getEmail(), password);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建GitLab用户时发生异常[" + e.getMessage() + "]");
		}
		int namespaceId;
		try {
			namespaceId = gitlabApi.fetchNamespace(userDTO.getUsername());
		} catch (GitLabApiException e) {
			throw new ServiceException("获取GitLab用户名称空间时发生异常[" + e.getMessage() + "]");
		}
		String token;
		try {
			token = gitlabApi.createToken(userId);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建GitLab用户Token时发生异常[" + e.getMessage() + "]");
		}
		user = new User();
		BeanUtils.copyProperties(userDTO, user);
		user.setId(userId);
		user.setPassword(DigestUtils.sha256Hex(password));
		user.setNamespace(namespaceId);
		user.setToken(token);
		user.setCreatedAt(LocalDateTime.now());
		ModelMap model = new ModelMap();
		model.addAttribute("username", userDTO.getUsername());
		model.addAttribute("password", password);
		model.addAttribute("role", userDTO.getRole());
		mailService.send(userDTO.getEmail(), userDTO.getUsername(), "注册成功", new ModelAndView("mail/register", model));
		return new UserVO(userDAO.save(user));
	}

	@Override
	@Transactional
	public void register(StudentRegisterForm form) throws ServiceException {
		User user = userDAO.findByUsername(form.getUsername());
		Asserts.isNull(user, "用户名已被使用！");
		user = userDAO.findByEmail(form.getEmail());
		Asserts.isNull(user, "邮箱已被使用！");
		int userId;
		try {
			userId = gitlabApi.createUser(form.getUsername(), form.getEmail(), form.getPassword());
		} catch (GitLabApiException e) {
			throw new ServiceException("创建GitLab用户时发生异常[" + e.getMessage() + "]");
		}
		int namespaceId;
		try {
			namespaceId = gitlabApi.fetchNamespace(form.getUsername());
		} catch (GitLabApiException e) {
			throw new ServiceException("获取GitLab用户名称空间时发生异常[" + e.getMessage() + "]");
		}
		String token;
		try {
			token = gitlabApi.createToken(userId);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建GitLab用户Token时发生异常[" + e.getMessage() + "]");
		}
		user = new User();
		BeanUtils.copyProperties(form, user, "password");
		user.setId(userId);
		user.setPassword(DigestUtils.sha256Hex(form.getPassword()));
		user.setNamespace(namespaceId);
		user.setToken(token);
		user.setRole(UserRole.STUDENT);
		user.setCreatedAt(LocalDateTime.now());
		userDAO.save(user);
		ModelMap model = new ModelMap();
		model.addAttribute("username", form.getUsername());
		model.addAttribute("password", form.getPassword());
		model.addAttribute("role", UserRole.STUDENT);
		mailService.send(form.getEmail(), form.getUsername(), "注册成功", new ModelAndView("mail/register", model));
	}

	@Override
	@Transactional
	public void register(TeacherRegisterForm form) throws ServiceException {
		User user = userDAO.findByUsername(form.getUsername());
		Asserts.isNull(user, "用户名已被使用！");
		user = userDAO.findByEmail(form.getEmail());
		Asserts.isNull(user, "邮箱已被使用！");
		String password = ToolKit.randomPassword();
		int userId;
		try {
			userId = gitlabApi.createUser(form.getUsername(), form.getEmail(), password);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建GitLab用户时发生异常[" + e.getMessage() + "]");
		}
		int namespaceId;
		try {
			namespaceId = gitlabApi.fetchNamespace(form.getUsername());
		} catch (GitLabApiException e) {
			throw new ServiceException("获取GitLab用户名称空间时发生异常[" + e.getMessage() + "]");
		}
		String token;
		try {
			token = gitlabApi.createToken(userId);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建GitLab用户Token时发生异常[" + e.getMessage() + "]");
		}
		user = new User();
		BeanUtils.copyProperties(form, user);
		user.setId(userId);
		user.setPassword(DigestUtils.sha256Hex(password));
		user.setNamespace(namespaceId);
		user.setToken(token);
		user.setRole(UserRole.TEACHER);
		user.setCreatedAt(LocalDateTime.now());
		userDAO.save(user);
		ModelMap model = new ModelMap();
		model.addAttribute("username", form.getUsername());
		model.addAttribute("password", password);
		model.addAttribute("role", UserRole.TEACHER);
		mailService.send(form.getEmail(), form.getUsername(), "注册成功", new ModelAndView("mail/register", model));
	}

	@Override
	@Transactional
	public void changePassword(String username, ChangePasswordForm form) throws ServiceException {
		User user = userDAO.findByUsername(username);
		Assert.notNull(user, "找不到用户");
		if (!StringUtils.equals(DigestUtils.sha256Hex(form.getOrigin()), user.getPassword())) {
			throw new AccessDeniedException("原密码不正确！");
		}
		try {
			gitlabApi.changePassword(user.getId(), form.getPassword());
		} catch (GitLabApiException e) {
			throw new ServiceException("修改GitLab用户密码时发生异常[" + e.getMessage() + "]");
		}
		user.setPassword(DigestUtils.sha256Hex(form.getPassword()));
		userDAO.save(user);
	}

	@Override
	@Transactional
	public void delete(int userId) throws ServiceException {
		User user = userDAO.findById(userId);
		Assert.notNull(user, "找不到要删除的用户");
		Assert.isTrue(user.getRole() == UserRole.STUDENT, "目标用户只能是学生");
		for (Result result : user.getResultSet()) {
			recordDAO.deleteAll(result.getRecordSet());
			try {
				gitlabApi.deleteUser(userId);
			} catch (GitLabApiException e) {
				throw new ServiceException("删除GitLab用户时发生异常[" + e.getMessage() + "]");
			}
			if (!result.getDeleted()) {
				try {
					jenkinsApi.deleteJob(result.getId());
				} catch (IOException e) {
					throw new ServiceException("删除Jenkins项目时发生异常[" + e.getMessage() + "]");
				}
			}
			resultDAO.delete(result);
		}
		userDAO.delete(user);
	}
}
