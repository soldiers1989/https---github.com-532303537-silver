package org.silver.shop.impl.system.commerce;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.GoodsRecordService;
import org.silver.shop.dao.system.commerce.GoodsRecordDao;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecordContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = GoodsRecordService.class)
public class GoodsRecordServiceImpl implements GoodsRecordService {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private GoodsRecordDao goodsRecordDao;

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
		Map<String, Object> params = null;
		JSONArray jsonList = null;
		List<Object> goodsBaseList = new ArrayList<>();
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("-------前端传递基本信息参数错误！---------");
		}
		String goodsId = "";
		String topStr = "YM_";
		int count =0;
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
		
		goodsId = String.valueOf(count);
		// 当商户ID没有5位数时,前面补0
		while (goodsId.length() < 5) {
			goodsId = "0" + goodsId;
		}
		// 获取到当前时间戳
		Long current = System.currentTimeMillis();
		// 随机4位数
		int ramCount = (int) ((Math.random() * 9 + 1) * 1000);
		// 商品(备案商品的)自编号为 YM_+(当前)年+五位数(数据库表自增ID)+时间戳(13位)+4位随机数
		goodsId = topStr + year + goodsId + current + ramCount;

		for (int i = 0; i < jsonList.size(); i++) {
			params = new HashMap<>();
			// 获取传递过来的商品ID
			Map<String, Object> goodsMap = (Map) jsonList.get(i);
			String mapGoodsId = goodsMap.get("goodsId") + "";
			String mapGoodsName = goodsMap.get("goodsName") + "";
			String descParam = "createDate";
			// key=数据库列名,value=查询参数
			params.put("goodsMerchantName", mapGoodsName);
			// 删除标识:0-未删除,1-已删除
			params.put("deleteFlag", 0);
			// 根据商品名,扫描商品备案信息表
			List<Object> goodsRecordList = goodsRecordDao.findPropertyDesc(GoodsRecordContent.class, params, descParam,
					1, 1);
			if (goodsRecordList != null && goodsRecordList.size() > 0) {
				GoodsRecordContent goodsRecordInfo = (GoodsRecordContent) goodsRecordList.get(0);
				goodsRecordInfo.setEntGoodsNo(goodsId);
				goodsBaseList.add(goodsRecordInfo);
				System.out.println("扫描了备案商品信息表");
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
				System.out.println("扫描了商品信息表");
			}
		}
		params.put(BaseCode.DATAS.toString(), goodsBaseList);
		return params;
	}

	public static void main(String[] args) {
		List list = new ArrayList();
		Map<String, Object> datasMap = null;
		for (int i = 0; i < 5; i++) {
			datasMap = new HashMap<>();
			datasMap.put("goodsId", "YM_20170000715060732279179879");
			datasMap.put("goodsMerchantName", "商户测试");
			datasMap.put("goodsName", "商品地址测试");
			list.add(datasMap);
		}
		System.out.println(JSONArray.fromObject(list).toString());
	}
}
