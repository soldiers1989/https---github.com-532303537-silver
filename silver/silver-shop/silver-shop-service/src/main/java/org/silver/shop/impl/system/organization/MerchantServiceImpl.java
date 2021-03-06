package org.silver.shop.impl.system.organization;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.AgentService;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.api.system.organization.MerchantService;
import org.silver.shop.dao.system.organization.MerchantDao;
import org.silver.shop.model.system.AuthorityUser;
import org.silver.shop.model.system.organization.AgentBaseContent;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.organization.MerchantDetail;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.model.system.tenant.MerchantRelatedMemberContent;
import org.silver.shop.util.IdUtils;
import org.silver.shop.util.InquireHelperService;
import org.silver.shop.util.WalletUtils;
import org.silver.util.EmailUtils;
import org.silver.util.MD5;
import org.silver.util.PhoneUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.common.json.JSONObject;
import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONArray;

@Service(interfaceClass = MerchantService.class)
public class MerchantServiceImpl implements MerchantService {

	@Autowired
	private AgentService agentService;
	@Autowired
	private IdUtils<Merchant> idUtils;
	@Autowired
	private MemberService memberService;
	@Autowired
	private WalletUtils walletUtils;
	@Autowired
	private InquireHelperService inquireHelperService;

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
	/**
	 * 登录密码的组成至少要包括大小写字母、数字及标点符号的其中两项、且长度要在6-20位之间
	 */
	private static final String LOGIN_PASSWORD_REGEX = "^(?![A-Za-z]+$)(?!\\d+$)(?![\\W_]+$)\\S{6,20}$";

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
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("商户注册参数不能为空!");
		}
		Date dateTime = new Date();
		int type = Integer.parseInt(datasMap.get("type") + "");
		String loginPassword = datasMap.get("loginPassword") + "";
		if (!loginPassword.matches(LOGIN_PASSWORD_REGEX)) {
			return ReturnInfoUtils.errorInfo("密码至少要由包括大小写字母、数字、特殊符号的其中两项，且长度要在6-20位之间！");
		}
		Map<String, Object> reIdMap = idUtils.createId(Merchant.class, "MerchantId_");
		if (!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))) {
			return reIdMap;
		}
		String merchantId = reIdMap.get(BaseCode.DATAS.toString()) + "";
		String merchantName = datasMap.get("merchantName") + "";
		String managerName = datasMap.get("managerName") + "";
		String phone = datasMap.get("phone") + "";
		if (!PhoneUtils.isPhone(phone)) {
			return ReturnInfoUtils.errorInfo("手机号码错误！");
		}
		String agentId = datasMap.get("agentId") + "";
		String merchantStatus = datasMap.get("merchantStatus") + "";
		if (type == 1) {// 1-银盟商户注册
			if (!createMerchantInfo(type, merchantId, merchantName, loginPassword, managerName, phone, agentId,
					merchantStatus)) {
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
			if (!createMerchantInfo(type, merchantId, merchantName, loginPassword, managerName, phone, agentId,
					merchantStatus)) {
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
		Map<String, Object> reWalletMap = walletUtils.checkWallet(1, merchantId, merchantName);
		if ("1".equals(reWalletMap.get(BaseCode.DATAS.toString()))) {
			return reWalletMap;
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
		if (StringEmptyUtils.isNotEmpty(merchantStatus)) {
			merchant.setMerchantStatus(merchantStatus);
		} else {
			merchant.setMerchantStatus("3");
		}
		merchant.setCreateBy(managerName);
		merchant.setCreateDate(new Date());
		merchant.setDeleteFlag(0);// 删除标识:0-未删除,1-已删除
		if (StringEmptyUtils.isNotEmpty(agentId)) {
			Map<String, Object> reAgentMap = agentService.getAgentInfo(agentId);
			if (!"1".equals(reAgentMap.get(BaseCode.STATUS.toString()))) {
				return false;
			}
			AgentBaseContent agent = (AgentBaseContent) reAgentMap.get(BaseCode.STATUS.toString());
			merchant.setAgentParentId(agent.getAgentId());
			merchant.setAgentParentName(agent.getAgentName());
		} else {
			merchant.setAgentParentId("AgentId_00002");
			merchant.setAgentParentName("广州银盟");
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
			merchantDetail.setUpdateDate(new Date());
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
		if (merchantInfo == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		MD5 md5 = new MD5();
		if (!newLoginPassword.matches(LOGIN_PASSWORD_REGEX)) {
			return ReturnInfoUtils.errorInfo("密码至少要由包括大小写字母、数字、特殊符号的其中两项，且长度要在6-20位之间！");
		}
		merchantInfo.setLoginPassword(md5.getMD5ofStr(newLoginPassword));
		merchantInfo.setUpdateDate(new Date());
		merchantInfo.setUpdateBy(merchantInfo.getMerchantName());
		if (!merchantDao.update(merchantInfo)) {
			return ReturnInfoUtils.errorInfo("修改失败，服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();

	}

	@Override
	public Map<String, Object> getMerchantRecordInfo(String merchantId) {
		if (StringEmptyUtils.isEmpty(merchantId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能空！");
		}
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("merchantId", merchantId);
		List<Object> reList = merchantDao.findByProperty(MerchantRecordInfo.class, paramMap, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
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

	@Override
	public Map<String, Object> setRelatedMember(String merchantId, String merchantName, String accountName,
			String loginPassword, String payPassword) {
		if (StringEmptyUtils.isEmpty(merchantId) || StringEmptyUtils.isEmpty(accountName)
				|| StringEmptyUtils.isEmpty(loginPassword) || StringEmptyUtils.isEmpty(payPassword)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		List<Member> reList = memberService.findMemberBy(accountName);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("账号不存在！", "500");
		}
		Member member = reList.get(0);
		MD5 md5 = new MD5();
		if (!member.getLoginPass().equals(md5.getMD5ofStr(loginPassword))) {
			return ReturnInfoUtils.errorInfo("登录密码错误！", "501");
		}
		String paymentPassword = member.getPaymentPassword();
		if (StringEmptyUtils.isEmpty(paymentPassword)) {
			return ReturnInfoUtils.errorInfo("用户还未设置交易密码，请先设置交易密码！");
		}
		if (!paymentPassword.equals(md5.getMD5ofStr(payPassword))) {
			return ReturnInfoUtils.errorInfo("交易密码错误！", "502");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", member.getMemberId());
		params.put("merchantId", merchantId);
		List<MerchantRelatedMemberContent> reList2 = merchantDao.findByProperty(MerchantRelatedMemberContent.class,
				params, 0, 0);
		if (reList2 == null) {
			return ReturnInfoUtils.errorInfo("查询关联用户失败,服务器繁忙!");
		} else if (!reList2.isEmpty()) {
			return ReturnInfoUtils.errorInfo("该用户已关联该商户,请勿重复操作!");
		} else {
			return saveMerchantRelatedMember(member.getMemberId(), merchantId, merchantName);
		}
	}

	/**
	 * 保存商户代付会员信息
	 * 
	 * @param memberId
	 *            用户id
	 * @param merchantId
	 *            商户id
	 * @param createBy
	 *            创建人
	 * @return Map
	 */
	private Map<String, Object> saveMerchantRelatedMember(String memberId, String merchantId, String createBy) {
		MerchantRelatedMemberContent content = new MerchantRelatedMemberContent();
		content.setMemberId(memberId);
		content.setMerchantId(merchantId);
		content.setCreateDate(new Date());
		content.setCreateBy(createBy);
		if (!merchantDao.add(content)) {
			return ReturnInfoUtils.errorInfo("保存失败,服务器繁忙!!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> getRelatedMemberFunds(String merchantId, int page, int size) {
		if (StringEmptyUtils.isEmpty(merchantId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Table t = merchantDao.getRelatedMemberFunds(merchantId, null, page, size);
		Table count = merchantDao.getRelatedMemberFunds(merchantId, null, 0, 0);
		if (t == null) {
			return ReturnInfoUtils.warnInfo();
		} else if (!t.getRows().isEmpty()) {
			List<Object> newList = new ArrayList<>();
			JSONArray jsonArr = JSONArray.fromObject(Transform.tableToJson(t).get("rows"));
			for (int i = 0; i < jsonArr.size(); i++) {
				net.sf.json.JSONObject item = new net.sf.json.JSONObject();
				net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(jsonArr.get(i));
				Iterator iterator = json.keys();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					String value = json.get(key) + "";
					System.out.println("-----key>>"+key+";==value="+value);
					if (StringEmptyUtils.isNotEmpty(value)) {
						item.put(key, StringUtil.replace(value));
					}
				}
				newList.add(item);
			}
			return ReturnInfoUtils.successDataInfo(newList, count.getRows().size());
		} else {
			return ReturnInfoUtils.noDatas();
		}
	}

	@Override
	public Map<String, Object> getBusinessInfo(String merchantId) {
		if (StringEmptyUtils.isEmpty(merchantId)) {
			return ReturnInfoUtils.errorInfo("商户id不能为空！");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		List<MerchantDetail> reList = merchantDao.findByProperty(MerchantDetail.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询商户业务信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList);
		} else {
			return ReturnInfoUtils.errorInfo("未找到该商户业务信息!");
		}
	}

	@Override
	public Map<String, Object> updateBaseInfo(String merchantId, String merchantName, Map<String, Object> datasMap) {
		if (StringEmptyUtils.isEmpty(merchantId) || datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空！");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		List<Merchant> reList = merchantDao.findByProperty(Merchant.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询商户业务信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			Merchant merchant = reList.get(0);
			String qq = datasMap.get("qq") + "";
			if (StringEmptyUtils.isNotEmpty(qq)) {
				merchant.setMerchantQQ(qq);
			}
			String email = datasMap.get("email") + "";
			if (StringEmptyUtils.isNotEmpty(email)) {
				if (EmailUtils.checkEmail(email)) {
					merchant.setMerchantEmail(email);
				} else {
					return ReturnInfoUtils.errorInfo("邮箱错误！");
				}
			}
			// 公司简称
			String companyName = datasMap.get("companyName") + "";
			if (StringEmptyUtils.isEmpty(companyName)) {
				return ReturnInfoUtils.errorInfo("简称不能为空！");
			}
			merchant.setCompanyName(companyName);
			if (!merchantDao.update(merchant)) {
				return ReturnInfoUtils.errorInfo("修改失败，服务器繁忙！!");
			}
			return ReturnInfoUtils.successDataInfo(merchant);
		} else {
			return ReturnInfoUtils.errorInfo("未找到该商户业务信息!");
		}
	}

	@Override
	public Map<String, Object> getMerchantInfo(Map<String, Object> params, int page, int size) {
		return inquireHelperService.findInfo(Merchant.class, params, page, size);
	}

	@Override
	public Map<String, Object> resetLoginPwd(String merchantId, String newPassword) {
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		Map<String, Object> reMerchantMap = inquireHelperService.findInfo(Merchant.class, params, 1, 1);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		if (!newPassword.matches(LOGIN_PASSWORD_REGEX)) {
			return ReturnInfoUtils.errorInfo("密码至少要由包括大小写字母、数字、特殊符号的其中两项，且长度要在6-20位之间！");
		}
		MD5 md5 = new MD5();
		merchant.setLoginPassword(md5.getMD5ofStr(newPassword));
		return updateMerchantInfo(merchant);
	}

	/**
	 * 更新商户基本信息
	 * 
	 * @param entity
	 * @return
	 */
	private Map<String, Object> updateMerchantInfo(Merchant entity) {
		if (entity == null) {
			return ReturnInfoUtils.errorInfo("更新参数不能为null");
		}
		entity.setUpdateDate(new Date());
		if (merchantDao.update(entity)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("修改失败，服务器繁忙！!");
	}
}
