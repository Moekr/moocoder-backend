package com.moekr.aes.web.controller.view.teacher;

import com.moekr.aes.logic.service.*;
import com.moekr.aes.logic.vo.model.ProblemModel;
import com.moekr.aes.logic.vo.model.UserModel;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.ServiceException;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.web.dto.form.ChangeExaminationForm;
import com.moekr.aes.web.dto.form.CreateExaminationForm;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/t/examination")
public class TeacherExaminationController {
	private final UserService userService;
	private final ProblemService problemService;
	private final ExaminationService examinationService;
	private final ResultService resultService;
	private final RecordService recordService;
	private final AesProperties properties;

	public TeacherExaminationController(UserService userService, ProblemService problemService, ExaminationService examinationService,
										ResultService resultService, RecordService recordService, AesProperties properties) {
		this.userService = userService;
		this.problemService = problemService;
		this.examinationService = examinationService;
		this.resultService = resultService;
		this.recordService = recordService;
		this.properties = properties;
	}

	@GetMapping({"/", "/index.html"})
	public String index(Model model) {
		UserModel user = userService.findByUsername(ToolKit.currentUserDetails().getUsername());
		model.addAttribute("user", user);
		model.addAttribute("examinationList", examinationService.findAll(user.getId()));
		return "teacher/examination/index";
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@GetMapping("/detail.html")
	public String detail(@RequestParam("e") Optional<Integer> examinationId, Model model) {
		if (!examinationId.isPresent()) return "redirect:/t/examination/";
		UserModel user = userService.findByUsername(ToolKit.currentUserDetails().getUsername());
		model.addAttribute("user", user);
		model.addAttribute("examination", examinationService.findById(user.getId(), examinationId.get()));
		model.addAttribute("result", resultService.findByExamination(user.getId(), examinationId.get()));
		model.addAttribute("host", properties.getGitlab().getProxy());
		model.addAttribute("scoreDistribution", resultService.scoreDistribution(examinationId.get()));
		model.addAttribute("scoreData", resultService.scoreData(examinationId.get()));
		model.addAttribute("failData", recordService.failData(examinationId.get()));
		return "teacher/examination/detail";
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@PostMapping("/detail.html")
	public String detail(@ModelAttribute @Valid ChangeExaminationForm form, Errors errors, @RequestParam("e") Optional<Integer> examinationId, Model model) {
		if (!examinationId.isPresent()) return "redirect:/t/examination/";
		UserModel user = userService.findByUsername(ToolKit.currentUserDetails().getUsername());
		if (errors.hasFieldErrors()) {
			model.addAttribute("error", errors.getFieldError().getDefaultMessage());
		} else {
			try {
				examinationService.change(user.getId(), examinationId.get(), form);
				model.addAttribute("success", "操作成功！");
			} catch (ServiceException e) {
				model.addAttribute("error", e.getMessage());
			}
		}
		model.addAttribute("user", user);
		model.addAttribute("examination", examinationService.findById(user.getId(), examinationId.get()));
		model.addAttribute("result", resultService.findByExamination(user.getId(), examinationId.get()));
		model.addAttribute("host", properties.getGitlab().getProxy());
		model.addAttribute("scoreDistribution", resultService.scoreDistribution(examinationId.get()));
		model.addAttribute("scoreData", resultService.scoreData(examinationId.get()));
		model.addAttribute("failData", recordService.failData(examinationId.get()));
		return "teacher/examination/detail";
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@GetMapping(value = "/result.html")
	public void result(@RequestParam("e") Optional<Integer> examinationId, HttpServletResponse response) throws IOException {
		if (!examinationId.isPresent()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		UserModel user = userService.findByUsername(ToolKit.currentUserDetails().getUsername());
		if (!examinationService.canAccess(user.getId(), examinationId.get())) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		Map<String, Integer> scoreData = resultService.scoreData(examinationId.get());
		StringBuilder stringBuilder = new StringBuilder("用户名,成绩\n");
		scoreData.forEach((key, value) -> stringBuilder.append(key).append(",").append(value).append("\n"));
		response.setContentType("text/comma-separated-values");
		response.setCharacterEncoding("UTF-8");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=Result-" + examinationId.get() + ".csv");
		OutputStream outputStream = response.getOutputStream();
		// 添加UTF-8 BOM文件头，解决Excel打开不带BOM的UTF-8编码CSV时中文乱码的问题
		outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
		outputStream.write(stringBuilder.toString().getBytes(Charset.forName("UTF-8")));
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@GetMapping("/create.html")
	public String create(@RequestParam("p") Optional<Integer> problemId, Model model) {
		if (!problemId.isPresent()) return "redirect:/t/examination/";
		model.addAttribute("user", userService.findByUsername(ToolKit.currentUserDetails().getUsername()));
		ProblemModel problem = problemService.findById(problemId.get());
		model.addAttribute("problem", problem);
		if (problem.getDeprecated()) {
			model.addAttribute("warning", "注意：当前选择的题目已被标记为弃用");
		}
		return "teacher/examination/create";
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@PostMapping("/create.html")
	public String create(@ModelAttribute @Valid CreateExaminationForm form, Errors errors, @RequestParam("p") Optional<Integer> problemId, Model model) {
		if (!problemId.isPresent()) return "redirect:/t/examination/";
		UserModel user = userService.findByUsername(ToolKit.currentUserDetails().getUsername());
		model.addAttribute("user", user);
		if (errors.hasFieldErrors()) {
			model.addAttribute("error", errors.getFieldError().getDefaultMessage());
			model.addAttribute("problem", problemService.findById(problemId.get()));
		} else {
			try {
				examinationService.create(user.getId(), problemId.get(), form);
				model.addAttribute("success", "考试创建成功，页面将在5秒后跳转！");
			} catch (ServiceException e) {
				model.addAttribute("error", e.getMessage());
				model.addAttribute("problem", problemService.findById(problemId.get()));
			}
		}
		return "teacher/examination/create";
	}
}
