package org.silver.shop.service.system;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.AuthorityService;
import org.silver.shop.model.system.organization.Manager;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class AuthorityTransaction {

	@Reference
	private AuthorityService authorityService;

	public Map<String, Object> addAuthorityInfo(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取管理员登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		datasMap.put("managerId", managerId);
		datasMap.put("managerName", managerName);
		return authorityService.addAuthorityInfo(datasMap);
	}

	//
	public Map<String, Object> getAuthorityInfo() {
		return authorityService.getAuthorityInfo();
	}

	//
	public Map<String, Object> getUserAuthorityInfo(String userId, String groupName) {
		return authorityService.getUserAuthorityInfo(userId,groupName);
	}

	//
	public Map<String, Object> setAuthorityInfo(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取管理员登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		datasMap.put("managerName", managerName);
		return authorityService.setAuthorityInfo(datasMap);
	}

	//修改权限字典信息
	public Map<String,Object> editAuthorityInfo(Map<String, Object> datasMap) {
		return authorityService.editAuthorityInfo(datasMap);
	}

	public Map<String,Object> deleteAuthorityInfo(String authorityId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取管理员登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		return authorityService.deleteAuthorityInfo(authorityId,managerName);
	}
}
