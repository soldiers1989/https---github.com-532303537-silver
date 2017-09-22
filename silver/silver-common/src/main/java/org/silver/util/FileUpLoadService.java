package org.silver.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;



@Component("fileUpLoadService")
public class FileUpLoadService {
	
	@Resource
	private CompressPic compressPic;

	
	public Map<String, Object> groupUpload(HttpServletRequest req, HttpServletResponse resp) {
		return universalDoUpload(req, "WEB-INF/res/upload/", ".jpg", true, 400, 400, null);
	}

/**
 * 
 * @param req
 * @param storePath  存储的路径
 * @param fType 存储的格式 后缀名  (如  .jpg ，.png)  
 * @param compress  是否进行压缩
 * @param width 压缩比例宽
 * @param height 压缩比例高
 * @param sign  不为null则作为图片名标识
 * @return  
 */
	public Map<String, Object> universalDoUpload(HttpServletRequest req,String storePath,String fType,boolean compress,int width,int height,String sign){
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(req.getSession().getServletContext());
		Map<String, Object> reqMap = new HashMap<String, Object>();
		if (multipartResolver.isMultipart(req)) {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) req;
			Iterator<String> iter = multiRequest.getFileNames();
			String imgName="";
			List<String> strl = new ArrayList<String>();
			while (iter.hasNext()) {
				MultipartFile file = multiRequest.getFile(iter.next());
				if (file != null) {
					String myFileName = file.getOriginalFilename();
					CommonsMultipartFile cf = (CommonsMultipartFile) file;
					DiskFileItem fi = (DiskFileItem) cf.getFileItem();
					if (myFileName.trim() != "") {
						if(sign!=null&&!"".equals(sign)){
							Random r = new Random();
							imgName=sign + (r.nextInt(100) + "")+fType;
						}else{
							imgName = AppUtil.generateAppKey()+"_"+System.currentTimeMillis() + fType;
						}
						//System.out.println(req.getSession().getServletContext().getRealPath("/"));
						String path= /*req.getSession().getServletContext().getRealPath("/") + */storePath;
						if(compress){
							try {
								if (compressPic.compressPic(fi.getStoreLocation(),path,imgName, width, height, true)) {
									strl.add(imgName);
								}else{
									strl.add("");
								}
							} catch (IllegalStateException e) {
								e.printStackTrace();
								reqMap.put("status", -2);
								reqMap.put("err", "CompressPic invalid");
								return reqMap;
							}
						}else{
							File localFile = new File(path+imgName);
							try {
								if (!localFile.exists()){
									localFile.mkdirs(); 
								}
								file.transferTo(localFile);
								strl.add(imgName);
							} catch (IllegalStateException e) {
								e.printStackTrace();
								reqMap.put("status", -2);
								reqMap.put("err", "CompressPic invalid");
								return reqMap;
							} catch (IOException e) {
								e.printStackTrace();
								reqMap.put("status", -2);
								reqMap.put("err", "IOException : "+e.getMessage() );
								return reqMap;
							}
						}
					}
				}
			}
			reqMap.put("status", 1);
			reqMap.put("datas", strl);
			return reqMap;
		}
		reqMap.put("status", -3);
		reqMap.put("err", "Invalid params");
		return reqMap;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
