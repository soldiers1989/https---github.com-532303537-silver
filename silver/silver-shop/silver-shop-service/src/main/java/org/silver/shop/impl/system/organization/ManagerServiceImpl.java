package org.silver.shop.impl.system.organization;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.ManagerService;
import org.silver.shop.dao.system.organization.ManagerDao;
import org.silver.shop.impl.system.commerce.StockServiceImpl;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.FileUpLoadService;
import org.silver.util.MD5;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = ManagerService.class)
public class ManagerServiceImpl implements ManagerService {

	@Autowired
	private ManagerDao managerDao;
	@Autowired
	private StockServiceImpl stockServiceImpl;
	@Autowired
	private FileUpLoadService fileUpLoadService;

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
		long totalCount = managerDao.findByPropertyCount(Member.class, null);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), totalCount);
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
		Date date = new Date();
		Manager managerInfo = new Manager();
		Map<String, Object> statusMap = new HashMap<>();
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
		managerInfo.setCreateDate(date);
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
	public Map<String, Object> findAllMerchantInfo(Map<String, Object> datasMap) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reDatasMap = stockServiceImpl.universalSearch(datasMap);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		List<Object> reList = managerDao.findByProperty(Merchant.class, paramMap, 0, 0);
		long totalCount = managerDao.findByPropertyCount(Merchant.class, null);
		List<Object> item = new ArrayList<>();
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Merchant merchantInfo = (Merchant) reList.get(i);
				merchantInfo.setLoginPassword("");
				item.add(merchantInfo);
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), item);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), totalCount);
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
			if (!md5OldLoginPassword.equals(reLoginPassword)) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), "旧密码错误,请重试!");
				return statusMap;
			}
			managerInfo.setLoginPassword(md5.getMD5ofStr(newLoginPassword));
			managerInfo.setUpdateDate(date);
			managerInfo.setUpdateBy(managerName);
			if (!managerDao.update(managerInfo)) {
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

	@Override
	public Map<String, Object> editMerchantStatus(String merchantId, String managerId, String managerName, int status) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("merchantId", merchantId);
		List<Object> reList = managerDao.findByProperty(Merchant.class, paramMap, 1, 1);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			Merchant merchantInfo = (Merchant) reList.get(0);
			merchantInfo.setMerchantStatus(String.valueOf(status));
			if (!managerDao.update(merchantInfo)) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), "修改商户状态,服务器繁忙!");
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

	@Override
	public Map<String, Object> findAllManagerInfo() {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("deleteFlag", 0);
		List<Object> reList = managerDao.findByProperty(Manager.class, paramMap, 0, 0);
		long totalCount = managerDao.findByPropertyCount(Manager.class, null);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), totalCount);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> resetManagerPassword(String managerId, String managerName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(managerId, managerId);
		List<Object> reList = managerDao.findByProperty(Manager.class, paramMap, 0, 0);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			Manager managerInfo = (Manager) reList.get(0);
			// 默认为：888888
			managerInfo.setLoginPassword("21218CCA77804D2BA1922C33E0151105");
			if (!managerDao.update(managerInfo)) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), "重置密码失败,服务器繁忙!");
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

	@Override
	public Map<String, Object> editMerhcnatInfo(String managerId, String managerName, String[] arrStr,
			List<Object> imglist) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();

		String merchantId = "";
		String merchantName = "";
		String merchantAvatar = "";
		String merchantPhone = "";
		String merchantQQ = "";
		String merchantEmail = "";
		String merchantIdCard = "";
		String merchantIdCardName = "";
		String merchantAddress = "";
		String merchantStatus = "";
		String merchantBusinessLicenseLink = "";
		String merchantCustomsregistrationCodeLink = "";
		String merchantOrganizationCodeLink = "";
		String merchantChecktheRegistrationCodeLink = "";
		String merchantTaxRegistrationCertificateLink = "";
		String merchantSpecificIndustryLicenseLink = "";
		String merchantCustomsregistrationCode = "";
		String merchantOrganizationCode = "";
		String merchantChecktheRegistrationCode = "";
		int[] intArr = null;
		for (int i = 0; i < arrStr.length; i++) {
			String value = arrStr[i];
			switch (i) {
			case 0:
				merchantId = value;
				break;
			case 1:
				merchantName = value;
				break;
			case 2:
				merchantAvatar = value;
				break;
			case 3:
				merchantPhone = value;
				break;
			case 4:
				merchantQQ = value;
				break;
			case 5:
				merchantEmail = value;
				break;
			case 6:
				merchantIdCard = value;
				break;
			case 7:
				merchantIdCardName = value;
				break;
			case 8:
				merchantAddress = value;
				break;
			case 9:
				merchantStatus = value;
				break;
			case 10:
				merchantBusinessLicenseLink = value;
				break;
			case 11:
				merchantCustomsregistrationCodeLink = value;
				break;
			case 12:
				merchantOrganizationCodeLink = value;
				break;
			case 13:
				merchantChecktheRegistrationCodeLink = value;
				break;
			case 14:
				merchantTaxRegistrationCertificateLink = value;
				break;
			case 15:
				merchantSpecificIndustryLicenseLink = value;
				break;
			case 16:
				merchantCustomsregistrationCode = value;
				break;
			case 17:
				merchantOrganizationCode = value;
				break;
			case 18:
				merchantChecktheRegistrationCode = value;
				break;
			case 19:
				intArr = new int[Integer.parseInt(value)];
				break;
			default:
				break;
			}
		}

		return null;
	}

}
