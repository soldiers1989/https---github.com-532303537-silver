package org.silver.shop.impl.system.commerce;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.shop.api.system.commerce.MerchantCounterService;
import org.silver.shop.dao.system.commerce.MerchantCounterDao;
import org.silver.shop.model.system.commerce.CounterGoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecord;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantCounterContent;
import org.silver.shop.util.IdUtils;
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
			return ReturnInfoUtils.successDataInfo(reList,count);
		}
		return ReturnInfoUtils.errorInfo("暂无数据！");
	}

	@Override
	public Map<String, Object> getGoodsInfo(String merchantId, Map<String, Object> datasMap, int page, int size) {
		Map<String, Object> params = new HashMap<>();
		if (StringEmptyUtils.isNotEmpty(merchantId)) {
			params.put("counterOwnerId", merchantId);
		}
		if(StringEmptyUtils.isNotEmpty(datasMap.get("counterId"))){
			params.put("counterId", datasMap.get("counterId")+"");
		}
		params.put("deleteFlag", 0);
		List<CounterGoodsContent> reList = merchantCounterDao.findByProperty(CounterGoodsContent.class, params, page,
				size);
		long count = merchantCounterDao.findByPropertyCount(CounterGoodsContent.class, params);
		if (reList == null) {
			return ReturnInfoUtils.warnInfo();
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList,count);
		}
		return ReturnInfoUtils.errorInfo("暂无数据！");
	}

	@Override
	public Map<String, Object> addCounterInfo(Merchant merchantInfo, Map<String, Object> datasMap) {
		if (merchantInfo == null || datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		return null;
	}

	@Override
	public Map<String, Object> addGoodsInfo(Merchant merchantInfo, Map<String, Object> datasMap) {
		if (merchantInfo == null || datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		Map<String, Object> params = new HashMap<>();
		if("MerchantId_00047".equals(merchantInfo.getMerchantId())){
			params.put("counterId", datasMap.get("counterId") + "");
		}else if("MerchantId_00069".equals(merchantInfo.getMerchantId())){
			params.put("counterId", datasMap.get("counterId") + "");
		}else{
			return ReturnInfoUtils.errorInfo("未找到专柜信息，请联系管理员！");
		}
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

		
		for (int i = 0; i < jsonArray.size(); i++) {
			String entGoodsNo = jsonArray.get(i) + "";
			params.clear();
			params.put("entGoodsNo", entGoodsNo);
			List<GoodsRecordDetail> reGoodsList = merchantCounterDao.findByProperty(GoodsRecordDetail.class, params, 0,
					0);
			long id = merchantCounterDao.findLastId(GoodsRecordDetail.class);
			if(id < 0 ){
				return ReturnInfoUtils.errorInfo("查询id失败，服务器繁忙！");
			}
			if (reGoodsList != null && !reGoodsList.isEmpty()) {
				GoodsRecordDetail goods = reGoodsList.get(0);
				CounterGoodsContent content = new CounterGoodsContent();
				content.setSerialNo(SerialNoUtils.getSerialNo("C", id));
				content.setCounterId(datasMap.get("counterId") + "");
				content.setCounterOwnerId(merchantInfo.getMerchantId());
				content.setCounterOwnerName(merchantInfo.getMerchantName());
				content.setMerchantId(goods.getGoodsMerchantId());
				content.setMerchantName(goods.getGoodsMerchantName());
				content.setEntGoodsNo(entGoodsNo);
				content.setGoodsName(goods.getGoodsName());
				content.setRegPrice(goods.getRegPrice());
				//推广标识：1-允许分销、2-不允许分销
				content.setPopularizeFlag(2);
				content.setCreateDate(new Date());
				content.setCreateBy(merchantInfo.getMerchantName());
				content.setRemark(goods.getSpareGoodsImage());
				// popularizeProfit
				if (!merchantCounterDao.add(content)) {
					return ReturnInfoUtils.errorInfo("保存失败,服务器繁忙！");
				}
			} else {
				System.out.println("--未找到商品信息--");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> counterInfo(String counterId) {
		if(StringEmptyUtils.isEmpty(counterId)){
			return ReturnInfoUtils.errorInfo("请求参数不能为空！");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("counterId", counterId);
		List<MerchantCounterContent> reList = merchantCounterDao.findByProperty(MerchantCounterContent.class, params, 0,
				0);
		if (reList == null) {
			return ReturnInfoUtils.warnInfo();
		}else if(!reList.isEmpty()){
			return ReturnInfoUtils.successDataInfo(reList.get(0));
		}
		return ReturnInfoUtils.errorInfo("暂无柜台信息！");
	}


}
