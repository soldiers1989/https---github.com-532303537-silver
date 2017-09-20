package org.silver.shop.service.common.base;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.FileUploadBase.InvalidContentTypeException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.util.*;
import org.silver.util.AppUtil;
import org.silver.util.CompressPic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * UEditor文件上传辅助类
 *
 */
public class Uploader {
	// 输出文件地址
	private String url = "";
	// 上传文件名
	private String fileName = "";
	// 状态
	private String state = "";
	// 文件类型
	private String type = ".jpg";
	// 原始文件名
	private String originalName = "";
	// 文件大小
	private long size = 1000;

	private HttpServletRequest request = null;
	private String title = "";

	// 保存路径
	private String savePath = "D://upload";
	// 文件允许格式
	private String[] allowFiles = { ".rar", ".doc", ".docx", ".zip", ".pdf", ".txt", ".swf", ".wmv", ".gif", ".png",
			".jpg", ".jpeg", ".bmp" };
	// 文件大小限制，单位KB
	private int maxSize = 100000;

	private HashMap<String, String> errorInfo = new HashMap<String, String>();

	@Autowired
	private CompressPic compressPic = new CompressPic();

	public Uploader(HttpServletRequest request) {
		this.request = request;
		HashMap<String, String> tmp = this.errorInfo;
		tmp.put("SUCCESS", "SUCCESS"); // 默认成功
		tmp.put("NOFILE", "未包含文件上传域");
		tmp.put("TYPE", "不允许的文件格式");
		tmp.put("SIZE", "文件大小超出限制");
		tmp.put("ENTYPE", "请求类型ENTYPE错误");
		tmp.put("REQUEST", "上传请求异常");
		tmp.put("IO", "IO异常");
		tmp.put("DIR", "目录创建失败");
		tmp.put("UNKNOWN", "未知错误");

	}

	public void upload(String path, String merchantName) throws Exception {
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				this.request.getSession().getServletContext());
		if (multipartResolver.isMultipart(this.request)) {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) this.request;
			Iterator<String> iter = multiRequest.getFileNames();
			while (iter.hasNext()) {
				MultipartFile file = multiRequest.getFile(iter.next());
				if (file != null) {
					String myFileName = file.getOriginalFilename();
				    this.size=file.getSize();
					CommonsMultipartFile cf = (CommonsMultipartFile) file;
					DiskFileItem fi = (DiskFileItem) cf.getFileItem();
					//String path = "/opt/www/img/";
					if (myFileName.trim() != "") {
						String imgName = AppUtil.generateAppKey() + "_" + System.currentTimeMillis() + this.type;
						try {
							if (compressPic.compressPic(fi.getStoreLocation(), path, imgName, 800, 800, true)) {
								this.originalName = myFileName;
								this.fileName = imgName;
								this.type = this.getFileExt(this.fileName);
								this.url =  "http://localhost:8080/UME/img/"+merchantName+"/" + this.fileName;
								this.state = "SUCCESS";
							}
						} catch (IllegalStateException e) {
							e.printStackTrace();
							this.state = e.getMessage();

						}
					}
				}
			}
		}

	}

	/**
	 * 获取文件扩展名
	 * 
	 * @return string
	 */
	private String getFileExt(String fileName) {
		return fileName.substring(fileName.lastIndexOf("."));
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public void setAllowFiles(String[] allowFiles) {
		this.allowFiles = allowFiles;
	}

	public void setMaxSize(int size) {
		this.maxSize = size;
	}

	public long getSize() {
		return this.size;
	}

	public String getUrl() {
		return this.url;
	}

	public String getFileName() {
		return this.fileName;
	}

	public String getState() {
		return this.state;
	}

	public String getTitle() {
		return this.title;
	}

	public String getType() {
		return this.type;
	}

	public String getOriginalName() {
		return this.originalName;
	}
}
