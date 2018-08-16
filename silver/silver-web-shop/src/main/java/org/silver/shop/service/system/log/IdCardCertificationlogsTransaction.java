package org.silver.shop.service.system.log;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.log.IdCardCertificationlogsService;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class IdCardCertificationlogsTransaction {

	@Reference
	private IdCardCertificationlogsService idCardCertificationService;
	
	public Object getlogsInfo(Map<String, Object> datasMap, int page, int size) {
		return idCardCertificationService.getlogsInfo(datasMap,page,size);
	}

	public Object merchantGetInfo(Map<String, Object> datasMap, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		datasMap.put("merchantId", merchantId);
		return idCardCertificationService.getlogsInfo(datasMap,page,size);
	}

	public Object tempUpdate() {
		return idCardCertificationService.tempUpdate();
	}

	public static void main(String[] args) {
		//由数字和字母组成，并且要同时含有数字和字母，且长度要在8-16位之间
		String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,16}$";		

		String value = "aaa";  // 长度不够
		System.out.println(value.matches(regex));

		value = "1111aaaa1111aaaaa";  // 太长
		System.out.println(value.matches(regex));

		value = "111111111"; // 纯数字
		System.out.println(value.matches(regex));

		value = "aaaaaaaaa"; // 纯字母
		System.out.println(value.matches(regex));

		value = "####@@@@#"; // 特殊字符
		System.out.println("--特殊字符->"+value.matches(regex));
		value = "x_1"; // 特殊字符
		System.out.println("--特殊字符222->"+value.matches(regex));
		value = "1111aaaa";  // 数字字母组合
		System.out.println(value.matches(regex));

		value = "aaaa1111"; // 数字字母组合
		System.out.println(value.matches(regex));

		value = "aa1111aa";	// 数字字母组合
		System.out.println(value.matches(regex));

		value = "11aaaa11";	// 数字字母组合
		System.out.println(value.matches(regex));

		value = "aa11aa11"; // 数字字母组合
		System.out.println(value.matches(regex));
	}
}
