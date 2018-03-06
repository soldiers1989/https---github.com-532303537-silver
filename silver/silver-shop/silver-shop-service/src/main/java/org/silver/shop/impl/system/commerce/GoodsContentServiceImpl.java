package org.silver.shop.impl.system.commerce;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.GoodsContentService;
import org.silver.shop.dao.system.commerce.GoodsContentDao;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.util.SearchUtils;
import org.silver.util.ConvertUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

@Service(interfaceClass = GoodsContentService.class)
public class GoodsContentServiceImpl implements GoodsContentService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private GoodsContentDao goodsContentDao;

	@Override
	public Map<String, Object> createGoodsId() {
		Map<String, Object> datasMap = new HashMap<>();
		Calendar cal = Calendar.getInstance();
		// 获取当前年份
		int year = cal.get(Calendar.YEAR);
		// 查询数据库字段名
		String property = "goodsId";
		// 根据年份查询,当前年份下的id数量
		long goodsIdCount = goodsContentDao.findSerialNoCount(GoodsContent.class, property, year);
		// 当返回-1时,则查询数据库失败
		if (goodsIdCount < 0) {
			datasMap.clear();
			datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return datasMap;
		}
		String goodsId = SerialNoUtils.getSerialNo("YM_", year, goodsIdCount);
		datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
		datasMap.put(BaseCode.DATAS.getBaseCode(), goodsId);
		return datasMap;
	}

	@Override
	public Map<String, Object> addGoodsBaseInfo(String merchantId, String merchantName, Map<String, Object> params,
			int goodsYear, Date date) {
		Map<String, Object> statusMap = new HashMap<>();
		GoodsContent goodsInfo = new GoodsContent();
		goodsInfo.setGoodsId(params.get("goodsId") + "");
		goodsInfo.setGoodsName(params.get("goodsName") + "");
		goodsInfo.setGoodsMerchantName(merchantName);
		goodsInfo.setGoodsImage(params.get("mainImg") + "");
		goodsInfo.setGoodsFirstTypeId(params.get("goodsFirstTypeId") + "");
		goodsInfo.setGoodsFirstTypeName(params.get("goodsFirstTypeName") + "");
		goodsInfo.setGoodsSecondTypeId(params.get("goodsSecondTypeId") + "");
		goodsInfo.setGoodsSecondTypeName(params.get("goodsSecondTypeName") + "");
		goodsInfo.setGoodsThirdTypeId(params.get("goodsThirdTypeId") + "");
		goodsInfo.setGoodsThirdTypeName(params.get("goodsThirdTypeName") + "");
		goodsInfo.setGoodsDetail(params.get("goodsDetail") + "");
		goodsInfo.setGoodsBrand(params.get("goodsBrand") + "");
		goodsInfo.setGoodsStyle(params.get("goodsStyle") + "");
		goodsInfo.setGoodsUnit(params.get("goodsUnit") + "");
		double goodsRegPrice = 0;
		try {
			goodsRegPrice = Double.valueOf((params.get("goodsRegPrice") + ""));
		} catch (Exception e) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "价格不正确,请重新输入!");
			return statusMap;
		}
		goodsInfo.setGoodsRegPrice(goodsRegPrice);
		goodsInfo.setGoodsOriginCountry(params.get("goodsOriginCountry") + "");
		goodsInfo.setGoodsBarCode(params.get("goodsBarCode") + "");
		goodsInfo.setGoodsYear(String.valueOf(goodsYear));
		goodsInfo.setCreateDate(date);
		goodsInfo.setCreateBy(merchantName);
		goodsInfo.setDeleteFlag(0);
		goodsInfo.setGoodsMerchantId(merchantId);
		if (!goodsContentDao.add(goodsInfo)) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "保存商品基本信息错误!服务器繁忙！");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> findAllGoodsInfo(String merchantName, int page, int size, String merchantId) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();

		// key=数据库列名,value=查询参数
		params.put("goodsMerchantId", merchantId);
		// 删除标识:0-未删除,1-已删除
		params.put("deleteFlag", 0);
		List<Object> reList = goodsContentDao.findByProperty(GoodsContent.class, params, page, size);
		long total = goodsContentDao.findByPropertyCount(GoodsContent.class, params);
		if (reList != null && reList.size() > 0) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.TOTALCOUNT.toString(), total);
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> editGoodsBaseInfo(Map<String, Object> datasMap, String merchantName, String merchantId) {
		Map<String, Object> statusMap = new HashMap<>();
		Date date = new Date();
		Map<String, Object> params = new HashMap<>();
		params.put("goodsId", datasMap.get("goodsId") + "");
		params.put("goodsMerchantName", merchantName);
		// 根据商品ID查询商品基本信息
		List<Object> reList = goodsContentDao.findByProperty(GoodsContent.class, params, 1, 1);
		if (reList != null && !reList.isEmpty()) {
			GoodsContent goodsInfo = (GoodsContent) reList.get(0);
			goodsInfo = (GoodsContent) ConvertUtils.mapChangeToEntity(goodsInfo, datasMap);
			goodsInfo.setUpdateDate(date);
			goodsInfo.setUpdateBy(merchantName);
			goodsInfo.setDeleteFlag(0);
			if (!goodsContentDao.update(goodsInfo)) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), "更新商品基本信息失败,请重试!");
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public boolean deleteBaseInfo(String merchantName, String goodsId) {
		boolean flag = false;
		Map<String, Object> params = new HashMap<>();
		// key=数据库列名,value=查询参数
		params.put("goodsMerchantName", merchantName);
		params.put("goodsId", goodsId);
		// 根据商户名与商品ID查询商品,确认商品是否存在
		List<Object> reList = goodsContentDao.findByProperty(GoodsContent.class, params, 1, 1);
		if (reList != null && reList.size() > 0) {
			GoodsContent goodsInfo = (GoodsContent) reList.get(0);
			// 删除标识:0-未删除,1-已删除
			goodsInfo.setDeleteFlag(1);
			goodsInfo.setGoodsMerchantName(merchantName);
			flag = goodsContentDao.update(goodsInfo);
		}
		return flag;
	}

	@Override
	public Map<String, Object> getShowGoodsBaseInfo(int firstType, int secndType, int thirdType, int page, int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Table t = goodsContentDao.getAlreadyRecordGoodsBaseInfo(firstType, secndType, thirdType, page, size);
		Table tCount = goodsContentDao.getAlreadyRecordGoodsBaseInfo(firstType, secndType, thirdType, 0, 0);
		if (t == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), Transform.tableToJson(t));
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.TOTALCOUNT.toString(), tCount.getRows().size());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> goodsContentService(String entGoodsNo) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("entGoodsNo", entGoodsNo);
		List<Object> reList = goodsContentDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
		List<Object> reStockList = goodsContentDao.findByProperty(StockContent.class, params, 1, 1);
		if (reList != null && reList.size() > 0 && reStockList != null && reStockList.size() > 0) {
			Map<String, Object> datas = new HashMap<>();
			List<Map<String, Object>> lm = new ArrayList<>();
			GoodsRecordDetail goods = (GoodsRecordDetail) reList.get(0);
			StockContent stockInfo = (StockContent) reStockList.get(0);
			datas.put("goods", goods);
			datas.put("stock", stockInfo);
			lm.add(datas);
			int reRedingCount = stockInfo.getReadingCount();
			reRedingCount++;
			stockInfo.setReadingCount(reRedingCount);
			if (!goodsContentDao.update(stockInfo)) {
				return ReturnInfoUtils.errorInfo("暂无数据!");
			}
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.getBaseCode(), lm);
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> getCategoryGoods(Integer firstType, Integer secndType, Integer thirdType, int page,
			int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		if (firstType != null && firstType > 0) {
			paramMap.put("spareGoodsFirstTypeId", firstType + "");
		} else if (secndType != null && secndType > 0) {
			paramMap.put("spareGoodsSecondTypeId", secndType + "");
		} else if (thirdType != null && thirdType > 0) {
			paramMap.put("spareGoodsThirdTypeId", thirdType + "");
		}
		paramMap.put("deleteFlag", 0);
		paramMap.put("status", 2);
		List<Object> reGoodsRecordList = goodsContentDao.findByProperty(GoodsRecordDetail.class, paramMap, page, size);
		long reGoodsRecordCount = goodsContentDao.findByPropertyCount(GoodsRecordDetail.class, paramMap);
		if (reGoodsRecordList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reGoodsRecordList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.getBaseCode(), reGoodsRecordList);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), reGoodsRecordCount);
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> searchGoodsInfo(String goodsName, int page, int size) {
		if (page >= 0 && size >= 0) {
			Map<String, Object> statusMap = new HashMap<>();
			Table t = goodsContentDao.getBlurryRecordGoodsInfo(goodsName, page, size);
			Table tCount = goodsContentDao.getBlurryRecordGoodsInfo(goodsName, 0, 0);
			if (t == null) {
				return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
			} else {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
				statusMap.put(BaseCode.DATAS.getBaseCode(), Transform.tableToJson(t));
				statusMap.put(BaseCode.TOTALCOUNT.toString(), tCount.getRows().size());
				return statusMap;
			}
		}
		return ReturnInfoUtils.errorInfo("请求参数出错!");

	}

	@Override
	public Map<String, Object> searchMerchantGoodsDetailInfo(String merchantId, String merchantName,
			Map<String, Object> datasMap, int page, int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reDatasMap = SearchUtils.universalSearch(datasMap);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		Map<String, Object> blurryMap = (Map<String, Object>) reDatasMap.get("blurry");
		List<Map<String, Object>> errorList = (List<Map<String, Object>>) reDatasMap.get("error");
		paramMap.put("goodsMerchantId", merchantId);
		paramMap.put("deleteFlag", 0);
		List<Object> reList = goodsContentDao.findByPropertyLike(GoodsContent.class, paramMap, blurryMap, page, size);
		long totalCount = goodsContentDao.findByPropertyLikeCount(GoodsContent.class, paramMap, blurryMap);
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
	public Map<String, Object> merchantGetGoodsBaseInfo(String merchantId, String merchantName, String goodsId) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> param = new HashMap<>();
		param.put("goodsId", goodsId);
		param.put("goodsMerchantId", merchantId);
		List<Object> reList = goodsContentDao.findByProperty(GoodsContent.class, param, 1, 1);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			GoodsContent goodsInfo = (GoodsContent) reList.get(0);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), goodsInfo);
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}
}
