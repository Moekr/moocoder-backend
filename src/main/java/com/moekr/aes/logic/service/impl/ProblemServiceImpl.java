package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.ProblemDAO;
import com.moekr.aes.data.dao.UserDAO;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.logic.service.ProblemService;
import com.moekr.aes.logic.storage.StorageProvider;
import com.moekr.aes.logic.vo.model.ProblemModel;
import com.moekr.aes.util.Asserts;
import com.moekr.aes.util.ServiceException;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.enums.Language;
import com.moekr.aes.util.enums.Role;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@CommonsLog
public class ProblemServiceImpl implements ProblemService {
	private final UserDAO userDAO;
	private final ProblemDAO problemDAO;
	private final StorageProvider storageProvider;

	@Autowired
	public ProblemServiceImpl(UserDAO userDAO, ProblemDAO problemDAO, StorageProvider storageProvider) {
		this.userDAO = userDAO;
		this.problemDAO = problemDAO;
		this.storageProvider = storageProvider;
	}

	@Override
	public List<ProblemModel> findAll() {
		return problemDAO.findAll().stream()
				.map(ProblemModel::new)
				.sorted((o1, o2) -> o2.getId() - o1.getId())
				.collect(Collectors.toList());
	}

	@Override
	public List<ProblemModel> findAllUndeprecated() {
		return problemDAO.findAll().stream()
				.filter(p -> !p.getDeprecated())
				.map(ProblemModel::new)
				.sorted((o1, o2) -> o2.getId() - o1.getId())
				.collect(Collectors.toList());
	}

	@Override
	public ProblemModel findById(int problemId) {
		Problem problem = problemDAO.findById(problemId).orElse(null);
		Asserts.isTrue(problem != null, HttpStatus.SC_NOT_FOUND);
		return new ProblemModel(problem);
	}

	@Override
	@Transactional
	public void upload(int userId, byte[] content) {
		User user = userDAO.findById(userId).orElse(null);
		Assert.notNull(user, "找不到用户");
		Assert.isTrue(user.getRole() == Role.TEACHER, "没有权限");
		List<String> descriptionFile = decompressDescriptionFile(content);
		Asserts.isTrue(descriptionFile.size() >= 4, "描述文件不完整！");
		Language language = Arrays.stream(Language.values())
				.filter(l -> StringUtils.equalsIgnoreCase(l.toString(), descriptionFile.get(0)))
				.findFirst().orElse(null);
		Asserts.isTrue(language != null, "不能处理的编程语言类型！");
		String name = descriptionFile.get(1);
		StringBuilder stringBuilder = new StringBuilder();
		descriptionFile.stream().skip(3).forEach(line -> stringBuilder.append(line).append('\n'));
		String description = stringBuilder.toString();
		String file = ToolKit.randomUUID();
		try {
			storageProvider.save(content, file + ".zip");
		} catch (IOException e) {
			throw new ServiceException("保存题目文件失败！");
		}
		Problem problem = new Problem();
		problem.setName(name);
		problem.setLanguage(language);
		problem.setDescription(description);
		problem.setCreatedAt(LocalDateTime.now());
		problem.setDeprecated(false);
		problem.setFile(file);
		problem.setUser(user);
		problemDAO.save(problem);
	}

	@Override
	@Transactional
	public void deprecate(int problemId) {
		Problem problem = problemDAO.findById(problemId).orElse(null);
		Assert.notNull(problem, "找不到题目");
		problem.setDeprecated(true);
		problemDAO.save(problem);
	}

	private List<String> decompressDescriptionFile(byte[] content) {
		File file;
		try {
			file = File.createTempFile("upload", "zip");
		} catch (IOException e) {
			throw new ServiceException("创建临时文件失败！");
		}
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			outputStream.write(content);
		} catch (IOException e) {
			throw new ServiceException("写入临时文件失败！");
		}
		try (ZipFile zipFile = new ZipFile(file)) {
			ZipArchiveEntry entry = zipFile.getEntry("README.md");
			Asserts.isTrue(entry != null, "题目描述文件不存在！");
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)))) {
				List<String> lineList = new ArrayList<>();
				String line;
				while ((line = reader.readLine()) != null) {
					lineList.add(line);
				}
				return lineList;
			} catch (IOException e) {
				throw new ServiceException("读取题目描述文件失败！");
			}
		} catch (IOException e) {
			throw new ServiceException("读取题目压缩文件失败！");
		} finally {
			if (!file.delete()) {
				log.warn("删除临时文件[" + file.getAbsolutePath() + "]失败！");
			}
		}
	}

}
