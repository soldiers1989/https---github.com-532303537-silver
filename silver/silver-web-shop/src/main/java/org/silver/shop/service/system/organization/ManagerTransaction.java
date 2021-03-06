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
import org.silver.shop.api.system.organization.MerchantService;
import org.silver.shop.model.system.organization.Manager;
import org.silver.util.FileUpLoadService;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;

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
				Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
				if (managerInfo == null) {
					WebUtil.getSession().setAttribute(LoginType.MANAGER_INFO.toString(), reList.get(0));
				}
				datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
				datasMap.put(BaseCode.MSG.getBaseCode(), "登录成功");
				return datasMap;
			}
		}
		return null;
	}

	// 管理员查询所有用户信息
	public Map<String, Object> findAllmemberInfo(HttpServletRequest req, int page, int size) {
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> iskey = req.getParameterNames();
		while (iskey.hasMoreElements()) {
			String key = iskey.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		return managerService.findAllmemberInfo(page, size, datasMap);
	}

	// 创建管理员
	public Map<String, Object> createManager(String managerName, String loginPassword, int managerMarks, String description, String realName) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String reManagerName = managerInfo.getManagerName();
		return managerService.createManager(managerName, loginPassword, managerMarks, reManagerName,description,realName);
	}

	// 管理员查询所有商户信息
	public Map<String, Object> findAllMerchantInfo(HttpServletRequest req, int page, int size) {
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> iskey = req.getParameterNames();
		while (iskey.hasMoreElements()) {
			String key = iskey.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		datasMap.remove("page");
		datasMap.remove("size");
		return managerService.findAllMerchantInfo(datasMap, page, size);
	}

	// 管理员查询商户详情
	public Map<String, Object> findMerchantDetail(String merchantId) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		return managerService.findMerchantDetail(managerName, merchantId);
	}

	// 修改管理员密码
	public Map<String, Object> updateManagerPassword(String oldLoginPassword, String newLoginPassword) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return managerService.updateManagerPassword(managerId, managerName, oldLoginPassword, newLoginPassword);
	}

	// 查询所有管理员信息
	public Map<String, Object> findAllManagerInfo() {
		return managerService.findAllManagerInfo();
	}

	// 超级管理员重置运营人员密码
	public Map<String, Object> resetManagerPassword(String managerId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		return managerService.resetManagerPassword(managerId, managerName);
	}

	// 管理员添加商户
	public Map<String, Object> managerAddMerchantInfo(HttpServletRequest req,Map<String,Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		int imgLength = Integer.parseInt(datasMap.get("imgLength")+"");
		datasMap.put("managerName", managerName);
		// 添加商户
		Map<String, Object> registerMap = merchantService.merchantRegister(datasMap);
		if (!"1".equals(registerMap.get(BaseCode.STATUS.toString()))) {
			return registerMap;
		}
		String merchantId = registerMap.get(BaseCode.DATAS.toString())+"";
		// 图片上传路径
		String path = "/opt/www/img/" + merchantId + "/";
		Map<String, Object> imgMap = fileUpLoadService.universalDoUpload(req, path, ".jpg", true, 800, 800, null);
		if (!"1".equals(imgMap.get(BaseCode.STATUS.toString()) + "")) {
			return imgMap;
		}
		List<Object> imglist = (List) imgMap.get(BaseCode.DATAS.getBaseCode());
		// 前端有上传图片
		if (!imglist.isEmpty() && imgLength > 0) {
			// 创建一个与前台图片数量一样长度的字符串数组
			String[] arrayStr = new String[imgLength];
			// 获取前台传递图片对应的下标值
			for (int i = 0; i < imgLength; i++) {
				if (StringEmptyUtils.isNotEmpty(req.getParameter("img[" + i + "]"))) {
					arrayStr[i] = req.getParameter("img[" + i + "]") + "";
				} else {
					arrayStr[i] = "";
				}
			}
			Map<String,Object> reMap = managerService.addMerchantBusinessInfo(merchantId, arrayStr, imglist);
			if(!"1".equals(reMap.get(BaseCode.STATUS.toString()))){
				return reMap;
			}
		}
		
		return registerMap;
	}

	// 修改商户信息
	public Map<String, Object> editMerhcnatInfo(HttpServletRequest req, int length) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
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
	public Map<String, Object> editMerhcnatBusinessInfo(HttpServletRequest req, int imgLength, String merchantId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		String path = "/opt/www/img/" + merchantId + "/";
		Map<String, Object> imgMap = fileUpLoadService.universalDoUpload(req, path, ".jpg", true, 800, 800, null);
		if (!"1".equals(imgMap.get(BaseCode.STATUS.toString()) + "")) {
			return imgMap;
		}
		List<Object> imglist = (List) imgMap.get(BaseCode.DATAS.getBaseCode());
		// 前端有上传图片
		if (!imglist.isEmpty() && imgLength > 0) {
			// 创建一个与前台图片数量一样长度的字符串数组
			String[] arrayStr = new String[imgLength];
			// 获取前台传递图片对应的下标值
			for (int i = 0; i < imgLength; i++) {
				if (StringEmptyUtils.isNotEmpty(req.getParameter("img[" + i + "]"))) {
					arrayStr[i] = req.getParameter("img[" + i + "]") ;
				} else {
					arrayStr[i] = "-1";
				}
			}
			return managerService.editMerhcnatBusinessInfo(managerId, managerName, imglist, arrayStr, merchantId);
		}
		return null;
	}

	// 查询用户详情
	public Map<String, Object> findMemberDetail(String memberId) {
		return managerService.findMemberDetail(memberId);
	}

	public Map<String, Object> managerEditMemberInfo(HttpServletRequest req) {
		Map<String, Object> datasMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		return managerService.managerEditMemberInfo(managerId, managerName, datasMap);
	}

	// 管理员查看商户备案信息
	public Map<String, Object> findMerchantRecordDetail(String merchantId) {
		return merchantService.getMerchantRecordInfo(merchantId);
	}

	// 管理员修改商户备案信息
	public Map<String, Object> editMerchantRecordDetail(String merchantId, String merchantRecordInfo) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return managerService.editMerchantRecordDetail(managerId, managerName, merchantId, merchantRecordInfo);
	}

	// 管理员删除商户备案信息
	public Map<String, Object> deleteMerchantRecordInfo(long id) {
		return managerService.deleteMerchantRecordInfo(id);
	}

	//
	public Map<String, Object> managerAuditMerchantInfo(String merchantPack) {
		JSONArray json = null;
		try {
			json = JSONArray.fromObject(merchantPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("参数错误!");
		}
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return managerService.managerAuditMerchantInfo(managerId,managerName,json);
	}

	//
	public Map<String, Object> resetMerchantLoginPassword(String merchantId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return managerService.resetMerchantLoginPassword(merchantId,managerId,managerName);
	}

	public Map<String, Object> getMerchantBusinessInfo(String merchantId) {
		return managerService.getMerchantBusinessInfo(merchantId);
	}

	public List<String> getManagerAuthority() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerId = managerInfo.getManagerId();
		//String managerName = managerInfo.getManagerName();
		String redisKey = "Shop_Key_Manager_Authority_List__" + managerId;
		byte[] redisByte = JedisUtil.get(redisKey.getBytes());
	//	if (redisByte != null && redisByte.length > 0) {
			//return (List<String>) SerializeUtil.toObject(redisByte);
		//} else {
			Map<String, Object> reMap = managerService.getManagerAuthority(managerId);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return null;
			}
			List<String> item = (List<String>) reMap.get(BaseCode.DATAS.toString());
			//JedisUtil.set(redisKey.getBytes(), SerializeUtil.toBytes(item), 3600);
			return item;
		//}
	}

	public Object tmpUpdateAuthority() {
		return managerService.tmpUpdateAuthority();
	}

}
