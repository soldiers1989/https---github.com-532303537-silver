package org.silver.shop.impl.system.organization;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.MerchantService;
import org.silver.shop.dao.system.organization.MerchantDao;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.RecordInfo;
import org.silver.util.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = MerchantService.class)
public class MerchantServiceImpl implements MerchantService {

	private Logger logger = LoggerFactory.getLogger(getClass());
	// 口岸
	private static final String EPORT = "eport";
	// 电商企业编号
	private static final String EBENTNO = "ebEntNo";
	// 电商企业名称
	private static final String EBENTNAME = "ebEntName";
	// 电商平台企业编号
	private static final String EBPENTNO = "ebpEntNo";
	// 电商平台名称
	private static final String EBPENTNAME = "EBPEntName";
	
	//
	static List<Map> list = null;
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
		record1.put(EPORT, 0);// 1-广州电子口岸(目前只支持BC业务)
		record1.put(EBENTNO, ebEntNo);
		record1.put(EBENTNAME, ebEntName);
		record1.put(EBPENTNO, ebEntNo);
		record1.put(EBPENTNAME, ebEntName);
		record2.put(EPORT, 1);// 2-南沙智检(支持BBC业务)
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
		Map<String, Object> statusMap = new HashMap<>();
		// 扫描获取数据库表中的商户自增长ID
		Long merchantCount = merchantDao.findLastId();
		// 得出的总数上+1
		Long count = merchantCount + 1;
		if (count < 1) {// 判断数据库查询出数据如果小于1,则中断程序,告诉异常
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		String merchantId = count + "";
		// 当商户ID没有5位数时,前面补0
		while (merchantId.length() < 5) {
			merchantId = "0" + merchantId;
		}
		merchantId = "MerchantId_" + merchantId;
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.DATAS.getBaseCode(), merchantId);
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public boolean addMerchantRecordInfo(RecordInfo entity, String type) {
		boolean flag = false;
		if (type.equals("1")) {// 银盟自己商户备案信息插入
			for (int x = 0; x < list.size(); x++) {
				Map<String, Object> listMap = list.get(x);
				entity.setEport((int) listMap.get(EPORT));
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
	public Map<String, Object> merchantRegister(String merchantId, String account, String loginPassword,
			String merchantIdCard, String merchantIdCardName, String recordInfoPack, String type) {
		Map<String, Object> statusMap = new HashMap<>();
		Date dateTime = new Date();
		Merchant merchant = new Merchant();
		RecordInfo recordInfo = new RecordInfo();
		MD5 md5 = new MD5();
		boolean merchantFlag = false;
		boolean recordFlag = false;
		if (type.equals("1")) {// 1-银盟商户注册
			merchant.setMerchantId(merchantId);
			merchant.setMerchantCusNo("YM_" + merchantId);
			merchant.setMerchantName(account);
			merchant.setLoginPassword(md5.getMD5ofStr(loginPassword));
			merchant.setMerchantIdCard(merchantIdCard);
			merchant.setMerchantIdCardName(merchantIdCardName);
			merchant.setMerchantStatus("3");// 商户状态：1-启用，2-禁用，3-审核
			merchant.setCreateBy(account);
			merchant.setCreateDate(dateTime);
			merchant.setDeleteFlag(0);// 删除标识:0-未删除,1-已删除
			recordInfo.setMerchantId(merchantId);
			recordInfo.setCreateBy(account);
			recordInfo.setCreateDate(dateTime);
			recordInfo.setDeleteFlag(0);// 删除标识:0-未删除,1-已删除
			// 商戶基本信息实例化
			merchantFlag = merchantDao.add(merchant);
			if (merchantFlag) {
				// 保存商户对应的电商平台名称(及编码)
				recordFlag = addMerchantRecordInfo(recordInfo, "1");
				if (recordFlag) {// 数据库添加完成后,返回成功信息
					statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
					statusMap.put(BaseCode.MSG.getBaseCode(), "注册成功！");
					return statusMap;
				}
			}
		} else {// 2-第三方商户注册
			merchant.setMerchantId(merchantId);
			merchant.setMerchantCusNo("TP_" + merchantId);
			merchant.setMerchantName(account);
			merchant.setLoginPassword(md5.getMD5ofStr(loginPassword));
			merchant.setMerchantIdCard(merchantIdCard);
			merchant.setMerchantIdCardName(merchantIdCardName);
			merchant.setMerchantStatus("3");// 商户状态：1-启用，2-禁用，3-审核
			merchant.setCreateBy(account);
			merchant.setCreateDate(dateTime);
			merchant.setDeleteFlag(0);// 删除标识:0-未删除,1-已删除
			// 商戶基本信息实例化
			merchantFlag = merchantDao.add(merchant);
			if (merchantFlag) {
				JSONArray jsonList = null;
				try {
					jsonList = JSONArray.fromObject(recordInfoPack);
				} catch (Exception e) {
					logger.debug("商户备案参数不正确！");
					e.getStackTrace();
					statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
					statusMap.put(BaseCode.MSG.getBaseCode(), "注册失败,请检查商户备案信息是否正确！");
					return statusMap;
				}
				// 取出前台打包好的商户备案信息
				for (int x = 0; x < jsonList.size(); x++) {
					Map<String, Object> packMap = (Map) jsonList.get(x);
					String eport = packMap.get(EPORT) + "";
					String ebEntNo = packMap.get(EBENTNO) + "";
					String ebEntName = packMap.get(EBENTNAME) + "";
					String ebpEntNo = packMap.get(EBPENTNO) + "";
					String ebpEntName = packMap.get(EBPENTNAME) + "";
					recordInfo.setMerchantId(merchantId);
					recordInfo.setEport(Integer.valueOf(eport));// 1-广州电子口岸
																// 2-南沙智检
					recordInfo.setEbEntNo(ebEntNo);
					recordInfo.setEbEntName(ebEntName);
					recordInfo.setEbpEntNo(ebpEntNo);
					recordInfo.setEbpEntName(ebpEntName);
					recordInfo.setCreateBy(account);
					recordInfo.setCreateDate(dateTime);
					recordInfo.setDeleteFlag(0);// 删除标识:0-未删除,1-已删除
					// 保存商户对应的电商平台名称(及编码)
					recordFlag = addMerchantRecordInfo(recordInfo, "2");
					if (!recordFlag) {
						if (Integer.valueOf(eport) == 0) {
							eport = "电子口岸";
						} else if (Integer.valueOf(eport) == 1) {
							eport = "南沙智检";
						}
						statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
						statusMap.put(BaseCode.MSG.getBaseCode(), eport + "商户备案信息错误,保存失败！");
						return statusMap;
					}
				}
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), "注册成功！");
				return statusMap;
			}
		}
		return statusMap;
	}

	@Override
	public List<Object> findMerchantBy(String account) {
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("merchantName", account);
		
		return merchantDao.findByProperty(Merchant.class, paramsMap, 0, 0);
	}

	@Override
	public Map<String, Object> editBusinessInfo(Object merchantInfo, List<Object> imglist, int[] array,
			String customsregistrationCode, String organizationCode, String checktheRegistrationCode) {
		Date data = new Date();
		Merchant mInfo = (Merchant) merchantInfo;
		Map<String, Object> entityMap = new HashMap<>();
		boolean flag = false;
		for (int i = 0; i < array.length; i++) {
			int picIndex = array[i];
			switch (picIndex) {
			case 1:
				mInfo.setMerchantBusinessLicenseLink(imglist.get(i) + "");
				break;
			case 2:
				mInfo.setMerchantCustomsregistrationCodeLink(imglist.get(i) + "");
				break;
			case 3:
				mInfo.setMerchantOrganizationCodeLink(imglist.get(i) + "");
				break;
			case 4:
				mInfo.setMerchantChecktheRegistrationCodeLink(imglist.get(i) + "");
				break;
			case 5:
				mInfo.setMerchantTaxRegistrationCertificateLink(imglist.get(i) + "");
				break;
			case 6:
				mInfo.setMerchantSpecificIndustryLicenseLink(imglist.get(i) + "");
				break;
			default:
				logger.info("没有要更新的图片!");
				break;
			}
		}

		mInfo.setMerchantCustomsregistrationCode(customsregistrationCode);
		mInfo.setMerchantOrganizationCode(organizationCode);
		mInfo.setMerchantChecktheRegistrationCode(checktheRegistrationCode);
		// 将商户状态修改为审核状态
		mInfo.setMerchantStatus("3");
		mInfo.setUpdateDate(data);
		mInfo.setUpdateBy(mInfo.getUpdateBy());
		// 更新实体
		flag = merchantDao.update(mInfo);
		entityMap.put(BaseCode.STATUS.getBaseCode(), flag);
		entityMap.put(BaseCode.DATAS.getBaseCode(), mInfo);
		return entityMap;
	}

	@Override
	public Map<String,Object> updateLoginPassword(Merchant merchantInfo,String newLoginPassword) {
		Map<String,Object> reMap = new HashMap<>();
		MD5 md5 = new MD5();
		merchantInfo.setLoginPassword(md5.getMD5ofStr(newLoginPassword));
		boolean flag = merchantDao.update(merchantInfo);
		if(flag){
			reMap.put(BaseCode.STATUS.getBaseCode(), 1);
			reMap.put(BaseCode.MSG.getBaseCode(), "修改成功");
		}else{
			reMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.UNKNOWN.getStatus());
			reMap.put(BaseCode.MSG.getBaseCode(), StatusCode.UNKNOWN.getMsg());
		}
		return reMap;
	}
}
