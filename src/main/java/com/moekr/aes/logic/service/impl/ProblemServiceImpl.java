package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.ProblemDAO;
import com.moekr.aes.data.dao.UserDAO;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.logic.service.ProblemService;
import com.moekr.aes.logic.storage.StorageProvider;
import com.moekr.aes.logic.vo.ProblemVO;
import com.moekr.aes.util.exceptions.*;
import com.moekr.aes.web.dto.ProblemDTO;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
	private final StorageProvider storageProvider;

	private final ProblemFormatter formatter = new ProblemFormatter();

	@Autowired
	public ProblemServiceImpl(UserDAO userDAO, ProblemDAO problemDAO, StorageProvider storageProvider) {
		this.userDAO = userDAO;
		this.problemDAO = problemDAO;
		this.storageProvider = storageProvider;
	}

	@Override
	@Transactional
	public ProblemVO create(int userId, byte[] content) throws ServiceException {
		User user = userDAO.findById(userId);
		return create(user, content);
	}

	@Override
	public Page<ProblemVO> retrievePage(int userId, int page, int limit) {
		User user = userDAO.findById(userId);
		return problemDAO.findAllByOwner(user, PageRequest.of(page, limit, PAGE_SORT)).map(ProblemVO::new);
	}

	@Override
	public ProblemVO retrieve(int userId, int problemId) throws ServiceException {
		Problem problem = problemDAO.findById(problemId);
		Asserts.notNull(problem, "所选题目不存在");
		if (problem.getOwner().getId() != userId) {
			throw new AccessDeniedException();
		}
		return new ProblemVO(problem);
	}

	@Override
	public ProblemVO update(int userId, int problemId, ProblemDTO problemDTO) throws ServiceException {
		Problem problem = problemDAO.findById(problemId);
		Asserts.notNull(problem, "所选题目不存在");
		if (problem.getOwner().getId() != userId) {
			throw new AccessDeniedException();
		}
		return update(problem, problemDTO);
	}

	@Override
	@Transactional
	public void delete(int userId, int problemId) throws ServiceException {
		Problem problem = problemDAO.findById(problemId);
		Asserts.notNull(problem, "所选题目不存在");
		if (problem.getOwner().getId() != userId) {
			throw new AccessDeniedException();
		}
		if (!problem.getExaminationSet().isEmpty()) {
			throw new EntityNotAvailableException("题目已被使用至少一次，无法删除");
		}
		problemDAO.delete(problem);
	}

	@Override
	public ProblemVO deprecate(int userId, int problemId) throws ServiceException {
		Problem problem = problemDAO.findById(problemId);
		Asserts.notNull(problem, "所选题目不存在");
		if (problem.getOwner().getId() != userId) {
			throw new AccessDeniedException();
		}
		problem.setDeprecated(true);
		return new ProblemVO(problemDAO.save(problem));
	}

	@Override
	@Transactional
	public ProblemVO create(byte[] content) throws ServiceException {
		return create(null, content);
	}

	@Override
	public Page<ProblemVO> retrievePage(int page, int limit) {
		return problemDAO.findAll(PageRequest.of(page, limit, PAGE_SORT)).map(ProblemVO::new);
	}

	@Override
	public ProblemVO retrieve(int problemId) throws ServiceException {
		Problem problem = problemDAO.findById(problemId);
		Asserts.notNull(problem, "所选题目不存在");
		return new ProblemVO(problem);
	}

	@Override
	@Transactional
	public ProblemVO update(int problemId, ProblemDTO problemDTO) throws ServiceException {
		Problem problem = problemDAO.findById(problemId);
		Asserts.notNull(problem, "所选题目不存在");
		return update(problem, problemDTO);
	}

	@Override
	@Transactional
	public void delete(int problemId) throws ServiceException {
		Problem problem = problemDAO.findById(problemId);
		Asserts.notNull(problem, "所选题目不存在");
		if (!problem.getExaminationSet().isEmpty()) {
			throw new EntityNotAvailableException("题目已被使用至少一次，无法删除");
		}
		problemDAO.delete(problem);
	}

	@Override
	@Transactional
	public ProblemVO deprecate(int problemId) throws ServiceException {
		Problem problem = problemDAO.findById(problemId);
		Asserts.notNull(problem, "所选题目不存在");
		problem.setDeprecated(true);
		return new ProblemVO(problemDAO.save(problem));
	}

	private ProblemVO create(User user, byte[] content) throws ServiceException {
		FormattedProblemInfo info;
		try {
			info = formatter.format(content);
		} catch (IOException e) {
			throw new ServiceException("格式化题目文件时发生异常[" + e.getMessage() + "]");
		}
		Problem problem = new Problem();
		BeanUtils.copyProperties(info, problem);
		problem.setPublicFiles(info.getPublicFiles());
		problem.setProtectedFiles(info.getProtectedFiles());
		problem.setPrivateFiles(info.getPrivateFiles());
		problem.setOwner(user);
		problem = problemDAO.save(problem);
		try {
			storageProvider.save(content, problem.getId() + ".zip");
		} catch (IOException e) {
			throw new ServiceException("保存题目文件时发生异常[" + e.getMessage() + "]");
		}
		return new ProblemVO(problem);
	}

	private ProblemVO update(Problem problem, ProblemDTO problemDTO) throws ServiceException {
		Set<String> originFiles = new HashSet<>();
		originFiles.addAll(problem.getPublicFiles());
		originFiles.addAll(problem.getProtectedFiles());
		originFiles.addAll(problem.getPrivateFiles());
		Set<String> newFiles = new HashSet<>();
		newFiles.addAll(problemDTO.getPublicFiles());
		newFiles.addAll(problemDTO.getProtectedFiles());
		newFiles.addAll(problemDTO.getPrivateFiles());
		if (!originFiles.equals(newFiles)) {
			throw new InvalidRequestException("文件列表不匹配！");
		}
		problem.setPublicFiles(problemDTO.getPublicFiles());
		problem.setProtectedFiles(problemDTO.getProtectedFiles());
		problem.setPrivateFiles(problemDTO.getPrivateFiles());
		return new ProblemVO(problemDAO.save(problem));
	}
}
