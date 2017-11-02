package org.silver.shop.impl.common.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.category.CategoryService;
import org.silver.shop.dao.common.category.CategoryDao;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Row;
import com.justep.baas.data.Table;

@Service(interfaceClass = CategoryService.class)
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryDao categoryDao;

	@Override
	public Map<String, Object> findGoodsType() {
		List<Object> totalList = new ArrayList<>();
		Map<String, Object> datasMap = new HashMap<>();
		Map<String, Map<String, List<Map<String, Object>>>> firstMap = null;
		Map<String, List<Map<String, Object>>> secondMap = null;
		Map<String, Object> thirdMap = null;
		Table t = categoryDao.findAllCategory();
		if (t != null && t.getRows().size() > 0) {
			// 获取表中的数据
			List<Row> lr = t.getRows();
			for (int i = 0; i < lr.size(); i++) {
				String firstId = lr.get(i).getValue("id") + "";
				String firstName = lr.get(i).getValue("firstTypeName") + "";
				String secId = lr.get(i).getValue("secId") + "";
				String secName = lr.get(i).getValue("goodsSecondTypeName") + "";
				String thirdId = lr.get(i).getValue("thirdId") + "";
				String thirdName = lr.get(i).getValue("goodsThirdTypeName") + "";
				// 增值税
				String vat = lr.get(i).getValue("vat") + "";
				// 消费税
				String consumptionTax = lr.get(i).getValue("consumptionTax") + "";
				// 综合税 跨境电商综合税率 = （消费税率+增值税率）/（1-消费税率）×70%
				String consolidatedTax = lr.get(i).getValue("consolidatedTax") + "";

				if (firstMap != null && firstMap.get(firstId + "_" + firstName) != null) {
					if (secondMap != null && secondMap.get(secId + "_" + secName) != null) {
						thirdMap = new HashMap<>();
						thirdMap.put("thirdId", thirdId);
						thirdMap.put("thirdName", thirdName);
						thirdMap.put("vat", vat);
						thirdMap.put("consumptionTax", consumptionTax);
						thirdMap.put("consolidatedTax", consolidatedTax);
						List<Map<String,Object>> thirdList = new ArrayList<>();
						thirdList.add(thirdMap);
						firstMap.get(firstId + "_" + firstName).get(secId + "_" + secName).addAll(thirdList);
					} else {
						thirdMap = new HashMap<>();
						thirdMap.put("thirdId", thirdId);
						thirdMap.put("thirdName", thirdName);
						thirdMap.put("vat", vat);
						thirdMap.put("consumptionTax", consumptionTax);
						thirdMap.put("consolidatedTax", consolidatedTax);
						List<Map<String,Object>> thirdList = new ArrayList<>();
						thirdList.add(thirdMap);
						firstMap.get(firstId + "_" + firstName).put(secId + "_" + secName, thirdList);
					}
				} else {
					firstMap = new HashMap<>();
					secondMap = new HashMap<>();
					thirdMap = new HashMap<>();
					thirdMap.put("thirdId", thirdId);
					thirdMap.put("thirdName", thirdName);
					thirdMap.put("vat", vat);
					thirdMap.put("consumptionTax", consumptionTax);
					thirdMap.put("consolidatedTax", consolidatedTax);
					List<Map<String,Object>> thirdList = new ArrayList<>();
					thirdList.add(thirdMap);
					secondMap.put(secId + "_" + secName, thirdList);
					
					firstMap.put(firstId + "_" + firstName, secondMap);
					totalList.add(firstMap);
				}
			}
		}
		datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		datasMap.put(BaseCode.DATAS.toString(),totalList);
		datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return datasMap;
	}
}
