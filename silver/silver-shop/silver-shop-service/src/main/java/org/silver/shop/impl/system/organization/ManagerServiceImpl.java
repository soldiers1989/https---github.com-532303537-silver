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
import org.silver.shop.model.system.Authority;
import org.silver.shop.model.system.AuthorityUser;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.organization.MerchantDetail;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.util.IdUtils;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.SearchUtils;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = ManagerService.class)
public class ManagerServiceImpl implements ManagerService {

	@Autowired
	private ManagerDao managerDao;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private IdUtils idUtils;

	@Override
	public List<Object> findManagerBy(String account) {
		Map<String, Object> params = new HashMap<>();
		params.put("managerName", account);
		return managerDao.findByProperty(Manager.class, params, 0, 0);
	}

	@Override
	public Map<String, Object> findAllmemberInfo(int page, int size, Map<String, Object> datasMap) {
		Map<String, Object> reDatasMap = SearchUtils.universalMemberSearch(datasMap);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		List<Object> reList = managerDao.findByProperty(Member.class, paramMap, page, size);
		long totalCount = managerDao.findByPropertyCount(Member.class, null);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, totalCount);
		} else {
			return ReturnInfoUtils.errorInfo("暂无会员信息!");
		}
	}

	@Override
	public Map<String, Object> createManager(String managerName, String loginPassword, int managerMarks,
			String reManagerName, String description, String realName) {
		if (StringEmptyUtils.isEmpty(managerName) || StringEmptyUtils.isEmpty(loginPassword)) {
			return ReturnInfoUtils.errorInfo("参数不能为空！");
		}
		Date date = new Date();
		MD5 md5 = new MD5();
		Map<String, Object> reIdMap = idUtils.createId(Manager.class, "managerId_");
		if (!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))) {
			return reIdMap;
		}
		String serialNo = reIdMap.get(BaseCode.DATAS.toString()) + "";
		Manager managerInfo = new Manager();
		managerInfo.setManagerId(serialNo);
		managerInfo.setManagerName(managerName);
		managerInfo.setLoginPassword(md5.getMD5ofStr(loginPassword));
		// 管理员标识：1-超级管理员、2-运营管理员、3-财务管理员
		if (managerMarks == 1 || managerMarks == 2 || managerMarks == 3) {
			managerInfo.setManagerMarks(managerMarks);
		} else {
			return ReturnInfoUtils.errorInfo("管理员类型错误，请重新选择！");
		}
		if (!StringUtil.isContainChinese(realName)) {
			return ReturnInfoUtils.errorInfo("真实姓名错误，请重新输入！");
		}
		managerInfo.setRealName(realName);
		managerInfo.setCreateBy(reManagerName);
		managerInfo.setCreateDate(date);
		// 删除标识:0-未删除,1-已删除
		managerInfo.setDeleteFlag(0);
		managerInfo.setDescription(description);
		if (!managerDao.add(managerInfo)) {
			return ReturnInfoUtils.errorInfo("创建失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> findAllMerchantInfo(Map<String, Object> datasMap, int page, int size) {
		Map<String, Object> reDatasMap = SearchUtils.universalSearch(datasMap);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		List<Merchant> reList = managerDao.findByProperty(Merchant.class, paramMap, page, size);
		Long totalCount = managerDao.findByPropertyCount(Merchant.class, paramMap);
		List<Object> item = new ArrayList<>();
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Merchant merchantInfo = reList.get(i);
				merchantInfo.setLoginPassword("");
				item.add(merchantInfo);
			}
			return ReturnInfoUtils.successDataInfo(item, totalCount.intValue());
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> findMerchantDetail(String managerName, String merchantId) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("merchantId", merchantId);
		List<Object> reList = managerDao.findByProperty(Merchant.class, paramMap, 1, 1);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
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
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
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
		paramMap.put("managerId", managerId);
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
	public Map<String, Object> editMerhcnatInfo(String managerId, String managerName, String[] arrStr) {
		if (arrStr == null || arrStr.length <= 0) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> statusMap = new HashMap<>();
		String merchantId = "";
		String merchantName = "";
		String merchantPhone = "";
		String merchantQQ = "";
		String merchantEmail = "";
		String merchantIdCard = "";
		String merchantIdCardName = "";
		String merchantAddress = "";
		String merchantStatus = "";

		for (int i = 0; i < arrStr.length; i++) {
			String value = arrStr[i];
			switch (i) {
			case 0:
				if (StringEmptyUtils.isNotEmpty(value)) {
					merchantId = value;
				} else {
					statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.getBaseCode(), "商户Id不能为空!");
					return statusMap;
				}
				break;
			case 1:
				merchantName = value;
				break;
			case 2:
				merchantPhone = value;
				break;
			case 3:
				merchantQQ = value;
				break;
			case 4:
				merchantEmail = value;
				break;
			case 5:
				merchantIdCard = value;
				break;
			case 6:
				merchantIdCardName = value;
				break;
			case 7:
				merchantAddress = value;
				break;
			case 8:
				merchantStatus = value;
				break;
			default:
				break;
			}
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchantInfo = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		merchantInfo.setMerchantName(merchantName);
		merchantInfo.setMerchantPhone(merchantPhone);
		merchantInfo.setMerchantQQ(merchantQQ);
		merchantInfo.setMerchantEmail(merchantEmail);
		merchantInfo.setMerchantIdCard(merchantIdCard);
		merchantInfo.setMerchantIdCardName(merchantIdCardName);
		merchantInfo.setMerchantAddress(merchantAddress);
		merchantInfo.setMerchantStatus(merchantStatus);
		/*
		 * merchantInfo.setMerchantCustomsregistrationCode(
		 * merchantCustomsregistrationCode);
		 * merchantInfo.setMerchantOrganizationCode(merchantOrganizationCode);
		 * merchantInfo.setMerchantChecktheRegistrationCode(
		 * merchantChecktheRegistrationCode);
		 */
		if (!managerDao.update(merchantInfo)) {
			return ReturnInfoUtils.errorInfo("更新商户基本信息失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> editMerhcnatBusinessInfo(String managerId, String managerName, List<Object> imglist,
			String[] arrayStr, String merchantId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("merchantId", merchantId);
		List<MerchantDetail> reList = managerDao.findByProperty(MerchantDetail.class, paramMap, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return updateMerchantDetail(reList.get(0), arrayStr, imglist);
		}
		return ReturnInfoUtils.errorInfo("未找到商户业务信息!");
	}

	@Override
	public Map<String, Object> addMerchantBusinessInfo(String merchantId, String[] arrayStr, List<Object> imglist) {
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		List<MerchantDetail> reList = managerDao.findByProperty(MerchantDetail.class, params, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			return updateMerchantDetail(reList.get(0), arrayStr, imglist);
		}
		return ReturnInfoUtils.errorInfo("商户详情信息查询失败！");
	}

	/**
	 * 更新商户详细信息
	 * 
	 * @param merchantDetail
	 *            商户详情信息实体类
	 * @param arrayStr
	 *            图片对应下标数组
	 * @param imglist
	 *            图片名称-集合
	 * @return Map
	 */
	private Map<String, Object> updateMerchantDetail(MerchantDetail merchantDetail, String[] arrayStr,
			List<Object> imglist) {
		if (arrayStr.length > 0 && !imglist.isEmpty()) {
			// 创建图片List下标值
			int imgIndex = 0;
			for (int i = 0; i < arrayStr.length; i++) {
				String picIndex = arrayStr[i];
				switch (picIndex) {
				case "1":
					merchantDetail.setMerchantBusinessLicenseLink(imglist.get(imgIndex) + "");
					break;
				case "2":
					merchantDetail.setMerchantCustomsregistrationCodeLink(imglist.get(imgIndex) + "");
					break;
				case "3":
					merchantDetail.setMerchantOrganizationCodeLink(imglist.get(imgIndex) + "");
					break;
				case "4":
					merchantDetail.setMerchantChecktheRegistrationCodeLink(imglist.get(imgIndex) + "");
					break;
				case "5":
					merchantDetail.setMerchantTaxRegistrationCertificateLink(imglist.get(imgIndex) + "");
					break;
				case "6":
					merchantDetail.setMerchantSpecificIndustryLicenseLink(imglist.get(imgIndex) + "");
					break;
				default:
					break;
				}
				// arrayStr字符串数组当前下标中的值不为空时,则代表有图片上传,index +1
				if (StringEmptyUtils.isNotEmpty(picIndex)) {
					imgIndex++;
				}
			}
			if (!managerDao.update(merchantDetail)) {
				return ReturnInfoUtils.errorInfo("修改商户业务信息失败,服务器繁忙!");
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("修改商户业务信息时参数错误!");
	}

	@Override
	public Map<String, Object> findMemberDetail(String memberId) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("memberId", memberId);
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
	public Map<String, Object> managerEditMemberInfo(String managerId, String managerName,
			Map<String, Object> datasMap) {
		Map<String, Object> statusMap = new HashMap<>();
		Member memberInfo = null;
		String memberId = datasMap.get("memberId") + "";
		if (StringEmptyUtils.isNotEmpty(memberId)) {
			// 查询数据库已存在的用户信息
			Map<String, Object> reMemberMap = findMemberDetail(memberId);
			if (!"1".equals(reMemberMap.get(BaseCode.STATUS.toString()))) {
				return reMemberMap;
			}
			List<Object> dataList = (List<Object>) reMemberMap.get(BaseCode.DATAS.toString());
			memberInfo = (Member) dataList.get(0);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "用户Id不能为空!");
			return statusMap;
		}
		for (Map.Entry<String, Object> entry : datasMap.entrySet()) {
			String key = entry.getKey();
			String value = datasMap.get(key) + "".trim();
			switch (key.trim()) {
			case "memberTel":
				if (StringEmptyUtils.isNotEmpty(value)) {
					memberInfo.setMemberTel(value);
				} else {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "用户手机号码不能为空!");
					return statusMap;
				}
				break;
			case "memberMail":
				if (StringEmptyUtils.isNotEmpty(value)) {
					memberInfo.setMemberMail(value);
				}
				break;
			case "memberIdCardName":
				if (StringEmptyUtils.isNotEmpty(value)) {
					memberInfo.setMemberIdCardName(value);
				} else {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "用户姓名不能为空!");
					return statusMap;
				}
				break;
			case "memberIdCard":
				if (StringEmptyUtils.isNotEmpty(value)) {
					memberInfo.setMemberIdCard(value);
				} else {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "用户身份证号码不能为空!");
					return statusMap;
				}
				break;
			case "memberStatus":
				if (StringEmptyUtils.isNotEmpty(value)) {
					memberInfo.setMemberStatus(Integer.parseInt(value));
				} else {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "用户状态参数不能为空!");
					return statusMap;
				}
				break;
			case "deleteFlag":
				if (StringEmptyUtils.isNotEmpty(value)) {
					memberInfo.setDeleteFlag(Integer.parseInt(value));
				} else {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "用户删除参数不能为空!");
					return statusMap;
				}
				break;
			default:
				break;
			}
		}
		Date date = new Date();
		memberInfo.setUpdateBy(managerName);
		memberInfo.setUpdateDate(date);
		if (!managerDao.update(memberInfo)) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "服务器繁忙!");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> editMerchantRecordDetail(String managerId, String managerName, String merchantId,
			String merchantRecordInfo) {
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(merchantRecordInfo);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("商户备案信息包格式错误!");
		}
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> datasMap = (Map<String, Object>) jsonList.get(i);
			String strId = datasMap.get("id") + "";
			if (StringEmptyUtils.isNotEmpty(strId)) {// 当有商品备案Id时
				long id = Long.parseLong(strId);
				Map<String, Object> params = new HashMap<>();
				params.put("id", id);
				List<MerchantRecordInfo> reList = managerDao.findByProperty(MerchantRecordInfo.class, params, 0, 0);
				if (reList != null && !reList.isEmpty()) {
					MerchantRecordInfo recordInfo = reList.get(0);
					int customsPort = Integer.parseInt(datasMap.get("customsPort") + "");
					String customsPortName = datasMap.get("customsPortName") + "";
					String ebEntNo = datasMap.get("ebEntNo") + "";
					String ebEntName = datasMap.get("ebEntName") + "";
					String ebpEntNo = datasMap.get("ebpEntNo") + "";
					String ebpEntName = datasMap.get("ebpEntName") + "";
					recordInfo.setCustomsPort(customsPort);
					recordInfo.setCustomsPortName(customsPortName);
					recordInfo.setEbEntNo(ebEntNo);
					recordInfo.setEbEntName(ebEntName);
					recordInfo.setEbpEntNo(ebpEntNo);
					recordInfo.setEbpEntName(ebpEntName);
					recordInfo.setUpdateBy(managerName);
					recordInfo.setUpdateDate(new Date());
					if (!managerDao.update(recordInfo)) {
						return ReturnInfoUtils.errorInfo("更新备案信息失败,服务器繁忙!");
					}
				} else {
					return ReturnInfoUtils.errorInfo("商户备案信息查询失败,服务器繁忙!");
				}
			} else {// 当没有传Id时则为新建
				MerchantRecordInfo recordInfo = new MerchantRecordInfo();
				int customsPort = Integer.parseInt(datasMap.get("customsPort") + "");
				String ebEntNo = datasMap.get("ebEntNo") + "";
				String ebEntName = datasMap.get("ebEntName") + "";
				String ebpEntNo = datasMap.get("ebpEntNo") + "";
				String ebpEntName = datasMap.get("ebpEntName") + "";
				recordInfo.setMerchantId(merchantId);
				recordInfo.setCustomsPort(customsPort);
				recordInfo.setEbEntNo(ebEntNo);
				recordInfo.setEbEntName(ebEntName);
				recordInfo.setEbpEntNo(ebpEntNo);
				recordInfo.setEbpEntName(ebpEntName);
				recordInfo.setDeleteFlag(0);
				recordInfo.setCreateBy(managerName);
				recordInfo.setCreateDate(new Date());
				if (!managerDao.add(recordInfo)) {
					return ReturnInfoUtils.errorInfo("保存失败,服务器繁忙!");
				}
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> deleteMerchantRecordInfo(long id) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("id", id);
		List<MerchantRecordInfo> reList = managerDao.findByProperty(MerchantRecordInfo.class, paramMap, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			MerchantRecordInfo recordInfo = reList.get(0);
			if (!managerDao.delete(recordInfo)) {
				return ReturnInfoUtils.errorInfo("删除失败,服务器繁忙!");
			}
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> managerAuditMerchantInfo(String managerId, String managerName, JSONArray json) {
		if (StringEmptyUtils.isNotEmpty(managerName) && !json.isEmpty()) {
			for (int i = 0; i < json.size(); i++) {
				Map<String, Object> datasMap = (Map<String, Object>) json.get(i);
				String merchantId = datasMap.get("merchantId") + "";
				String status = datasMap.get("status") + "";
				// 商户状态：1-启用，2-禁用，3-审核
				if ("1".equals(status) || "2".equals(status) || "3".equals(status)) {
					Map<String, Object> params = new HashMap<>();
					params.put("merchantId", merchantId);
					List<Merchant> reList = managerDao.findByProperty(Merchant.class, params, 0, 0);
					if (reList != null && !reList.isEmpty()) {
						Merchant merchant = reList.get(0);
						merchant.setMerchantStatus(status);
						if (!managerDao.update(merchant)) {
							return ReturnInfoUtils.errorInfo("更新失败,请重试!");
						}
					}
				} else {
					return ReturnInfoUtils.errorInfo("状态参数不正确,请重新输入!");
				}
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("请求参数出错!");
	}

	@Override
	public Map<String, Object> resetMerchantLoginPassword(String merchantId, String managerId, String managerName) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("merchantId", merchantId);
		List<Merchant> reList = managerDao.findByProperty(Merchant.class, paramMap, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			Merchant merchant = reList.get(0);
			MD5 md5 = new MD5();
			// 默认为：Ym@888!333
			merchant.setLoginPassword(md5.getMD5("Ym@888!333".getBytes()));
			if (!managerDao.update(merchant)) {
				return ReturnInfoUtils.errorInfo("重置密码失败,服务器繁忙!");
			}
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("商户信息未找到,请核对商户是否存在!");
		}
	}

	@Override
	public Map<String, Object> getMerchantBusinessInfo(String merchantId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("merchantId", merchantId);
		List<MerchantDetail> reList = managerDao.findByProperty(MerchantDetail.class, paramMap, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, 0);
		} else {
			List<Merchant> reMerchantList = managerDao.findByProperty(Merchant.class, paramMap, 0, 0);
			if (reMerchantList != null && !reMerchantList.isEmpty()) {
				MerchantDetail merchantDetail = new MerchantDetail();
				merchantDetail.setMerchantId(merchantId);
				merchantDetail.setCreateDate(new Date());
				merchantDetail.setCreateBy("system");
				if (!managerDao.add(merchantDetail)) {
					return ReturnInfoUtils.errorInfo("服务器异常!");
				}
				return ReturnInfoUtils.successDataInfo(merchantDetail, 0);
			}
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> getManagerAuthority(String managerId) {
		if (StringEmptyUtils.isEmpty(managerId)) {
			return ReturnInfoUtils.errorInfo("管理员Id不能为空");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("userId", managerId);
		params.put("checkFlag", "true");
		List<AuthorityUser> reList = managerDao.findByProperty(AuthorityUser.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询管理员权限信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			List<String> list = new ArrayList<>();
			for (AuthorityUser authorityRole : reList) {
				list.add(authorityRole.getAuthorityCode());
			}
			return ReturnInfoUtils.successDataInfo(list);
		} else {
			return ReturnInfoUtils.errorInfo("未找到该管理员权限信息!");
		}
	}

	@Override
	public Object tmpUpdateAuthority() {
		Map<String, Object> params = new HashMap<>();
		@SuppressWarnings("unchecked")
		List<Authority> reList = managerDao.findByProperty(Authority.class, null, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询管理员权限信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			for (Authority authority : reList) {
				params.clear();
				if ("manager".equals(authority.getGroupName().trim())) {
					List<Manager> reManagerList = managerDao.findByProperty(Manager.class, null, 0, 0);
					for (Manager manager : reManagerList) {
						params.clear();
						params.put("userId", manager.getManagerId());
						params.put("authorityId", authority.getAuthorityId());
						List<AuthorityUser> reAuthorityUserList = managerDao.findByProperty(AuthorityUser.class, params,
								0, 0);
						if (reAuthorityUserList != null && reAuthorityUserList.isEmpty()) {
							AuthorityUser authorityUser = new AuthorityUser();
							authorityUser.setUserId(manager.getManagerId());
							authorityUser.setUserName(manager.getManagerName());
							authorityUser.setAuthorityId(authority.getAuthorityId());
							authorityUser.setAuthorityCode(authority.getSecondCode() + ":" + authority.getThirdCode());
							System.out
									.println("---路径-?>>" + authority.getSecondCode() + ":" + authority.getThirdCode());
							authorityUser.setCheckFlag("true");
							authorityUser.setCreateBy("system");
							authorityUser.setCreateDate(new Date());
							if (managerDao.add(authorityUser)) {
								System.out.println(
										"-管理员->>>" + manager.getManagerName() + "保存" + authority.getThirdName());
							}
						} else {
							System.out.println(
									manager.getManagerId() + "--管理员Id对应的权限id-->>>" + authority.getId() + "已存在");
						}
					}

				}
			}
		} else {
			return ReturnInfoUtils.errorInfo("未找到该管理员权限信息!");
		}
		return ReturnInfoUtils.successInfo();
	}

}
