package org.silver.shop.impl.system.commerce;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = GoodsRecordService.class)
public class GoodsRecordServiceImpl implements GoodsRecordService {

	// 进出境标志I-进，E-出
	private static final String IEFLAG = "I";

	// 币制默认为人民币
	private static final String CURRCODE = "142";

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private GoodsRecordDao goodsRecordDao;
	@Autowired
	private CustomsPortService customsPortService;
	@Autowired
	private AccessTokenService accessTokenService;

	@Override
	public List<Object> findGoodsBaseInfo(String merchantName, int page, int size) {
		Map<String, Object> params = new HashMap<>();
		// key=数据库列名,value=查询参数
		params.put("goodsMerchantName", merchantName);
		// 删除标识:0-未删除,1-已删除
		params.put("deleteFlag", 0);
		String descParam = "createDate";
		List<Object> reList = goodsRecordDao.findGoodsBaseInfo(params, descParam, page, size);
		if (reList != null && reList.size() > 0) {
			return reList;
		}
		return null;
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
			e.printStackTrace();
			logger.info("-------前端传递基本信息参数错误！---------");
		}
		Calendar cal = Calendar.getInstance();
		// 获取当前年份
		int year = cal.get(Calendar.YEAR);
		// 查询数据库字段名
		String property = "goodsId";
		// 根据年份查询,当前年份下的id数量
		long goodsIdCount = goodsRecordDao.findSerialNoCount(GoodsContent.class, property, year);
		// 当返回-1时,则查询数据库失败
		if (goodsIdCount < 0) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		// 生成商品基本信息ID
		String goodsId = SerialNoUtils.getSerialNo("YM_", year,goodsIdCount);

		for (int i = 0; i < jsonList.size(); i++) {
			params = new HashMap<>();
			// 获取传递过来的商品ID
			Map<String, Object> goodsMap = (Map) jsonList.get(i);
			String mapGoodsId = goodsMap.get("goodsId") + "";
			String mapGoodsName = goodsMap.get("goodsName") + "";
			String descParam = "createDate";
			// key=数据库列名,value=查询参数
			params.put("goodsName", mapGoodsName);
			params.put("goodsMerchantName", merchantName);
			// 删除标识:0-未删除,1-已删除
			params.put("deleteFlag", 0);
			// 根据商品名,扫描商品备案信息表
			List<Object> goodsRecordList = goodsRecordDao.findPropertyDesc(GoodsRecord.class, params, descParam, 1, 1);
			if (goodsRecordList != null && goodsRecordList.size() > 0) {
				GoodsRecordDetail goodsRecordInfo = (GoodsRecordDetail) goodsRecordList.get(0);
				goodsRecordInfo.setEntGoodsNo(goodsId);
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
				goodsInfo.setGoodsId(goodsId);
				goodsBaseList.add(goodsInfo);
			}
		}
		params.put(BaseCode.DATAS.toString(), goodsBaseList);
		return params;
	}

	@Override
	public Map<String, Object> merchantSendGoodsRecord(String merchantName, String merchantId,
			String recordGoodsInfoPack, String eport, String customsCode, String ciqOrgCode) {
		Map<String, Object> datasMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(recordGoodsInfoPack);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("-------前端传递基本信息参数错误！---------");
		}
		// 校验前台传递口岸编码
		if (!checkCustomsPort(eport, customsCode, ciqOrgCode)) {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.NOTICE.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.NOTICE.getMsg());
			return datasMap;
		}

		// 根据商户ID,口岸编码查询商户备案信息
		Map<String, Object> merchantInfoMap = getMerchantInfo(merchantId, Integer.valueOf(eport));
		if (!merchantInfoMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return merchantInfoMap;
		}
		// 电商企业编号
		String ebEntNo = merchantInfoMap.get("ebEntNo") + "";
		// 电商企业名称
		String ebEntName = merchantInfoMap.get("ebEntName") + "";
		String tok = "";
		// 请求获取tok
		Map<String, Object> tokMap = accessTokenService.getAccessToken();
		if (!tokMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return tokMap;
		}
		tok = tokMap.get(BaseCode.DATAS.toString()) + "";
		// 封装备案商品信息
		List<Object> datas = null;
		Map<String, Object> reMap = addRecordInfo(jsonList);
		if (!reMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return reMap;
		}
		//将备案商品信息插入数据库
		
		
		datas = (List) reMap.get(BaseCode.DATAS.toString());
		Map<String, Object> recordMap = sendRecord(eport, ebEntNo, ebEntName, tok, datas);
		if (!recordMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return recordMap;
		}

		datasMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		datasMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		return datasMap;
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
	protected boolean checkCustomsPort(String eport, String customsCode, String ciqOrgCode) {
		List<Object> datasList = null;
		boolean flag = false;
		String redisList = JedisUtil.get("shop_port_AllCustomsPort");
		if (StringEmptyUtils.isNotEmpty(redisList)) {
			// 当缓存中不为空时,将字符串数据转换为List
			datasList = JSONArray.fromObject(redisList);
		} else {// 缓存中没有数据,重新访问数据库读取数据
			Map<String, Object> datasMap = customsPortService.findAllCustomsPort();
			if (!datasMap.get(BaseCode.STATUS.toString()).equals("1")) {
				return false;
			}
			datasList = (List<Object>) datasMap.get(BaseCode.DATAS.toString());
		}
		for (int i = 0; i < datasList.size(); i++) {
			CustomsPort info = (CustomsPort) datasList.get(i);
			// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
			String reCustomsPort = info.getCustomsPort() + "";
			// 主管海关代码
			String reCustomsCode = info.getCustomsCode();
			// 检验检疫机构代码
			String reCIQOrgCode = info.getCiqOrgCode();
			// 判断前端传递的口岸端口、海关代码、智检代码是否正确
			if (reCustomsPort.trim().equals(eport.trim()) && reCustomsCode.trim().equals(customsCode.trim())
					&& reCIQOrgCode.trim().equals(ciqOrgCode.trim())) {
				flag = true;
				break;
			}
		}
		return flag;
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
	protected Map<String, Object> getMerchantInfo(String merchantId, int eport) {
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		params.put("eport", eport);
		// 根据商户ID查询商户备案信息数据
		List<Object> reList = goodsRecordDao.findByProperty(MerchantRecordInfo.class, params, 1, 1);
		if (reList != null && reList.size() > 0) {
			params.clear();
			MerchantRecordInfo merchantRecordInfo = (MerchantRecordInfo) reList.get(0);
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

	// 发送商品备案
	protected Map<String, Object> sendRecord(String eport, String ebEntNo, String ebEntName, String tok,
			List<Object> datas) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
		// 1-特殊监管区域BBC保税进口;2-保税仓库BBC保税进口;3-BC直购进口
		String businessType = "";
		if ("1".equals(eport)) {
			businessType = "3";
		} else if ("2".equals(eport)) {
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
			logger.info("----------客户端签名生成出错--------");
			e.printStackTrace();
		}
		// 0:商品备案 1:订单推送 2:支付单推送
		params.put("type", 0);
		params.put("eport", Integer.valueOf(eport));
		params.put("businessType", Integer.valueOf(businessType));
		params.put("ieFlag", IEFLAG);
		params.put("currCode", CURRCODE);
		params.put("inputDate", inputDate);
		// 电商企业编号
		params.put("eBEntNo", ebEntNo);
		// 电商企业名称
		params.put("eBEntName", ebEntName);
		params.put("appkey", YmMallConfig.APPKEY);
		params.put("clientsign", clientsign);
		params.put("timestamp", timestamp);
		params.put("datas", datas.toString());
		params.put("notifyurl", YmMallConfig.URL);
		params.put("note", note);
		String resultStr = YmHttpUtil.HttpPost("http://localhost:8166/silver-web/Eport/Report", params);
		if (StringEmptyUtils.isNotEmpty(resultStr)) {
			return JSONObject.fromObject(resultStr);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		}
		return statusMap;
	}

	//保存备案商品信息
	private final Map<String,Object> saveRecordInfo(){
		Map<String,Object> statusMap = new HashMap<>();
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
		String goodsSerialNo = SerialNoUtils.getSerialNo("YM_", year, goodsSerialNoCount);
		
		return null;
	}
	public static void main(String[] args) {

		List list = new ArrayList();
		Map<String, Object> datasMap = null;
		for (int i = 0; i < 2; i++) {
			datasMap = new HashMap<>();
			datasMap.put("EntGoodsNo", "测试商品编码");
			datasMap.put("EPortGoodsNo", "1");
			datasMap.put("CIQGoodsNo", "2");
			datasMap.put("CusGoodsNo", "2");
			datasMap.put("EmsNo", "3");
			datasMap.put("ItemNo", "4");
			datasMap.put("ShelfGName", "测试商品上架名称");
			datasMap.put("NcadCode", "5");
			datasMap.put("HSCode", "6");
			datasMap.put("BarCode", "7");
			datasMap.put("GoodsName", "测试商品名称");
			datasMap.put("GoodsStyle", "900/罐");
			datasMap.put("Brand", "8");
			datasMap.put("GUnit", "9");
			datasMap.put("StdUnit", "10");
			datasMap.put("SecUnit", "11");
			datasMap.put("RegPrice", "12");
			datasMap.put("GiftFlag", "13");
			datasMap.put("OriginCountry", "14");
			datasMap.put("Quality", "15");
			datasMap.put("QualityCertify", "16");
			datasMap.put("Manufactory", "17");
			datasMap.put("NetWt", "18");
			datasMap.put("GrossWt", "19");
			datasMap.put("Notes", "");
			list.add(datasMap);
		}
		System.out.println(JSONArray.fromObject(list).toString());
	}
}
