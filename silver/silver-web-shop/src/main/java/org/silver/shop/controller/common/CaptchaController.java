package org.silver.shop.controller.common;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/captcha")
public class CaptchaController {

	@Autowired
	private Producer captchaProducer = null;

	@RequestMapping(value = "/captcha-image", produces = "application/json; charset=utf-8")
	public ModelAndView getKaptchaImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String originHeader = request.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = request.getSession();
		response.setDateHeader("Expires", 0);
		// Set standard HTTP/1.1 no-cache headers.
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

		// Set IE extended HTTP/1.1 no-cache headers (use addHeader).
		response.addHeader("Cache-Control", "post-check=0, pre-check=0");

		// Set standard HTTP/1.0 no-cache header.
		response.setHeader("Pragma", "no-cache");

		// return a jpeg
		response.setContentType("image/jpeg");

		// create the text for the image
		String capText = captchaProducer.createText();

		// store the text in the session
		session.setAttribute(Constants.KAPTCHA_SESSION_KEY, capText);
		String code = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
		System.out.println("******************验证码是: " + code + "******************");

		// create the image with the text
		BufferedImage bi = captchaProducer.createImage(capText);
		ServletOutputStream out = response.getOutputStream();
		// NoiseProducer np = new NoiseProducer();
		// np.makeNoise(bi, 54f, 555f, 77f, 777f);
		// write the data out
		ImageIO.write(bi, "jpg", out);

		try {
			out.flush();
		} finally {
			out.close();
		}
		return null;
	}

	@RequestMapping(value = "/checkCaptcha", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String checkCaptcha(HttpServletRequest request, HttpServletResponse response, String captcha) {
		String originHeader = request.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(captcha)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("验证码不能为空!")).toString();
		}
		HttpSession session = request.getSession();
		String code = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
		if (captcha.equalsIgnoreCase(code)) {
			return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("验证码错误,请重新输入!")).toString();
	}
}
