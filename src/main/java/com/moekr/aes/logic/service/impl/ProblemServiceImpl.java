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
import org.json.JSONArray;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@CommonsLog
public class ProblemServiceImpl implements ProblemService {
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
		problem.setPublicFiles(new JSONArray(info.getPublicFiles()).toString());
		problem.setProtectedFiles(new JSONArray(info.getProtectedFiles()).toString());
		problem.setPrivateFiles(new JSONArray(info.getPrivateFiles()).toString());
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
		List<JSONArray> originFileArrayList = Arrays.asList(
				new JSONArray(problem.getPublicFiles()),
				new JSONArray(problem.getProtectedFiles()),
				new JSONArray(problem.getPublicFiles())
		);
		Set<String> originFiles = new HashSet<>();
		for (JSONArray originFileArray : originFileArrayList) {
			for (Object object : originFileArray) {
				if (object instanceof String) {
					originFiles.add((String) object);
				}
			}
		}
		List<Set<String>> newFileSetList = Arrays.asList(
				problemDTO.getPublicFiles(),
				problemDTO.getProtectedFiles(),
				problemDTO.getPrivateFiles()
		);
		Set<String> newFiles = new HashSet<>();
		for (Set<String> newFileSet : newFileSetList) {
			newFiles.addAll(newFileSet);
		}
		if (!originFiles.equals(newFiles)) {
			throw new InvalidRequestException("文件列表不匹配！");
		}
		problem.setPublicFiles(new JSONArray(problemDTO.getPublicFiles()).toString());
		problem.setProtectedFiles(new JSONArray(problemDTO.getProtectedFiles()).toString());
		problem.setPrivateFiles(new JSONArray(problemDTO.getPrivateFiles()).toString());
		return new ProblemVO(problemDAO.save(problem));
	}
}
