package org.silver.shop.controller.common.base;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silver.shop.service.common.base.Uploader;
import org.silver.util.DateUtil;
import org.silver.util.FileUpLoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/upload")
public class UploadController {
	private static final String FILEPATH = "E:/STSworkspace/apache-tomcat-7.0.57/webapps/UME/img/";
	@Autowired
	private FileUpLoadService fileUpLoadService;

	@RequestMapping(value = "/img")
	@ResponseBody
	public String pushMsg(HttpServletRequest req, HttpServletResponse resp) {

		Map<String, Object> reqMap = fileUpLoadService.universalDoUpload(req, "/opt/www/img/", ".jpg", true, 800, 800,
				null);
		return JSONObject.fromObject(reqMap).toString();

	}

	@RequestMapping(value = "/ueImg", method = RequestMethod.POST)
	@ResponseBody
	public void pushMsg2(HttpServletRequest req, HttpServletResponse resp) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Date date = new Date();
		String fileDate =  DateUtil.formatDate(date, "yyyyMM");
		String path = FILEPATH + "goodsContent/" + fileDate + "/";
		try {
			req.setCharacterEncoding("utf-8");
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		resp.setCharacterEncoding("utf-8");
		Uploader up = new Uploader(req);
		up.setSavePath("upload");
		String[] fileType = { ".gif", ".png", ".jpg", ".jpeg", ".bmp" };
		up.setAllowFiles(fileType);
		up.setMaxSize(100000); // 单位KB
		try {
			up.upload(path, fileDate);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		String callback = req.getParameter("callback");
		String result = "{\"name\":\"" + up.getFileName() + "\", \"originalName\": \"" + up.getOriginalName()
				+ "\", \"size\": " + up.getSize() + ", \"state\": \"" + up.getState() + "\", \"type\": \""
				+ up.getType() + "\", \"url\": \"" + up.getUrl() + "\"}";
		result = result.replaceAll("\\\\", "\\\\");
		if (callback == null) {
			try {
				resp.getWriter().print(result);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			try {
				resp.getWriter().print("<script>" + callback + "(" + result + ")</script>");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
