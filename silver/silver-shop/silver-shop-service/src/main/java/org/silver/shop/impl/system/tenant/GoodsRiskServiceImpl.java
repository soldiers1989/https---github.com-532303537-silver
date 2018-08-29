package org.silver.shop.impl.system.tenant;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.tenant.GoodsRiskService;
import org.silver.shop.dao.system.tenant.GoodsRiskDao;
import org.silver.shop.model.system.commerce.GoodsRecord;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.tenant.EvaluationContent;
import org.silver.shop.model.system.tenant.GoodsRiskControlContent;
import org.silver.shop.util.IdUtils;
import org.silver.shop.util.SearchUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringOrderUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = GoodsRiskService.class)
public class GoodsRiskServiceImpl implements GoodsRiskService {

	@Autowired
	private GoodsRiskDao goodsRiskDao;
	@Autowired
	private IdUtils idUtils;

	@Override
	public Map<String, Object> getInfo(Map<String, Object> datasMap, int page, int size) {
		Map<String, Object> reDatasMap = SearchUtils.goodsRiskControl(datasMap);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		Map<String, Object> blurryMap = (Map<String, Object>) reDatasMap.get("blurry");
		paramMap.put("deleteFlag", 0);
		List<GoodsRiskControlContent> reList = goodsRiskDao.findByPropertyLike(GoodsRiskControlContent.class, paramMap,
				blurryMap, page, size);
		long count = goodsRiskDao.findByPropertyLikeCount(GoodsRiskControlContent.class, paramMap, blurryMap);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, count);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据");
		}
	}

	@Override
	public Object tmpUpdate() {
		List<GoodsRecordDetail> reList = goodsRiskDao.findByProperty(GoodsRecordDetail.class, null, 0, 0);
		for (GoodsRecordDetail goods : reList) {
			addGoodsRiskControlContent(goods);
		}
		return null;
	}

	@Override
	public void addGoodsRiskControlContent(GoodsRecordDetail goods) {
		Map<String, Object> params = new HashMap<>();
		params.put("hsCode", goods.getHsCode());
		params.put("goodsStyle", goods.getGoodsStyle());
		params.put("goodsBrand", goods.getBrand());
		List<GoodsRiskControlContent> reList2 = goodsRiskDao.findByProperty(GoodsRiskControlContent.class, params,
				0, 0);
		if (reList2 != null && !reList2.isEmpty()) {
			GoodsRiskControlContent content = reList2.get(0);
			if (StringOrderUtil.isScrambledString(goods.getGoodsName(), content.getGoodsName())) {
				System.out.println("----商品已存在--");
			} else {
				GoodsRiskControlContent entity = new GoodsRiskControlContent();
				Map<String, Object> reIdMap = idUtils.createId(GoodsRiskControlContent.class, "riskNo_");
				entity.setRiskNo(reIdMap.get(BaseCode.DATAS.toString()) + "");
				entity.setEntGoodsNo(goods.getEntGoodsNo());
				entity.setHsCode(goods.getHsCode());
				entity.setGoodsName(goods.getGoodsName());
				entity.setGoodsStyle(goods.getGoodsStyle());
				entity.setGoodsBrand(goods.getBrand());
				entity.setRegPrice(goods.getRegPrice());
				entity.setCreateBy("system");
				entity.setCreateDate(new Date());
				if (!goodsRiskDao.add(entity)) {
					System.out.println("--保存失败--");
				}
				System.out.println("--保存成功--");
			}
		} else {
			GoodsRiskControlContent entity = new GoodsRiskControlContent();
			Map<String, Object> reIdMap = idUtils.createId(GoodsRiskControlContent.class, "riskNo_");
			entity.setRiskNo(reIdMap.get(BaseCode.DATAS.toString()) + "");
			entity.setEntGoodsNo(goods.getEntGoodsNo());
			entity.setHsCode(goods.getHsCode());
			entity.setGoodsName(goods.getGoodsName());
			entity.setGoodsStyle(goods.getGoodsStyle());
			entity.setGoodsBrand(goods.getBrand());
			entity.setRegPrice(goods.getRegPrice());
			entity.setCreateBy("system");
			entity.setCreateDate(new Date());
			if (!goodsRiskDao.add(entity)) {
				System.out.println("--保存失败--");
			}
			System.out.println("==保存成功==");
		}
		
	}

	@Override
	public Map<String, Object> updateInfo(Map<String, Object> datasMap, Manager managerInfo) {
		if (datasMap == null || managerInfo == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("riskNo", datasMap.get("riskNo"));
		List<GoodsRiskControlContent> reList2 = goodsRiskDao.findByProperty(GoodsRiskControlContent.class, params, 0,
				0);
		if (reList2 == null) {
			return ReturnInfoUtils.errorInfo("查询失败，服务器繁忙！");
		} else if (!reList2.isEmpty()) {
			GoodsRiskControlContent content = reList2.get(0);
			if (StringEmptyUtils.isNotEmpty(datasMap.get("platformOnePrice"))) {
				content.setPlatformOnePrice(Double.parseDouble(datasMap.get("platformOnePrice") + ""));
			}
			if (StringEmptyUtils.isNotEmpty(datasMap.get("platformTwoPrice"))) {
				content.setPlatformTwoPrice(Double.parseDouble(datasMap.get("platformTwoPrice") + ""));
			}
			if (StringEmptyUtils.isNotEmpty(datasMap.get("platformThreePrivce"))) {
				content.setPlatformThreePrivce(Double.parseDouble(datasMap.get("platformThreePrivce") + ""));
			}
			if (StringEmptyUtils.isNotEmpty(datasMap.get("platformFourPrice"))) {
				content.setPlatformFourPrice(Double.parseDouble(datasMap.get("platformFourPrice") + ""));
			}
			if (StringEmptyUtils.isNotEmpty(datasMap.get("referencePrice"))) {
				content.setReferencePrice(Double.parseDouble(datasMap.get("referencePrice") + ""));
			}
			if (StringEmptyUtils.isNotEmpty(datasMap.get("note"))) {
				content.setNote(datasMap.get("note") + "");
			}
			content.setUpdateDate(new Date());
			content.setUpdateBy(managerInfo.getRealName());
			if(!goodsRiskDao.update(content)){
				return ReturnInfoUtils.errorInfo("更新失败，服务器繁忙！");
			}
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("未找到对应信息");
		}
	}

}
