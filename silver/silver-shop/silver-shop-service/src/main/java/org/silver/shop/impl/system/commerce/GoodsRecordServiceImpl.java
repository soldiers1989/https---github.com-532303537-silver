package org.silver.shop.impl.system.commerce;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.loader.custom.Return;
import org.silver.common.BaseCode;
import org.silver.common.RedisKey;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.CustomsPortService;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.api.system.commerce.GoodsRecordService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.commerce.GoodsRecordDao;
import org.silver.shop.impl.common.base.CustomsPortServiceImpl;
import org.silver.shop.model.common.base.CustomsPort;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecord;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.commerce.WarehouseContent;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.util.SearchUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.DateUtil;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.StringUtil;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

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
			List<Object> goodsRecordList = goodsRecordDao.findByPropertyDesc(GoodsRecordDetail.class, params, descParam,
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
		int port = Integer.parseInt(customsPort);
		// 校验前台传递口岸、海关、智检编码
		datasMap = checkCustomsPort(port, customsCode, ciqOrgCode);
		if (!datasMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return datasMap;
		}
		CustomsPort portInfo = (CustomsPort) datasMap.get(BaseCode.DATAS.toString());
		// 根据商户ID,口岸编码查询商户备案信息
		Map<String, Object> merchantInfoMap = getMerchantInfo(merchantId, Integer.valueOf(customsPort));
		if (!merchantInfoMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return merchantInfoMap;
		}
		Map<String, Object> mRecordMap = (Map<String, Object>) merchantInfoMap.get(BaseCode.DATAS.toString());
		// 请求获取tok
		Map<String, Object> reTokMap = accessTokenService.getRedisToks(YmMallConfig.APPKEY, YmMallConfig.APPSECRET);
		if (!reTokMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return reTokMap;
		}
		String tok = reTokMap.get(BaseCode.DATAS.toString()) + "";
		// 验证前台传值
		Map<String, Object> reGoodsMap = checkData(jsonList, port, 1);
		if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
			return reGoodsMap;
		}
		JSONArray jsonGoods = JSONArray.fromObject(reGoodsMap.get(BaseCode.DATAS.toString()));

		// 保存商品备案信息头
		Map<String, Object> reGoodsHeadMap = saveRecordHeadInfo(merchantId, merchantName, portInfo, mRecordMap);
		if (!"1".equals(reGoodsHeadMap.get(BaseCode.STATUS.toString()) + "")) {
			return reGoodsHeadMap;
		}
		// 获取商品备案流水号
		String goodsSerialNo = reGoodsHeadMap.get(BaseCode.DATAS.toString()) + "";
		// 保存备案商品详情
		Map<String, Object> reRecordGoodsMap = saveRecordGoodsInfo(merchantId, merchantName, jsonGoods, goodsSerialNo,
				mRecordMap, port);
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
		String customsName = portInfo.getCustomsName();
		// 创建商品仓库
		Map<String, Object> warehousMap = createWarehous(merchantId, merchantName, customsCode, customsName);
		if (!warehousMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return warehousMap;
		}
		// 发起商品备案
		Map<String, Object> recordMap = sendRecord(port, mRecordMap, tok, datas, goodsSerialNo, ciqOrgCode,
				customsCode);
		if (!"1".equals(recordMap.get(BaseCode.STATUS.toString()) + "")) {
			return recordMap;
		}
		// 服务器接收信息后更新商品备案信息
		return updateGoodsRecordInfo(recordMap, merchantId, goodsSerialNo);
	}

	@Override
	public Map<String, Object> checkCustomsPort(int eport, String customsCode, String ciqOrgCode) {
		Map<String, Object> paramsMap = null;
		List<CustomsPort> customsPortList = null;
		byte[] redisByte = JedisUtil.get(RedisKey.SHOP_KEY_ALL_PORT_CUSTOMS_LIST.getBytes());
		if (redisByte != null) {
			customsPortList = (List<CustomsPort>) SerializeUtil.toObject(redisByte);
		} else {// 缓存中没有数据,重新访问数据库读取数据
			paramsMap = customsPortService.findAllCustomsPort();
			if (!paramsMap.get(BaseCode.STATUS.toString()).equals("1")) {
				return paramsMap;
			}
			customsPortList = (List<CustomsPort>) paramsMap.get(BaseCode.DATAS.toString());
			// 将查询出来的口岸数据放入缓存中
			JedisUtil.set(RedisKey.SHOP_KEY_ALL_PORT_CUSTOMS_LIST.getBytes(), SerializeUtil.toBytes(customsPortList),
					86400);
		}
		for (int i = 0; i < customsPortList.size(); i++) {
			CustomsPort portInfo = customsPortList.get(i);
			// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
			int reCustomsPort = portInfo.getCustomsPort();
			// 主管海关代码
			String reCustomsCode = portInfo.getCustomsCode();
			// 检验检疫机构代码
			String reCiqOrgCode = portInfo.getCiqOrgCode();
			// 判断前端传递的口岸端口、海关代码、智检代码是否正确
			if (reCustomsPort == eport && reCustomsCode.trim().equals(customsCode.trim())
					&& reCiqOrgCode.trim().equals(ciqOrgCode.trim())) {
				return ReturnInfoUtils.successDataInfo(portInfo);
			}
		}
		return ReturnInfoUtils.errorInfo("海关口岸、主管海关、检验检疫机构代码错误,请核对信息!");
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
	 *            1-广州电子口岸(目前只支持BC业务) 2-南沙智检(支持BBC业务)
	 * @return Map
	 */
	public final Map<String, Object> getMerchantInfo(String merchantId, int port) {
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		params.put("customsPort", port);
		List<MerchantRecordInfo> datasList = goodsRecordDao.findByProperty(MerchantRecordInfo.class, params, 0, 0);
		if (datasList != null && !datasList.isEmpty()) {
			Map<String, Object> mRecordMap = new HashMap<>();
			MerchantRecordInfo mRecordInfo = datasList.get(0);
			// 电商企业编号
			mRecordMap.put("ebEntNo", mRecordInfo.getEbEntNo());
			// 电商企业名称
			mRecordMap.put("ebEntName", mRecordInfo.getEbEntName());
			// 电商平台企业编号
			mRecordMap.put("ebpEntNo", mRecordInfo.getEbpEntNo());
			// 电商平台名称
			mRecordMap.put("ebpEntName", mRecordInfo.getEbpEntName());
			if (port == 2) {// 当为智检备案时,多获取一次电子口岸的电商企业海关备案号
				params.put("merchantId", merchantId);
				params.put("customsPort", 1);
				List<MerchantRecordInfo> datasList2 = goodsRecordDao.findByProperty(MerchantRecordInfo.class, params, 0,
						0);
				if (datasList2 != null && !datasList2.isEmpty()) {
					MerchantRecordInfo mRecordInfo2 = datasList2.get(0);
					mRecordMap.put("DZKNNo", mRecordInfo2.getEbEntNo());
				} else {
					params.clear();
					params.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					params.put(BaseCode.MSG.toString(), "电商企业海关备案号错误,请核实是否填写【电子口岸】信息!");
					return params;
				}
			}
			params.clear();
			params.put(BaseCode.DATAS.toString(), mRecordMap);
			params.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			return params;
		}
		params.clear();
		params.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		params.put(BaseCode.MSG.toString(), "商户备案信息查询失败！");

		return params;
	}

	/**
	 * 发起商品备案信息
	 * 
	 * @param eport
	 *            端口号
	 * @param mRecordMap
	 *            商户备案信息
	 * @param tok
	 * @param datas
	 *            商品数据
	 * @param goodsSerialNo
	 *            商品流水号
	 * @param ciqOrgCode
	 *            主管海关代码
	 * @param customsCode
	 *            检验检疫机构代码
	 * @return Map
	 */
	private final Map<String, Object> sendRecord(int eport, Map<String, Object> mRecordMap, String tok,
			List<Object> datas, String goodsSerialNo, String ciqOrgCode, String customsCode) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
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
		params.put("ebEntNo", mRecordMap.get("ebEntNo") + "");
		// 电商企业名称
		params.put("ebEntName", mRecordMap.get("ebEntName") + "");
		params.put("appkey", YmMallConfig.APPKEY);
		params.put("ciqOrgCode", ciqOrgCode);
		params.put("customsCode", customsCode);
		params.put("clientsign", clientsign);
		params.put("timestamp", timestamp);
		params.put("datas", datas.toString());
		params.put("notifyurl", YmMallConfig.GOODSRECORDNOTIFYURL);
		params.put("note", note);
		// 商城商品备案流水号
		params.put("goodsSerialNo", goodsSerialNo);
		// 是否像海关发送
		// params.put("uploadOrNot", false);
		String resultStr = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/Eport/Report", params);
		if (StringUtil.isNotEmpty(resultStr)) {
			return JSONObject.fromObject(resultStr);
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		statusMap.put(BaseCode.MSG.toString(), "服务器接受备案信息失败,服务器繁忙！");
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
	 * @param ebpEntName
	 *            电商企业编号
	 * @param ebpEntNo
	 *            电商企业名称
	 * @param ebEntName
	 *            电商平台企业编号
	 * @param ebEntNo
	 *            电商平台名称
	 * @param jsonList
	 *            商品信息
	 * @return Map
	 */
	private final Map<String, Object> saveRecordHeadInfo(String merchantId, String merchantName, CustomsPort portInfo,
			Map<String, Object> mRecordMap) {
		Map<String, Object> statusMap = new HashMap<>();
		// 查询缓存中商品自编号自增Id
		int count = SerialNoUtils.getSerialNo("goodsRecordHead");
		String goodsRecordHeadSerialNo = SerialNoUtils.getSerialNo("GRH", count);
		Date date = new Date();
		GoodsRecord recordInfo = new GoodsRecord();
		recordInfo.setMerchantId(merchantId);
		recordInfo.setMerchantName(merchantName);
		recordInfo.setGoodsSerialNo(goodsRecordHeadSerialNo);
		// 海关口岸代码 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
		recordInfo.setCustomsPort(portInfo.getCustomsPort());
		recordInfo.setCustomsPortName(portInfo.getCustomsPortName());
		recordInfo.setCustomsCode(portInfo.getCustomsCode());
		recordInfo.setCustomsName(portInfo.getCustomsName());
		recordInfo.setCiqOrgCode(portInfo.getCiqOrgCode());
		recordInfo.setCiqOrgName(portInfo.getCiqOrgName());

		recordInfo.setEbEntNo(mRecordMap.get("ebEntNo") + "");
		recordInfo.setEbEntName(mRecordMap.get("ebEntName") + "");
		recordInfo.setEbpEntNo(mRecordMap.get("ebpEntNo") + "");
		recordInfo.setEbpEntName(mRecordMap.get("ebpEntName") + "");
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
		statusMap.put(BaseCode.DATAS.toString(), goodsRecordHeadSerialNo);
		return statusMap;
	}

	/**
	 * 保存(未)备案商品信息
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
			String goodsSerialNo, Map<String, Object> mRecordMap, int eport) {
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
			String goodsDetailId = goodsInfo.get("goodsDetailId") + "";
			params.put("goodsId", goodsDetailId);
			params.put("goodsMerchantId", merchantId);
			List<Object> reGoodsList = goodsRecordDao.findByProperty(GoodsContent.class, params, 1, 1);
			if (reGoodsList == null || reGoodsList.isEmpty()) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
				return statusMap;
			}
			GoodsContent goods = (GoodsContent) reGoodsList.get(0);
			goodRecordInfo.setSeq(i + 1);
			goodRecordInfo.setEntGoodsNo(goodsRecordSerialNo);
			goodRecordInfo.setEportGoodsNo(String.valueOf(goodsInfo.get("eportGoodsNo")));
			goodRecordInfo.setCiqGoodsNo(String.valueOf(goodsInfo.get("ciqGoodsNo")));
			goodRecordInfo.setCusGoodsNo(String.valueOf(goodsInfo.get("cusGoodsNo")));
			goodRecordInfo.setEmsNo(String.valueOf(goodsInfo.get("emsNo")));
			goodRecordInfo.setItemNo(String.valueOf(goodsInfo.get("itemNo")));
			goodRecordInfo.setShelfGName(goodsInfo.get("shelfGName") + "");
			goodRecordInfo.setNcadCode(goodsInfo.get("ncadCode") + "");
			goodRecordInfo.setHsCode(goodsInfo.get("hsCode") + "");
			goodRecordInfo.setBarCode(goodsInfo.get("barCode") + "");
			goodRecordInfo.setGoodsName(goodsInfo.get("goodsName") + "");
			goodRecordInfo.setGoodsStyle(goodsInfo.get("goodsStyle") + "");
			goodRecordInfo.setBrand(goodsInfo.get("brand") + "");
			goodRecordInfo.setgUnit(goodsInfo.get("gUnit") + "");
			goodRecordInfo.setStdUnit(goodsInfo.get("stdUnit") + "");
			goodRecordInfo.setSecUnit(goodsInfo.get("secUnit") + "");
			goodRecordInfo.setRegPrice(Double.valueOf(goodsInfo.get("regPrice") + ""));
			goodRecordInfo.setGiftFlag(goodsInfo.get("giftFlag") + "");
			goodRecordInfo.setOriginCountry(String.valueOf(goodsInfo.get("originCountry")));
			goodRecordInfo.setQuality(goodsInfo.get("quality") + "");
			goodRecordInfo.setQualityCertify(goodsInfo.get("qualityCertify") + "");
			goodRecordInfo.setManufactory(goodsInfo.get("manufactory") + "");
			goodRecordInfo.setNetWt(Double.valueOf(goodsInfo.get("netWt") + ""));
			goodRecordInfo.setGrossWt(Double.valueOf(goodsInfo.get("grossWt") + ""));
			goodRecordInfo.setNotes(String.valueOf(goodsInfo.get("notes")));
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
			goodRecordInfo.setIngredient(goodsInfo.get("ingredient") + "");
			goodRecordInfo.setAdditiveflag(goodsInfo.get("additiveflag") + "");
			goodRecordInfo.setPoisonflag(goodsInfo.get("poisonflag") + "");

			goodRecordInfo.setEbEntNo(mRecordMap.get("ebEntNo") + "");
			goodRecordInfo.setEbEntName(mRecordMap.get("ebEntName") + "");
			if (eport == 2) {
				goodRecordInfo.setDZKNNo(mRecordMap.get("DZKNNo") + "");
			}
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

	@Override
	public final Map<String, Object> createWarehous(String merchantId, String merchantName, String customsCode,
			String customsName) {
		if (StringEmptyUtils.isEmpty(merchantId) || StringEmptyUtils.isEmpty(merchantName)
				|| StringEmptyUtils.isEmpty(customsCode) || StringEmptyUtils.isEmpty(customsName)) {
			return ReturnInfoUtils.errorInfo("创建仓库时,请求参数不能为空!");
		}
		Date date = new Date();
		Map<String, Object> paramsMap = new HashMap<>();
		// 主管海关代码(同仓库编码)
		String code = merchantId + "_" + customsCode;
		// key=数据库列名,value=查询参数
		paramsMap.put("warehouseCode", code);
		List<Object> reList = goodsRecordDao.findByProperty(WarehouseContent.class, paramsMap, 0, 0);
		// 数据库查询错误
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询仓库信息失败,服务器繁忙!");
		} else if (reList.isEmpty()) {// 当没有仓库时
			WarehouseContent warehous = new WarehouseContent();
			warehous.setMerchantId(merchantId);
			warehous.setMerchantName(merchantName);
			warehous.setWarehouseCode(code);
			warehous.setWarehouseName(customsName);
			warehous.setCreateBy(merchantName);
			warehous.setCreateDate(date);
			warehous.setDeleteFlag(0);
			if (!goodsRecordDao.add(warehous)) {
				return ReturnInfoUtils.errorInfo("创建仓库失败,服务器繁忙!");
			}
		}
		return ReturnInfoUtils.successInfo();
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
	private Map<String, Object> updateGoodsRecordStatus(String merchantId, String goodsSerialNo, String msgId) {
		Date date = new Date();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("goodsSerialNo", goodsSerialNo);
		paramMap.put("goodsMerchantId", merchantId);
		List<Object> reList = goodsRecordDao.findByProperty(GoodsRecordDetail.class, paramMap, 0, 0);
		if (reList != null && reList.size() > 0) {
			for (int i = 0; i < reList.size(); i++) {
				GoodsRecordDetail goods = (GoodsRecordDetail) reList.get(i);
				goods.setReSerialNo(msgId);
				goods.setUpdateBy("system");
				goods.setUpdateDate(date);
				goods.setStatus(1);
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
		// paramMap.put("reSerialNo", datasMap.get("messageID") + "");
		paramMap.put("entGoodsNo", entGoodsNo);
		String reMsg = datasMap.get("msg") + "";
		List<Object> reGoodsRecordList = goodsRecordDao.findByPropertyOr2(GoodsRecordDetail.class, paramMap, 1, 1);
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
			return ReturnInfoUtils.successInfo();
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
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

	/**
	 * 备案时校验前台传递数据
	 * 
	 * @param jsonList
	 *            商品参数
	 * @param customsPort
	 *            口岸编码：1-电子口岸，2-智检
	 * @param status
	 *            检查备案状态：1-未备案检查,2-已备案检查
	 * @return Map
	 */
	private Map<String, Object> checkData(JSONArray jsonList, Integer customsPort, int status) {
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("shelfGName");
		noNullKeys.add("ncadCode");
		noNullKeys.add("hsCode");
		noNullKeys.add("goodsName");
		noNullKeys.add("goodsStyle");
		noNullKeys.add("brand");
		noNullKeys.add("gUnit");
		noNullKeys.add("stdUnit");
		noNullKeys.add("regPrice");
		noNullKeys.add("giftFlag");
		noNullKeys.add("originCountry");
		noNullKeys.add("quality");
		noNullKeys.add("manufactory");
		noNullKeys.add("netWt");
		noNullKeys.add("grossWt");
		if (customsPort == 2) {
			noNullKeys.add("ingredient");
		}
		if (status == 2) {
			noNullKeys.add("entGoodsNo");
			noNullKeys.add("eportGoodsNo");
			noNullKeys.add("ciqGoodsNo");
			noNullKeys.add("cusGoodsNo");
		}
		return CheckDatasUtil.changeMsg(jsonList, noNullKeys);
	}

	@Override
	public Map<String, Object> getGoodsRecordDetail(String entGoodsNo) {
		if (StringEmptyUtils.isEmpty(entGoodsNo)) {
			return ReturnInfoUtils.errorInfo("请求参数错误,商品自编号不能为空!");
		}
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("entGoodsNo", entGoodsNo);
		List<Object> reGoodsRecordInfo = goodsRecordDao.findByProperty(GoodsRecordDetail.class, paramMap, 1, 1);
		List<Object> reStockList = goodsRecordDao.findByProperty(StockContent.class, paramMap, 1, 1);
		if (reGoodsRecordInfo == null || reStockList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		} else if (!reGoodsRecordInfo.isEmpty()) {
			List<Object> itemList = new ArrayList<>();
			paramMap.clear();
			paramMap.put("goods", reGoodsRecordInfo);
			paramMap.put("stock", reStockList);
			itemList.add(paramMap);
			return ReturnInfoUtils.successDataInfo(itemList);
		} else {
			return ReturnInfoUtils.errorInfo("查询失败,未找到商品备案信息!");
		}
	}

	@Override
	public Map<String, Object> editMerchantRecordGoodsDetailInfo(String merchantId, String merchantName,
			Map<String, Object> paramMap, int type) {
		Map<String, Object> params = new HashMap<>();
		params.put("entGoodsNo", paramMap.get("entGoodsNo"));
		params.put("goodsMerchantId", merchantId);
		// 根据商品ID查询商品基本信息
		List<GoodsRecordDetail> reGoodsList = goodsRecordDao.findByProperty(GoodsRecordDetail.class, params, 0, 0);
		if (reGoodsList == null) {
			return ReturnInfoUtils.errorInfo("查询备案商品信息失败,服务器繁忙!");
		} else if (!reGoodsList.isEmpty()) {
			GoodsRecordDetail goodsRecordInfo = reGoodsList.get(0);
			//
			paramMap.put("goodsImage", paramMap.get("spareGoodsImage"));
			paramMap.put("merchantName", merchantName);
			// type 1-全部修改,2-修改商品信息(价格除外),3-只修改商品价格(商品基本信息不修改)
			switch (type) {
			case 1:
				Map<String, Object> reMap = editGoodsBaseInfo(paramMap, goodsRecordInfo);
				if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
					return reMap;
				}
				return editGoodsStockInfo(paramMap);
			case 2:
				return editGoodsBaseInfo(paramMap, goodsRecordInfo);
			case 3:
				return editGoodsStockInfo(paramMap);
			default:
				break;
			}
		}
		return ReturnInfoUtils.errorInfo("查询商品信息失败,服务器繁忙!");
	}

	/**
	 * 修改备案商品对应的库存商品信息
	 * 
	 * @param paramMap
	 *            修改参数
	 * @return Map
	 */
	private Map<String, Object> editGoodsStockInfo(Map<String, Object> paramMap) {
		Map<String, Object> params = new HashMap<>();
		double regPrice;
		double marketPrice;
		try {
			regPrice = Double.parseDouble(String.valueOf(paramMap.get("regPrice")));
			marketPrice = Double.parseDouble(String.valueOf(paramMap.get("marketPrice")));
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("字段错误,请重新输入!");
		}
		params.put("entGoodsNo", paramMap.get("entGoodsNo"));
		List<StockContent> reStockList = goodsRecordDao.findByProperty(StockContent.class, params, 0, 0);
		if (reStockList != null && !reStockList.isEmpty()) {
			StockContent stockInfo = reStockList.get(0);
			String merchantName = String.valueOf(paramMap.get("merchantName"));
			stockInfo.setRegPrice(regPrice);
			stockInfo.setMarketPrice(marketPrice);
			stockInfo.setUpdateDate(new Date());
			stockInfo.setUpdateBy(merchantName);
			if (!goodsRecordDao.update(stockInfo)) {
				return ReturnInfoUtils.errorInfo("修改库存信息错误!");
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("查询商品对应的库存信息失败,服务器繁忙!");
	}

	/**
	 * 修改商品备案信息中商品基本信息
	 * 
	 * @param paramMap
	 *            商品信息包
	 * @param goodsRecordInfo
	 *            商品备案信息实体类
	 * @return Map
	 */
	private Map<String, Object> editGoodsBaseInfo(Map<String, Object> paramMap, GoodsRecordDetail goodsRecordInfo) {
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
		String merchantName = String.valueOf(paramMap.get("merchantName"));
		String goodsImage = String.valueOf(paramMap.get("goodsImage"));
		int taxFlag;
		int freightFlag;
		try {
			taxFlag = Integer.parseInt(paramMap.get("taxFlag") + "");
			freightFlag = Integer.parseInt(paramMap.get("freightFlag") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("运费或税费标识错误,请重新输入!");
		}
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
		goodsRecordInfo.setUpdateDate(new Date());
		goodsRecordInfo.setUpdateBy(merchantName);
		if (!goodsRecordDao.update(goodsRecordInfo)) {
			return ReturnInfoUtils.errorInfo("修改备案商品中基本信息错误!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> merchantAddAlreadyRecordGoodsInfo(String merchantId, String merchantName,
			Map<String, Object> paramMap) {
		if (paramMap == null || paramMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		JSONArray jsonList = new JSONArray();
		// 电商企业编号
		String ebEntNo = paramMap.get("ebEntNo") + "";
		if (StringEmptyUtils.isEmpty(ebEntNo)) {
			return ReturnInfoUtils.errorInfo("电商企业编号不能为空！");
		}
		// 电商企业名称
		String ebEntName = paramMap.get("ebEntName") + "";
		if (StringEmptyUtils.isEmpty(ebEntName)) {
			return ReturnInfoUtils.errorInfo("电商企业名称不能为空");
		}
		// 口岸编码
		int customsPort = Integer.parseInt(paramMap.get("customsPort") + "");
		// 主管海关代码
		String customsCode = paramMap.get("customsCode") + "";
		// 检验检疫编码
		String ciqOrgCode = paramMap.get("ciqOrgCode") + "";
		// 检查口岸,海关代码是否正确
		Map<String, Object> customsMap = checkCustomsPort(customsPort, customsCode, ciqOrgCode);
		if (!"1".equals(customsMap.get(BaseCode.STATUS.toString()))) {
			return customsMap;
		}
		CustomsPort portInfo = (CustomsPort) customsMap.get(BaseCode.DATAS.toString());
		// 验证前台传值
		jsonList.add(paramMap);
		Map<String, Object> reDataMap = checkData(jsonList, customsPort, 2);
		if (!"1".equals(reDataMap.get(BaseCode.STATUS.toString()) + "")) {
			return reDataMap;
		}
		// 检查商品自编号是否存在
		String entGoodsNo = String.valueOf(paramMap.get("entGoodsNo"));
		Map<String, Object> checkMap = checkEntGoodsNoRepeat(entGoodsNo);
		if (!"1".equals(checkMap.get(BaseCode.STATUS.toString()))) {
			return checkMap;
		}
		String customsName = portInfo.getCustomsName();
		// 创建商品仓库
		Map<String, Object> warehousMap = createWarehous(merchantId, merchantName, customsCode, customsName);
		if (!warehousMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return warehousMap;
		}
		// 保存商品备案信息头
		Map<String, Object> reGoodsHeadMap = saveRecordHeadInfo(merchantId, merchantName, portInfo, paramMap);
		if (!"1".equals(reGoodsHeadMap.get(BaseCode.STATUS.toString()) + "")) {
			return reGoodsHeadMap;
		}
		// 获取商品备案流水号
		String goodsSerialNo = reGoodsHeadMap.get(BaseCode.DATAS.toString()) + "";
		// 保存已备案的商品信息
		Map<String, Object> reRecordMap = saveRecordGoodsInfo(paramMap, merchantId, merchantName, goodsSerialNo);
		if (!"1".equals(reRecordMap.get(BaseCode.STATUS.toString()) + "")) {
			return reRecordMap;
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 保存(已)备案商品信息
	 * 
	 * @param paramMap
	 *            商品信息
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param goodsSerialNo
	 *            备案(头部)流水编号
	 * @return Map
	 */
	private Map<String, Object> saveRecordGoodsInfo(Map<String, Object> paramMap, String merchantId,
			String merchantName, String goodsSerialNo) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		GoodsRecordDetail goodsRecordDetail = new GoodsRecordDetail();
		goodsRecordDetail.setSeq(1);
		goodsRecordDetail.setEntGoodsNo(String.valueOf(paramMap.get("entGoodsNo")));
		goodsRecordDetail.setEportGoodsNo(String.valueOf(paramMap.get("eportGoodsNo")));
		goodsRecordDetail.setCiqGoodsNo(String.valueOf(paramMap.get("ciqGoodsNo")));
		goodsRecordDetail.setCusGoodsNo(String.valueOf(paramMap.get("cusGoodsNo")));
		goodsRecordDetail.setEmsNo(String.valueOf(paramMap.get("emsNo")));
		goodsRecordDetail.setItemNo(String.valueOf(paramMap.get("itemNo")));
		goodsRecordDetail.setShelfGName(paramMap.get("shelfGName") + "");
		goodsRecordDetail.setNcadCode(paramMap.get("ncadCode") + "");
		goodsRecordDetail.setHsCode(paramMap.get("hsCode") + "");
		goodsRecordDetail.setBarCode(paramMap.get("barCode") + "");
		goodsRecordDetail.setGoodsName(paramMap.get("goodsName") + "");
		goodsRecordDetail.setGoodsStyle(paramMap.get("goodsStyle") + "");
		goodsRecordDetail.setBrand(paramMap.get("brand") + "");
		goodsRecordDetail.setgUnit(paramMap.get("gUnit") + "");
		goodsRecordDetail.setStdUnit(paramMap.get("stdUnit") + "");
		goodsRecordDetail.setSecUnit(paramMap.get("secUnit") + "");
		goodsRecordDetail.setGiftFlag(paramMap.get("giftFlag") + "");
		goodsRecordDetail.setOriginCountry(String.valueOf(paramMap.get("originCountry")));
		goodsRecordDetail.setQuality(paramMap.get("quality") + "");
		goodsRecordDetail.setQualityCertify(paramMap.get("qualityCertify") + "");
		goodsRecordDetail.setManufactory(paramMap.get("manufactory") + "");
		double regPrice = 0.0;
		double netWt = 0.0;
		double grossWt = 0.0;
		try {
			regPrice = Double.valueOf(paramMap.get("regPrice") + "");
			netWt = Double.valueOf(paramMap.get("netWt") + "");
			grossWt = Double.valueOf(paramMap.get("grossWt") + "");
		} catch (Exception e) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "单价或净重/毛重输入错误,请核对后重试!");
			return statusMap;
		}
		goodsRecordDetail.setRegPrice(regPrice);
		goodsRecordDetail.setNetWt(netWt);
		goodsRecordDetail.setGrossWt(grossWt);
		goodsRecordDetail.setNotes(String.valueOf(paramMap.get("notes")));
		// 备案状态：1-备案中，2-备案成功，3-备案失败
		goodsRecordDetail.setStatus(2);
		// 已备案商品状态:0-已备案,待审核,1-备案审核通过,2-正常备案
		goodsRecordDetail.setRecordFlag(0);
		goodsRecordDetail.setGoodsMerchantId(merchantId);
		goodsRecordDetail.setGoodsMerchantName(merchantName);
		goodsRecordDetail.setCreateBy(merchantName);
		goodsRecordDetail.setCreateDate(date);
		// 删除标识:0-未删除,1-已删除
		goodsRecordDetail.setDeleteFlag(0);
		goodsRecordDetail.setGoodsSerialNo(goodsSerialNo);
		goodsRecordDetail.setIngredient(paramMap.get("ingredient") + "");
		goodsRecordDetail.setAdditiveflag(paramMap.get("grossWt") + "");
		goodsRecordDetail.setPoisonflag(String.valueOf(paramMap.get("poisonflag")));
		// 计算税费标识：1-计算税费,2-不计税费;默认为：1
		goodsRecordDetail.setTaxFlag(1);
		// 计算(国内快递)物流费标识：1-无运费,2-计算运费;默认为：1
		goodsRecordDetail.setFreightFlag(1);
		if (!goodsRecordDao.add(goodsRecordDetail)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "保存商品备案信息错误,服务器繁忙!");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	@Override
	public Map<String, Object> searchGoodsRecordInfo(String merchantId, String merchantName,
			Map<String, Object> datasMap, int page, int size) {
		//
		Map<String, Object> reDatasMap = SearchUtils.universalRecordGoodsSearch(datasMap);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		Map<String, Object> blurryMap = (Map<String, Object>) reDatasMap.get("blurry");
		paramMap.put("goodsMerchantId", merchantId);
		paramMap.put("deleteFlag", 0);
		Table reList = goodsRecordDao.findByRecordInfoLike(GoodsRecordDetail.class, paramMap, blurryMap, page, size);
		Table reListCount = goodsRecordDao.findByRecordInfoLike(GoodsRecordDetail.class, paramMap, blurryMap, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.getRows().isEmpty()) {
			return ReturnInfoUtils.successDataInfo(Transform.tableToJson(reList), reListCount.getRows().size());
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> batchCreateNotRecordGoods(GoodsRecordDetail goodsRecordDetail, String merchantId,
			String merchantName) {
		Map<String, Object> statusMap = new HashMap<>();

		// 备案状态：1-备案中，2-备案成功，3-备案失败,4-未备案
		goodsRecordDetail.setStatus(4);
		// 已备案商品状态:0-已备案,待审核,1-备案审核通过,2-正常备案
		goodsRecordDetail.setRecordFlag(2);
		goodsRecordDetail.setGoodsMerchantId(merchantId);
		goodsRecordDetail.setGoodsMerchantName(merchantName);
		goodsRecordDetail.setCreateBy(merchantName);
		goodsRecordDetail.setCreateDate(new Date());
		// 删除标识:0-未删除,1-已删除
		goodsRecordDetail.setDeleteFlag(0);
		// goodRecordInfo.setGoodsSerialNo(goodsRecordSerialNo);
		// 计算(国内快递)物流费标识：1-无运费,2-计算运费;默认为：1
		goodsRecordDetail.setFreightFlag(1);
		// 计算税费标识：1-计算税费,2-不计税费;默认为：1
		goodsRecordDetail.setTaxFlag(1);
		if (!goodsRecordDao.add(goodsRecordDetail)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "保存商品备案信息错误,服务器繁忙!");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	@Override
	public Map<String, Object> merchantBatchOrSingleGoodsRecord(String goodsRecordInfo, String merchantId,
			String merchantName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Map<String, Object>> errorList = new ArrayList<>();
		JSONArray jsonList = null;
		String customsCode = "";
		String ciqOrgCode = "";
		try {
			jsonList = JSONArray.fromObject(goodsRecordInfo);
		} catch (Exception e) {
			logger.error(e);
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "前端传递基本信息参数错误！");
			return statusMap;
		}
		// 校验商品是否属于同一口岸
		Map<String, Object> checkMap = checkGoods(jsonList);
		if (!"1".equals(checkMap.get(BaseCode.STATUS.toString()))) {
			return checkMap;
		}
		for (int i = 0; i < jsonList.size(); i++) {
			// 默认不穿值时为-1,
			int eport = -1;
			Map<String, Object> item = (Map<String, Object>) jsonList.get(i);
			String entGoodsNo = item.get("entGoodsNo") + "";
			// 获取存在List缓存中已校验过的口岸
			eport = Integer.parseInt(item.get("eport") + "");
			customsCode = item.get("customsCode") + "";
			ciqOrgCode = item.get("ciqOrgCode") + "";
			// 根据商品编号检查商品是否存在
			Map<String, Object> reMap = checkGoodsRecord(entGoodsNo);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
			List<Object> reGoodsInfoList = (List<Object>) reMap.get(BaseCode.DATAS.toString());
			GoodsRecordDetail goodsInfo = (GoodsRecordDetail) reGoodsInfoList.get(0);
			// 备案状态：1-备案中，2-备案成功，3-备案失败,4-未备案
			int stauts = goodsInfo.getStatus();
			if (stauts == 2) {
				Map<String, Object> errmap = new HashMap<>();
				errmap.put(BaseCode.MSG.toString(), "第" + (i + 1) + "个商品---->已备案,无需重复发起");
				errorList.add(errmap);
				break;
			}
			// 商品备案流水号,只有发起过备案才会有
			String reGoodsSerialNo = goodsInfo.getGoodsSerialNo();
			Map<String, Object> reDataMap = readGoodsInfo(goodsInfo, eport);
			if (!"1".equals(reDataMap.get(BaseCode.STATUS.toString()) + "")) {
				Map<String, Object> errmap = new HashMap<>();
				errmap.put(BaseCode.MSG.toString(), reDataMap.get(BaseCode.MSG.toString()));
				errorList.add(errmap);
				break;
			}
			// 请求获取tok
			Map<String, Object> reTokMap = accessTokenService.getRedisToks(YmMallConfig.APPKEY, YmMallConfig.APPSECRET);
			if (!reTokMap.get(BaseCode.STATUS.toString()).equals("1")) {
				return reTokMap;
			}
			String tok = reTokMap.get(BaseCode.DATAS.toString()) + "";
			// 当商品没有备过案的时候
			if (goodsInfo.getGoodsSerialNo() == null || "".equals(goodsInfo.getGoodsSerialNo())
					|| "null".equals(goodsInfo.getGoodsSerialNo())) {
				// 校验前台传递口岸、海关、智检编码
				Map<String, Object> reCustomsPortMap = checkCustomsPort(eport, customsCode, ciqOrgCode);
				if (!reCustomsPortMap.get(BaseCode.STATUS.toString()).equals("1")) {
					Map<String, Object> errmap = new HashMap<>();
					errmap.put(BaseCode.MSG.toString(),
							"第" + (i + 1) + "个商品---->" + reCustomsPortMap.get(BaseCode.MSG.toString()));
					errorList.add(errmap);
					continue;
				}
				CustomsPort portInfo = (CustomsPort) reCustomsPortMap.get(BaseCode.DATAS.toString());
				// 根据商户ID,口岸编码查询商户备案信息
				Map<String, Object> merchantInfoMap = getMerchantInfo(merchantId, eport);
				if (!merchantInfoMap.get(BaseCode.STATUS.toString()).equals("1")) {
					Map<String, Object> errmap = new HashMap<>();
					errmap.put(BaseCode.MSG.toString(),
							"第" + i + 1 + "个商品---->" + merchantInfoMap.get(BaseCode.MSG.toString()));
					errorList.add(errmap);
					continue;
				}
				Map<String, Object> mRecordMap = (Map<String, Object>) merchantInfoMap.get(BaseCode.DATAS.toString());
				// 保存商品备案信息头
				Map<String, Object> reGoodsHeadMap = saveRecordHeadInfo(merchantId, merchantName, portInfo, mRecordMap);
				if (!"1".equals(reGoodsHeadMap.get(BaseCode.STATUS.toString()) + "")) {
					Map<String, Object> errmap = new HashMap<>();
					errmap.put(BaseCode.MSG.toString(),
							"第" + (i + 1) + "个商品---->" + reGoodsHeadMap.get(BaseCode.MSG.toString()));
					errorList.add(errmap);
					continue;
				}
				// 获取商品备案流水号
				String newGoodsSerialNo = reGoodsHeadMap.get(BaseCode.DATAS.toString()) + "";
				// 更新已存在的商品备案信息的流水号及商品备案
				goodsInfo.setGoodsSerialNo(newGoodsSerialNo);
				goodsInfo.setEbEntNo(mRecordMap.get("ebEntNo") + "");
				goodsInfo.setEbEntName(mRecordMap.get("ebEntName") + "");
				if (eport == 2) {
					goodsInfo.setDZKNNo(mRecordMap.get("DZKNNo") + "");
				}
				if (!goodsRecordDao.update(goodsInfo)) {
					Map<String, Object> errmap = new HashMap<>();
					errmap.put(BaseCode.MSG.toString(), "第" + (i + 1) + "个商品更新商品备案流水号失败,服务器繁忙!");
					errorList.add(errmap);
					continue;
				}
				// 封装备案商品信息
				List<Object> datas = null;
				Map<String, Object> reGoodsMap = addRecordInfo(reGoodsInfoList);
				if (!reGoodsMap.get(BaseCode.STATUS.toString()).equals("1")) {
					return reGoodsMap;
				}
				datas = (List) reGoodsMap.get(BaseCode.DATAS.toString());
				String customsName = portInfo.getCustomsName();
				// 创建商品仓库
				Map<String, Object> warehousMap = createWarehous(merchantId, merchantName, customsCode, customsName);
				if (!warehousMap.get(BaseCode.STATUS.toString()).equals("1")) {
					return warehousMap;
				}
				// 发起商品备案
				Map<String, Object> recordMap = sendRecord(eport, mRecordMap, tok, datas, newGoodsSerialNo, ciqOrgCode,
						customsCode);
				// 服务器接收信息后更新商品备案信息
				Map<String, Object> reUpdateMap = updateGoodsRecordInfo(recordMap, merchantId, newGoodsSerialNo);
				if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()) + "")) {
					Map<String, Object> errmap = new HashMap<>();
					errmap.put(BaseCode.MSG.toString(),
							"第" + (i + 1) + "个商品---->" + reUpdateMap.get(BaseCode.MSG.toString()));
					errorList.add(errmap);
					continue;
				}
			} else {// 当商品已经发起过一次备案
				MerchantRecordInfo merchantRecordInfo = null;
				Map<String, Object> mRecordMap = new HashMap<>();
				paramMap.put("merchantId", merchantId);
				paramMap.put("customsPort", eport);
				List<MerchantRecordInfo> reList = goodsRecordDao.findByProperty(MerchantRecordInfo.class, paramMap, 0,
						0);
				if (reList != null && !reList.isEmpty()) {
					merchantRecordInfo = reList.get(0);
					// 电商企业编号
					mRecordMap.put("ebEntNo", merchantRecordInfo.getEbEntNo());
					// 电商企业名称
					mRecordMap.put("ebEntName", merchantRecordInfo.getEbEntName());
					// 电商平台企业编号
					mRecordMap.put("ebpEntNo", merchantRecordInfo.getEbpEntNo());
					// 电商平台名称
					mRecordMap.put("ebpEntName", merchantRecordInfo.getEbpEntName());
				} else {
					Map<String, Object> errmap = new HashMap<>();
					errmap.put(BaseCode.MSG.toString(), "第" + (i + 1) + "个商品检查商品备案信息错误,服务器繁忙!");
					errorList.add(errmap);
					continue;
				}
				// 封装备案商品信息
				List<Object> datas = null;
				Map<String, Object> reGoodsMap = addRecordInfo(reGoodsInfoList);
				if (!reGoodsMap.get(BaseCode.STATUS.toString()).equals("1")) {
					Map<String, Object> errmap = new HashMap<>();
					errmap.put(BaseCode.MSG.toString(),
							"第" + (i + 1) + "个商品---->" + reGoodsMap.get(BaseCode.MSG.toString()));
					errorList.add(errmap);
					continue;
				}
				datas = (List) reGoodsMap.get(BaseCode.DATAS.toString());
				// 发起商品备案
				Map<String, Object> recordMap = sendRecord(eport, mRecordMap, tok, datas, reGoodsSerialNo, ciqOrgCode,
						customsCode);
				if (!"1".equals(recordMap.get(BaseCode.STATUS.toString()) + "")) {
					Map<String, Object> errmap = new HashMap<>();
					errmap.put(BaseCode.MSG.toString(),
							"第" + (i + 1) + "个商品---->" + recordMap.get(BaseCode.MSG.toString()));
					errorList.add(errmap);
					continue;
				}

				// 服务器接收信息后更新商品备案信息
				Map<String, Object> reUpdateMap = updateGoodsRecordInfo(recordMap, merchantId, reGoodsSerialNo);
				if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()) + "")) {
					Map<String, Object> errmap = new HashMap<>();
					errmap.put(BaseCode.MSG.toString(),
							"第" + (i + 1) + "个商品---->" + reUpdateMap.get(BaseCode.MSG.toString()));
					errorList.add(errmap);
					continue;
				}
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		statusMap.put(BaseCode.ERROR.toString(), errorList);
		return statusMap;
	}

	/**
	 * 校验数据库取出的商品数据
	 * 
	 * @param goodsInfo
	 * @param eport
	 * @return
	 */
	private Map<String, Object> readGoodsInfo(GoodsRecordDetail goodsInfo, int eport) {
		// 校验数据库取出的商品数据
		JSONArray dataKeys = new JSONArray();
		Map<String, Object> datasMap = new HashMap<>();
		datasMap.put("shelfGName", goodsInfo.getShelfGName());
		datasMap.put("ncadCode", goodsInfo.getNcadCode());
		datasMap.put("hsCode", goodsInfo.getHsCode());
		datasMap.put("goodsName", goodsInfo.getGoodsName());
		datasMap.put("goodsStyle", goodsInfo.getGoodsStyle());
		datasMap.put("brand", goodsInfo.getBrand());
		datasMap.put("gUnit", goodsInfo.getgUnit());
		datasMap.put("stdUnit", goodsInfo.getStdUnit());
		datasMap.put("regPrice", goodsInfo.getRegPrice());
		datasMap.put("giftFlag", goodsInfo.getGiftFlag());
		datasMap.put("originCountry", goodsInfo.getOriginCountry());
		datasMap.put("quality", goodsInfo.getQuality());
		datasMap.put("manufactory", goodsInfo.getManufactory());
		datasMap.put("netWt", goodsInfo.getNetWt());
		datasMap.put("grossWt", goodsInfo.getGrossWt());
		datasMap.put("ingredient", goodsInfo.getIngredient());
		dataKeys.add(datasMap);
		// 验证前台传值
		return checkData(dataKeys, eport, 1);
	}

	/**
	 * 批量发起商品时检查是否都属于同一口岸
	 * 
	 * @param jsonList
	 * @return
	 */
	private Map<String, Object> checkGoods(JSONArray jsonList) {
		List<Object> cacheList = new ArrayList<>();
		Map<String, Object> statusMap = new HashMap<>();
		if (jsonList != null && !jsonList.isEmpty()) {
			for (int c = 0; c < jsonList.size(); c++) {
				Map<String, Object> datasMap = (Map<String, Object>) jsonList.get(c);
				int eport = 0;
				try {
					eport = Integer.parseInt(datasMap.get("eport") + "");
					if (cacheList.isEmpty() && !cacheList.contains(eport)) {
						cacheList.add(eport);
					} else {
						int oldEport = (int) cacheList.get(0);
						if (eport != oldEport) {
							statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
							statusMap.put(BaseCode.MSG.getBaseCode(), "不同口岸不能一起备案！");
							return statusMap;
						}
					}
				} catch (Exception e) {
					logger.error(e);
					statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.getBaseCode(), "前端传递eport参数错误！");
					return statusMap;
				}
			}
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), "商品信息错误,请核实!");
		return statusMap;

	}

	/**
	 * 服务器接收信息后更新商品备案信息
	 * 
	 * @param reStatus
	 *            状态码
	 * @param merchantId
	 *            商品Id
	 * @param reGoodsSerialNo
	 *            商品备案流水号
	 * @param msgId
	 *            服务器
	 * @return
	 */
	private Map<String, Object> updateGoodsRecordInfo(Map<String, Object> recordMap, String merchantId,
			String reGoodsSerialNo) {
		Map<String, Object> statusMap = new HashMap<>();
		List<Map<String, Object>> errorList = new ArrayList<>();
		String reStatus = recordMap.get(BaseCode.STATUS.toString()) + "";
		String msgId = recordMap.get("messageID") + "";
		if (!reStatus.equals("1")) {
			errorList.add(recordMap);
			// 对方接受商品备案信息失败后,修改商品备案状态
			Map<String, Object> reUpdateMap = updateGoodsRecordStatus(merchantId, reGoodsSerialNo);
			if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
				return reUpdateMap;
			}
		} else {
			Map<String, Object> reUpdateMsg = updateGoodsRecordStatus(merchantId, reGoodsSerialNo, msgId);
			if (!"1".equals(reUpdateMsg.get(BaseCode.STATUS.toString()))) {
				return reUpdateMsg;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		statusMap.put(BaseCode.ERROR.toString(), errorList);
		return statusMap;
	}

	// 检查商品备案信息
	private Map<String, Object> checkGoodsRecord(String entGoodsNo) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("entGoodsNo", entGoodsNo);
		List<Object> reList = goodsRecordDao.findByProperty(GoodsRecordDetail.class, paramMap, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		statusMap.put(BaseCode.MSG.toString(), "检查商品备案信息错误,服务器繁忙!");
		return statusMap;
	}

	@Override
	public Map<String, Object> editGoodsRecordStatus(String managerId, String managerName, String goodsPack) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(goodsPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("商品备案信息包格式错误,请核对!");
		}
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> goodsMap = (Map<String, Object>) jsonList.get(i);
			int status = 0;
			try {
				status = Integer.parseInt(goodsMap.get("status") + "");
				// 已备案商品状态:0-已备案,待审核,1-备案审核通过,2-正常备案,3-审核不通过
				if (status == 1 || status == 3) {

				} else {
					return ReturnInfoUtils.errorInfo("已备案商品状态参数错误,请重试!");
				}
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("已备案商品状态参数错误,请重试!");
			}
			paramMap.clear();
			paramMap.put("entGoodsNo", goodsMap.get("entGoodsNo"));
			List<Object> reList = goodsRecordDao.findByProperty(GoodsRecordDetail.class, paramMap, 1, 1);
			if (reList == null) {
				return ReturnInfoUtils.errorInfo("查询备案商品信息失败,服务器繁忙!");
			} else if (!reList.isEmpty()) {
				GoodsRecordDetail goodsRecordInfo = (GoodsRecordDetail) reList.get(0);
				String goodsId = goodsRecordInfo.getGoodsDetailId();
				paramMap.clear();
				paramMap.put("goodsId", goodsId);
				List<GoodsContent> reGoodsList = goodsRecordDao.findByProperty(GoodsContent.class, paramMap, 1, 1);
				if (reGoodsList != null && !reGoodsList.isEmpty()) {
					GoodsContent goodsInfo = reGoodsList.get(0);
					// if (status == 2) {
					goodsRecordInfo.setRecordFlag(status);

					goodsRecordInfo.setStatus(2);
					goodsRecordInfo.setSpareGoodsName(goodsRecordInfo.getGoodsName());
					goodsRecordInfo.setSpareGoodsFirstTypeId(goodsInfo.getGoodsFirstTypeId());
					goodsRecordInfo.setSpareGoodsFirstTypeName(goodsInfo.getGoodsFirstTypeName());
					goodsRecordInfo.setSpareGoodsSecondTypeId(goodsInfo.getGoodsSecondTypeId());
					goodsRecordInfo.setSpareGoodsSecondTypeName(goodsInfo.getGoodsSecondTypeName());
					goodsRecordInfo.setSpareGoodsThirdTypeId(goodsInfo.getGoodsThirdTypeId());
					goodsRecordInfo.setSpareGoodsThirdTypeName(goodsInfo.getGoodsThirdTypeName());
					goodsRecordInfo.setSpareGoodsImage(goodsInfo.getGoodsImage());
					goodsRecordInfo.setSpareGoodsDetail(goodsInfo.getGoodsDetail());
					goodsRecordInfo.setSpareGoodsBrand(goodsRecordInfo.getBrand());
					goodsRecordInfo.setSpareGoodsStyle(goodsRecordInfo.getGoodsStyle());
					goodsRecordInfo.setSpareGoodsUnit(goodsRecordInfo.getgUnit());
					goodsRecordInfo.setSpareGoodsOriginCountry(goodsRecordInfo.getOriginCountry());
					goodsRecordInfo.setSpareGoodsBarCode(goodsRecordInfo.getBarCode());
					goodsRecordInfo.setUpdateBy(managerName);
					goodsRecordInfo.setUpdateDate(new Date());
					String time = DateUtil.formatTime(new Date());
					goodsRecordInfo.setReNote(time + "#" + managerName + "审核通过!;");
					if (!goodsRecordDao.update(goodsRecordInfo)) {
						statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
						statusMap.put(BaseCode.MSG.getBaseCode(), "修改商品备案状态,服务器繁忙!");
						return statusMap;
					}
					// } else if (status == 3) {
					// goodsRecordInfo.setStatus(status);
					// if (!goodsRecordDao.update(goodsInfo)) {
					// statusMap.put(BaseCode.STATUS.getBaseCode(),
					// StatusCode.WARN.getStatus());
					// statusMap.put(BaseCode.MSG.getBaseCode(),
					// "修改商品备案状态,服务器繁忙!");
					// return statusMap;
					// }
					// }
				} else {// 如果找不到商品基本信息,则复制备案信息
					// 备案状态：0-未备案，1-备案中，2-备案成功，3-备案失败
					goodsRecordInfo.setRecordFlag(status);
					goodsRecordInfo.setStatus(2);
					goodsRecordInfo.setSpareGoodsName(goodsRecordInfo.getGoodsName());
					goodsRecordInfo.setSpareGoodsBrand(goodsRecordInfo.getBrand());
					goodsRecordInfo.setSpareGoodsStyle(goodsRecordInfo.getGoodsStyle());
					goodsRecordInfo.setSpareGoodsUnit(goodsRecordInfo.getgUnit());
					goodsRecordInfo.setSpareGoodsOriginCountry(goodsRecordInfo.getOriginCountry());
					goodsRecordInfo.setSpareGoodsBarCode(goodsRecordInfo.getBarCode());
					goodsRecordInfo.setUpdateBy(managerName);
					goodsRecordInfo.setUpdateDate(new Date());
					String time = DateUtil.formatTime(new Date());
					goodsRecordInfo.setReNote(time + "#" + managerName + "审核通过!;");
					if (!goodsRecordDao.update(goodsRecordInfo)) {
						statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
						statusMap.put(BaseCode.MSG.getBaseCode(), "修改商品备案状态,服务器繁忙!");
						return statusMap;
					}
				}
			} else {
				return ReturnInfoUtils.errorInfo("商品自编号[" + goodsMap.get("entGoodsNo") + "]为找到对应商品信息,请重试!");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> merchantEditGoodsRecordInfo(String merchantId, String merchantName,
			Map<String, Object> datasMap) {
		Map<String, Object> params = new HashMap<>();
		// 商家平台代码
		String marCode = datasMap.get("marCode") + "";
		String oldEntGoodsNo = null;
		String newEntGoodsNo = null;
		if (StringEmptyUtils.isNotEmpty(marCode)) {
			oldEntGoodsNo = marCode + "_" + oldEntGoodsNo;
			newEntGoodsNo = marCode + "_" + datasMap.get("entGoodsNo");
		} else {
			oldEntGoodsNo = datasMap.get("oldEntGoodsNo") + "";
			newEntGoodsNo = datasMap.get("entGoodsNo") + "";
		}
		params.put("entGoodsNo", oldEntGoodsNo);
		List<GoodsRecordDetail> reList = goodsRecordDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
		if (reList != null && !reList.isEmpty()) {
			GoodsRecordDetail goodsRecordInfo = reList.get(0);
			goodsRecordInfo.setEntGoodsNo(newEntGoodsNo);
			goodsRecordInfo.setEmsNo(datasMap.get("emsNo") + "");
			goodsRecordInfo.setItemNo(datasMap.get("itemNo") + "");
			goodsRecordInfo.setShelfGName(datasMap.get("shelfGName") + "");
			goodsRecordInfo.setNcadCode(datasMap.get("ncadCode") + "");
			goodsRecordInfo.setHsCode(datasMap.get("hsCode") + "");
			goodsRecordInfo.setBarCode(datasMap.get("barCode") + "");
			goodsRecordInfo.setGoodsName(datasMap.get("goodsName") + "");
			goodsRecordInfo.setGoodsStyle(datasMap.get("goodsStyle") + "");
			goodsRecordInfo.setBrand(datasMap.get("brand") + "");
			goodsRecordInfo.setgUnit(datasMap.get("gUnit") + "");
			goodsRecordInfo.setStdUnit(datasMap.get("stdUnit") + "");
			goodsRecordInfo.setSecUnit(datasMap.get("secUnit") + "");
			try {
				goodsRecordInfo.setRegPrice(Double.valueOf(datasMap.get("regPrice") + ""));
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("商品价格错误,请核对参数是否正确!");
			}
			goodsRecordInfo.setGiftFlag(datasMap.get("giftFlag") + "");
			goodsRecordInfo.setOriginCountry(datasMap.get("originCountry") + "");
			goodsRecordInfo.setQuality(datasMap.get("quality") + "");
			goodsRecordInfo.setQualityCertify(datasMap.get("qualityCertify") + "");
			goodsRecordInfo.setManufactory(datasMap.get("manufactory") + "");
			double netWt = 0.0;
			try {
				netWt = Double.valueOf(datasMap.get("netWt") + "");
				goodsRecordInfo.setNetWt(netWt);
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("商品净重错误,请核对参数是否正确!");
			}
			double grossWt = 0.0;
			try {
				grossWt = Double.valueOf(datasMap.get("grossWt") + "");
				goodsRecordInfo.setGrossWt(grossWt);
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("商品毛重错误,请核对参数是否正确!");
			}
			goodsRecordInfo.setNotes(datasMap.get("notes") + "");
			goodsRecordInfo.setIngredient(datasMap.get("ingredient") + "");
			goodsRecordInfo.setAdditiveflag(datasMap.get("additiveflag") + "");
			goodsRecordInfo.setPoisonflag(datasMap.get("poisonflag") + "");
			goodsRecordInfo.setUpdateDate(new Date());
			goodsRecordInfo.setUpdateBy(merchantName);
			if (netWt > grossWt) {
				return ReturnInfoUtils.errorInfo("商品净重不能大于毛重,请重新输入!");
			}
			if (StringEmptyUtils.isNotEmpty(marCode)) {
				JSONObject json = JSONObject.fromObject(goodsRecordInfo.getSpareParams());
				json.put("SKU", datasMap.get("SKU") + "");
				json.put("marCode", marCode);
				goodsRecordInfo.setSpareParams(json.toString());
			}
			if (!goodsRecordDao.update(goodsRecordInfo)) {
				return ReturnInfoUtils.errorInfo("修改商品备案信息错误,服务器繁忙!");
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("商品自编号查询商品信息失败,请核对信息!");
	}

	@Override
	public Map<String, Object> managerGetGoodsRecordInfo(Map<String, Object> datasMap, int page, int size) {
		Map<String, Object> reDatasMap = SearchUtils.universalRecordGoodsSearch(datasMap);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		Map<String, Object> blurryMap = (Map<String, Object>) reDatasMap.get("blurry");
		Table reList = goodsRecordDao.findByRecordInfoLike(GoodsRecordDetail.class, paramMap, blurryMap, page, size);
		Table reListCount = goodsRecordDao.findByRecordInfoLike(GoodsRecordDetail.class, paramMap, blurryMap, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.getRows().isEmpty()) {
			return ReturnInfoUtils.successDataInfo(Transform.tableToJson(reList), reListCount.getRows().size());
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> batchCreateRecordGoodsHead(String merchantId, String merchantName, int customsPort,
			String customsPortName, String customsCode, String customsName, String ciqOrgCode, String ciqOrgName) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		// 口岸、海关、智检编码
		Map<String, Object> reCustomsMap = checkCustomsPort(customsPort, customsCode, ciqOrgCode);
		if (!"1".equals(reCustomsMap.get(BaseCode.STATUS.toString()))) {
			return reCustomsMap;
		}
		// 创建商品仓库
		Map<String, Object> warehousMap = createWarehous(merchantId, merchantName, customsCode, customsName);
		if (!warehousMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return warehousMap;
		}
		// 查询缓存中商品自编号自增Id
		int count = SerialNoUtils.getSerialNo("goodsRecordHead");
		String goodsRecordHeadSerialNo = SerialNoUtils.getSerialNo("GRH", count);
		// 根据商户编号,查询商户海关备案信息
		paramMap.put("merchantId", merchantId);
		List<Object> reMerchantRecordList = goodsRecordDao.findByProperty(MerchantRecordInfo.class, paramMap, 0, 0);
		String ebEntNo = "";
		String ebEntName = "";
		String ebpEntNo = "";
		String ebpEntName = "";
		try {
			MerchantRecordInfo merchantRecordInfo = (MerchantRecordInfo) reMerchantRecordList.get(0);
			ebEntNo = merchantRecordInfo.getEbEntNo();
			ebEntName = merchantRecordInfo.getEbEntName();
			ebpEntNo = merchantRecordInfo.getEbpEntNo();
			ebpEntName = merchantRecordInfo.getEbpEntName();
		} catch (Exception e) {
			logger.error(e);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "查询商户备案信息错误,请重试!");
			return statusMap;
		}
		GoodsRecord goodsRecordInfo = new GoodsRecord();
		goodsRecordInfo.setMerchantId(merchantId);
		goodsRecordInfo.setMerchantName(merchantName);
		goodsRecordInfo.setGoodsSerialNo(goodsRecordHeadSerialNo);
		goodsRecordInfo.setCustomsPort(customsPort);
		goodsRecordInfo.setCustomsPortName(customsPortName);
		goodsRecordInfo.setCustomsCode(customsCode);
		goodsRecordInfo.setCustomsName(customsName);
		goodsRecordInfo.setCiqOrgCode(ciqOrgCode);
		goodsRecordInfo.setCiqOrgName(ciqOrgName);
		goodsRecordInfo.setEbEntNo(ebEntNo);
		goodsRecordInfo.setEbEntName(ebEntName);
		goodsRecordInfo.setEbpEntNo(ebpEntNo);
		goodsRecordInfo.setEbpEntName(ebpEntName);
		goodsRecordInfo.setDeleteFlag(0);
		goodsRecordInfo.setCreateBy(merchantName);
		goodsRecordInfo.setCreateDate(date);
		if (!goodsRecordDao.add(goodsRecordInfo)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "添加商品备案信息头部失败,请重试!");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		statusMap.put("goodsSerialNo", goodsRecordHeadSerialNo);
		return statusMap;
	}

	@Override
	public Map<String, Object> batchCreateRecordGoodsDetail(GoodsRecordDetail goodsRecordDetail) {
		goodsRecordDetail.setCreateDate(new Date());
		if (!goodsRecordDao.add(goodsRecordDetail)) {
			return ReturnInfoUtils.errorInfo("商品编号[" + goodsRecordDetail.getEntGoodsNo() + "]保存失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> checkEntGoodsNoRepeat(String value) {
		if (StringEmptyUtils.isNotEmpty(value) && value.length() <= 20) {
			Map<String, Object> param = new HashMap<>();
			param.put("entGoodsNo", value);
			List<Object> reList = goodsRecordDao.findByProperty(GoodsRecordDetail.class, param, 1, 1);
			if (reList == null) {
				return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
			} else if (!reList.isEmpty()) {
				return ReturnInfoUtils.errorInfo("该企业商品自编号已存在!");
			} else {
				return ReturnInfoUtils.successInfo();
			}
		} else {
			return ReturnInfoUtils.errorInfo("商品自编号长度不能超过20或不能为空!");
		}
	}

	@Override
	public Map<String, Object> merchantDeleteGoodsRecordInfo(String merchantId, String merchantName,
			String entGoodsNo) {
		Date date = new Date();
		Map<String, Object> param = new HashMap<>();
		param.put("goodsMerchantId", merchantId);
		param.put("entGoodsNo", entGoodsNo);
		param.put("deleteFlag", 0);
		List<Object> reList = goodsRecordDao.findByProperty(GoodsRecordDetail.class, param, 1, 1);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			GoodsRecordDetail goodsRecordInfo = (GoodsRecordDetail) reList.get(0);
			int status = goodsRecordInfo.getStatus();
			int recordFlag = goodsRecordInfo.getRecordFlag();
			// 当商品备案状态为未备案或失败并且商品已备案状态不是审核通过的其他任何状态,才允许删除商品信息
			// if (status == 3 || status == 4 && recordFlag != 1) {
			goodsRecordInfo.setDeleteFlag(1);
			goodsRecordInfo.setDeleteBy(merchantName);
			goodsRecordInfo.setDeleteDate(date);
			if (!goodsRecordDao.update(goodsRecordInfo)) {
				return ReturnInfoUtils.errorInfo(goodsRecordInfo.getShelfGName() + " 删除失败,请重试!");
			}
			// } else {
			// return ReturnInfoUtils.errorInfo(goodsRecordInfo.getShelfGName()
			// + "的备案状态不允许删除信息!");
			// }
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

}
