package org.silver.shop.impl.system.organization;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.loader.custom.Return;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.MerchantService;
import org.silver.shop.dao.system.organization.MerchantDao;
import org.silver.shop.model.system.AuthorityUser;
import org.silver.shop.model.system.organization.AgentBaseContent;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.organization.MerchantDetail;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.util.IdUtils;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = MerchantService.class)
public class MerchantServiceImpl implements MerchantService {

	@Autowired
	private AgentServiceImpl agentServiceImpl;
	@Autowired
	private IdUtils<Merchant> idUtils;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	// 口岸
	private static final String EPORT = "eport";
	// 口岸名称
	private static final String PORTNAME = "customsPortName";
	// 电商企业编号
	private static final String EBENTNO = "ebEntNo";
	// 电商企业名称
	private static final String EBENTNAME = "ebEntName";
	// 电商平台企业编号
	private static final String EBPENTNO = "ebpEntNo";
	// 电商平台名称
	private static final String EBPENTNAME = "ebpEntName";

	//
	static List<Map<String, Object>> list = null;
	static {
		// 银盟商城备案信息 1-广州电子口岸(目前只支持BC业务)
		final String ebEntNo = "C010000000537118";
		final String ebEntName = "广州银盟信息科技有限公司";

		// 银盟商城备案信息2-南沙智检(支持BBC业务)
		final String ebEntNo2 = "1509007917";

		// 初始化银盟自己商户备案信息
		list = new ArrayList<>();
		Map<String, Object> record1 = new HashMap<>();
		Map<String, Object> record2 = new HashMap<>();
		record1.put(EPORT, 1);// 1-广州电子口岸(目前只支持BC业务)
		record1.put(PORTNAME, "广州电子口岸");
		record1.put(EBENTNO, ebEntNo);
		record1.put(EBENTNAME, ebEntName);
		record1.put(EBPENTNO, ebEntNo);
		record1.put(EBPENTNAME, ebEntName);
		record2.put(EPORT, 2);// 2-南沙智检(支持BBC业务)
		record2.put(PORTNAME, "南沙智检");
		record2.put(EBENTNO, ebEntNo2);
		record2.put(EBENTNAME, ebEntName);
		record2.put(EBPENTNO, ebEntNo2);
		record2.put(EBPENTNAME, ebEntName);
		list.add(record1);
		list.add(record2);
	}
	@Autowired
	private MerchantDao merchantDao;

	@Override
	public List<Object> checkMerchantName(Map dataMap) {
		return merchantDao.findByProperty(Merchant.class, dataMap, 1, 1);
	}

	@Override
	public Map<String, Object> findOriginalMerchantId() {
		// 扫描获取数据库表中的商户自增长ID
		long merchantCount = merchantDao.findLastId();
		if (merchantCount < 0) {// 判断数据库查询出数据如果小于0,则中断程序,告诉异常
			return ReturnInfoUtils.errorInfo("查询商户商户自增长Id错误,服务器繁忙!");
		}
		// 得出的总数上+1
		long count = merchantCount + 1;
		String merchantId = String.valueOf(count);
		// 当商户ID没有5位数时,前面补0
		while (merchantId.length() < 5) {
			merchantId = "0" + merchantId;
		}
		merchantId = "MerchantId_" + merchantId;
		return ReturnInfoUtils.successDataInfo(merchantId);
	}

	@Override
	public boolean addMerchantRecordInfo(MerchantRecordInfo entity, String type) {
		boolean flag = false;
		if (type.equals("1")) {// 银盟自己商户备案信息插入
			for (int x = 0; x < list.size(); x++) {
				Map<String, Object> listMap = list.get(x);
				entity.setCustomsPort(Integer.valueOf(listMap.get(EPORT) + ""));
				entity.setCustomsPortName(listMap.get(PORTNAME) + "");
				entity.setEbEntNo(listMap.get(EBENTNO) + "");
				entity.setEbEntName(listMap.get(EBENTNAME) + "");
				entity.setEbpEntNo(listMap.get(EBPENTNO) + "");
				entity.setEbpEntName(listMap.get(EBPENTNAME) + "");
				flag = merchantDao.add(entity);
			}
		} else {// 第三方商戶备案信息插入
			flag = merchantDao.add(entity);
		}
		return flag;
	}

	@Override
	public Map<String, Object> merchantRegister(Map<String, Object> datasMap) {
		if(datasMap == null || datasMap.isEmpty()){
			return ReturnInfoUtils.errorInfo("商户注册请求参数不能为空!");
		}
		Date dateTime = new Date();
		int type = Integer.parseInt(datasMap.get("type") + "");
		String loginPassword = datasMap.get("loginPassword") + "";
		Map<String,Object> reIdMap = idUtils.createId(Merchant.class, "MerchantId_");
		if(!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))){
			return reIdMap;
		}
		String merchantId = reIdMap.get(BaseCode.DATAS.toString())+"";
		String merchantName = datasMap.get("merchantName") + "";
		String managerName = datasMap.get("managerName") + "";
		String phone = datasMap.get("phone") + "";
		String agentId = datasMap.get("agentId") + "";
		String  merchantStatus = datasMap.get("merchantStatus") + "";
		if (type == 1) {// 1-银盟商户注册
			if (!createMerchantInfo(type, merchantId, merchantName, loginPassword, managerName, phone, agentId,merchantStatus)) {
				return ReturnInfoUtils.errorInfo("商户基本信息保存失败,服务器繁忙!");
			}
			MerchantRecordInfo recordInfo = new MerchantRecordInfo();
			recordInfo.setMerchantId(merchantId);
			recordInfo.setCreateBy(managerName);
			recordInfo.setCreateDate(dateTime);
			recordInfo.setDeleteFlag(0);// 删除标识:0-未删除,1-已删除
			// 保存商户对应的电商平台名称(及编码)
			if (!addMerchantRecordInfo(recordInfo, "1")) {
				return ReturnInfoUtils.errorInfo("保存商户备案信息失败,服务器繁忙!");
			}
		} else if (type == 2) {// 2-第三方商户注册
			if (!createMerchantInfo(type, merchantId, merchantName, loginPassword, managerName, phone, agentId,merchantStatus)) {
				return ReturnInfoUtils.errorInfo("商户基本信息保存失败,服务器繁忙!");
			}
			Map<String, Object> reRecordMap = createMerchantRecord(datasMap.get("recordInfoPack") + "", merchantId,
					managerName);
			if (!"1".equals(reRecordMap.get(BaseCode.STATUS.toString()))) {
				return reRecordMap;
			}
			/*
			 * Map<String, Object> reAppkeyMap =
			 * appkeyService.createRecord("银盟跨境商城-授权网关", "YM", phone,
			 * merchantId, merchantName); if
			 * (!"1".equals(reAppkeyMap.get(BaseCode.STATUS.toString()))) {
			 * return reAppkeyMap; }
			 */
		}
		return ReturnInfoUtils.successDataInfo(merchantId);
	}

	/**
	 * 保存商户备案信息
	 * 
	 * @param recordInfoPack
	 *            商户备案信息
	 * @param merchantId
	 *            商户Id
	 * @param managerName
	 *            管理员名称
	 * @return Map
	 */
	private Map<String, Object> createMerchantRecord(String recordInfoPack, String merchantId, String managerName) {
		if (StringEmptyUtils.isEmpty(recordInfoPack)) {
			return ReturnInfoUtils.errorInfo("商户备案信息错误,请核对信息!");
		}
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(recordInfoPack);
		} catch (Exception e) {
			e.getStackTrace();
			return ReturnInfoUtils.errorInfo("商户备案信息错误,请核对信息！");
		}
		for (int i = 0; i < jsonList.size(); i++) {
			MerchantRecordInfo recordInfo = new MerchantRecordInfo();
			Map<String, Object> recordMap = (Map<String, Object>) jsonList.get(0);
			String ebpEntNo = recordMap.get(EBPENTNO) + "";
			String ebpEntName = recordMap.get(EBPENTNAME) + "";
			String ebEntNo = recordMap.get(EBENTNO) + "";
			String ebEntName = recordMap.get(EBENTNAME) + "";
			recordInfo.setMerchantId(merchantId);
			// 1-广州电子口岸2-南沙智检
			int eport = Integer.parseInt(recordMap.get(EPORT) + "");
			recordInfo.setCustomsPort(eport);
			recordInfo.setCustomsPortName(recordMap.get(PORTNAME) + "");
			recordInfo.setEbpEntNo(ebpEntNo);
			recordInfo.setEbpEntName(ebpEntName);
			recordInfo.setEbEntNo(ebEntNo);
			recordInfo.setEbEntName(ebEntName);
			recordInfo.setCreateBy(managerName);
			recordInfo.setCreateDate(new Date());
			// 删除标识:0-未删除,1-已删除
			recordInfo.setDeleteFlag(0);
			// 保存商户对应的电商平台名称(及编码)
			if (!addMerchantRecordInfo(recordInfo, "2")) {
				return ReturnInfoUtils.errorInfo(recordMap.get(PORTNAME) + "商户备案信息错误,保存失败！");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 创建商户基本信息
	 * 
	 * @param phone
	 *            手机号码
	 * @param managerName
	 *            管理员名称
	 * @param loginPassword
	 *            登陆密码
	 * @param merchantName
	 *            商户名称
	 * @param merchantId
	 *            商户Id
	 * @param type
	 *            商户类型 1-银盟自营、2-第三方商城平台
	 * @param agentId
	 *            代理商Id
	 * @param merchantStatus 
	 * @return boolean
	 */
	private boolean createMerchantInfo(int type, String merchantId, String merchantName, String loginPassword,
			String managerName, String phone, String agentId, String merchantStatus) {
		MD5 md5 = new MD5();
		Merchant merchant = new Merchant();
		merchant.setMerchantId(merchantId);
		// merchant.setMerchantCusNo("YM_" + merchantId);
		merchant.setMerchantName(merchantName);
		merchant.setLoginPassword(md5.getMD5ofStr(loginPassword));
		// 商户状态：1-启用，2-禁用，3-审核
		if(StringEmptyUtils.isNotEmpty(merchantStatus)){
			merchant.setMerchantStatus(merchantStatus);
		}else{
			merchant.setMerchantStatus("3");
		}
		merchant.setCreateBy(managerName);
		merchant.setCreateDate(new Date());
		merchant.setDeleteFlag(0);// 删除标识:0-未删除,1-已删除
		if(StringEmptyUtils.isNotEmpty(agentId)){
			Map<String,Object> reAgentMap = agentServiceImpl.getAgentInfo(agentId);
			if(!"1".equals(reAgentMap.get(BaseCode.STATUS.toString()))){
				System.out.println("---->>"+reAgentMap.get(BaseCode.MSG.toString()));
				return false;
			}
			AgentBaseContent agent = (AgentBaseContent) reAgentMap.get(BaseCode.STATUS.toString());
			merchant.setAgentParentId(agent.getAgentId());
			merchant.setAgentParentName(agent.getAgentName());
		}else{
			merchant.setAgentParentId("prxoy_00001");
			merchant.setAgentParentName("银盟");
		}
		merchant.setMerchantPhone(phone);
		// 标识
		merchant.setThirdPartyFlag(type);
		MerchantDetail merchantDetail = new MerchantDetail();
		merchantDetail.setMerchantId(merchantId);
		merchantDetail.setCreateBy("system");
		merchantDetail.setCreateDate(new Date());
		return merchantDao.add(merchant) && merchantDao.add(merchantDetail);
	}

	@Override
	public List<Object> findMerchantBy(String account) {
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("merchantName", account);
		return merchantDao.findByProperty(Merchant.class, paramsMap, 0, 0);
	}

	@Override
	public Map<String, Object> editBusinessInfo(String merchantId, List<Object> imglist, int[] array,
			String customsregistrationCode, String organizationCode, String checktheRegistrationCode,
			String merchantName) {
		Date data = new Date();
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		List<MerchantDetail> reList = merchantDao.findByProperty(MerchantDetail.class, params, 1, 1);
		if (reList != null && !reList.isEmpty()) {
			MerchantDetail merchantDetail = reList.get(0);
			for (int i = 0; i < array.length; i++) {
				int picIndex = array[i];
				switch (picIndex) {
				case 1:
					merchantDetail.setMerchantBusinessLicenseLink(imglist.get(i) + "");
					break;
				case 2:
					merchantDetail.setMerchantCustomsregistrationCodeLink(imglist.get(i) + "");
					break;
				case 3:
					merchantDetail.setMerchantOrganizationCodeLink(imglist.get(i) + "");
					break;
				case 4:
					merchantDetail.setMerchantChecktheRegistrationCodeLink(imglist.get(i) + "");
					break;
				case 5:
					merchantDetail.setMerchantTaxRegistrationCertificateLink(imglist.get(i) + "");
					break;
				case 6:
					merchantDetail.setMerchantSpecificIndustryLicenseLink(imglist.get(i) + "");
					break;
				default:
					logger.info("没有要更新的图片!");
					break;
				}
			}

			merchantDetail.setMerchantCustomsregistrationCode(customsregistrationCode);
			merchantDetail.setMerchantOrganizationCode(organizationCode);
			merchantDetail.setMerchantChecktheRegistrationCode(checktheRegistrationCode);
			// 将商户状态修改为审核状态
			// mInfo.setMerchantStatus("3");
			merchantDetail.setUpdateDate(data);
			merchantDetail.setUpdateBy(merchantName);
			// 更新实体
			if (merchantDao.update(merchantDetail)) {
				return ReturnInfoUtils.successInfo();
			}
			return ReturnInfoUtils.errorInfo("更新商户详情失败,服务器繁忙!");
		} else {
			return ReturnInfoUtils.errorInfo("查询商户详细信息失败,服务器繁忙!");
		}

	}

	@Override
	public Map<String, Object> updateLoginPassword(Merchant merchantInfo, String newLoginPassword) {
		Map<String, Object> reMap = new HashMap<>();
		MD5 md5 = new MD5();
		merchantInfo.setLoginPassword(md5.getMD5ofStr(newLoginPassword));
		boolean flag = merchantDao.update(merchantInfo);
		if (flag) {
			reMap.put(BaseCode.STATUS.getBaseCode(), 1);
			reMap.put(BaseCode.MSG.getBaseCode(), "修改成功");
		} else {
			reMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.UNKNOWN.getStatus());
			reMap.put(BaseCode.MSG.getBaseCode(), StatusCode.UNKNOWN.getMsg());
		}
		return reMap;
	}

	@Override
	public Map<String, Object> getMerchantRecordInfo(String merchantId) {
		Map<String, Object> params = new HashMap<>();
		// key=表中列名,value=查询参数
		params.put("merchantId", merchantId);
		// 根据商户ID查询商户备案信息数据
		List<Object> reList = merchantDao.findByProperty(MerchantRecordInfo.class, params, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			params.clear();
			params.put(BaseCode.DATAS.toString(), reList);
			params.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			params.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			params.clear();
			params.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			params.put(BaseCode.MSG.toString(), "查询失败,服务器繁忙！");
		}
		return params;
	}

	@Override
	public Map<String, Object> publicMerchantInfo(String merchantId) {
		if (StringEmptyUtils.isNotEmpty(merchantId)) {
			Map<String, Object> params = new HashMap<>();
			// key=表中列名,value=查询参数
			params.put("merchantId", merchantId);
			// 根据商户ID查询商户备案信息数据
			List<Merchant> reList = merchantDao.findByProperty(Merchant.class, params, 0, 0);
			if (reList != null && !reList.isEmpty()) {
				Merchant merchant = reList.get(0);
				merchant.setLoginPassword("");
				return ReturnInfoUtils.successDataInfo(merchant, 0);
			} else {
				return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
			}
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	@Override
	public Map<String, Object> getMerchantAuthority(String merchantId) {
		if (StringEmptyUtils.isEmpty(merchantId)) {
			return ReturnInfoUtils.errorInfo("商户Id不能为空");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("userId", merchantId);
		List<AuthorityUser> reList = merchantDao.findByProperty(AuthorityUser.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询商户权限信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			List<String> list = new ArrayList<>();
			for (AuthorityUser authorityRole : reList) {
				list.add(authorityRole.getAuthorityCode());
			}
			return ReturnInfoUtils.successDataInfo(list);
		} else {
			return ReturnInfoUtils.errorInfo("未找到该商户权限信息!");
		}
	}

}
