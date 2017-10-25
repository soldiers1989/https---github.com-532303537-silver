package org.silver.shop.impl.system.commerce;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.CustomsPortService;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.api.system.commerce.GoodsRecordService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.commerce.GoodsRecordDao;
import org.silver.shop.model.common.base.CustomsPort;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecord;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.commerce.Warehous;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;
import org.silver.util.SerialNoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = GoodsRecordService.class)
public class GoodsRecordServiceImpl implements GoodsRecordService {
	private static final Logger logger = LogManager.getLogger(GoodsRecordServiceImpl.class);
	// 进出境标志I-进，E-出
	private static final String IEFLAG = "I";

	// 币制默认为人民币
	private static final String CURRCODE = "142";

	@Autowired
	private GoodsRecordDao goodsRecordDao;
	@Autowired
	private CustomsPortService customsPortService;
	@Autowired
	private AccessTokenService accessTokenService;

	@Override
	public Map<String, Object> findGoodsBaseInfo(String merchantName, int page, int size) {
		Map<String, Object> params = new HashMap<>();
		// key=数据库列名,value=查询参数
		params.put("goodsMerchantName", merchantName);
		// 删除标识:0-未删除,1-已删除
		params.put("deleteFlag", 0);
		String descParam = "createDate";
		List<Object> reList = goodsRecordDao.findGoodsBaseInfo(params, descParam, page, size);
		params.clear();
		if (reList != null && reList.size() >= 0) {
			params.put(BaseCode.DATAS.toString(), reList);
			params.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			params.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			return params;
		}
		params.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		params.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		return params;
	}

	@Override
	public Map<String, Object> getGoodsRecordInfo(String merchantName, String goodsInfoPack) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		JSONArray jsonList = null;
		List<Object> goodsBaseList = new ArrayList<>();
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			logger.error(e);
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "前端传递基本信息参数错误！");
			return statusMap;
		}
		for (int i = 0; i < jsonList.size(); i++) {
			/*
			 * Calendar cal = Calendar.getInstance(); // 获取当前年份 int year =
			 * cal.get(Calendar.YEAR); // 查询数据库字段名 String entGoodsNo =
			 * "entGoodsNo"; // 根据年份查询,当前年份下的id数量 long goodsIdCount =
			 * goodsRecordDao.findSerialNoCount(GoodsRecordDetail.class,
			 * entGoodsNo, year); // 当返回-1时,则查询数据库失败 if (goodsIdCount < 0) {
			 * statusMap.put(BaseCode.STATUS.getBaseCode(),
			 * StatusCode.WARN.getStatus());
			 * statusMap.put(BaseCode.MSG.getBaseCode(),
			 * StatusCode.WARN.getMsg()); return statusMap; } // 生成商品基本信息ID
			 * String goodsId = SerialNoUtils.getSerialNo("YM_", year,
			 * goodsIdCount+i);
			 */
			params = new HashMap<>();
			Map<String, Object> goodsMap = (Map) jsonList.get(i);
			// 获取传递过来的商品ID
			String mapGoodsId = goodsMap.get("goodsId") + "";
			String mapGoodsName = goodsMap.get("goodsName") + "";
			String descParam = "createDate";
			// key=数据库列名,value=查询参数
			// params.put("entGoodsNo", mapGoodsId);
			params.put("goodsName", mapGoodsName);
			params.put("goodsMerchantName", merchantName);
			// 删除标识:0-未删除,1-已删除
			params.put("deleteFlag", 0);
			// 根据商品名,扫描商品备案信息表
			List<Object> goodsRecordList = goodsRecordDao.findPropertyDesc(GoodsRecordDetail.class, params, descParam,
					1, 1);
			if (goodsRecordList != null && goodsRecordList.size() > 0) {// 取出商品备案信息最近一条记录
				GoodsRecordDetail goodsRecordInfo = (GoodsRecordDetail) goodsRecordList.get(0);
				// 重新生成的商品备案ID
				/* goodsRecordInfo.setEntGoodsNo(goodsId); */
				goodsRecordInfo.setGoodsDetailId(mapGoodsId);
				goodsBaseList.add(goodsRecordInfo);
			} else {// 如果该商品在商品备案信息表中没有数据,则根据商品名称商品ID扫描商品基本信息表
				params.clear();
				// key=数据库列名,value=查询参数
				params.put("goodsId", mapGoodsId);
				params.put("goodsName", mapGoodsName);
				params.put("goodsMerchantName", merchantName);
				params.put("deleteFlag", 0);
				List<Object> goodsList = goodsRecordDao.findByProperty(GoodsContent.class, params, 1, 1);
				GoodsContent goodsInfo = (GoodsContent) goodsList.get(0);
				// 旧商品基本信息Id+重新生成的商品备案ID
				goodsInfo.setGoodsId(goodsInfo.getGoodsId());
				goodsBaseList.add(goodsInfo);
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.DATAS.toString(), goodsBaseList);
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	// 商戶发起备案
	@Override
	public Map<String, Object> merchantSendGoodsRecord(String merchantName, String merchantId, String customsPort,
			String customsCode, String ciqOrgCode, String recordGoodsInfoPack) {
		Map<String, Object> datasMap = new HashMap<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(recordGoodsInfoPack);
		} catch (Exception e) {
			logger.error(e.getMessage());
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.NOTICE.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.NOTICE.getMsg());
			return datasMap;
		}
		datasMap = checkCustomsPort(Integer.valueOf(customsPort), customsCode, ciqOrgCode);
		// 校验前台传递口岸、海关、智检编码
		if (!datasMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return datasMap;
		}
		CustomsPort portInfo = (CustomsPort) datasMap.get(BaseCode.DATAS.toString());
		// 根据商户ID,口岸编码查询商户备案信息
		Map<String, Object> merchantInfoMap = getMerchantInfo(merchantId, Integer.valueOf(customsPort));
		if (!merchantInfoMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return merchantInfoMap;
		}
		// 请求获取tok
		Map<String, Object> tokMap = accessTokenService.getAccessToken();
		if (!tokMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return tokMap;
		}
		String tok = tokMap.get(BaseCode.DATAS.toString()) + "";
		// 封装备案商品信息
		List<Object> datas = null;
		Map<String, Object> reMap = addRecordInfo(jsonList);
		if (!reMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return reMap;
		}
		datas = (List) reMap.get(BaseCode.DATAS.toString());
		// String reSerialNo = recordMap.get(BaseCode.DATAS.toString()) + "";
		// 将备案商品头部及商品详情信息插入数据库
		Map<String, Object> reGoodsMap = saveRecordInfo(merchantId, merchantName, merchantInfoMap, portInfo, jsonList);
		if (!reGoodsMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return reGoodsMap;
		}
		// 获取商品备案流水号
		String goodsSerialNo = reGoodsMap.get(BaseCode.DATAS.toString()) + "";
		// 创建商品仓库
		Map<String, Object> warehousMap = createWarehous(merchantId, merchantName, portInfo);
		if (!warehousMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return warehousMap;
		}

		// 发起商品备案
		Map<String, Object> recordMap = sendRecord(Integer.valueOf(customsPort), merchantInfoMap, tok, datas,
				goodsSerialNo, portInfo);
		String reStatus = recordMap.get(BaseCode.STATUS.toString()) + "";
		if (!reStatus.equals("1")) {
			// 对方接受商品备案信息失败后,修改商品备案状态
			Map<String, Object> reUpdateMap = updateGoodsRecordStatus(merchantId, goodsSerialNo);
			if (!reUpdateMap.get(BaseCode.STATUS.toString()).equals("1")) {
				return reUpdateMap;
			}
			return recordMap;
		}
		return recordMap;
	}

	/**
	 * 检查口岸,海关代码是否正确
	 * 
	 * @param eport
	 *            1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
	 * @param customsCode
	 *            主管海关代码
	 * @param ciqOrgCode
	 *            检验检疫机构代码
	 * @return
	 */
	private final Map<String, Object> checkCustomsPort(int eport, String customsCode, String ciqOrgCode) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramsMap = null;
		List<CustomsPort> customsPortList = null;
		byte[] redisByte = JedisUtil.get("shop_port_AllCustomsPort".getBytes(), 3600);
		if (redisByte != null) {
			customsPortList = (List<CustomsPort>) SerializeUtil.toObject(redisByte);
		} else {// 缓存中没有数据,重新访问数据库读取数据
			paramsMap = customsPortService.findAllCustomsPort();
			if (!paramsMap.get(BaseCode.STATUS.toString()).equals("1")) {
				return paramsMap;
			}
			customsPortList = (List<CustomsPort>) paramsMap.get(BaseCode.DATAS.toString());
			// 将查询出来的口岸数据放入缓存中
			JedisUtil.set("shop_port_AllCustomsPort".getBytes(), SerializeUtil.toBytes(customsPortList), 3600);
		}
		for (int i = 0; i < customsPortList.size(); i++) {
			CustomsPort portInfo = customsPortList.get(i);
			// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
			int reCustomsPort = portInfo.getCustomsPort();
			// 口岸中文名称
			String recustomsPortName = portInfo.getCustomsPortName();
			// 主管海关代码
			String reCustomsCode = portInfo.getCustomsCode();
			// 主管海关名称
			String reCustomsName = portInfo.getCustomsName();
			// 检验检疫机构代码
			String reCiqOrgCode = portInfo.getCiqOrgCode();
			// 检验检疫机构名称
			String reCiqOrgName = portInfo.getCiqOrgName();
			// 判断前端传递的口岸端口、海关代码、智检代码是否正确
			if (reCustomsPort == eport && reCustomsCode.trim().equals(customsCode.trim())
					&& reCiqOrgCode.trim().equals(ciqOrgCode.trim())) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.DATAS.toString(), portInfo);
				return statusMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.NOTICE.getStatus());
		statusMap.put(BaseCode.MSG.toString(), "口岸代码错误！");
		return statusMap;
	}

	/**
	 * 将备案商品信息封装
	 * 
	 * @param jsonList
	 *            备案商品信息List
	 * @return Map
	 */
	private final Map<String, Object> addRecordInfo(List<Object> jsonList) {
		Map<String, Object> datasMap = new HashMap<>();
		List<JSONObject> datas = new ArrayList<>();
		JSONObject jsonObject = null;
		if (jsonList != null && jsonList.size() > 0) {
			for (int i = 0; i < jsonList.size(); i++) {
				jsonObject = new JSONObject();
				Map<String, Object> goodsInfo = (Map<String, Object>) jsonList.get(i);
				jsonObject.element("Seq", i + 1);
				jsonObject.element("EntGoodsNo", goodsInfo.get("EntGoodsNo") + "");
				jsonObject.element("EPortGoodsNo", goodsInfo.get("EPortGoodsNo") + "");
				jsonObject.element("CIQGoodsNo", goodsInfo.get("CIQGoodsNo") + "");
				jsonObject.element("CusGoodsNo", goodsInfo.get("CusGoodsNo") + "");
				jsonObject.element("EmsNo", goodsInfo.get("EmsNo") + "");
				jsonObject.element("ItemNo", goodsInfo.get("ItemNo") + "");
				jsonObject.element("ShelfGName", goodsInfo.get("ShelfGName") + "");
				jsonObject.element("NcadCode", goodsInfo.get("NcadCode") + "");
				jsonObject.element("HSCode", goodsInfo.get("HSCode") + "");
				jsonObject.element("BarCode", goodsInfo.get("BarCode") + "");
				jsonObject.element("GoodsName", goodsInfo.get("GoodsName") + "");
				jsonObject.element("GoodsStyle", goodsInfo.get("GoodsStyle") + "");
				jsonObject.element("Brand", goodsInfo.get("Brand") + "");
				jsonObject.element("GUnit", goodsInfo.get("GUnit") + "");
				jsonObject.element("StdUnit", goodsInfo.get("StdUnit") + "");
				jsonObject.element("SecUnit", goodsInfo.get("SecUnit") + "");
				jsonObject.element("RegPrice", goodsInfo.get("RegPrice") + "");
				jsonObject.element("GiftFlag", goodsInfo.get("GiftFlag") + "");
				jsonObject.element("OriginCountry", goodsInfo.get("OriginCountry") + "");
				jsonObject.element("Quality", goodsInfo.get("Quality") + "");
				jsonObject.element("QualityCertify", goodsInfo.get("QualityCertify") + "");
				jsonObject.element("Manufactory", goodsInfo.get("Manufactory") + "");
				jsonObject.element("NetWt", goodsInfo.get("NetWt") + "");
				jsonObject.element("GrossWt", goodsInfo.get("GrossWt") + "");
				jsonObject.element("Notes", goodsInfo.get("Notes") + "");
				datas.add(jsonObject);
			}
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.DATAS.toString(), datas);
		} else {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			datasMap.put(BaseCode.MSG.toString(), "封装商品备案信息错误!");
		}
		return datasMap;
	}

	/**
	 * 获取商户备案信息
	 * 
	 * @param merchantId
	 *            商户ID
	 * @param eport
	 *            1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
	 * @return Map
	 */
	public final Map<String, Object> getMerchantInfo(String merchantId, int eport) {
		Map<String, Object> params = new HashMap<>();
		// key=表中列名,value=查询参数
		params.put("merchantId", merchantId);
		params.put("customsPort", eport);
		// 根据商户ID查询商户备案信息数据
		List<Object> reList = goodsRecordDao.findByProperty(MerchantRecordInfo.class, params, 1, 1);
		if (reList != null && reList.size() > 0) {
			params.clear();
			MerchantRecordInfo merchantRecordInfo = (MerchantRecordInfo) reList.get(0);
			// 口岸:1-广州电子口岸,2-广东智检
			params.put("eport", Integer.valueOf(merchantRecordInfo.getCustomsPort()));
			// 电商企业编号
			params.put("ebEntNo", merchantRecordInfo.getEbEntNo());
			// 电商企业名称
			params.put("ebEntName", merchantRecordInfo.getEbEntName());
			// 电商平台企业编号
			params.put("ebpEntNo", merchantRecordInfo.getEbpEntNo());
			// 电商平台名称
			params.put("ebpEntName", merchantRecordInfo.getEbpEntName());
			params.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		} else {
			params.clear();
			params.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			params.put(BaseCode.MSG.toString(), "商户备案信息查询失败！");
		}
		return params;
	}

	/**
	 * 发起商品备案信息
	 * 
	 * @param eport
	 *            端口号
	 * @param merchantInfoMap
	 *            商户备案信息
	 * @param tok
	 * @param datas
	 *            商品数据
	 * @param goodsSerialNo
	 *            商品流水号
	 * @return Map
	 */
	private final Map<String, Object> sendRecord(int eport, Map<String, Object> merchantInfoMap, String tok,
			List<Object> datas, String goodsSerialNo, CustomsPort portInfo) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		// 电商企业编号
		String ebEntNo = merchantInfoMap.get("ebEntNo") + "";
		// 电商企业名称
		String ebEntName = merchantInfoMap.get("ebEntName") + "";
		// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
		// 1-特殊监管区域BBC保税进口;2-保税仓库BBC保税进口;3-BC直购进口
		String businessType = "";
		if (eport == 1) {
			businessType = "3";
		} else if (eport == 2) {
			businessType = "2";
		}
		// 客戶端签名
		String clientsign = "";
		// 备注
		String note = "";
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		// 商品发起备案(录入)日期
		String inputDate = sdf.format(date);
		String timestamp = String.valueOf(System.currentTimeMillis());
		try {
			clientsign = MD5.getMD5(
					(YmMallConfig.APPKEY + tok + datas.toString() + YmMallConfig.URL + timestamp).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		// 0:商品备案 1:订单推送 2:支付单推送
		params.put("type", 0);
		params.put("eport", Integer.valueOf(eport));
		params.put("businessType", Integer.valueOf(businessType));
		params.put("ieFlag", IEFLAG);
		params.put("currCode", CURRCODE);
		params.put("inputDate", inputDate);
		// 电商企业编号
		params.put("ebEntNo", ebEntNo);
		// 电商企业名称
		params.put("ebEntName", ebEntName);
		params.put("appkey", YmMallConfig.APPKEY);
		params.put("ciqOrgCode", portInfo.getCiqOrgCode());
		params.put("customsCode", portInfo.getCustomsCode());
		params.put("clientsign", clientsign);
		params.put("timestamp", timestamp);
		params.put("datas", datas.toString());
		params.put("notifyurl", YmMallConfig.URL);
		params.put("note", note);
		// 商城商品备案流水号
		// params.put("goodsSerialNo", "goodsSerialNo");
		String resultStr = YmHttpUtil.HttpPost("http:/ym.191ec.com/silver-web/Eport/Report", params);
		if (StringEmptyUtils.isNotEmpty(resultStr)) {
			return JSONObject.fromObject(resultStr);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "服务器接受备案信息错误,服务器繁忙！");
		}
		return statusMap;
	}

	/**
	 * 保存备案商品信息
	 * 
	 * @param merchantId
	 *            商户ID
	 * @param merchantName
	 *            商户名称
	 * @param merchantInfoMap
	 *            商户备案信息
	 * @param portInfo
	 *            端口信息
	 * @param jsonList
	 *            商品信息
	 * @return Map
	 */
	private final Map<String, Object> saveRecordInfo(String merchantId, String merchantName,
			Map<String, Object> merchantInfoMap, CustomsPort portInfo, List<Object> jsonList) {
		Map<String, Object> statusMap = new HashMap<>();
		boolean flag = false;
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		// 查询数据库字段名
		String property = "goodsSerialNo";
		// 根据年份查询,当前年份下的id数量
		long goodsSerialNoCount = goodsRecordDao.findSerialNoCount(GoodsRecord.class, property, year);
		// 当返回-1时,则查询数据库失败
		if (goodsSerialNoCount < 0) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		// 生成备案商品流水号
		String goodsSerialNo = SerialNoUtils.getSerialNo("GR_", year, goodsSerialNoCount);
		GoodsRecord recordInfo = new GoodsRecord();
		recordInfo.setMerchantId(merchantId);
		recordInfo.setMerchantName(merchantName);
		recordInfo.setGoodsSerialNo(goodsSerialNo);
		// 海关口岸代码 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
		recordInfo.setCustomsPort(portInfo.getCustomsPort());
		recordInfo.setCustomsPortName(portInfo.getCustomsPortName());
		recordInfo.setCustomsCode(portInfo.getCustomsCode());
		recordInfo.setCustomsName(portInfo.getCustomsName());
		recordInfo.setCiqOrgCode(portInfo.getCiqOrgCode());
		recordInfo.setCiqOrgName(portInfo.getCiqOrgName());
		recordInfo.setEbEntNo(merchantInfoMap.get("ebEntNo") + "");
		recordInfo.setEbEntName(merchantInfoMap.get("ebEntName") + "");
		recordInfo.setEbpEntNo(merchantInfoMap.get("ebpEntNo") + "");
		recordInfo.setEbpEntName(merchantInfoMap.get("ebpEntName") + "");
		// 备案信息接受状态：1-成功,2-失败
		recordInfo.setStatus("1");
		recordInfo.setCreateBy(merchantName);
		recordInfo.setCreateDate(date);
		recordInfo.setDeleteFlag(0);
		// recordInfo.setReSerialNo(reSerialNo);
		flag = goodsRecordDao.add(recordInfo);
		if (!flag) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "保存商品备案流水信息错误,服务器繁忙!");
			
			return statusMap;
		}
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> params = new HashMap<>();
			// 查询数据库字段名
			String property2 = "entGoodsNo";
			// 根据年份查询,当前年份下的id数量
			long goodsRecordSerialNoCount = goodsRecordDao.findSerialNoCount(GoodsRecordDetail.class, property2, year);
			// 当返回-1时,则查询数据库失败
			if (goodsRecordSerialNoCount < 0) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
				return statusMap;
			}
			String goodsRecordSerialNo = SerialNoUtils.getSerialNo("GR_", year, goodsRecordSerialNoCount );
			GoodsRecordDetail goodRecordInfo = new GoodsRecordDetail();
			Map<String, Object> goodsInfo = (Map<String, Object>) jsonList.get(i);
			String goodsDatesId = goodsInfo.get("GoodsDetailId") + "";
			params.put("goodsId", goodsDatesId);
			params.put("goodsMerchantId", merchantId);
			params.put("goodsMerchantName", merchantName);
			List reGoodsList = goodsRecordDao.findByProperty(GoodsContent.class, params, 1, 1);
			if (reGoodsList == null && reGoodsList.size() < 0) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
				return statusMap;
			}
			GoodsContent goods = (GoodsContent) reGoodsList.get(0);
			goodRecordInfo.setSeq(i + 1);
			goodRecordInfo.setEntGoodsNo(goodsRecordSerialNo);
			goodRecordInfo.setEportGoodsNo(goodsInfo.get("EPortGoodsNo") + "");
			goodRecordInfo.setCiqGoodsNo(goodsInfo.get("CIQGoodsNo") + "");
			goodRecordInfo.setCusGoodsNo(goodsInfo.get("CusGoodsNo") + "");
			goodRecordInfo.setEmsNo(goodsInfo.get("EmsNo") + "");
			goodRecordInfo.setItemNo(goodsInfo.get("ItemNo") + "");
			goodRecordInfo.setShelfGName(goodsInfo.get("ShelfGName") + "");
			goodRecordInfo.setNcadCode(goodsInfo.get("NcadCode") + "");
			goodRecordInfo.setHsCode(goodsInfo.get("HSCode") + "");
			goodRecordInfo.setBarCode(goodsInfo.get("BarCode") + "");
			goodRecordInfo.setGoodsName(goodsInfo.get("GoodsName") + "");
			goodRecordInfo.setGoodsStyle(goodsInfo.get("GoodsStyle") + "");
			goodRecordInfo.setBrand(goodsInfo.get("Brand") + "");
			goodRecordInfo.setgUnit(goodsInfo.get("GUnit") + "");
			goodRecordInfo.setStdUnit(goodsInfo.get("StdUnit") + "");
			goodRecordInfo.setSecUnit(goodsInfo.get("SecUnit") + "");
			goodRecordInfo.setRegPrice(Double.valueOf(goodsInfo.get("RegPrice") + ""));
			goodRecordInfo.setGiftFlag(goodsInfo.get("GiftFlag") + "");
			goodRecordInfo.setOriginCountry(goodsInfo.get("OriginCountry") + "");
			goodRecordInfo.setQuality(goodsInfo.get("Quality") + "");
			goodRecordInfo.setQualityCertify(goodsInfo.get("QualityCertify") + "");
			goodRecordInfo.setManufactory(goodsInfo.get("Manufactory") + "");
			goodRecordInfo.setNetWt(Double.valueOf(goodsInfo.get("NetWt") + ""));
			goodRecordInfo.setGrossWt(Double.valueOf(goodsInfo.get("GrossWt") + ""));
			goodRecordInfo.setNotes(goodsInfo.get("Notes") + "");
			// 备案状态：1-备案中，2-备案成功，3-备案失败
			goodRecordInfo.setStatus(1);
			goodRecordInfo.setGoodsMerchantId(merchantId);
			goodRecordInfo.setGoodsMerchantName(merchantName);
			goodRecordInfo.setCreateBy(merchantName);
			goodRecordInfo.setCreateDate(date);
			// 删除标识:0-未删除,1-已删除
			goodRecordInfo.setDeleteFlag(0);
			goodRecordInfo.setGoodsSerialNo(goodsSerialNo);
			goodRecordInfo.setGoodsDetailId(goods.getGoodsId());
			flag = goodsRecordDao.add(goodRecordInfo);
			if (!flag) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "保存商品备案信息错误,服务器繁忙!");
				goodsRecordDao.delete(recordInfo);
				return statusMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.DATAS.toString(), goodsSerialNo);
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	/**
	 * 接受商品备案信息失败后,修改商品备案状态
	 * 
	 * @param merchantId
	 *            商户ID
	 * @param goodsSerialNo
	 *            商品流水号
	 * @return Map
	 */
	private final Map<String, Object> updateGoodsRecordStatus(String merchantId, String goodsSerialNo) {
		Map<String, Object> statusMap = new HashMap<>();
		boolean flag = false;
		// 修改商品备案头状态
		// 备案信息接受状态：1-成功,2-失败
		flag = goodsRecordDao.updateGoodsRecordStatus("ym_shop_goods_record", "merchantId", merchantId, goodsSerialNo,
				2);
		if (!flag) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "接受商品备案信息失败后,修改商品备案头状态失败！");
			return statusMap;
		}
		// 修改商品备案详情状态
		// 备案状态：1-备案中，2-备案成功，3-备案失败
		flag = goodsRecordDao.updateGoodsRecordStatus("ym_shop_goods_record_detail", "goodsMerchantId", merchantId,
				goodsSerialNo, 3);
		if (!flag) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "接受商品备案信息失败后,修改商品备案状态失败！");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	/**
	 * 创建商品仓库
	 * 
	 * @param merchantId
	 *            商户ID
	 * @param merchantName
	 *            商户名称
	 * @param portInfo
	 *            口岸管理实体类
	 * @return Map
	 */
	private final Map<String, Object> createWarehous(String merchantId, String merchantName, CustomsPort portInfo) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramsMap = new HashMap<>();
		// 主管海关代码(同仓库编码)
		String code = merchantId + "_" + portInfo.getCustomsCode();
		// key=数据库列名,value=查询参数
		paramsMap.put("warehouseCode", code);
		List<Object> reList = goodsRecordDao.findByProperty(Warehous.class, paramsMap, 0, 0);
		// 数据库查询错误
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (reList.size() == 0) {// 当没有仓库时
			Warehous warehous = new Warehous();
			warehous.setMerchantId(merchantId);
			warehous.setMerchantName(merchantName);
			warehous.setWarehouseCode(code);
			warehous.setWarehouseName(portInfo.getCustomsName());
			warehous.setCreateBy(merchantName);
			warehous.setCreateDate(date);
			warehous.setDeleteFlag(0);
			if (!goodsRecordDao.add(warehous)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "创建仓库失败,服务器繁忙!");
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> findAllGoodsRecordInfo(String merchantId, String goodsId, int page, int size) {
		Map<String, Object> params = new HashMap<>();
		if (StringEmptyUtils.isNotEmpty(goodsId)) {
			// key=(表中列名称),value=(查询参数)
			params.put("entGoodsNo", goodsId);
			page = 1;
			size = 1;
		}
		params.put("goodsMerchantId", merchantId);
		params.put("deleteFlag", 0);
		List<Object> reList = goodsRecordDao.findByProperty(GoodsRecordDetail.class, params, page, size);
		params.clear();
		if (reList != null && reList.size() >= 0) {
			params.put(BaseCode.DATAS.toString(), reList);
			params.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			params.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			return params;
		}
		params.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		params.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		return params;
	}

	public static void main(String[] args) {
		System.out.println(YmMallConfig.APPKEY);
		/*
		 * List list = new ArrayList(); Map<String, Object> datasMap = null; for
		 * (int i = 0; i < 2; i++) { datasMap = new HashMap<>();
		 * datasMap.put("EntGoodsNo", "测试商品编码"); datasMap.put("EPortGoodsNo",
		 * "1"); datasMap.put("CIQGoodsNo", "2"); datasMap.put("CusGoodsNo",
		 * "2"); datasMap.put("EmsNo", "3"); datasMap.put("ItemNo", "4");
		 * datasMap.put("ShelfGName", "测试商品上架名称"); datasMap.put("NcadCode",
		 * "5"); datasMap.put("HSCode", "6"); datasMap.put("BarCode", "7");
		 * datasMap.put("GoodsName", "测试商品名称"); datasMap.put("GoodsStyle",
		 * "900/罐"); datasMap.put("Brand", "8"); datasMap.put("GUnit", "9");
		 * datasMap.put("StdUnit", "10"); datasMap.put("SecUnit", "11");
		 * datasMap.put("RegPrice", "12"); datasMap.put("GiftFlag", "13");
		 * datasMap.put("OriginCountry", "14"); datasMap.put("Quality", "15");
		 * datasMap.put("QualityCertify", "16"); datasMap.put("Manufactory",
		 * "17"); datasMap.put("NetWt", "18"); datasMap.put("GrossWt", "19");
		 * datasMap.put("Notes", ""); list.add(datasMap); }
		 * System.out.println(JSONArray.fromObject(list).toString());
		 */
	}

}
