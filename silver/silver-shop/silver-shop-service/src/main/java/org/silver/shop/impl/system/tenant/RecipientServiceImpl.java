package org.silver.shop.impl.system.tenant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.tenant.RecipientService;
import org.silver.shop.dao.system.tenant.RecipientDao;
import org.silver.shop.model.system.tenant.RecipientContent;
import org.silver.util.CheckDatasUtil;
import org.silver.util.SerialNoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = RecipientService.class)
public class RecipientServiceImpl implements RecipientService {

	private static final Logger LOGGER = Logger.getLogger(RecipientServiceImpl.class);

	@Autowired
	private RecipientDao recipientDao;

	@Override
	public Map<String, Object> addRecipientInfo(String memberId, String memberName, String recipientInfo) {
		JSONArray jsonList = null;
		Map<String, Object> statusMap = new HashMap<>();
		List<Object> cacheList = new ArrayList<>();
		Date date = new Date();
		try {
			jsonList = JSONArray.fromObject(recipientInfo);
		} catch (Exception e) {
			LOGGER.error("----前台传递用户收货地址信息错误-----");
			LOGGER.error("context", e);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		}
		if (jsonList != null && jsonList.size() > 0) {
			for (int i = 0; i < jsonList.size(); i++) {
				// 校验数据
				List<String> noNullKeys = new ArrayList<>();
				noNullKeys.add("recipientName");
				noNullKeys.add("recipientCardId");
				noNullKeys.add("recipientTel");
				noNullKeys.add("recipientCountryName");
				noNullKeys.add("recipientCountryCode");
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
				Map<String, Object> recipientMap = (Map<String, Object>) jsonList.get(i);
				RecipientContent recipient = new RecipientContent();
				recipient.setMemberId(memberId);
				recipient.setMemberName(memberName);
				recipient.setRecipientName(recipientMap.get("recipientName") + "");
				recipient.setRecipientCardId(recipientMap.get("recipientCardId") + "");
				recipient.setRecipientTel(recipientMap.get("recipientTel") + "");
				recipient.setRecipientCountryName(recipientMap.get("recipientCountryName") + "");
				recipient.setRecipientCountryCode(recipientMap.get("recipientCountryCode") + "");
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
			for (int x = 0; x < cacheList.size(); x++) {
				RecipientContent recipient = (RecipientContent) cacheList.get(x);
				Calendar cal = Calendar.getInstance();
				// 获取当前年份
				int year = cal.get(Calendar.YEAR);
				// 查询数据库字段名
				String property = "recipientId";
				// 根据年份查询,当前年份下的id数量
				long recipientIdCount = recipientDao.findSerialNoCount(RecipientContent.class, property, year);
				// 当返回-1时,则查询数据库失败
				if (recipientIdCount < 0) {
					statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
					return statusMap;
				}
				// 生成用户收获地址ID
				// RCPT=Recipient缩写
				String recipientId = SerialNoUtils.getSerialNotTimestamp("RCPT_", year, recipientIdCount);
				recipient.setRecipientId(recipientId);
				if (!recipientDao.add(recipient)) {
					statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.getBaseCode(), "添加地址失败,服务器繁忙!");
					return statusMap;
				}
			}
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> getMemberRecipientInfo(String memberId, String memberName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("memberName", memberName);
		List<Object> reLsit = recipientDao.findByProperty(RecipientContent.class, params, 0, 0);
		if (reLsit != null && reLsit.size() > 0) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.getBaseCode(), reLsit);
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> deleteMemberRecipientInfo(String memberId, String memberName, String recipientId) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("recipientId", recipientId);
		List<Object> reLsit = recipientDao.findByProperty(RecipientContent.class, params, 0, 0);
		if (reLsit != null && reLsit.size() > 0) {
			RecipientContent recipient = (RecipientContent) reLsit.get(0);
			if(!recipientDao.delete(recipient)){
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

}
