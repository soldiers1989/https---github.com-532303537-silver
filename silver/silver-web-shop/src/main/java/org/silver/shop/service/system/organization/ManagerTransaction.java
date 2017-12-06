package org.silver.shop.service.system.organization;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.ManagerService;
import org.silver.shop.api.system.organization.MerchantService;
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
	private MerchantService merchantService;
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

	// 查询所有管理员信息
	public Map<String, Object> findAllManagerInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return managerService.findAllManagerInfo();
	}

	// 超级管理员重置运营人员密码
	public Map<String, Object> resetManagerPassword(String managerId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerName = managerInfo.getManagerName();
		return managerService.resetManagerPassword(managerId, managerName);
	}

	// 管理员添加商户
	public Map<String, Object> managerAddMerchantInfo(String merchantName, String loginPassword, String merchantIdCard,
			String merchantIdCardName, String recordInfoPack, String type,int length,HttpServletRequest req) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerName = managerInfo.getManagerName();
		// 获取商户ID
		Map<String, Object> reIdMap = merchantService.findOriginalMerchantId();
		String status = reIdMap.get(BaseCode.STATUS.getBaseCode()) + "";
		if (!"1".equals(status)) {
			return reIdMap;
		}
		String merchantId = reIdMap.get(BaseCode.DATAS.getBaseCode()) + "";
		//添加商户
		Map<String,Object> registerMap=	merchantService.merchantRegister(merchantId, merchantName, loginPassword, merchantIdCard,
				merchantIdCardName, recordInfoPack, type, managerName);
		if(!"1".equals(registerMap.get(BaseCode.STATUS.toString()))){
			return registerMap;
		}
		String path = "/opt/www/img/" + merchantName + "/";
		Map<String, Object> imgMap = fileUpLoadService.universalDoUpload(req, path, ".jpg", true, 800, 800, null);
		if (!"1".equals(imgMap.get(BaseCode.STATUS.toString()))) {
			return imgMap;
		}
		List<Object> imglist = (List) imgMap.get(BaseCode.DATAS.getBaseCode());
		//前端有上传图片
		if(!imglist.isEmpty()){
			// 创建一个与前台图片数量一样长度的数组
			int[] arrayInt = new int[length];
			for (int i = 0; i < length; i++) {
				arrayInt[i] = Integer.parseInt(req.getParameter("img[" + i + "]") + "");
			}
			return managerService.addMerchantBusinessInfo(merchantId,arrayInt,imglist);
		}
		return registerMap;
	}

	// 修改商户信息
	public Map<String, Object> editMerhcnatInfo(HttpServletRequest req, int length) {
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
		return managerService.editMerhcnatInfo(managerId, managerName, arrStr);
	}

	// 修改商户业务(图片)信息
	public Map<String, Object> editMerhcnatBusinessInfo(HttpServletRequest req, int length, String merchantId,
			String merchantName) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		String path = "/opt/www/img/" + merchantName + "/";
		Map<String, Object> imgMap = fileUpLoadService.universalDoUpload(req, path, ".jpg", true, 800, 800, null);
		if (!"1".equals(imgMap.get(BaseCode.STATUS.toString()))) {
			return imgMap;
		}
		List<Object> imglist = (List) imgMap.get(BaseCode.DATAS.getBaseCode());
		// 创建一个与前台图片数量一样长度的数组
		int[] arrayInt = new int[length];
		for (int i = 0; i < length; i++) {
			arrayInt[i] = Integer.parseInt(req.getParameter("img[" + i + "]") + "");
		}
		return managerService.editMerhcnatBusinessInfo(managerId, managerName, imglist, arrayInt, merchantId);
	}
}
