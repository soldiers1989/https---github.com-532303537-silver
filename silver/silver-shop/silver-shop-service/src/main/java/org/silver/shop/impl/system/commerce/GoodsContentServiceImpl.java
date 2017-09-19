package org.silver.shop.impl.system.commerce;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.GoodsContentService;
import org.silver.shop.dao.system.commerce.GoodsContentDao;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = GoodsContentService.class)
public class GoodsContentServiceImpl implements GoodsContentService {

	@Autowired
	private GoodsContentDao goodsContentDao;

	@Override
	public Map<String, Object> findGoodsId() {
		Map<String, Object> datasMap = new HashMap<>();
		// 扫描获取数据库表中的商户自增长ID
		Long goodsCount = goodsContentDao.findLastId();
		// 得出的总数上+1
		Long count = goodsCount + 1;
		if (count < 1) {// 判断数据库查询出数据如果小于1,则中断程序,告诉异常
			datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return datasMap;
		}
		String goodsId = count + "";
		// 当商户ID没有5位数时,前面补0
		while (goodsId.length() < 5) {
			goodsId = "0" + goodsId;
		}
		datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
		datasMap.put(BaseCode.DATAS.getBaseCode(), goodsId);
		return datasMap;
	}

	@Override
	public boolean addGoodsBaseInfo(String goodsId, String merchantName, String goodsName, List imgList,
			String goodsFirstType, String goodsSecondType, String goodsThirdType, String goodsDetail, String goodsBrand,
			String goodsStyle, String goodsUnit, String goodsRegPrice, String goodsOriginCountry, String goodsBarCode,
			int year, Date date) {
		boolean flag = false;
		String goodsImage = "";
		// 拼接多张图片字符串
		for (int i = 0; i < imgList.size(); i++) {
			String imgStr = imgList.get(i) + "";
			if (!"".equals(imgStr) || imgStr != null) {
				goodsImage = goodsImage + imgStr + ";";
			} else {
				return false;
			}

		}
		GoodsContent goodsInfo = new GoodsContent();
		goodsInfo.setGoodsId(goodsId);
		goodsInfo.setGoodsName(goodsName);
		goodsInfo.setGoodsMerchantName(merchantName);
		goodsInfo.setGoodsImage(goodsImage);
		goodsInfo.setGoodsFirstType(goodsFirstType);
		goodsInfo.setGoodsSecondType(goodsSecondType);
		goodsInfo.setGoodsThirdType(goodsThirdType);
		goodsInfo.setGoodsDetail(goodsDetail);
		goodsInfo.setGoodsBrand(goodsBrand);
		goodsInfo.setGoodsStyle(goodsStyle);
		goodsInfo.setGoodsUnit(goodsUnit);
		goodsInfo.setGoodsRegPrice(Long.parseLong(goodsRegPrice));
		goodsInfo.setGoodsOriginCountry(goodsOriginCountry);
		goodsInfo.setGoodsBarCode(goodsBarCode);
		goodsInfo.setYmYear(year + "");
		goodsInfo.setCreateDate(date);
		goodsInfo.setCreateBy(merchantName);
		goodsInfo.setDeletFlag(0);
		flag = goodsContentDao.add(goodsInfo);
		return flag;
	}
}
