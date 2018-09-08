package org.silver.shop.impl.system.commerce;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.commerce.MerchantCounterService;
import org.silver.shop.dao.system.commerce.MerchantCounterDao;
import org.silver.shop.model.system.commerce.CounterGoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantCounterContent;
import org.silver.shop.util.IdUtils;
import org.silver.shop.util.InquireHelperService;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = MerchantCounterService.class)
public class MerchantCounterServiceImpl implements MerchantCounterService {

	@Autowired
	private MerchantCounterDao merchantCounterDao;
	@Autowired
	private IdUtils idUtils;
	@Autowired
	private InquireHelperService inquireHelperService;

	@Override
	public Map<String, Object> getInfo(Map<String, Object> datasMap, int page, int size) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		Map<String, Object> params = new HashMap<>();
		if (StringEmptyUtils.isNotEmpty(datasMap.get("merchantId"))) {
			params.put("merchantId", datasMap.get("merchantId") + "");
		}
		List<MerchantCounterContent> reList = merchantCounterDao.findByProperty(MerchantCounterContent.class, params,
				page, size);
		long count = merchantCounterDao.findByPropertyCount(MerchantCounterContent.class, params);
		if (reList == null) {
			return ReturnInfoUtils.warnInfo();
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, count);
		}
		return ReturnInfoUtils.errorInfo("暂无数据！");
	}

	@Override
	public Map<String, Object> getGoodsInfo(String merchantId, Map<String, Object> datasMap, int page, int size) {
		Map<String, Object> params = new HashMap<>();
		if (StringEmptyUtils.isNotEmpty(merchantId)) {
			params.put("counterOwnerId", merchantId);
		}
		if (StringEmptyUtils.isNotEmpty(datasMap.get("counterId"))) {
			params.put("counterId", datasMap.get("counterId") + "");
		}
		params.put("deleteFlag", 0);
		List<CounterGoodsContent> reList = merchantCounterDao.findByProperty(CounterGoodsContent.class, params, page,
				size);
		long count = merchantCounterDao.findByPropertyCount(CounterGoodsContent.class, params);
		if (reList == null) {
			return ReturnInfoUtils.warnInfo();
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, count);
		}
		return ReturnInfoUtils.errorInfo("暂无数据！");
	}

	@Override
	public Map<String, Object> addCounterInfo(Merchant merchantInfo, Map<String, Object> datasMap,
			List<Object> imglist) {
		if (merchantInfo == null || datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		MerchantCounterContent counter = new MerchantCounterContent();
		long id = merchantCounterDao.findLastId(GoodsRecordDetail.class);
		if (id < 0) {
			return ReturnInfoUtils.errorInfo("查询id失败，服务器繁忙！");
		}
		String counterId = SerialNoUtils.getSerialNo("C", id);
		counter.setCounterId(counterId);
		counter.setMerchantId(merchantInfo.getMerchantId());
		counter.setMerchantName(merchantInfo.getMerchantName());
		Map<String, Object> reCheckMap = checkCounterInfo(datasMap);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		counter.setHeadContent(datasMap.get("headContent") + "");
		counter.setDescription(datasMap.get("description") + "");
		if (imglist == null || imglist.isEmpty()) {
			return ReturnInfoUtils.errorInfo("log图片不能为空");
		}
		// https://vip.191ec.com?counterId=
		String topUrl = "https://vip.191ec.com?counterId=";
		counter.setCounterURL(topUrl + counterId);
		counter.setLogo(imglist.get(0) + "");
		counter.setImge(datasMap.get("imge") + "");
		counter.setCreateBy(merchantInfo.getMerchantName());
		counter.setCreateDate(new Date());
		if (!merchantCounterDao.add(counter)) {
			return ReturnInfoUtils.errorInfo("保存失败，服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 校验专柜必填信息
	 * 
	 * @param datasMap
	 * @return
	 */
	private Map<String, Object> checkCounterInfo(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		String headContent = datasMap.get("headContent") + "";
		if (StringEmptyUtils.isEmpty(headContent)) {
			return ReturnInfoUtils.errorInfo("专柜名称不能为空！");
		}
		String imge = datasMap.get("imge") + "";
		if (StringEmptyUtils.isEmpty(imge)) {
			return ReturnInfoUtils.errorInfo("专柜展示图片不能为空！");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> addGoodsInfo(Merchant merchantInfo, Map<String, Object> datasMap) {
		if (merchantInfo == null || datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantInfo.getMerchantId());
		List<MerchantCounterContent> reList = merchantCounterDao.findByProperty(MerchantCounterContent.class, params, 0,
				0);
		if (reList == null) {
			return ReturnInfoUtils.warnInfo();
		} else if (reList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("该商户尚未设置商品专柜！");
		}
		String goodsInfoPack = datasMap.get("goodsInfoPack") + "";
		JSONArray jsonArray = null;
		try {
			jsonArray = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("商品信息参数错误！");
		}

		List<Map<String, Object>> errorList = new ArrayList<>();
		Map<String, Object> errMap = null;
		for (int i = 0; i < jsonArray.size(); i++) {
			String entGoodsNo = jsonArray.get(i) + "";
			params.clear();
			params.put("entGoodsNo", entGoodsNo);
			List<GoodsRecordDetail> reGoodsList = merchantCounterDao.findByProperty(GoodsRecordDetail.class, params, 0,
					0);
			long id = merchantCounterDao.findLastId(GoodsRecordDetail.class);
			if (id < 0) {
				return ReturnInfoUtils.errorInfo("查询id失败，服务器繁忙！");
			}
			if (reGoodsList != null && !reGoodsList.isEmpty()) {
				List<StockContent> reStockList = merchantCounterDao.findByProperty(StockContent.class, params, 0, 0);
				if (reStockList == null || reStockList.isEmpty()) {
					errMap = new HashMap<>();
					errMap.put(BaseCode.MSG.toString(), "商品自编号[" + entGoodsNo + "]对应的商品尚未上架，不能放入专柜中！");
					errorList.add(errMap);
				} else {
					StockContent stock = reStockList.get(0);
					GoodsRecordDetail goods = reGoodsList.get(0);
					CounterGoodsContent content = new CounterGoodsContent();
					content.setSerialNo(SerialNoUtils.getSerialNo("C", id));
					content.setCounterId(datasMap.get("counterId") + "");
					content.setCounterOwnerId(merchantInfo.getMerchantId());
					content.setCounterOwnerName(merchantInfo.getMerchantName());
					content.setGoodsMerchantId(goods.getGoodsMerchantId());
					content.setGoodsMerchantName(goods.getGoodsMerchantName());
					content.setEntGoodsNo(entGoodsNo);
					content.setGoodsName(goods.getGoodsName());
					content.setRegPrice(stock.getRegPrice());
					// 推广标识：1-允许分销、2-不允许分销
					content.setPopularizeFlag(2);
					content.setCreateDate(new Date());
					content.setCreateBy(merchantInfo.getMerchantName());
					content.setGoodsImage(goods.getSpareGoodsImage());
					if (!merchantCounterDao.add(content)) {
						return ReturnInfoUtils.errorInfo("保存失败,服务器繁忙！");
					}
				}
			} else {
				errMap = new HashMap<>();
				errMap.put(BaseCode.MSG.toString(), "商品自编号[" + entGoodsNo + "]未找到商品信息!");
				errorList.add(errMap);
			}
		}
		return ReturnInfoUtils.errorInfo(errorList);
	}

	@Override
	public Map<String, Object> counterInfo(String counterId) {
		if (StringEmptyUtils.isEmpty(counterId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空！");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("counterId", counterId);
		List<MerchantCounterContent> reList = merchantCounterDao.findByProperty(MerchantCounterContent.class, params, 0,
				0);
		if (reList == null) {
			return ReturnInfoUtils.warnInfo();
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList.get(0));
		}
		return ReturnInfoUtils.errorInfo("暂无柜台信息！");
	}

	@Override
	public Map<String, Object> updatePopularizeFlag(Map<String, Object> datasMap) {
		String popularizeProfit = datasMap.get("popularizeProfit") + "";
		String popularizeFlag = datasMap.get("popularizeFlag") + "";
		datasMap.remove("popularizeProfit");
		datasMap.remove("popularizeFlag");
		Map<String, Object> reMap = inquireHelperService.getInfo(CounterGoodsContent.class, datasMap, 1, 1);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		CounterGoodsContent goods = (CounterGoodsContent) reMap.get(BaseCode.DATAS.toString());
		int flag = 0;
		try {
			flag = Integer.parseInt(popularizeFlag);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("推广标识错误！");
		}
		goods.setPopularizeFlag(flag);
		if (flag == 1) {// 推广标识：1-允许分销、2-不允许分销
			if (StringEmptyUtils.isEmpty(popularizeProfit) || Double.parseDouble(popularizeProfit) == 0) {
				return ReturnInfoUtils.errorInfo("推广分润不能为0");
			}
			goods.setPopularizeProfit(Double.parseDouble(popularizeProfit));
		}
		return updateCounterGoods(goods);
	}

	private Map<String, Object> updateCounterGoods(CounterGoodsContent entity) {
		if (entity == null) {
			return ReturnInfoUtils.errorInfo("更新参数不能为null");
		}
		entity.setUpdateDate(new Date());
		if (!merchantCounterDao.update(entity)) {
			return ReturnInfoUtils.errorInfo("更新商品信息失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successDataInfo(entity);
	}
}
