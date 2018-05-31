package org.silver.shop.impl.system;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.engine.query.ReturnMetadata;
import org.hibernate.loader.custom.Return;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.AuthorityService;
import org.silver.shop.dao.system.AuthorityDao;
import org.silver.shop.model.system.Authority;
import org.silver.shop.model.system.AuthorityGroup;
import org.silver.shop.model.system.AuthorityUser;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.util.MerchantUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Row;
import com.justep.baas.data.Table;

import net.sf.json.JSONArray;

@Service(interfaceClass = AuthorityService.class)
public class AuthorityServiceImpl implements AuthorityService {

	@Autowired
	private AuthorityDao authorityDao;
	@Autowired
	private MerchantUtils merchantUtils;

	@Override
	public Map<String, Object> addAuthorityInfo(Map<String, Object> datasMap) {
		if (datasMap != null && !datasMap.isEmpty()) {
			String type = datasMap.get("type") + "";
			switch (type) {
			case "1":
				return addNotFirstAuthority(datasMap);
			case "2":
				return addAlreadyFirstAuthority(datasMap);
			default:
				break;
			}
		}
		return ReturnInfoUtils.errorInfo("请求参数不能为空!");
	}

	/**
	 * 当已存在第一级权限时进行权限信息的添加
	 * 
	 * @param datasMap
	 *            参数
	 * @return Map
	 */
	private Map<String, Object> addAlreadyFirstAuthority(Map<String, Object> datasMap) {
		Map<String, Object> reCheckMap = checkAlreadyFirstAuthority(datasMap);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		Map<String, Object> reAddMap = addFirstAuthority(datasMap);
		if (!"1".equals(reAddMap.get(BaseCode.STATUS.toString()))) {
			return reAddMap;
		}
		Authority authority = (Authority) reAddMap.get(BaseCode.DATAS.toString());
		return addAuthorityGroup(datasMap.get("groupName") + "", authority.getId());
	}

	/**
	 * 当没有第一级权限信息时,进行权限信息添加
	 * 
	 * @param datasMap
	 *            参数
	 * @return Map
	 */
	private Map<String, Object> addNotFirstAuthority(Map<String, Object> datasMap) {
		Map<String, Object> reAddMap = addFirstAuthority(datasMap);
		if (!"1".equals(reAddMap.get(BaseCode.STATUS.toString()))) {
			return reAddMap;
		}
		Authority authority = (Authority) reAddMap.get(BaseCode.DATAS.toString());
		Map<String, Object> reGroupMap = addAuthorityGroup(datasMap.get("groupName") + "", authority.getId());
		if (!"1".equals(reGroupMap.get(BaseCode.STATUS.toString()))) {
			return reGroupMap;
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 校验前台传递的第一级权限菜单是否正确
	 * 
	 * @param datasMap
	 *            参数
	 * @return Map
	 */
	private Map<String, Object> checkAlreadyFirstAuthority(Map<String, Object> datasMap) {
		String firstName = datasMap.get("firstName") + "";
		String firstCode = datasMap.get("firstCode") + "";
		if (StringEmptyUtils.isEmpty(firstName) || StringEmptyUtils.isEmpty(firstCode)) {
			return ReturnInfoUtils.errorInfo("第一级参数不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("firstName", firstName);
		params.put("firstCode", firstCode);
		List<Authority> reList = authorityDao.findByProperty(Authority.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询权限失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("未找到对应第一级权限信息!");
		}
	}

	/**
	 * 添加权限信息中对应的组(关联键)信息
	 * 
	 * @param groupName
	 *            组名-merchant,member,manager
	 * @param authorityId
	 *            关联权限Id
	 * @return Map
	 */
	private Map<String, Object> addAuthorityGroup(String groupName, long authorityId) {
		if (StringEmptyUtils.isEmpty(groupName) || authorityId < 0) {
			return ReturnInfoUtils.errorInfo("权限组参数错误!");
		}
		AuthorityGroup authorityGroup = new AuthorityGroup();
		authorityGroup.setGroupName(groupName);
		authorityGroup.setAuthorityId(authorityId);
		// 状态1-启用,2-禁用
		authorityGroup.setStatus("1");
		if (authorityDao.add(authorityGroup)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("保存权限组失败,服务器繁忙!");
	}

	/**
	 * 添加权限信息
	 * 
	 * @param datasMap
	 */
	private Map<String, Object> addFirstAuthority(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("添加权限信息表,请求参数不能为空!");
		}
		Authority authority = new Authority();
		authority.setFirstName(datasMap.get("firstName") + "");
		authority.setFirstCode(datasMap.get("firstCode") + "");
		authority.setSecondName(datasMap.get("secondName") + "");
		authority.setSecondCode(datasMap.get("secondCode") + "");
		authority.setThirdName(datasMap.get("thirdName") + "");
		authority.setThirdCode(datasMap.get("thirdCode") + "");
		authority.setGroupName(datasMap.get("groupName") + "");
		authority.setCreateBy(datasMap.get("managerName") + "");
		authority.setCreateDate(new Date());
		authority.setDeleteFlag(0);
		if (authorityDao.add(authority)) {
			return ReturnInfoUtils.successDataInfo(authority);
		}
		return ReturnInfoUtils.errorInfo("权限信息保存失败,服务器繁忙！");
	}

	@Override
	public Map<String, Object> getAuthorityInfo() {
		List<Authority> reList = authorityDao.findByProperty(Authority.class, null, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询权限信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return baleInfo(reList);
		} else {
			return ReturnInfoUtils.errorInfo("暂无权限信息数据!");
		}
	}

	/**
	 * 封装对应的权限等级传递至前端信息数据
	 * 
	 * @param reList
	 *            权限实体类的信息
	 * @return Map
	 */
	private Map<String, Object> baleInfo(List<Authority> reList) {
		// 嵌套3及map封装数据
		Map<String, Map<String, Map<String, Map<String, Object>>>> item = new HashMap<>();
		for (Authority authority : reList) {
			Map<String, Map<String, Map<String, Object>>> firstMap = null;
			Map<String, Map<String, Object>> secMap = null;
			Map<String, Object> thirdMap = null;
			String groupName = authority.getGroupName();
			String secCode = authority.getSecondCode();
			String secName = authority.getSecondName();
			String thirdCode = authority.getThirdCode();
			String thirdName = authority.getThirdName();
			long authorityCode = authority.getId();
			// 当分组(第一级)名称已存在后
			if (item.containsKey(groupName)) {
				firstMap = item.get(groupName);
				// 当(该名称)分组第一级类型存在时
				if (firstMap != null
						&& firstMap.containsKey(authority.getFirstCode() + "_" + authority.getFirstName())) {
					secMap = firstMap.get(authority.getFirstCode() + "_" + authority.getFirstName());
					if (secMap != null
							&& secMap.containsKey(authority.getSecondCode() + "_" + authority.getSecondName())) {
						thirdMap = secMap.get(authority.getSecondCode() + "_" + authority.getSecondName());
						if (thirdMap != null
								&& thirdMap.containsKey(authority.getThirdCode() + "_" + authority.getThirdName())) {
						} else {
							thirdMap = new HashMap<>();
							thirdMap.put(authorityCode + "_" + thirdCode, thirdName);
							secMap.get(authority.getSecondCode() + "_" + authority.getSecondName())
									.put(authorityCode + "_" + thirdCode, thirdName);
						}
					} else {// 当(该名称)分组第一级类型不存在时
						// 添加第三级权限信息
						if (StringEmptyUtils.isNotEmpty(thirdCode) && StringEmptyUtils.isNotEmpty(thirdName)) {
							thirdMap = new HashMap<>();
							thirdMap.put(authorityCode + "_" + thirdCode, thirdName);
						}
						// 当第二级参数不为空时,往当前的第一级键值对名称下添加第二级权限信息
						if (StringEmptyUtils.isNotEmpty(secCode) && StringEmptyUtils.isNotEmpty(secName)) {
							firstMap.get(authority.getFirstCode() + "_" + authority.getFirstName())
									.put(secCode + "_" + secName, thirdMap);
						}
					}
				} else {// 当(该名称)分组第一级类型不存在时
					addBaleGroupInfo(item, authority);
				}
			} else {// 当分组(第一级)名称不存在
				addBaleGroupInfo(item, authority);
			}
		}
		return ReturnInfoUtils.successDataInfo(item);
	}

	/**
	 * 封装存在与不存在第一级权限信息
	 * 
	 * @param item
	 * @param authority
	 */
	private void addBaleGroupInfo(Map<String, Map<String, Map<String, Map<String, Object>>>> item,
			Authority authority) {
		String firstCode = authority.getFirstCode();
		String firstName = authority.getFirstName();
		String secCode = authority.getSecondCode();
		String secName = authority.getSecondName();
		String thirdCode = authority.getThirdCode();
		String thirdName = authority.getThirdName();
		String groupName = authority.getGroupName();
		long authorityCode = authority.getId();
		Map<String, Map<String, Map<String, Object>>> firstMap = null;
		Map<String, Map<String, Object>> secMap = null;
		Map<String, Object> thirdMap = null;
		if (StringEmptyUtils.isNotEmpty(thirdCode) && StringEmptyUtils.isNotEmpty(thirdName)) {
			thirdMap = new HashMap<>();
			thirdMap.put(authorityCode + "_" + thirdCode, thirdName);
		}
		if (StringEmptyUtils.isNotEmpty(secCode) && StringEmptyUtils.isNotEmpty(secName)) {
			secMap = new HashMap<>();
			secMap.put(secCode + "_" + secName, thirdMap);
		}
		firstMap = new HashMap<>();
		firstMap.put(firstCode + "_" + firstName, secMap);
		if (item.containsKey(groupName)) {
			item.get(groupName).put(firstCode + "_" + firstName, secMap);
		} else {
			item.put(groupName, firstMap);
		}
	}

	@Override
	public Map<String, Object> getUserAuthorityInfo(String userId, String groupName) {
		if (StringEmptyUtils.isEmpty(userId) || StringEmptyUtils.isEmpty(groupName)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空！");
		}
		Table table = authorityDao.getAuthorityGroupInfo(userId, groupName);
		if (table == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!table.getRows().isEmpty()) {
			List<Row> lr = table.getRows();
			return baleUserAuthorityInfo(lr);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	private Map<String, Object> baleUserAuthorityInfo(List<Row> lr) {
		// 三层嵌套
		Map<String, Map<String, Map<String, Object>>> item = new HashMap<>();
		Map<String, Map<String, Object>> secMap = null;
		Map<String, Object> thirdMap = null;
		for (int i = 0; i < lr.size(); i++) {
			String firstName = lr.get(i).getValue("firstName") + "";
			String secondName = lr.get(i).getValue("secondName") + "";
			String thirdName = lr.get(i).getValue("thirdName") + "";
			String authorityId = lr.get(i).getValue("authorityId") + "";
			String checkFlag = lr.get(i).getValue("checkFlag") + "";
			if (StringEmptyUtils.isEmpty(checkFlag)) {
				checkFlag = "false";
			}
			if (item.containsKey(firstName)) {
				secMap = item.get(firstName);
				if (secMap != null && secMap.containsKey(secondName)) {
					secMap.get(secondName).put(authorityId + "_" + checkFlag, thirdName);
				} else {
					thirdMap = new HashMap<>();
					thirdMap.put(authorityId + "_" + checkFlag, thirdName);
					item.get(firstName).put(secondName, thirdMap);
				}
			} else {
				thirdMap = new HashMap<>();
				thirdMap.put(authorityId + "_" + checkFlag, thirdName);
				secMap = new HashMap<>();
				secMap.put(secondName, thirdMap);
				item.put(firstName, secMap);
			}
		}
		return ReturnInfoUtils.successDataInfo(item);
	}

	@Override
	public Map<String, Object> setAuthorityInfo(Map<String, Object> datasMap) {
		if (datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		JSONArray jsonArr = null;
		try {
			jsonArr = jsonArr.fromObject(datasMap.get("authorityPack"));
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("参数格式错误!");
		}
		String userId = "";
		String userName = "";
		String type = datasMap.get("type") + "";
		switch (type) {
		case "merchant":
			Map<String, Object> reCheckMerchantMap = merchantUtils.getMerchantInfo(datasMap.get("userId") + "");
			if (!"1".equals(reCheckMerchantMap.get(BaseCode.STATUS.toString()))) {
				return reCheckMerchantMap;
			}
			Merchant merchant = (Merchant) reCheckMerchantMap.get(BaseCode.DATAS.toString());
			userId = merchant.getMerchantId();
			userName = merchant.getMerchantName();
			break;
		case "manager":
			Map<String, Object> reCheckManagerMap = getManagerInfo(datasMap.get("userId") + "");
			if (!"1".equals(reCheckManagerMap.get(BaseCode.STATUS.toString()))) {
				return reCheckManagerMap;
			}
			Manager manager = (Manager) reCheckManagerMap.get(BaseCode.DATAS.toString());
			userId = manager.getManagerId();
			userName = manager.getManagerName();
			break;
		case "member":
			break;
		default:
			return ReturnInfoUtils.errorInfo("类型错误!");
		}
		return checkAuthorityInfo(jsonArr, datasMap.get("managerName") + "", userId, userName);
	}

	/**
	 * 获取管理员信息
	 * 
	 * @param managerId
	 *            管理员Id
	 * @return Map
	 */
	private Map<String, Object> getManagerInfo(String managerId) {
		if (StringEmptyUtils.isEmpty(managerId)) {
			return ReturnInfoUtils.errorInfo("用户Id不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("managerId", managerId);
		List<Manager> reList = authorityDao.findByProperty(Manager.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询管理员信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList.get(0));
		} else {
			return ReturnInfoUtils.errorInfo("未找到该管理员信息,请核对信息!");
		}
	}

	/**
	 * 设置对应用户权限时,根据对应的参数进行校验选择
	 * 
	 * @param jsonArr
	 *            权限信息
	 * @param managerName
	 *            管理员名称
	 * @param userId
	 *            用户Id
	 * @param userName
	 *            用户名称
	 * @return Map
	 */
	private Map<String, Object> checkAuthorityInfo(JSONArray jsonArr, String managerName, String roleId,
			String roleName) {
		if (jsonArr == null || StringEmptyUtils.isEmpty(roleId) || StringEmptyUtils.isEmpty(roleName)) {
			return ReturnInfoUtils.errorInfo("权限参数不能为空!");
		}
		if (jsonArr.isEmpty()) {
			return updateAllAuthorityUser(roleId, managerName);
		}
		return updateAuthorityUser(jsonArr, roleId, managerName);
	}

	/**
	 * 根据前台传递的权限流水Id,将对应的用户Id下对应的权限信息checkFlag标识修改为false
	 * 
	 * @param jsonArr
	 *            流水Id集合
	 * @param roleId
	 *            用户Id
	 * @param managerName
	 *            管理员名称
	 * @return Map
	 */
	private Map<String, Object> updateAuthorityUser(JSONArray jsonArr, String roleId, String managerName) {
		if (jsonArr == null || jsonArr.isEmpty() || StringEmptyUtils.isEmpty(roleId)
				|| StringEmptyUtils.isEmpty(managerName)) {
			return ReturnInfoUtils.errorInfo("权限信息参数不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		long id = 0;
		for (int i = 0; i < jsonArr.size(); i++) {
			try {
				id = Long.parseLong(jsonArr.get(i) + "");
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("权限Id流水格式错误！");
			}
			params.clear();
			params.put("userId", roleId);
			params.put("authorityId", id);
			List<AuthorityUser> reUserList = authorityDao.findByProperty(AuthorityUser.class, params, 0, 0);
			if (reUserList != null && !reUserList.isEmpty()) {
				AuthorityUser authorityUser = reUserList.get(0);
				authorityUser.setCheckFlag("false");
				authorityUser.setUpdateBy(managerName);
				authorityUser.setUpdateDate(new Date());
				if (!authorityDao.update(authorityUser)) {
					return ReturnInfoUtils.errorInfo("设置权限失败,服务器繁忙!");
				}
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 将当前用户Id下所有权限信息中checkFlag标识更新为true
	 * 
	 * @param roleId
	 *            用户Id
	 * @param managerName
	 *            管理员名称
	 * @return Map
	 */
	private Map<String, Object> updateAllAuthorityUser(String roleId, String managerName) {
		Map<String, Object> params = new HashMap<>();
		params.put("userId", roleId);
		List<AuthorityUser> reUserList = authorityDao.findByProperty(AuthorityUser.class, params, 0, 0);
		if (reUserList != null && !reUserList.isEmpty()) {
			for (AuthorityUser authorityUser : reUserList) {
				authorityUser.setCheckFlag("true");
				authorityUser.setUpdateBy(managerName);
				authorityUser.setUpdateDate(new Date());
				if (!authorityDao.update(authorityUser)) {
					return ReturnInfoUtils.errorInfo("设置权限失败,服务器繁忙!");
				}
			}
		}
		return ReturnInfoUtils.successInfo();
	}
}
