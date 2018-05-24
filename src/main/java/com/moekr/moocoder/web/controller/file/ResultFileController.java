package com.moekr.moocoder.web.controller.file;

import com.moekr.moocoder.logic.service.ResultService;
import com.moekr.moocoder.logic.vo.ResultVO;
import com.moekr.moocoder.util.editors.DefaultNumberEditor;
import com.moekr.moocoder.util.exceptions.AccessDeniedException;
import com.moekr.moocoder.util.exceptions.EntityNotFoundException;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.security.impl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.moekr.moocoder.web.security.WebSecurityConstants.ADMIN_ROLE;
import static com.moekr.moocoder.web.security.WebSecurityConstants.TEACHER_ROLE;

@Controller
@RequestMapping("/file/result")
public class ResultFileController {
	private ResultService resultService;

	@Autowired
	public ResultFileController(ResultService resultService) {
		this.resultService = resultService;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(int.class, new DefaultNumberEditor(-1));
	}

	@GetMapping("/{examId:\\d+}")
	@RolesAllowed({TEACHER_ROLE, ADMIN_ROLE})
	public void score(@AuthenticationPrincipal CustomUserDetails userDetails,
					  @PathVariable int examId,
					  HttpServletResponse response) throws IOException {
		Map<String, Integer> scoreMap = new HashMap<>();
		try {
			List<ResultVO> resultList;
			if (userDetails.isAdmin()) {
				resultList = resultService.retrieveByExam(examId);
			} else {
				resultList = resultService.retrieveByExam(userDetails.getId(), examId);
			}
			for (ResultVO result : resultList) {
				scoreMap.put(result.getUsername(), result.getScore());
			}
		} catch (AccessDeniedException e) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		} catch (EntityNotFoundException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} catch (ServiceException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		StringBuilder stringBuilder = new StringBuilder("用户名,成绩\n");
		scoreMap.forEach((key, value) -> stringBuilder.append(key).append(",").append(value).append("\n"));
		response.setContentType("text/comma-separated-values");
		response.setCharacterEncoding("UTF-8");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=Score-" + examId + ".csv");
		OutputStream outputStream = response.getOutputStream();
		// 添加UTF-8 BOM文件头，解决Excel打开不带BOM的UTF-8编码CSV时中文乱码的问题
		outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
		outputStream.write(stringBuilder.toString().getBytes(Charset.forName("UTF-8")));
	}
}
