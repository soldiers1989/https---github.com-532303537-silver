package org.silver.shop.service.system.commerce;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.shop.api.system.commerce.MerchantCounterService;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.FileUpLoadService;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class MerchantCounterTransaction {

	@Reference
	private MerchantCounterService merchantCounterService;
	@Autowired
	private FileUpLoadService fileUpLoadService;
	
	//
	public Map<String,Object> getInfo(Map<String, Object> datasMap, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		datasMap.put("merchantId", merchantInfo.getMerchantId());
		return merchantCounterService.getInfo(datasMap,page,size);
	}

	//
	public Object getGoodsInfo(Map<String, Object> datasMap, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		return merchantCounterService.getGoodsInfo(merchantInfo.getMerchantId(),datasMap,page,size);
	}

	//添加专柜信息
	public Map<String,Object> addCounterInfo(HttpServletRequest req, Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		//路径
	//	String path = "E:/STSworkspace/apache-tomcat-7.0.57/webapps/UME/img/" + merchantInfo.getMerchantId() + "/";
		String path = "/opt/www/img/counter/" + merchantInfo.getMerchantId() + "/";
		Map<String, Object> imgMap = fileUpLoadService.universalDoUpload(req, path, ".jpg", false, 800, 800, null);
		if (!"1".equals(imgMap.get(BaseCode.STATUS.toString()) + "")) {
			return imgMap;
		}
		// 获取文件上传后的文件名称
		List<Object> imglist = (List) imgMap.get(BaseCode.DATAS.getBaseCode());
		
		return merchantCounterService.addCounterInfo(merchantInfo,datasMap,imglist);
	}

	//商户专柜添加商品信息
	public Map<String,Object> addGoodsInfo(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		return merchantCounterService.addGoodsInfo(merchantInfo,datasMap);
	}

	public Map<String,Object> counterInfo(String counterId) {
		return merchantCounterService.counterInfo(counterId);
	}

	//查询专柜商品信息
	public Map<String,Object> counterGoods(Map<String, Object> datasMap, int page, int size) {
		return merchantCounterService.getGoodsInfo("",datasMap, page,size);
	}

	public Map<String,Object> updatePopularizeFlag(Map<String, Object> datasMap) {
		return merchantCounterService.updatePopularizeFlag(datasMap);
	}

	//
	public Map<String,Object> getEnteringTheCabinetGoods(Map<String, Object> datasMap, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		return merchantCounterService.getEnteringTheCabinetGoods(merchantInfo.getMerchantId(),datasMap,page,size);
	}
	
}
