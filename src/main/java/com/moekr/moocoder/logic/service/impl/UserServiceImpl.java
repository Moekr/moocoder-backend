package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.UserDAO;
import com.moekr.moocoder.data.entity.Result;
import com.moekr.moocoder.data.entity.User;
import com.moekr.moocoder.logic.api.GitlabApi;
import com.moekr.moocoder.logic.api.JenkinsApi;
import com.moekr.moocoder.logic.api.vo.GitlabUser;
import com.moekr.moocoder.logic.service.MailService;
import com.moekr.moocoder.logic.service.UserService;
import com.moekr.moocoder.logic.vo.UserVO;
import com.moekr.moocoder.util.ToolKit;
import com.moekr.moocoder.util.enums.UserRole;
import com.moekr.moocoder.util.exceptions.Asserts;
import com.moekr.moocoder.util.exceptions.InvalidRequestException;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.dto.UserDTO;
import com.moekr.moocoder.web.dto.form.ChangePasswordForm;
import com.moekr.moocoder.web.dto.form.RegisterForm;
import org.apache.commons.codec.digest.DigestUtils;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;

@Service
public class UserServiceImpl implements UserService {
	private static final Sort PAGE_SORT = Sort.by(Sort.Direction.ASC, "id");

	private final UserDAO userDAO;
	private final MailService mailService;
	private final GitlabApi gitlabApi;
	private final JenkinsApi jenkinsApi;

	@Autowired
	public UserServiceImpl(UserDAO userDAO, MailService mailService, GitlabApi gitlabApi, JenkinsApi jenkinsApi) {
		this.userDAO = userDAO;
		this.mailService = mailService;
		this.gitlabApi = gitlabApi;
		this.jenkinsApi = jenkinsApi;
	}

	@Override
	@Transactional
	public UserVO create(UserDTO userDTO) throws ServiceException {
		return register(userDTO.getUsername(), userDTO.getEmail(), null, userDTO.getRole());
	}

	@Override
	public Page<UserVO> retrievePage(int page, int limit, String search) {
		Pageable pageable = PageRequest.of(page, limit, PAGE_SORT);
		Page<User> pageResult;
		if (search == null || search.isEmpty()) {
			pageResult = userDAO.findAll(pageable);
		} else {
			pageResult = userDAO.findAllByUsernameLike("%" + search + "%", pageable);
		}
		return pageResult.map(UserVO::new);
	}

	@Override
	public UserVO retrieve(int userId) throws ServiceException {
		User user = userDAO.findById(userId);
		Asserts.notNull(user, "所选用户不存在");
		return new UserVO(user);
	}

	@Override
	@Transactional
	public void delete(int userId) throws ServiceException {
		User user = userDAO.findById(userId);
		Assert.notNull(user, "找不到要删除的用户");
		Assert.isTrue(user.getRole() == UserRole.STUDENT, "目标用户只能是学生");
		try {
			gitlabApi.deleteUser(userId);
		} catch (Exception e) {
			throw new ServiceException("删除GitLab用户时发生异常[" + e.getMessage() + "]");
		}
		for (Result result : user.getResultSet()) {
			if (!result.isDeleted()) {
				try {
					jenkinsApi.deleteJob(result.getId());
				} catch (Exception e) {
					throw new ServiceException("删除Jenkins项目时发生异常[" + e.getMessage() + "]");
				}
			}
		}
		userDAO.delete(user);
	}

	@Override
	@Transactional
	public void register(RegisterForm form) throws ServiceException {
		register(form.getUsername(), form.getEmail(), form.getPassword(), UserRole.STUDENT);
	}

	private UserVO register(String username, String email, String password, UserRole role) throws ServiceException {
		User user = userDAO.findByUsername(username);
		Asserts.isNull(user, "用户名已被使用！");
		user = userDAO.findByEmail(email);
		Asserts.isNull(user, "邮箱已被使用！");
		String realPassword = password == null ? ToolKit.randomPassword() : password;
		GitlabUser gitlabUser;
		try {
			gitlabUser = gitlabApi.createUser(username, email, realPassword);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建GitLab用户时发生异常[" + e.getMessage() + "]");
		}
		user = new User();
		BeanUtils.copyProperties(gitlabUser, user);
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(DigestUtils.sha256Hex(realPassword));
		user.setRole(role);
		ModelMap model = new ModelMap();
		model.addAttribute("username", username);
		if (password == null) {
			model.addAttribute("password", realPassword);
		}
		model.addAttribute("role", role);
		mailService.send(email, username, "注册成功 | MOOCODER", new ModelAndView("mail/register", model));
		return new UserVO(userDAO.save(user));
	}

	@Override
	public void changePassword(int userId, ChangePasswordForm form) throws ServiceException {
		User user = userDAO.findById(userId);
		if (!user.getPassword().equals(DigestUtils.sha256Hex(form.getOrigin()))) {
			throw new InvalidRequestException("原密码验证失败，如忘记密码请联系管理员重置");
		}
		changePassword(user, form.getPassword(), false);
	}

	@Override
	public void resetPassword(int userId) throws ServiceException {
		User user = userDAO.findById(userId);
		Asserts.notNull(user, "所选用户不存在");
		String password = ToolKit.randomPassword();
		changePassword(user, password, true);
	}

	private void changePassword(User user, String password, boolean reset) throws ServiceException {
		try {
			gitlabApi.changePassword(user.getId(), password);
		} catch (GitLabApiException e) {
			throw new ServiceException("修改GitLab用户密码时发生异常[" + e.getMessage() + "]");
		}
		user.setPassword(DigestUtils.sha256Hex(password));
		user = userDAO.save(user);
		ModelMap model = new ModelMap();
		if (reset) {
			model.addAttribute("password", password);
		}
		mailService.send(user.getEmail(), user.getUsername(), "密码变更提醒 | MOOCODER", new ModelAndView("mail/password", model));
	}
}
