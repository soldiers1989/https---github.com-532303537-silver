package org.silver.shop.impl.system.commerce;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.GoodsContentService;
import org.silver.shop.dao.system.commerce.GoodsContentDao;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = GoodsContentService.class)
public class GoodsContentServiceImpl implements GoodsContentService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private GoodsContentDao goodsContentDao;

	@Override
	public Map<String, Object> createGoodsId() {
		Map<String, Object> datasMap = new HashMap<>();
		Calendar cal = Calendar.getInstance();
		String goodsId = "";
		String topStr = "YM_";
		int count = 0;
		// 获取当前年份
		int year = cal.get(Calendar.YEAR) ;
		// 根据年份查询,当前年份有多少条数据
		String lastOneGoodsId = goodsContentDao.findGoodsYearLastId(GoodsContent.class, year);
		if (lastOneGoodsId == null) {
			// 当查询无记录时为：1
			count = 1;
		} else if (lastOneGoodsId.equals("-1")) {
			datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			logger.debug("连接数据错误!");
			return datasMap;
		} else {
			// 截取 YM_2017|00001|15058114089963091 自增部分
			String countId = lastOneGoodsId.substring(7, 12);
			// 商品自增ID,得出的自增数上+1
			count = Integer.parseInt(countId) + 1;
		}
		String strCount = String.valueOf(count);
		// 当商户ID没有5位数时,前面补0
		while (strCount.length() < 5) {
			strCount = "0" + strCount;
		}
		// 获取到当前时间戳
		Long current = System.currentTimeMillis();
		// 随机4位数
		int ramCount = (int) ((Math.random() * 9 + 1) * 1000);
		// 商品自编号为 YM_+(当前)年+五位自增数(数据库中当前年份的数量+1)+时间戳(13位)+4位随机数
		goodsId = topStr + year + strCount + current + ramCount;
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
			if (imgStr != null && !"".equals(imgStr.trim())) {
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
		goodsInfo.setGoodsRegPrice(Double.valueOf((goodsRegPrice)));
		goodsInfo.setGoodsOriginCountry(goodsOriginCountry);
		goodsInfo.setGoodsBarCode(goodsBarCode);
		goodsInfo.setYmYear(year + "");
		goodsInfo.setCreateDate(date);
		goodsInfo.setCreateBy(merchantName);
		goodsInfo.setDeleteFlag(0);
		flag = goodsContentDao.add(goodsInfo);
		return flag;
	}

	@Override
	public List<Object> blurryFindGoodsInfo(String goodsId, String merchantName, String goodsName, String startTime,
			String endTime, String ymYear, int page, int size) {
		// key=数据库列名,value=查询参数
		Map<String, Object> params = new HashMap<>();
		if (goodsId != null && !"".equals(goodsId.trim())) {
			params.put("goodsId", goodsId);
			page = 0;
			size = 0;
		} else {
			if (merchantName != null && !"".equals(merchantName.trim())) {
				params.put("goodsMerchantName", merchantName);
			}
			if (startTime != null && !"".equals(startTime.trim())) {
				params.put("goodsName", goodsName);
			}
			if (ymYear != null && !"".equals(ymYear.trim())) {
				params.put("ymYear", ymYear);
			}
		}
		// 删除标识:0-未删除,1-已删除
		params.put("deleteFlag", 0);
		return goodsContentDao.findBlurryProperty(GoodsContent.class, params, startTime, endTime, page, size);
	}

	@Override
	public boolean editGoodsBaseInfo(String goodsId, String goodsName, String goodsFirstType, String goodsSecondType,
			String goodsThirdType, List<Object> imgList, String goodsDetail, String goodsBrand, String goodsStyle,
			String goodsUnit, String goodsRegPrice, String goodsOriginCountry, String goodsBarCode,
			String merchantName) {
		Date date = new Date();
		boolean flag = false;
		String goodsImage = "";
		// 拼接多张图片字符串
		for (int i = 0; i < imgList.size(); i++) {
			String imgStr = imgList.get(i) + "";
			if (imgStr != null && !"".equals(imgStr.trim())) {
				goodsImage = goodsImage + imgStr + ";";
			} else {
				return false;
			}
		}
		Map<String, Object> params = new HashMap<>();
		params.put("goodsId", goodsId);
		params.put("goodsMerchantName", merchantName);
		// 根据商品ID查询商品基本信息
		List reList = goodsContentDao.findByProperty(GoodsContent.class, params, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			GoodsContent goodsInfo = (GoodsContent) reList.get(0);
			goodsInfo.setGoodsName(goodsName);
			goodsInfo.setGoodsImage(goodsImage);
			goodsInfo.setGoodsFirstType(goodsFirstType);
			goodsInfo.setGoodsSecondType(goodsSecondType);
			goodsInfo.setGoodsThirdType(goodsThirdType);
			goodsInfo.setGoodsDetail(goodsDetail);
			goodsInfo.setGoodsBrand(goodsBrand);
			goodsInfo.setGoodsStyle(goodsStyle);
			goodsInfo.setGoodsUnit(goodsUnit);
			goodsInfo.setGoodsRegPrice(Double.valueOf((goodsRegPrice)));
			goodsInfo.setGoodsOriginCountry(goodsOriginCountry);
			goodsInfo.setGoodsBarCode(goodsBarCode);
			goodsInfo.setUpdateDate(date);
			goodsInfo.setUpdateBy(merchantName);
			goodsInfo.setDeleteFlag(0);
			flag = goodsContentDao.update(goodsInfo);
		}

		return flag;
	}

	@Override
	public boolean deleteBaseInfo(String merchantName, String goodsId) {
		boolean flag = false;
		Map<String, Object> params = new HashMap<>();
		// key=数据库列名,value=查询参数
		params.put("goodsMerchantName", merchantName);
		params.put("goodsId", goodsId);
		// 根据商户名与商品ID查询商品,确认商品是否存在
		List reList = goodsContentDao.findByProperty(GoodsContent.class, params, 1, 1);
		if (reList != null && reList.size() > 0) {
			GoodsContent goodsInfo = (GoodsContent) reList.get(0);
			// 删除标识:0-未删除,1-已删除
			goodsInfo.setDeleteFlag(1);
			goodsInfo.setGoodsMerchantName(merchantName);
			;
			flag = goodsContentDao.update(goodsInfo);
		}
		return flag;
	}

}
