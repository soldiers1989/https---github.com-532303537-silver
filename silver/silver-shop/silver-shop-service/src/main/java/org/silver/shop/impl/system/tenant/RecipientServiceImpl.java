package org.silver.shop.impl.system.tenant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.tenant.RecipientService;
import org.silver.shop.dao.system.tenant.RecipientDao;
import org.silver.shop.model.system.tenant.RecipientContent;
import org.silver.util.CheckDatasUtil;
import org.silver.util.IdcardValidator;
import org.silver.util.PhoneUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = RecipientService.class)
public class RecipientServiceImpl implements RecipientService {

	@Autowired
	private RecipientDao recipientDao;

	@Override
	public Map<String, Object> addRecipientInfo(String memberId, String memberName, String recipientInfo) {
		JSONArray jsonList = null;
		Date date = new Date();
		try {
			jsonList = JSONArray.fromObject(recipientInfo);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("用户收货地址信息格式错误!");
		}
		List<RecipientContent> cacheList = new ArrayList<>();
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> reMap = checkRecipient(jsonList);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
			Map<String, Object> recipientMap = (Map<String, Object>) jsonList.get(0);
			RecipientContent recipient = new RecipientContent();
			recipient.setMemberId(memberId);
			recipient.setMemberName(memberName);
			String recipientName = recipientMap.get("recipientName") + "";
			if (!StringUtil.isContainChinese(recipientName)) {
				return ReturnInfoUtils.errorInfo("收货人姓名错误,请重新输入");
			}
			recipient.setRecipientName(recipientName);
			String recipientCardId = recipientMap.get("recipientCardId") + "";
			if (!IdcardValidator.validate18Idcard(recipientCardId)) {
				return ReturnInfoUtils.errorInfo("收货人身份证号码错误,请重新输入!");
			}
			recipient.setRecipientCardId(recipientCardId);
			String recipientTel = recipientMap.get("recipientTel") + "";
			if (!PhoneUtils.isPhone(recipientTel)) {
				return ReturnInfoUtils.errorInfo("手机号码不正确,请重新输入!");
			}
			recipient.setRecipientTel(recipientTel);
			recipient.setRecipientCountryName("中国");
			recipient.setRecipientCountryCode("142");
			recipient.setRecProvincesName(recipientMap.get("recProvincesName") + "");
			recipient.setRecProvincesCode(recipientMap.get("recProvincesCode") + "");
			recipient.setRecCityName(recipientMap.get("recCityName") + "");
			recipient.setRecCityCode(recipientMap.get("recCityCode") + "");
			recipient.setRecAreaName(recipientMap.get("recAreaName") + "");
			recipient.setRecAreaCode(recipientMap.get("recAreaCode") + "");
			recipient.setRecipientAddr(recipientMap.get("recipientAddr") + "");
			recipient.setNotes(recipientMap.get("notes") + "");
			recipient.setCreateBy(memberName);
			recipient.setCreateDate(date);
			recipient.setDeleteFlag(0);
			// 将收货信息实体放入缓存中
			cacheList.add(recipient);
		}
		return saveRecipientContent(cacheList);
	}

	/**
	 * 保存收货人信息实体类
	 * 
	 * @param cacheList
	 *            缓存收货人信息实体集合
	 * @return Map
	 */
	private Map<String, Object> saveRecipientContent(List<RecipientContent> cacheList) {
		if (cacheList == null || cacheList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		for (int x = 0; x < cacheList.size(); x++) {
			RecipientContent recipient = cacheList.get(x);
			Calendar cal = Calendar.getInstance();
			// 获取当前年份
			int year = cal.get(Calendar.YEAR);
			// 查询数据库字段名
			String property = "recipientId";
			// 根据年份查询,当前年份下的id数量
			long recipientIdCount = recipientDao.findSerialNoCount(RecipientContent.class, property, year);
			// 当返回-1时,则查询数据库失败
			if (recipientIdCount < 0) {
				return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
			}
			// 生成用户收获地址ID
			// RCPT=Recipient缩写
			String recipientId = SerialNoUtils.getSerialNotTimestamp("RCPT_", year, recipientIdCount);
			recipient.setRecipientId(recipientId);
			if (!recipientDao.add(recipient)) {
				return ReturnInfoUtils.errorInfo("保存收货人地址信息失败,服务器繁忙!");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 校验数据
	 * 
	 * @param jsonList
	 * @return Map
	 */
	private Map<String, Object> checkRecipient(JSONArray jsonList) {
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("recipientName");
		noNullKeys.add("recipientCardId");
		noNullKeys.add("recipientTel");
		noNullKeys.add("recProvincesName");
		noNullKeys.add("recProvincesCode");
		noNullKeys.add("recCityName");
		noNullKeys.add("recCityCode");
		noNullKeys.add("recAreaName");
		noNullKeys.add("recAreaCode");
		noNullKeys.add("recipientAddr");
		Map<String, Object> reDateMap = CheckDatasUtil.checkData(jsonList, noNullKeys);
		if (!"1".equals(reDateMap.get(BaseCode.STATUS.toString()) + "")) {
			return reDateMap;
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> getMemberRecipientInfo(String memberId, String memberName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("memberName", memberName);
		List<Object> reLsit = recipientDao.findByProperty(RecipientContent.class, params, 0, 0);
		if (reLsit != null && !reLsit.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reLsit);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> deleteMemberRecipientInfo(String memberId, String memberName, String recipientId) {
		Map<String, Object> reMap = getRecipientInfo(recipientId);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		RecipientContent recipient = (RecipientContent) reMap.get(BaseCode.DATAS.toString());
		if (!recipientDao.delete(recipient)) {
			return ReturnInfoUtils.errorInfo("刪除失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> modifyRecipientInfo(String recipientInfoPack, String updateBy) {
		if (StringEmptyUtils.isEmpty(recipientInfoPack)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		JSONObject json = null;
		try {
			json = JSONObject.fromObject(recipientInfoPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("用户收货地址信息格式错误!");
		}
		if (!json.isEmpty()) {
			String recipientId = json.get("recipientId") + "";
			Map<String, Object> reMap = getRecipientInfo(recipientId);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
			RecipientContent recipient = (RecipientContent) reMap.get(BaseCode.DATAS.toString());
			recipient.setRecipientCountryName("中国");
			recipient.setRecipientCountryCode("142");
			recipient.setRecProvincesName(json.get("recProvincesName") + "");
			recipient.setRecProvincesCode(json.get("recProvincesCode") + "");
			recipient.setRecCityName(json.get("recCityName") + "");
			recipient.setRecCityCode(json.get("recCityCode") + "");
			recipient.setRecAreaName(json.get("recAreaName") + "");
			recipient.setRecAreaCode(json.get("recAreaCode") + "");
			String recipientName = json.get("recipientName") + "";
			if (!StringUtil.isContainChinese(recipientName)) {
				return ReturnInfoUtils.errorInfo("收货人姓名错误,请重新输入");
			}
			recipient.setRecipientName(recipientName);
			String recipientCardId = json.get("recipientCardId") + "";
			if (!IdcardValidator.validate18Idcard(recipientCardId)) {
				return ReturnInfoUtils.errorInfo("收货人身份证号码错误,请重新输入!");
			}
			recipient.setRecipientCardId(recipientCardId);
			String recipientTel = json.get("recipientTel") + "";
			if (!PhoneUtils.isPhone(recipientTel)) {
				return ReturnInfoUtils.errorInfo("手机号码不正确,请重新输入!");
			}
			recipient.setRecipientTel(recipientTel);
			recipient.setRecipientAddr(json.get("recipientAddr") + "");
			recipient.setUpdateDate(new Date());
			recipient.setUpdateBy(updateBy);
			if(!recipientDao.update(recipient)){
				return ReturnInfoUtils.errorInfo("更新收货人信息失败,服务器繁忙!");
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("请求参数不能为空!");
	}
	
	/**
	 * 根据收货地址信息流水Id 查询对应的收货地址信息
	 * 
	 * @param recipientId
	 *            收获地址信息流水Id
	 * @return Map
	 */
	private Map<String, Object> getRecipientInfo(String recipientId) {
		if (StringEmptyUtils.isEmpty(recipientId)) {
			return ReturnInfoUtils.errorInfo("收货信息Id不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("recipientId", recipientId);
		List<RecipientContent> reLsit = recipientDao.findByProperty(RecipientContent.class, params, 0, 0);
		if (reLsit == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reLsit.isEmpty()) {
			RecipientContent recipient = reLsit.get(0);
			return ReturnInfoUtils.successDataInfo(recipient);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}
}
