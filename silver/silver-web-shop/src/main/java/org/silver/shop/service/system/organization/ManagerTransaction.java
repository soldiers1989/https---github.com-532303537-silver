package org.silver.shop.service.system.organization;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.ManagerService;
import org.silver.shop.model.system.organization.Manager;
import org.silver.util.MD5;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;


@Service("managerTransaction")
public class ManagerTransaction {

	@Autowired
	private ManagerService managerService;

	public Map<String, Object> managerLogin(String account, String loginPassword) {
		MD5 md5 = new MD5();
		Map<String, Object> datasMap = new HashMap<>();
		List<Object> reList = managerService.findManagerBy(account);
		if (reList != null && !reList.isEmpty()) {
			Manager manager = (Manager) reList.get(0);
			String name = manager.getManagerName();
			String loginpas = manager.getLoginPassword();
			String md5Pas = md5.getMD5ofStr(loginPassword);
			// 判断查询出的账号密码与前台登录的账号密码是否一致
			if (account.equals(name) && md5Pas.equals(loginpas)) {
				Subject currentUser = SecurityUtils.getSubject();
				// 获取商户登录时,shiro存入在session中的数据
				Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
				if (managerInfo == null) {
					WebUtil.getSession().setAttribute(LoginType.MANAGERINFO.toString(), reList.get(0));
				}
				datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
				datasMap.put(BaseCode.MSG.getBaseCode(), "登录成功");
				return datasMap;
			}
		} 
		return null;
	}

	//管理员查询所有用户信息
	public Map<String,Object> findAllmemberInfo() {
		return managerService.findAllmemberInfo();
	}

	//创建管理员
	public Map<String, Object> createManager(String managerName, String loginPassword, int managerMarks) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String reManagerName = managerInfo.getManagerName();
		return managerService.createManager(managerName,loginPassword,managerMarks,reManagerName);
	}

	//管理员查询所有商户信息
	public Map<String, Object> findAllMerchantInfo() {		
		return managerService.findAllMerchantInfo();
	}

	//管理员查询商户详情
	public Map<String, Object> findMerchantDetail(String merchantId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerName = managerInfo.getManagerName();
		return managerService.findMerchantDetail(managerName,merchantId);
	}

	//
	public Map<String, Object> updateManagerPassword(String oldLoginPassword,String newLoginPassword) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return managerService.updateManagerPassword(managerId ,managerName,oldLoginPassword,newLoginPassword);
	}

	public Map<String, Object> editMerchantStatus(String merchantId,int status) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return managerService.editMerchantStatus(merchantId,managerId,managerName,status);
	}



}
