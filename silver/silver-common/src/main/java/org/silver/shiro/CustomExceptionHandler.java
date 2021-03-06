package org.silver.shiro;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.AuthorizationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

public class CustomExceptionHandler implements HandlerExceptionResolver {

	@Override
	public ModelAndView resolveException(HttpServletRequest arg0, HttpServletResponse response, Object arg2,
			Exception ex) {
		ex.printStackTrace();
		ModelAndView mv = new ModelAndView();
		response.setStatus(HttpStatus.OK.value()); // 设置状态码
		response.setContentType(MediaType.APPLICATION_JSON_VALUE); // 设置ContentType
		response.setCharacterEncoding("UTF-8"); // 避免乱码
		String originHeader = arg0.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (ex instanceof AuthorizationException) {
			try {
				response.getWriter().write("{\"success\":false,\"msg\":\"" + "没有权限操作" + "\"}");
			} catch (IOException e) {
			}
		} else {
			try {
				response.getWriter().write("{\"success\":false,\"msg\":\"" + "请求参数出错" + "\"}");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return mv;
	}

}
