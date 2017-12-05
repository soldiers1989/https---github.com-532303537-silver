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
import org.silver.util.FileUpLoadService;
import org.silver.util.MD5;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("managerTransaction")
public class ManagerTransaction {

	@Autowired
	private ManagerService managerService;

	@Autowired
	private FileUpLoadService fileUpLoadService;

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

	// 管理员查询所有用户信息
	public Map<String, Object> findAllmemberInfo() {
		return managerService.findAllmemberInfo();
	}

	// 创建管理员
	public Map<String, Object> createManager(String managerName, String loginPassword, int managerMarks) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String reManagerName = managerInfo.getManagerName();
		return managerService.createManager(managerName, loginPassword, managerMarks, reManagerName);
	}

	// 管理员查询所有商户信息
	public Map<String, Object> findAllMerchantInfo(HttpServletRequest req) {
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> iskey = req.getParameterNames();
		while (iskey.hasMoreElements()) {
			String key = iskey.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		return managerService.findAllMerchantInfo(datasMap);
	}

	// 管理员查询商户详情
	public Map<String, Object> findMerchantDetail(String merchantId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerName = managerInfo.getManagerName();
		return managerService.findMerchantDetail(managerName, merchantId);
	}

	// 修改管理员密码
	public Map<String, Object> updateManagerPassword(String oldLoginPassword, String newLoginPassword) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return managerService.updateManagerPassword(managerId, managerName, oldLoginPassword, newLoginPassword);
	}

	// 修改商户状态
	public Map<String, Object> editMerchantStatus(String merchantId, int status) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return managerService.editMerchantStatus(merchantId, managerId, managerName, status);
	}

	// 查询所有管理员信息
	public Map<String, Object> findAllManagerInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return managerService.findAllManagerInfo();
	}

	// 超级管理员充值运营人员密码
	public Map<String, Object> resetManagerPassword(String managerId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		// String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return managerService.resetManagerPassword(managerId, managerName);
	}

	// 管理员添加商户
	public Map<String, Object> managerAddMerchantInfo(HttpServletRequest req) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		// String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return null;
	}

	public Map<String, Object> editMerhcnatInfo(HttpServletRequest req, int length) {
		Map<String,Object> statusMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		String[] arrStr = new String[length];
		for (int i = 0; i < length; i++) {
			String value = req.getParameter(Integer.toString(i));
			arrStr[i] = value.trim();
		}
		//获取商户名称
		String merchantName = arrStr[1];
		String path = "/opt/www/img/merchant/" + merchantName + "/goods/";
		Map<String, Object> imgMap = fileUpLoadService.universalDoUpload(req, path, ".jpg", true, 800, 800, null);
		if(!"1".equals(imgMap.get(BaseCode.STATUS.toString()))){
			return imgMap;
		}
		// 获取文件上传后的文件名称
		List<Object> imglist = (List) imgMap.get(BaseCode.DATAS.getBaseCode());
		
		
		
		return managerService.editMerhcnatInfo(managerId,managerName,arrStr,imglist);
	}

	public static void main(String[] args) {
		String[] arrStr =  {"sss","aaa"};
		for(String str : arrStr){
			if(str.contains("sss")){
				System.out.println(str+"-0------<<<<<<ss's");
			}
		}
	}
}
