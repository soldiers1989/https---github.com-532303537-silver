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
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = GoodsRecordService.class)
public class GoodsRecordServiceImpl implements GoodsRecordService {

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
	public Map<String, Object> getGoodsRecordInfo(String merchantName,String goodsIdPack) {
		Map<String, Object> params = null;
		JSONArray jsonList = null;
		List<Object> goodsBaseList = new ArrayList<>();
		System.out.println("循环之前------->"+goodsIdPack);
		try {
			jsonList = JSONArray.fromObject(goodsIdPack);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Date date = new Date();
		String goodsId = "";
		String topStr = "YM_";
		Calendar cal = Calendar.getInstance();
		// 获取当前年份
		int year = cal.get(Calendar.YEAR);
		// 获取商品基本信息的自增ID
		Long reGoodsId = goodsRecordDao.findLastId();

		// 得出的总数上+1
		Long count = reGoodsId + 1;
		if (count < 1) {// 判断数据库查询出数据如果小于1,则中断程序,告诉异常
			params.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			params.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return params;
		}
		String GoodsId = count + "";
		// 当商户ID没有5位数时,前面补0
		while (goodsId.length() < 5) {
			goodsId = "0" + goodsId;
		}
		// 获取到当前时间戳
		Long current = System.currentTimeMillis();
		// 随机4位数
		int ramCount = (int) ((Math.random() * 9 + 1) * 1000);
		// 商品自编号为 YM_+(当前)年+五位数(数据库表自增ID)+时间戳(13位)+4位随机数
		goodsId = topStr + year + goodsId + current + ramCount;
		
		
		for (int i = 0; i < jsonList.size(); i++) {
			params = new HashMap<>();
			String goodsId2 = jsonList.get(i) + "";
			params.put("goodsId", goodsId2);
			// 删除标识:0-未删除,1-已删除
			params.put("deleteFlag", 0);
			List<Object> goodsRecordList = goodsRecordDao.findByProperty(GoodsRecordContent.class, params, 1, 1);
			if (goodsRecordList != null && goodsRecordList.size() > 0) {
				GoodsRecordContent goodsRecordInfo = (GoodsRecordContent) goodsRecordList.get(0);
				goodsRecordInfo.setEntGoodsNo(goodsId);
				params.put(BaseCode.DATAS.toString(), goodsBaseList.add(goodsRecordInfo));
			} else {
				List<Object> goodsList = goodsRecordDao.findByProperty(GoodsContent.class, params, 1, 1);
				GoodsContent goodsInfo = (GoodsContent) goodsList.get(0);
				goodsInfo.setGoodsId(goodsId);
				params.put(BaseCode.DATAS.toString(), goodsBaseList.add(goodsInfo));
			}
		}
		return params;
	}

}
