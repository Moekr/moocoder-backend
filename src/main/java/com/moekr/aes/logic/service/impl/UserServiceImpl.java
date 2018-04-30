package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.UserDAO;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.logic.api.GitlabApi;
import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.logic.api.vo.GitlabUser;
import com.moekr.aes.logic.service.MailService;
import com.moekr.aes.logic.service.UserService;
import com.moekr.aes.logic.vo.UserVO;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.enums.UserRole;
import com.moekr.aes.util.exceptions.Asserts;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.UserDTO;
import com.moekr.aes.web.dto.form.StudentRegisterForm;
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
	public UserVO create(UserDTO userDTO) throws ServiceException {
		User user = userDAO.findByUsername(userDTO.getUsername());
		Asserts.isNull(user, "用户名已被使用！");
		user = userDAO.findByEmail(userDTO.getEmail());
		Asserts.isNull(user, "邮箱已被使用！");
		String password = ToolKit.randomPassword();
		GitlabUser gitlabUser;
		try {
			gitlabUser = gitlabApi.createUser(userDTO.getUsername(), userDTO.getEmail(), password);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建GitLab用户时发生异常[" + e.getMessage() + "]");
		}
		user = new User();
		BeanUtils.copyProperties(gitlabUser, user);
		BeanUtils.copyProperties(userDTO, user);
		user.setPassword(DigestUtils.sha256Hex(password));
		ModelMap model = new ModelMap();
		model.addAttribute("username", userDTO.getUsername());
		model.addAttribute("password", password);
		model.addAttribute("role", userDTO.getRole());
		mailService.send(userDTO.getEmail(), userDTO.getUsername(), "注册成功", new ModelAndView("mail/register", model));
		return new UserVO(userDAO.save(user));
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
	public void register(StudentRegisterForm form) throws ServiceException {
		User user = userDAO.findByUsername(form.getUsername());
		Asserts.isNull(user, "用户名已被使用！");
		user = userDAO.findByEmail(form.getEmail());
		Asserts.isNull(user, "邮箱已被使用！");
		GitlabUser gitlabUser;
		try {
			gitlabUser = gitlabApi.createUser(form.getUsername(), form.getEmail(), form.getPassword());
		} catch (GitLabApiException e) {
			throw new ServiceException("创建GitLab用户时发生异常[" + e.getMessage() + "]");
		}
		user = new User();
		BeanUtils.copyProperties(gitlabUser, user);
		BeanUtils.copyProperties(form, user);
		user.setPassword(DigestUtils.sha256Hex(form.getPassword()));
		user.setRole(UserRole.STUDENT);
		userDAO.save(user);
		ModelMap model = new ModelMap();
		model.addAttribute("username", form.getUsername());
		model.addAttribute("password", form.getPassword());
		model.addAttribute("role", UserRole.STUDENT);
		mailService.send(form.getEmail(), form.getUsername(), "注册成功", new ModelAndView("mail/register", model));
	}
}
