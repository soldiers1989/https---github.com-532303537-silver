package org.silver.shop.impl.system.organization;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
	static List<Map> list = null;
	static {
		// 初始化银盟自己商户备案信息
		list = new ArrayList<>();
		Map<String, Object> record1 = new HashMap<>();
		Map<String, Object> record2 = new HashMap<>();
		record1.put("eport", 0);// 1-广州电子口岸(目前只支持BC业务)
		record1.put("ebEntNo", "C010000000537118");
		record1.put("ebEntName", "广州银盟信息科技有限公司");
		record1.put("ebpEntNo", "C010000000537118");
		record1.put("ebpEntName", "广州银盟信息科技有限公司");
		record2.put("eport", 1);// 2-南沙智检(支持BBC业务)
		record2.put("ebEntNo", "1509007917");
		record2.put("ebEntName", "广州银盟信息科技有限公司");
		record2.put("ebpEntNo", "1509007917");
		record2.put("ebpEntName", "广州银盟信息科技有限公司");
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
			statusMap.put("status", StatusCode.WARN.getStatus());
			statusMap.put("msg", StatusCode.WARN.getMsg());
			return statusMap;
		}
		String merchantId = count + "";
		// 当商户ID没有5位数时,前面补0
		while (merchantId.length() < 5) {
			merchantId = "0" + merchantId;
		}
		merchantId = "MerchantId_" + merchantId;
		statusMap.put("status", StatusCode.SUCCESS.getStatus());
		statusMap.put("msg", StatusCode.SUCCESS.getMsg());
		statusMap.put("datas", merchantId);
		return statusMap;
	}

	@Override
	public boolean addMerchantRecordInfo(RecordInfo entity, String type) {
		boolean flag = false;
		if (type.equals("1")) {// 银盟自己商户备案信息插入
			for (int x = 0; x < list.size(); x++) {
				Map<String, Object> listMap = list.get(x);
				entity.setEport((int) listMap.get("eport"));
				entity.setEbEntNo(listMap.get("ebEntNo") + "");
				entity.setEbEntName(listMap.get("ebEntName") + "");
				entity.setEbpEntNo(listMap.get("ebpEntNo") + "");
				entity.setEbpEntName(listMap.get("ebpEntName") + "");
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
			merchant.setMerchantIdcard(merchantIdCard);
			merchant.setMerchantIdCardName(merchantIdCardName);
			merchant.setMerchantStatus("3");// 商户状态：1-启用，2-禁用，3-审核
			merchant.setCreateBy(account);
			merchant.setCreateDate(dateTime);
			merchant.setDeletFlag(0);// 删除标识:0-未删除,1-已删除
			recordInfo.setMerchantId(merchantId);
			recordInfo.setCreateBy(account);
			recordInfo.setCreateDate(dateTime);
			recordInfo.setDeletFlag(0);// 删除标识:0-未删除,1-已删除
			// 商戶基本信息实例化
			merchantFlag = merchantDao.add(merchant);
			if (merchantFlag) {
				// 保存商户对应的电商平台名称(及编码)
				recordFlag = addMerchantRecordInfo(recordInfo, "1");
				if (recordFlag) {// 数据库添加完成后,返回成功信息
					statusMap.put("status", StatusCode.SUCCESS.getStatus());
					statusMap.put("msg", "注册成功！");
					return statusMap;
				}
			}
		} else {// 2-第三方商户注册
			merchant.setMerchantId(merchantId);
			merchant.setMerchantCusNo("TP_" + merchantId);
			merchant.setMerchantName(account);
			merchant.setLoginPassword(md5.getMD5ofStr(loginPassword));
			merchant.setMerchantIdcard(merchantIdCard);
			merchant.setMerchantIdCardName(merchantIdCardName);
			merchant.setMerchantStatus("3");// 商户状态：1-启用，2-禁用，3-审核
			merchant.setCreateBy(account);
			merchant.setCreateDate(dateTime);
			merchant.setDeletFlag(0);// 删除标识:0-未删除,1-已删除
			// 商戶基本信息实例化
			merchantFlag = merchantDao.add(merchant);
			if (merchantFlag) {
				JSONArray jsonList = null;
				try {
					jsonList = JSONArray.fromObject(recordInfoPack);
				} catch (Exception e) {
					logger.debug("商户备案参数不正确！");
					e.getStackTrace();
					statusMap.put("status", StatusCode.NOTICE.getStatus());
					statusMap.put("msg", "注册失败,请检查商户备案信息是否正确！");
					return statusMap;
				}
				Map<String, Object> packMap = null;
				// 取出前台打包好的商户备案信息
				for (int x = 0; x < jsonList.size(); x++) {
					packMap = new HashMap<>();
					packMap = (Map) jsonList.get(x);
					String eport = packMap.get("eport") + "";
					String ebEntNo = packMap.get("ebEntNo") + "";
					String ebEntName = packMap.get("ebEntName") + "";
					String ebpEntNo = packMap.get("ebpEntNo") + "";
					String ebpEntName = packMap.get("ebpEntName") + "";
					recordInfo.setMerchantId(merchantId);
					recordInfo.setEport(Integer.valueOf(eport));// 1-广州电子口岸
																// 2-南沙智检
					recordInfo.setEbEntNo(ebEntNo);
					recordInfo.setEbEntName(ebEntName);
					recordInfo.setEbpEntNo(ebpEntNo);
					recordInfo.setEbpEntName(ebpEntName);
					recordInfo.setCreateBy(account);
					recordInfo.setCreateDate(dateTime);
					recordInfo.setDeletFlag(0);// 删除标识:0-未删除,1-已删除
					// 保存商户对应的电商平台名称(及编码)
					recordFlag = addMerchantRecordInfo(recordInfo, "2");
					if (!recordFlag) {
						if (Integer.valueOf(eport) == 0) {
							eport = "电子口岸";
						} else if (Integer.valueOf(eport) == 1) {
							eport = "南沙智检";
						}
						statusMap.put("status", StatusCode.NOTICE.getStatus());
						statusMap.put("msg", eport + "商户备案信息错误,保存失败！");
						return statusMap;
					}
				}
				statusMap.put("status", StatusCode.SUCCESS.getStatus());
				statusMap.put("msg", "注册成功！");
				return statusMap;
			}
		}
		return statusMap;
	}

	@Override
	public Map<String, Object> findMerchantBy(String account) {
		Map<String, Object> datasMap = new HashMap<>();
		datasMap.put("merchantName", account);
		List reList = merchantDao.findByProperty(Merchant.class, datasMap, 0, 0);
		if (!reList.isEmpty()) {
				datasMap.put("status", 1);
				datasMap.put("datas", reList);
				return datasMap;
		}
		datasMap.put("status", -1);
		return datasMap;
	}

}
