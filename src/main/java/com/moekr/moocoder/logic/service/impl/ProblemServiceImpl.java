package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.ProblemDAO;
import com.moekr.moocoder.data.dao.UserDAO;
import com.moekr.moocoder.data.entity.Problem;
import com.moekr.moocoder.data.entity.User;
import com.moekr.moocoder.logic.AsyncWrapper;
import com.moekr.moocoder.logic.service.ProblemService;
import com.moekr.moocoder.logic.storage.StorageProvider;
import com.moekr.moocoder.logic.vo.ProblemVO;
import com.moekr.moocoder.util.ToolKit;
import com.moekr.moocoder.util.enums.ProblemType;
import com.moekr.moocoder.util.exceptions.*;
import com.moekr.moocoder.web.dto.ProblemDTO;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
@CommonsLog
public class ProblemServiceImpl implements ProblemService {
	private static final Sort PAGE_SORT = Sort.by(Sort.Direction.DESC, "id");

	private final UserDAO userDAO;
	private final ProblemDAO problemDAO;
	private final DockerImageBuilder builder;
	private final StorageProvider provider;
	private final AsyncWrapper asyncWrapper;

	private final ProblemFormatter formatter = new ProblemFormatter();
	private final ProblemUpdater updater = new ProblemUpdater();

	@Autowired
	public ProblemServiceImpl(UserDAO userDAO, ProblemDAO problemDAO, DockerImageBuilder builder, StorageProvider provider, AsyncWrapper asyncWrapper) {
		this.userDAO = userDAO;
		this.problemDAO = problemDAO;
		this.builder = builder;
		this.provider = provider;
		this.asyncWrapper = asyncWrapper;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProblemVO create(int userId, ProblemDTO problemDTO, byte[] content) throws ServiceException {
		User user = userDAO.findById(userId);
		Problem problem = new Problem();
		BeanUtils.copyProperties(problemDTO, problem, "publicFiles", "protectedFiles", "privateFiles");
		try {
			content = formatter.format(problem, content);
		} catch (IOException e) {
			throw new ServiceException("格式化题目文件时发生异常" + ToolKit.format(e));
		}
		problem.setCreator(user);
		problem = problemDAO.save(problem);
		try {
			provider.save(content, problem.getUniqueName() + ".zip");
		} catch (IOException e) {
			throw new ServiceException("保存题目文件时发生异常" + ToolKit.format(e));
		}
		int problemId = problem.getId();
		asyncWrapper.asyncInvoke(() -> builder.buildDockerImage(problemId), 1000);
		return new ProblemVO(problem);
	}

	@Override
	public Page<ProblemVO> retrievePage(int userId, int page, int limit, ProblemType type) {
		User user = userDAO.findById(userId);
		Pageable pageable = PageRequest.of(page, limit, PAGE_SORT);
		Page<Problem> pageResult;
		if (type == null) {
			pageResult = problemDAO.findAllByCreatorIsNullOrCreator(user, pageable);
		} else {
			pageResult = problemDAO.findAllByCreatorIsNullOrCreatorAndType(user, type, pageable);
		}
		return pageResult.map(ProblemVO::new);
	}

	@Override
	public Page<ProblemVO> retrievePage(int page, int limit, ProblemType type) {
		Pageable pageable = PageRequest.of(page, limit, PAGE_SORT);
		Page<Problem> pageResult;
		if (type == null) {
			pageResult = problemDAO.findAll(pageable);
		} else {
			pageResult = problemDAO.findAllByType(type, pageable);
		}
		return pageResult.map(ProblemVO::new);
	}

	@Override
	public ProblemVO retrieve(int userId, int problemId) throws ServiceException {
		Problem problem = problemDAO.findById(problemId);
		Asserts.notNull(problem, "所选题目不存在");
		if (problem.getCreator() != null && userId != 0 && userId != problem.getCreator().getId()) {
			throw new AccessDeniedException();
		}
		return new ProblemVO(problem);
	}

	@Override
	public void update(int userId, int problemId, String path, byte[] content) throws ServiceException {
		Problem problem = problemDAO.findById(problemId);
		Asserts.notNull(problem, "所选题目不存在");
		checkAccess(userId, problem);
		Set<String> files = new HashSet<>();
		files.addAll(problem.getPublicFiles());
		files.addAll(problem.getProtectedFiles());
		files.addAll(problem.getPrivateFiles());
		if (files.contains(path)) {
			try {
				byte[] origin = provider.fetch(problem.getUniqueName() + ".zip");
				provider.save(updater.update(origin, path, content), problem.getUniqueName() + ".zip");
				asyncWrapper.asyncInvoke(() -> builder.buildDockerImage(problem.getId()));
			} catch (IOException e) {
				throw new ServiceException("更新题目文件时发生异常" + ToolKit.format(e));
			}
		} else {
			throw new InvalidRequestException("所选题目中没有该文件");
		}
	}

	@Override
	@Transactional
	public void delete(int userId, int problemId) throws ServiceException {
		Problem problem = problemDAO.findById(problemId);
		Asserts.notNull(problem, "所选题目不存在");
		checkAccess(userId, problem);
		if (!problem.getExamSet().isEmpty()) {
			problem.setDeprecated(true);
			problemDAO.save(problem);
		} else {
			problemDAO.delete(problem);
		}
	}

	private void checkAccess(int userId, Problem problem) throws AccessDeniedException {
		if (userId != 0 && (problem.getCreator() == null || problem.getCreator().getId() != userId)) {
			throw new AccessDeniedException();
		}
	}
}
