package org.silver.shop.impl.system.commerce;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.CustomsPortService;
import org.silver.shop.api.system.commerce.GoodsRecordService;
import org.silver.shop.dao.system.commerce.GoodsRecordDao;
import org.silver.shop.model.common.base.CustomsPort;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecord;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.util.JedisUtil;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;

@Service(interfaceClass = GoodsRecordService.class)
public class GoodsRecordServiceImpl implements GoodsRecordService {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private GoodsRecordDao goodsRecordDao;
	@Autowired
	private CustomsPortService customsPortService;

	@Override
	public List findGoodsBaseInfo(String merchantName, int page, int size) {
		Map<String, Object> params = new HashMap<>();
		// key=数据库列名,value=查询参数
		params.put("goodsMerchantName", merchantName);
		// 删除标识:0-未删除,1-已删除
		params.put("deleteFlag", 0);
		String descParam = "createDate";
		List reList = goodsRecordDao.findGoodsBaseInfo(params, descParam, page, size);
		if (reList != null && reList.size() > 0) {
			return reList;
		}
		return null;
	}

	@Override
	public Map<String, Object> getGoodsRecordInfo(String merchantName, String goodsInfoPack) {
		Map<String, Object> params = new HashMap<>();
		JSONArray jsonList = null;
		List<Object> goodsBaseList = new ArrayList<>();
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("-------前端传递基本信息参数错误！---------");
		}
		int count = 0;
		Calendar cal = Calendar.getInstance();
		// 获取当前年份
		int year = cal.get(Calendar.YEAR);
		// 根据年份查询,当前年份有多少条数据
		String lastOneGoodsId = goodsRecordDao.findGoodsYearLastId(GoodsContent.class, year);
		if (lastOneGoodsId == null) {
			// 当查询无记录时为：1
			count = 1;
		} else if (lastOneGoodsId.equals("-1")) {
			params.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			params.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			logger.debug("连接数据错误!");
			return params;
		} else {
			// 截取 YM_2017|00001|15058114089963091 自增部分
			String countId = lastOneGoodsId.substring(7, 12);
			// 商品自增ID,得出的自增数上+1
			count = Integer.parseInt(countId) + 1;
		}
		String topStr = "YM_";
		String strCount = String.valueOf(count);
		// 商品(备案商品的)自编号为 YM_+(当前)年+五位数(数据库表自增ID)+时间戳(13位)+4位随机数
		String goodsId = SerialNoUtils.getSerialNo(topStr, strCount);

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
		boolean flag = false;
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(recordGoodsInfoPack);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("-------前端传递基本信息参数错误！---------");
		}
		String redisList = JedisUtil.get("shop_port_AllCustomsPort");
		List<Object> datasList = null;
		if (StringEmptyUtils.isNotEmpty(redisList)) {
			// 当缓存中不为空时,将字符串数据转换为List
			datasList = JSONArray.fromObject(redisList);
		} else {// 缓存中没有数据,重新访问数据库读取数据
			datasMap = customsPortService.findAllCustomsPort();
			datasList = (List<Object>) datasMap.get(BaseCode.DATAS.toString());
		}
		for (int i = 0; i < datasList.size(); i++) {
			CustomsPort info = (CustomsPort) datasList.get(i);
			// 口岸:1-广州电子口岸,2-广东智检
			String reCustomsPort = info.getCustomsPort() + "";
			// 主管海关代码
			String reCustomsCode = info.getCustomsCode();
			// 检验检疫机构代码
			String reCIQOrgCode = info.getCiqOrgCode();
			// 判断前端传递的口岸端口、海关代码、智检代码是否正确
			if (reCustomsPort.equals(eport) && reCustomsCode.equals(customsCode) && reCIQOrgCode.equals(ciqOrgCode)) {
				flag = true;
			}
		}
		if (flag) {
			// 币制默认为人民币
			String CurrCode = "142";
			// 1-特殊监管区域BBC保税进口;2-保税仓库BBC保税进口;3-BC直购进口
			String BusinessType = "";
			// 进出境标志I-进，E-出
			String IeFlag = "I";
			params.put("merchantId", merchantId);
			params.put("eport", eport);
			// 根据商户ID查询商户备案信息数据
			List<Object> reList = goodsRecordDao.findByProperty(MerchantRecordInfo.class, params, 0, 0);
			// 电商企业编号
			String ebEntNo = "";
			// 电商企业名称
			String ebEntName = "";
			// 电商平台企业编号
			String ebpEntNo = "";
			// 电商平台名称
			String ebpEntName = "";
			if (reList != null && reList.size() > 0) {
				MerchantRecordInfo merchantRecordInfo = (MerchantRecordInfo) reList.get(0);
				ebEntNo = merchantRecordInfo.getEbEntNo();
				ebEntName = merchantRecordInfo.getEbEntName();
				ebpEntNo = merchantRecordInfo.getEbpEntNo();
				ebpEntName = merchantRecordInfo.getEbpEntName();
			} else {
				datasMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				return datasMap;
			}
			List<JSONObject> datas = new ArrayList<>();
			JSONObject jsonObject = null;
			for (int i = 0; i < jsonList.size(); i++) {
				jsonObject = new JSONObject();
				GoodsRecordDetail goods = (GoodsRecordDetail) jsonList.get(i);
				jsonObject.element("seq", i+1);
				jsonObject.element("EntGoodsNo", goods.getEntGoodsNo()+"");
				jsonObject.element("EPortGoodsNo", goods.getEportGoodsNo()+"");
				jsonObject.element("CIQGoodsNo", goods.getCiqGoodsNo()+"");
				jsonObject.element("CusGoodsNo", goods.getCusGoodsNo()+"");
				jsonObject.element("EmsNo", goods.getEmsNo()+"");
				jsonObject.element("ItemNo", goods.getItemNo()+"");
				jsonObject.element("ShelfGName", goods.getShelfGName()+"");
				jsonObject.element("NcadCode", goods.getNcadCode()+"");
				jsonObject.element("HSCode", goods.getHsCode()+"");
				jsonObject.element("BarCode", goods.getBarCode()+"");
				jsonObject.element("GoodsName", goods.getGoodsName()+"");
				jsonObject.element("GoodsStyle", goods.getGoodsStyle()+"");
				jsonObject.element("Brand", goods.getBrand()+"");
				jsonObject.element("GUnit", goods.getgUnit()+"");
				jsonObject.element("StdUnit", goods.getStdUnit()+"");
				jsonObject.element("SecUnit", goods.getSecUnit()+"");
				jsonObject.element("RegPrice", goods.getRegPrice()+"");
				jsonObject.element("GiftFlag", goods.getGiftFlag()+"");
				jsonObject.element("OriginCountry", goods.getOriginCountry()+"");
				jsonObject.element("Quality", goods.getQuality()+"");
				jsonObject.element("QualityCertify", goods.getQualityCertify()+"");
				jsonObject.element("Manufactory", goods.getManufactory()+"");
				jsonObject.element("NetWt", goods.getNetWt()+"");
				jsonObject.element("GrossWt", goods.getGrossWt()+"");
				jsonObject.element("Notes", goods.getNotes()+"");
				datas.add(jsonObject);
			}
		params.clear();
		params.put("type", 0);
		params.put("eport", Integer.valueOf(eport));
		params.put("eBEntNo",ebEntNo);
		params.put("eBEntName", ebEntName);
		
		}
		return null;
	}

	public static void main(String[] args) {
/*	List list = new ArrayList();
		Map<String, Object> datasMap = null;
		list.add("头部信息");
		for (int i = 0; i < 2; i++) {
			datasMap = new HashMap<>();
			datasMap.put("goodsId", "YM_20170000715060732279179879");
			datasMap.put("goodsMerchantName", "商户测试");
			datasMap.put("goodsName", "商品地址测试");
			list.add(datasMap);
		}*/
		 
		for(int i = 0; i < 10; i++){
			System.out.println(i+1);
	}
	}
}
