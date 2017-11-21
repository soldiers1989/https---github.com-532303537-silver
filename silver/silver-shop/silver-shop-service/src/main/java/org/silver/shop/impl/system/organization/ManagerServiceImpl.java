package org.silver.shop.impl.system.organization;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.ManagerService;
import org.silver.shop.dao.system.organization.ManagerDao;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.MD5;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = ManagerService.class)
public class ManagerServiceImpl implements ManagerService {

	@Autowired
	private ManagerDao managerDao;

	@Override
	public List<Object> findManagerBy(String account) {
		Map<String, Object> params = new HashMap<>();
		params.put("managerName", account);
		return managerDao.findByProperty(Manager.class, params, 0, 0);
	}

	@Override
	public Map<String, Object> findAllmemberInfo() {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Object> reList = managerDao.findByProperty(Member.class, paramMap, 0, 0);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> createManager(String managerName, String loginPassword, int managerMarks,
			String reManagerName) {
		Manager managerInfo = new Manager();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();

		// 查询数据库字段名
		String property = "managerId";
		// 根据年份查询,当前年份下的id数量
		long managerIdCount = managerDao.findSerialNoCount(Manager.class, property, 0);
		// 当返回-1时,则查询数据库失败
		if (managerIdCount < 0) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		// 得出的总数上+1
		long count = managerIdCount + 1;
		String managerId = String.valueOf(count);
		// 当商户ID没有5位数时,前面补0
		while (managerId.length() < 5) {
			managerId = "0" + managerId;
		}
		MD5 md5 = new MD5();
		managerId = "ManagerId_" + managerId;
		managerInfo.setManagerId(managerId);
		managerInfo.setManagerName(managerName);
		managerInfo.setLoginPassword(md5.getMD5ofStr(loginPassword));
		// 管理员标识1-超级管理员2-运营管理员
		managerInfo.setManagerMarks(managerMarks);
		managerInfo.setCreateBy(reManagerName);
		// 删除标识:0-未删除,1-已删除
		managerInfo.setDeleteFlag(0);
		if (!managerDao.add(managerInfo)) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "创建管理员失败,服务器繁忙!");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> findAllMerchantInfo() {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Object> reList = managerDao.findByProperty(Merchant.class, paramMap, 0, 0);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> findMerchantDetail(String managerName, String merchantId) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("merchantId", merchantId);
		List<Object> reList = managerDao.findByProperty(Merchant.class, paramMap, 1, 1);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> updateManagerPassword(String managerId, String managerName, String oldLoginPassword,
			String newLoginPassword) {
		Date date = new Date();
		MD5 md5 = new MD5();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("managerId", managerId);
		List<Object> reList = managerDao.findByProperty(Manager.class, paramMap, 1, 1);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			Manager managerInfo = (Manager) reList.get(0);
			String reLoginPassword = managerInfo.getLoginPassword();
			String md5OldLoginPassword = md5.getMD5ofStr(oldLoginPassword);
			if(!"md5OldLoginPassword".equals(reLoginPassword)){
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), "旧密码错误,请重试!");
				return statusMap;
			}
			managerInfo.setLoginPassword(md5.getMD5ofStr(newLoginPassword));
			managerInfo.setUpdateDate(date);
			managerInfo.setUpdateBy(managerName);
			if(!managerDao.update(managerInfo)){
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), "修改密码错误,服务器繁忙!");
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}
}
