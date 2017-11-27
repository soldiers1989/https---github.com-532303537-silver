package org.silver.shop.impl.system.commerce;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.commerce.WarehouseContent;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.util.CheckDatasUtil;
import org.silver.util.DateUtil;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;
import org.silver.util.SerialNoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.StringUtil;

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
	@Autowired
	private StockServiceImpl stockServiceImpl;

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
			params = new HashMap<>();
			Map<String, Object> goodsMap = (Map) jsonList.get(i);
			// 获取传递过来的商品ID
			String mapGoodsId = goodsMap.get("goodsId") + "";
			String descParam = "createDate";
			// key=数据库列名,value=查询参数
			params.put("goodsDetailId", mapGoodsId);
			params.put("goodsMerchantName", merchantName);
			// 删除标识:0-未删除,1-已删除
			params.put("deleteFlag", 0);
			// 根据商品名,扫描商品备案信息表
			List<Object> goodsRecordList = goodsRecordDao.findPropertyDesc(GoodsRecordDetail.class, params, descParam,
					1, 1);
			if (goodsRecordList != null && goodsRecordList.size() > 0) {// 取出商品备案信息最近一条记录
				GoodsRecordDetail goodsRecordInfo = (GoodsRecordDetail) goodsRecordList.get(0);
				// 重新生成的商品备案ID
				goodsRecordInfo.setGoodsDetailId(mapGoodsId);
				goodsBaseList.add(goodsRecordInfo);
			} else {// 如果该商品在商品备案信息表中没有数据,则根据商品名称商品ID扫描商品基本信息表
				params.clear();
				// key=数据库列名,value=查询参数
				params.put("goodsId", mapGoodsId);
				params.put("goodsMerchantName", merchantName);
				params.put("deleteFlag", 0);
				List<Object> goodsList = goodsRecordDao.findByProperty(GoodsContent.class, params, 1, 1);
				if (goodsList != null && goodsList.size() > 0) {
					GoodsContent goodsInfo = (GoodsContent) goodsList.get(0);
					// 旧商品基本信息Id+重新生成的商品备案ID
					goodsInfo.setGoodsId(goodsInfo.getGoodsId());
					goodsBaseList.add(goodsInfo);
				} else {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.UNKNOWN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.UNKNOWN.getMsg());
					return statusMap;
				}
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
		// 验证前台传值
		Map<String, Object> reDataMap = checkData(jsonList, Integer.valueOf(customsPort));
		if (!"1".equals(reDataMap.get(BaseCode.STATUS.toString()) + "")) {
			return reDataMap;
		}
		JSONArray reJSONObject = JSONArray.fromObject(reDataMap.get(BaseCode.DATAS.toString()));
		// 保存商品备案信息头
		// 将备案商品头部及商品详情信息插入数据库
		Map<String, Object> reGoodsHeadMap = saveRecordHeadInfo(merchantId, merchantName, merchantInfoMap, portInfo);
		if (!"1".equals(reGoodsHeadMap.get(BaseCode.STATUS.toString()) + "")) {
			return reGoodsHeadMap;
		}
		// 获取商品备案流水号
		String goodsSerialNo = reGoodsHeadMap.get(BaseCode.DATAS.toString()) + "";
		// 保存备案商品详情
		Map<String, Object> reRecordGoodsMap = saveRecordGoodsInfo(merchantId, merchantName, reJSONObject,
				goodsSerialNo);
		if (!"1".equals(reRecordGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
			return reRecordGoodsMap;
		}
		List<Object> goodsInfoList = (List) reRecordGoodsMap.get(BaseCode.DATAS.toString());
		// 封装备案商品信息
		List<Object> datas = null;
		Map<String, Object> reMap = addRecordInfo(goodsInfoList);
		if (!reMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return reMap;
		}
		datas = (List) reMap.get(BaseCode.DATAS.toString());
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
		} else {
			String msgId = recordMap.get("messageID") + "";
			Map<String, Object> reUpdateMsg = updateGoodsRecordInfo(merchantId, goodsSerialNo, msgId);
			if (!reUpdateMsg.get(BaseCode.STATUS.toString()).equals("1")) {
				return reUpdateMsg;
			}
			return reUpdateMsg;
		}
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
				GoodsRecordDetail goodsInfo = (GoodsRecordDetail) jsonList.get(i);
				jsonObject.element("Seq", i + 1);
				jsonObject.element("EntGoodsNo", goodsInfo.getEntGoodsNo());
				jsonObject.element("EPortGoodsNo", goodsInfo.getEportGoodsNo());
				jsonObject.element("CIQGoodsNo", goodsInfo.getCiqGoodsNo());
				jsonObject.element("CusGoodsNo", goodsInfo.getCusGoodsNo());
				jsonObject.element("EmsNo", goodsInfo.getEmsNo());
				jsonObject.element("ItemNo", goodsInfo.getItemNo());
				jsonObject.element("ShelfGName", goodsInfo.getShelfGName());
				jsonObject.element("NcadCode", goodsInfo.getNcadCode());
				jsonObject.element("HSCode", goodsInfo.getHsCode());
				jsonObject.element("BarCode", goodsInfo.getBarCode());
				jsonObject.element("GoodsName", goodsInfo.getGoodsName());
				jsonObject.element("GoodsStyle", goodsInfo.getGoodsStyle());
				jsonObject.element("Brand", goodsInfo.getBrand());
				jsonObject.element("GUnit", goodsInfo.getgUnit());
				jsonObject.element("StdUnit", goodsInfo.getStdUnit());
				jsonObject.element("SecUnit", goodsInfo.getSecUnit());
				jsonObject.element("RegPrice", goodsInfo.getRegPrice());
				jsonObject.element("GiftFlag", goodsInfo.getGiftFlag());
				jsonObject.element("OriginCountry", goodsInfo.getOriginCountry());
				jsonObject.element("Quality", goodsInfo.getQuality());
				jsonObject.element("QualityCertify", goodsInfo.getQualityCertify());
				jsonObject.element("Manufactory", goodsInfo.getManufactory());
				jsonObject.element("NetWt", goodsInfo.getNetWt());
				jsonObject.element("GrossWt", goodsInfo.getGrossWt());
				jsonObject.element("Notes", goodsInfo.getNotes());

				jsonObject.element("Ingredient", goodsInfo.getIngredient());
				jsonObject.element("Additiveflag", goodsInfo.getAdditiveflag());
				jsonObject.element("Poisonflag", goodsInfo.getPoisonflag());
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
		String businessType = eport == 1 ? "3" : "2";
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
					(YmMallConfig.APPKEY + tok + datas.toString() + YmMallConfig.GOODSRECORDNOTIFYURL + timestamp)
							.getBytes("UTF-8"));
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
		params.put("notifyurl", YmMallConfig.GOODSRECORDNOTIFYURL);
		params.put("note", note);
		// 商城商品备案流水号
		// params.put("goodsSerialNo", "goodsSerialNo");
		String resultStr = YmHttpUtil.HttpPost("http://ym.191ec.com/silver-web/Eport/Report", params);
		if (StringUtil.isNotEmpty(resultStr)) {
			return JSONObject.fromObject(resultStr);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "服务器接受备案信息失败,服务器繁忙！");
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
	private final Map<String, Object> saveRecordHeadInfo(String merchantId, String merchantName,
			Map<String, Object> merchantInfoMap, CustomsPort portInfo) {
		Map<String, Object> statusMap = new HashMap<>();
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
		recordInfo.setStatus(1);
		recordInfo.setCreateBy(merchantName);
		recordInfo.setCreateDate(date);
		recordInfo.setDeleteFlag(0);
		if (!goodsRecordDao.add(recordInfo)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "保存商品备案流水信息错误,服务器繁忙!");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.DATAS.toString(), goodsSerialNo);
		return statusMap;
	}

	/**
	 * 保存备案商品信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param jsonList
	 *            商品信息
	 * @param goodsSerialNo
	 *            备案流水
	 * @return Map
	 */
	private Map<String, Object> saveRecordGoodsInfo(String merchantId, String merchantName, JSONArray jsonList,
			String goodsSerialNo) {
		Map<String, Object> statusMap = new HashMap<>();
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		List<Object> reGoodsInfoList = new ArrayList<>();
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
			String goodsRecordSerialNo = SerialNoUtils.getSerialNotTimestamp("GR_", year, goodsRecordSerialNoCount);
			GoodsRecordDetail goodRecordInfo = new GoodsRecordDetail();
			Map<String, Object> goodsInfo = (Map<String, Object>) jsonList.get(i);
			String goodsDatesId = goodsInfo.get("GoodsDetailId") + "";
			params.put("goodsId", goodsDatesId);
			params.put("goodsMerchantId", merchantId);
			params.put("goodsMerchantName", merchantName);
			List<Object> reGoodsList = goodsRecordDao.findByProperty(GoodsContent.class, params, 1, 1);
			if (reGoodsList == null && reGoodsList.size() <= 0) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
				return statusMap;
			}
			GoodsContent goods = (GoodsContent) reGoodsList.get(0);
			goodRecordInfo.setSeq(i + 1);
			goodRecordInfo.setEntGoodsNo(goodsRecordSerialNo);
			goodRecordInfo.setEportGoodsNo(String.valueOf(goodsInfo.get("EPortGoodsNo")));
			goodRecordInfo.setCiqGoodsNo(String.valueOf(goodsInfo.get("CIQGoodsNo")));
			goodRecordInfo.setCusGoodsNo(String.valueOf(goodsInfo.get("CusGoodsNo")));
			goodRecordInfo.setEmsNo(String.valueOf(goodsInfo.get("EmsNo")));
			goodRecordInfo.setItemNo(String.valueOf(goodsInfo.get("ItemNo")));
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
			goodRecordInfo.setOriginCountry(String.valueOf(goodsInfo.get("OriginCountry")));
			goodRecordInfo.setQuality(goodsInfo.get("Quality") + "");
			goodRecordInfo.setQualityCertify(goodsInfo.get("QualityCertify") + "");
			goodRecordInfo.setManufactory(goodsInfo.get("Manufactory") + "");
			goodRecordInfo.setNetWt(Double.valueOf(goodsInfo.get("NetWt") + ""));
			goodRecordInfo.setGrossWt(Double.valueOf(goodsInfo.get("GrossWt") + ""));
			goodRecordInfo.setNotes(String.valueOf(goodsInfo.get("Notes")));
			// 备案状态：1-备案中，2-备案成功，3-备案失败
			goodRecordInfo.setStatus(1);
			// 已备案商品状态:0-已备案,待审核,1-备案审核通过,2-正常备案
			goodRecordInfo.setRecordFlag(2);
			goodRecordInfo.setGoodsMerchantId(merchantId);
			goodRecordInfo.setGoodsMerchantName(merchantName);
			goodRecordInfo.setCreateBy(merchantName);
			goodRecordInfo.setCreateDate(date);
			// 删除标识:0-未删除,1-已删除
			goodRecordInfo.setDeleteFlag(0);
			goodRecordInfo.setGoodsSerialNo(goodsSerialNo);
			goodRecordInfo.setGoodsDetailId(goods.getGoodsId());
			// 计算(国内快递)物流费标识：1-无运费,2-计算运费;默认为：1
			goodRecordInfo.setFreightFlag(1);
			// 计算税费标识：1-计算税费,2-不计税费;默认为：1
			goodRecordInfo.setTaxFlag(1);
			goodRecordInfo.setIngredient(String.valueOf(goodsInfo.get("Ingredient")));
			goodRecordInfo.setAdditiveflag(String.valueOf(goodsInfo.get("Additiveflag")));
			goodRecordInfo.setPoisonflag(String.valueOf(goodsInfo.get("Poisonflag")));
			if (!goodsRecordDao.add(goodRecordInfo)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "保存商品备案信息错误,服务器繁忙!");
				// goodsRecordDao.delete(recordInfo);
				return statusMap;
			}
			reGoodsInfoList.add(goodRecordInfo);
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.DATAS.toString(), reGoodsInfoList);
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
		List<Object> reList = goodsRecordDao.findByProperty(WarehouseContent.class, paramsMap, 0, 0);
		// 数据库查询错误
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (reList.isEmpty()) {// 当没有仓库时
			WarehouseContent warehous = new WarehouseContent();
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
				return statusMap;
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
		long totalCount = goodsRecordDao.findByPropertyCount(GoodsRecordDetail.class, params);
		params.clear();
		if (reList != null && reList.size() >= 0) {
			params.put(BaseCode.DATAS.toString(), reList);
			params.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			params.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			params.put(BaseCode.TOTALCOUNT.toString(), totalCount);
			return params;
		}
		params.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		params.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		return params;
	}

	/**
	 * 服务器接收备案商品信息成功后,更新订单返回信息Id
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param goodsSerialNo
	 *            备案商品流水
	 * @param msgId
	 *            返回信息
	 * @return Map
	 */
	private Map<String, Object> updateGoodsRecordInfo(String merchantId, String goodsSerialNo, String msgId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("goodsSerialNo", goodsSerialNo);
		paramMap.put("goodsMerchantId", merchantId);
		List<Object> reList = goodsRecordDao.findByProperty(GoodsRecordDetail.class, paramMap, 0, 0);
		paramMap.clear();
		if (reList != null && reList.size() > 0) {
			for (int i = 0; i < reList.size(); i++) {
				GoodsRecordDetail goods = (GoodsRecordDetail) reList.get(i);
				goods.setReSerialNo(msgId);
				if (!goodsRecordDao.update(goods)) {
					paramMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					paramMap.put(BaseCode.MSG.toString(), "更新服务器返回messageID错误!");
					return paramMap;
				}
			}
		}
		paramMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		paramMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return paramMap;
	}

	@Override
	public Map<String, Object> updateGoodsRecordInfo(Map<String, Object> datasMap) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		String entGoodsNo = datasMap.get("entGoodsNo") + "";
		paramMap.put("reSerialNo", datasMap.get("messageID") + "");
		paramMap.put("entGoodsNo", entGoodsNo);
		String reMsg = datasMap.get("msg") + "";
		List<Object> reGoodsRecordList = goodsRecordDao.findByProperty(GoodsRecordDetail.class, paramMap, 1, 1);
		if (reGoodsRecordList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reGoodsRecordList.isEmpty()) {
			GoodsRecordDetail goodsRecord = (GoodsRecordDetail) reGoodsRecordList.get(0);
			int status = Integer.parseInt(datasMap.get("status") + "");
			String note = goodsRecord.getReNote();
			if ("null".equals(note) || note == null) {
				note = "";
			}
			if (status == 1) {
				goodsRecord.setCiqGoodsNo(datasMap.get("ciqGoodsNo") + "");
				goodsRecord.setEportGoodsNo(datasMap.get("eportGoodsNo") + "");
				// 商品备案状态修改为成功
				goodsRecord.setStatus(2);
				String goodsDetailId = goodsRecord.getGoodsDetailId();
				// 复制一份商品基本信息到商品备案信息中
				Map<String, Object> reMap = cloneGoodsDetail(goodsRecord, goodsDetailId);
				if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
					return reMap;
				}
			} else if (status < 0) {
				// 商品备案状态修改为备案失败
				goodsRecord.setStatus(3);
			}
			String recTime = DateUtil.toStringDate(datasMap.get("recTime") + "");
			goodsRecord.setReNote(note + recTime + "#" + reMsg + ";");
			goodsRecord.setUpdateDate(date);
			goodsRecord.setUpdateBy("system");
			if (!goodsRecordDao.update(goodsRecord)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "异步更新备案商品信息错误!");
				return paramMap;
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
		return statusMap;
	}

	private Map<String, Object> cloneGoodsDetail(GoodsRecordDetail goodsRecord, String goodsDetailId) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("goodsId", goodsDetailId);
		List<Object> reGoodsList = goodsRecordDao.findByProperty(GoodsContent.class, paramMap, 1, 1);
		if (reGoodsList != null && reGoodsList.size() > 0) {
			GoodsContent goodsInfo = (GoodsContent) reGoodsList.get(0);
			goodsRecord.setSpareGoodsName(goodsInfo.getGoodsName());
			goodsRecord.setSpareGoodsFirstTypeId(goodsInfo.getGoodsFirstTypeId());
			goodsRecord.setSpareGoodsFirstTypeName(goodsInfo.getGoodsFirstTypeName());
			goodsRecord.setSpareGoodsSecondTypeId(goodsInfo.getGoodsSecondTypeId());
			goodsRecord.setSpareGoodsSecondTypeName(goodsInfo.getGoodsSecondTypeName());
			goodsRecord.setSpareGoodsThirdTypeId(goodsInfo.getGoodsThirdTypeId());
			goodsRecord.setSpareGoodsThirdTypeName(goodsInfo.getGoodsThirdTypeName());
			goodsRecord.setSpareGoodsImage(goodsInfo.getGoodsImage());
			goodsRecord.setSpareGoodsDetail(goodsInfo.getGoodsDetail());
			goodsRecord.setSpareGoodsBrand(goodsInfo.getGoodsBrand());
			goodsRecord.setSpareGoodsStyle(goodsInfo.getGoodsStyle());
			goodsRecord.setSpareGoodsUnit(goodsInfo.getGoodsUnit());
			goodsRecord.setSpareGoodsOriginCountry(goodsInfo.getGoodsOriginCountry());
			goodsRecord.setSpareGoodsBarCode(goodsInfo.getGoodsBarCode());
			// 计算税费标识：1-计算税费,2-不计税费
			goodsRecord.setTaxFlag(1);
			// 计算(国内快递)物流费标识：1-无运费,2-手动设置运费
			goodsRecord.setFreightFlag(1);
			if (!goodsRecordDao.update(goodsRecord)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "异步回调时,复制商品基本信息错误!");
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return statusMap;
	}

	private Map<String, Object> checkData(JSONArray jsonList, Integer customsPort) {
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("GoodsDetailId");
		noNullKeys.add("ShelfGName");
		noNullKeys.add("NcadCode");
		noNullKeys.add("HSCode");
		noNullKeys.add("GoodsName");
		noNullKeys.add("GoodsStyle");
		noNullKeys.add("Brand");
		noNullKeys.add("GUnit");
		noNullKeys.add("StdUnit");
		noNullKeys.add("RegPrice");
		noNullKeys.add("GiftFlag");
		noNullKeys.add("OriginCountry");
		noNullKeys.add("Quality");
		noNullKeys.add("Manufactory");
		noNullKeys.add("NetWt");
		noNullKeys.add("GrossWt");
		if (customsPort == 2) {
			noNullKeys.add("Ingredient");
		}
		return CheckDatasUtil.checkData(jsonList, noNullKeys);
	}

	@Override
	public Map<String, Object> getMerchantGoodsRecordDetail(String merchantId, String merchantName, String entGoodsNo) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Object> itemList = new ArrayList<>();
		paramMap.put("entGoodsNo", entGoodsNo);
		List<Object> reGoodsRecordInfo = goodsRecordDao.findByProperty(GoodsRecordDetail.class, paramMap, 1, 1);
		List<Object> reStockList = goodsRecordDao.findByProperty(StockContent.class, paramMap, 1, 1);
		if (reGoodsRecordInfo == null || reStockList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		} else if (!reGoodsRecordInfo.isEmpty()) {
			paramMap.clear();
			paramMap.put("goods", reGoodsRecordInfo);
			paramMap.put("stock", reStockList);
			itemList.add(paramMap);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), itemList);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return statusMap;
	}

	@Override
	public Map<String, Object> editMerchantRecordGoodsDetailInfo(String merchantId, String merchantName,
			Map<String, Object> paramMap, int type) {
		StockContent stockInfo = null;
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		List<Object> imgList = (List<Object>) paramMap.get("imgList");
		String goodsImage = "";
		// 拼接多张图片字符串
		for (int i = 0; i < imgList.size(); i++) {
			String imgStr = imgList.get(i) + "";
			goodsImage = goodsImage + imgStr + ";";
		}
		String goodsName = String.valueOf(paramMap.get("spareGoodsName"));
		String goodsFirstTypeId = String.valueOf(paramMap.get("spareGoodsFirstTypeId"));
		String goodsFirstTypeName = String.valueOf(paramMap.get("spareGoodsFirstTypeName"));
		String goodsSecondTypeId = String.valueOf(paramMap.get("spareGoodsSecondTypeId"));
		String goodsSecondTypeName = String.valueOf(paramMap.get("spareGoodsSecondTypeName"));
		String goodsThirdTypeId = String.valueOf(paramMap.get("spareGoodsThirdTypeId"));
		String goodsThirdTypeName = String.valueOf(paramMap.get("spareGoodsThirdTypeName"));
		String goodsDetail = String.valueOf(paramMap.get("spareGoodsDetail"));
		String goodsBrand = String.valueOf(paramMap.get("spareGoodsBrand"));
		String goodsStyle = String.valueOf(paramMap.get("spareGoodsStyle"));
		String goodsUnit = String.valueOf(paramMap.get("spareGoodsUnit"));
		String goodsOriginCountry = String.valueOf(paramMap.get("spareGoodsOriginCountry"));
		String goodsBarCode = String.valueOf(paramMap.get("spareGoodsBarCode"));
		int taxFlag;
		int freightFlag;
		double regPrice;
		double marketPrice;
		try {
			taxFlag = Integer.parseInt(paramMap.get("taxFlag") + "");
			freightFlag = Integer.parseInt(paramMap.get("freightFlag") + "");
			regPrice = Double.parseDouble(String.valueOf(paramMap.get("regPrice")));
			marketPrice = Double.parseDouble(String.valueOf(paramMap.get("marketPrice")));
		} catch (Exception e) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "字段错误,请重新输入！");
			return statusMap;
		}
		params.put("entGoodsNo", paramMap.get("entGoodsNo"));
		params.put("goodsMerchantId", merchantId);
		// 根据商品ID查询商品基本信息
		List<Object> reGoodsList = goodsRecordDao.findByProperty(GoodsRecordDetail.class, params, 0, 0);
		params.clear();
		params.put("entGoodsNo", paramMap.get("entGoodsNo"));
		List<Object> reStockList = goodsRecordDao.findByProperty(StockContent.class, params, 0, 0);
		if (reGoodsList == null || reStockList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reGoodsList.isEmpty()) {
			GoodsRecordDetail goodsRecordInfo = (GoodsRecordDetail) reGoodsList.get(0);
			// type 1-全部修改,2-修改商品信息(价格除外),3-只修改商品价格(商品基本信息不修改)
			switch (type) {
			case 1:
				stockInfo = (StockContent) reStockList.get(0);
				goodsRecordInfo.setSpareGoodsName(goodsName);
				goodsRecordInfo.setSpareGoodsImage(goodsImage);
				goodsRecordInfo.setSpareGoodsFirstTypeId(goodsFirstTypeId);
				goodsRecordInfo.setSpareGoodsFirstTypeName(goodsFirstTypeName);
				goodsRecordInfo.setSpareGoodsSecondTypeId(goodsSecondTypeId);
				goodsRecordInfo.setSpareGoodsSecondTypeName(goodsSecondTypeName);
				goodsRecordInfo.setSpareGoodsThirdTypeId(goodsThirdTypeId);
				goodsRecordInfo.setSpareGoodsThirdTypeName(goodsThirdTypeName);
				goodsRecordInfo.setSpareGoodsDetail(goodsDetail);
				goodsRecordInfo.setSpareGoodsBrand(goodsBrand);
				goodsRecordInfo.setSpareGoodsStyle(goodsStyle);
				goodsRecordInfo.setSpareGoodsUnit(goodsUnit);
				goodsRecordInfo.setSpareGoodsOriginCountry(goodsOriginCountry);
				goodsRecordInfo.setSpareGoodsBarCode(goodsBarCode);
				goodsRecordInfo.setTaxFlag(taxFlag);
				goodsRecordInfo.setFreightFlag(freightFlag);
				goodsRecordInfo.setUpdateDate(date);
				goodsRecordInfo.setUpdateBy(merchantName);
				stockInfo.setRegPrice(regPrice);
				stockInfo.setMarketPrice(marketPrice);
				stockInfo.setUpdateDate(date);
				stockInfo.setUpdateBy(merchantName);
				if (!goodsRecordDao.update(goodsRecordInfo) || !goodsRecordDao.update(stockInfo)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "修改备案商品中基本信息或库存价格错误！");
					return statusMap;
				}
				break;
			case 2:
				goodsRecordInfo.setSpareGoodsName(goodsName);
				goodsRecordInfo.setSpareGoodsImage(goodsImage);
				goodsRecordInfo.setSpareGoodsFirstTypeId(goodsFirstTypeId);
				goodsRecordInfo.setSpareGoodsFirstTypeName(goodsFirstTypeName);
				goodsRecordInfo.setSpareGoodsSecondTypeId(goodsSecondTypeId);
				goodsRecordInfo.setSpareGoodsSecondTypeName(goodsSecondTypeName);
				goodsRecordInfo.setSpareGoodsThirdTypeId(goodsThirdTypeId);
				goodsRecordInfo.setSpareGoodsThirdTypeName(goodsThirdTypeName);
				goodsRecordInfo.setSpareGoodsDetail(goodsDetail);
				goodsRecordInfo.setSpareGoodsBrand(goodsBrand);
				goodsRecordInfo.setSpareGoodsStyle(goodsStyle);
				goodsRecordInfo.setSpareGoodsUnit(goodsUnit);
				goodsRecordInfo.setSpareGoodsOriginCountry(goodsOriginCountry);
				goodsRecordInfo.setSpareGoodsBarCode(goodsBarCode);
				goodsRecordInfo.setTaxFlag(taxFlag);
				goodsRecordInfo.setFreightFlag(freightFlag);
				goodsRecordInfo.setUpdateDate(date);
				goodsRecordInfo.setUpdateBy(merchantName);
				if (!goodsRecordDao.update(goodsRecordInfo)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "修改备案商品中基本信息错误！");
					return statusMap;
				}
				break;
			case 3:
				stockInfo = (StockContent) reStockList.get(0);
				stockInfo.setRegPrice(regPrice);
				stockInfo.setMarketPrice(marketPrice);
				stockInfo.setUpdateDate(date);
				stockInfo.setUpdateBy(merchantName);
				if (!goodsRecordDao.update(stockInfo)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "修改商品价格错误！");
					return statusMap;
				}
				break;
			default:

				break;
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return statusMap;
	}

	@Override
	public Map<String, Object> merchantAddAlreadyRecordGoodsInfo(String merchantId, String merchantName,
			Map<String, Object> paramMap) {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		Map<String, Object> statusMap = new HashMap<>();
		GoodsRecordDetail goodsRecordDetail = new GoodsRecordDetail();
		// 查询数据库字段名
		String property = "entGoodsNo";
		// 根据年份查询,当前年份下的id数量
		long goodsRecordSerialNoCount = goodsRecordDao.findSerialNoCount(GoodsRecordDetail.class, property, year);
		// 当返回-1时,则查询数据库失败
		if (goodsRecordSerialNoCount < 0) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		String goodsRecordSerialNo = SerialNoUtils.getSerialNotTimestamp("GR_", year, goodsRecordSerialNoCount);
		goodsRecordDetail.setSeq(1);
		goodsRecordDetail.setEntGoodsNo(goodsRecordSerialNo);
		goodsRecordDetail.setEportGoodsNo(String.valueOf(paramMap.get("EPortGoodsNo")));
		goodsRecordDetail.setCiqGoodsNo(String.valueOf(paramMap.get("CIQGoodsNo")));
		goodsRecordDetail.setCusGoodsNo(String.valueOf(paramMap.get("CusGoodsNo")));
		goodsRecordDetail.setEmsNo(String.valueOf(paramMap.get("EmsNo")));
		goodsRecordDetail.setItemNo(String.valueOf(paramMap.get("ItemNo")));
		goodsRecordDetail.setShelfGName(paramMap.get("ShelfGName") + "");
		goodsRecordDetail.setNcadCode(paramMap.get("NcadCode") + "");
		goodsRecordDetail.setHsCode(paramMap.get("HSCode") + "");
		goodsRecordDetail.setBarCode(paramMap.get("BarCode") + "");
		goodsRecordDetail.setGoodsName(paramMap.get("GoodsName") + "");
		goodsRecordDetail.setGoodsStyle(paramMap.get("GoodsStyle") + "");
		goodsRecordDetail.setBrand(paramMap.get("Brand") + "");
		goodsRecordDetail.setgUnit(paramMap.get("GUnit") + "");
		goodsRecordDetail.setStdUnit(paramMap.get("StdUnit") + "");
		goodsRecordDetail.setSecUnit(paramMap.get("SecUnit") + "");
		goodsRecordDetail.setRegPrice(Double.valueOf(paramMap.get("RegPrice") + ""));
		goodsRecordDetail.setGiftFlag(paramMap.get("GiftFlag") + "");
		goodsRecordDetail.setOriginCountry(String.valueOf(paramMap.get("OriginCountry")));
		goodsRecordDetail.setQuality(paramMap.get("Quality") + "");
		goodsRecordDetail.setQualityCertify(paramMap.get("QualityCertify") + "");
		goodsRecordDetail.setManufactory(paramMap.get("Manufactory") + "");
		goodsRecordDetail.setNetWt(Double.valueOf(paramMap.get("NetWt") + ""));
		goodsRecordDetail.setGrossWt(Double.valueOf(paramMap.get("GrossWt") + ""));
		goodsRecordDetail.setNotes(String.valueOf(paramMap.get("Notes")));
		// 备案状态：1-备案中，2-备案成功，3-备案失败
		goodsRecordDetail.setStatus(1);
		// 已备案商品状态:0-已备案,待审核,1-备案审核通过,2-正常备案
		goodsRecordDetail.setRecordFlag(0);
		goodsRecordDetail.setGoodsMerchantId(merchantId);
		goodsRecordDetail.setGoodsMerchantName(merchantName);
		goodsRecordDetail.setCreateBy(merchantName);
		goodsRecordDetail.setCreateDate(date);
		// 删除标识:0-未删除,1-已删除
		goodsRecordDetail.setDeleteFlag(0);
		if (!goodsRecordDao.add(goodsRecordDetail)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "保存商品备案信息错误,服务器繁忙!");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> searchGoodsRecordInfo(String merchantId, String merchantName, Map<String, Object> datasMap,
			int page, int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reDatasMap = stockServiceImpl.universalSearch(datasMap);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		Map<String, Object> blurryMap = (Map<String, Object>) reDatasMap.get("blurry");
		List<Map<String, Object>> errorList = (List<Map<String, Object>>) reDatasMap.get("error");
		paramMap.put("goodsMerchantId", merchantId);
		paramMap.put("deleteFlag", 0);
		List<Object> reList = goodsRecordDao.findByPropertyLike(GoodsRecordDetail.class, paramMap, blurryMap, page,
				size);
		long totalCount = goodsRecordDao.findByPropertyLikeCount(GoodsRecordDetail.class, paramMap, blurryMap);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			statusMap.put(BaseCode.ERROR.toString(), errorList);
			return statusMap;
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), totalCount);
			statusMap.put(BaseCode.ERROR.toString(), errorList);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			statusMap.put(BaseCode.ERROR.toString(), errorList);
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> batchCreateNotRecordGoods(int seq,String shelfGName, String ncadCode, String hsCode,
			String barCode, String goodsName, String goodsStyle, String brand, String gUnit, String stdUnit,
			String secUnit, Double regPrice, String giftFlag, String originCountry, String quality,
			String qualityCertify, String manufactory, Double netWt, Double grossWt, String notes,String merchantId ,String merchantName,String ingredient,String additiveflag,String poisonflag) {
		Map<String, Object> statusMap = new HashMap<>();
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
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
		String goodsRecordSerialNo = SerialNoUtils.getSerialNotTimestamp("GR_", year, goodsRecordSerialNoCount);
		GoodsRecordDetail goodRecordInfo = new GoodsRecordDetail();
		goodRecordInfo.setSeq(seq);
		goodRecordInfo.setEntGoodsNo(goodsRecordSerialNo);
		goodRecordInfo.setShelfGName(shelfGName);
		goodRecordInfo.setNcadCode(ncadCode);
		goodRecordInfo.setHsCode(hsCode);
		goodRecordInfo.setBarCode(barCode);
		goodRecordInfo.setGoodsName(goodsName);
		goodRecordInfo.setGoodsStyle(goodsStyle);
		goodRecordInfo.setBrand(brand);
		goodRecordInfo.setgUnit(gUnit);
		goodRecordInfo.setStdUnit(stdUnit);
		goodRecordInfo.setSecUnit(secUnit);
		goodRecordInfo.setRegPrice(regPrice);
		goodRecordInfo.setGiftFlag(giftFlag);
		goodRecordInfo.setOriginCountry(originCountry);
		goodRecordInfo.setQuality(quality);
		goodRecordInfo.setQualityCertify(qualityCertify);
		goodRecordInfo.setManufactory(manufactory);
		goodRecordInfo.setNetWt(netWt);
		goodRecordInfo.setGrossWt(grossWt);
		goodRecordInfo.setNotes(notes);
		// 备案状态：1-备案中，2-备案成功，3-备案失败,4-未备案
		goodRecordInfo.setStatus(4);
		// 已备案商品状态:0-已备案,待审核,1-备案审核通过,2-正常备案
		goodRecordInfo.setRecordFlag(2);
		goodRecordInfo.setGoodsMerchantId(merchantId);
		goodRecordInfo.setGoodsMerchantName(merchantName);
		goodRecordInfo.setCreateBy(merchantName);
		goodRecordInfo.setCreateDate(date);
		// 删除标识:0-未删除,1-已删除
		goodRecordInfo.setDeleteFlag(0);
		//goodRecordInfo.setGoodsSerialNo(goodsRecordSerialNo);
		// 计算(国内快递)物流费标识：1-无运费,2-计算运费;默认为：1
		goodRecordInfo.setFreightFlag(1);
		// 计算税费标识：1-计算税费,2-不计税费;默认为：1
		goodRecordInfo.setTaxFlag(1);
		goodRecordInfo.setIngredient(ingredient);
		goodRecordInfo.setAdditiveflag(additiveflag);
		goodRecordInfo.setPoisonflag(poisonflag);
		if (!goodsRecordDao.add(goodRecordInfo)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "保存商品备案信息错误,服务器繁忙!");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}
}
