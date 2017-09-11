package org.silver.shop.impl.common.category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.shop.api.common.category.CategoryService;
import org.silver.shop.dao.common.category.CategoryDao;
import org.silver.shop.model.common.category.GoodsFirstType;
import org.silver.shop.model.common.category.GoodsSecondType;
import org.silver.shop.model.common.category.GoodsThirdType;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = CategoryService.class)
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryDao categoryDao;

	@Override
	public Map<String, HashMap<String, Object>> findGoodsType() {
		Map<String, HashMap<String, Object>> reMap = new HashMap<>();
		Map<String, Object> thirdTypeMap = null;
		Map<String, Object> secondTypeMap = null;
		Map<String, Object> firstTypeMap = null;
		// 查询商品所有第一级类型
		List<Object> firstTypeList = categoryDao.findAllfirstType();
		// 查询商品所有第二级类型
		List<Object> secondTypeList = categoryDao.findAllSecondType();
		// 查询商品所有第三级类型
		List<Object> thirdTypeList = categoryDao.findAllThirdType();
		// 商品类型第一级map计数
		int firstCount = 1;
		for (int n = 0; n < firstTypeList.size(); n++) {// 遍历商品类型第一级
			GoodsFirstType firstType = (GoodsFirstType) firstTypeList.get(n);
			firstTypeMap = new HashMap<>();
			firstTypeMap.put("id", firstType.getId());
			firstTypeMap.put("firstTypeName", firstType.getFirstTypeName());
			firstTypeMap.put("createBy", firstType.getCreateBy());
			firstTypeMap.put("createDate", firstType.getCreateDate());
			firstTypeMap.put("updateBy", firstType.getUpdateBy());
			firstTypeMap.put("updateDate", firstType.getUpdateDate());
			firstTypeMap.put("deletFlag", firstType.getDeletFlag());
			firstTypeMap.put("deleteBy", firstType.getDeleteBy());
			firstTypeMap.put("deleteDate", firstType.getDeleteDate());
			// 商品类型第二级map计数
			int secCount = 1;
			for (int y = 0; y < secondTypeList.size(); y++) {// 遍历商品类型第二级
				GoodsSecondType secondType = (GoodsSecondType) secondTypeList.get(y);
				// 获取到第一级类型ID
				Long id = firstType.getId();
				// 获取到第二级类型表中关联的第一级ID
				Long secondFirstId = secondType.getFirstTypeId();
				// 获取到第二级类型表中ID
				long secondId = secondType.getId();
				if (id == secondFirstId) {// 判断第二级表中的ID与第一级ID是否相同
					secondTypeMap = new HashMap<>();
					secondTypeMap.put("id", secondType.getId());
					secondTypeMap.put("firstTypeId", secondType.getFirstTypeId());
					secondTypeMap.put("goodsSecondTypeName", secondType.getGoodsSecondTypeName());
					secondTypeMap.put("createBy", firstType.getCreateBy());
					secondTypeMap.put("createDate", firstType.getCreateDate());
					secondTypeMap.put("updateBy", firstType.getUpdateBy());
					secondTypeMap.put("updateDate", firstType.getUpdateDate());
					secondTypeMap.put("deletFlag", firstType.getDeletFlag());
					secondTypeMap.put("deleteBy", firstType.getDeleteBy());
					secondTypeMap.put("deleteDate", firstType.getDeleteDate());
					// 商品类型第三级map计数
					int thirdCount = 1;
					for (int x = 0; x < thirdTypeList.size(); x++) {// 遍历商品类型第三级
						GoodsThirdType goodsThirdType = (GoodsThirdType) thirdTypeList.get(x);
						// 获取商品第三级(表)类型中关联的第二级ID
						long thirdSecId = goodsThirdType.getSecondTypeId();
						if (secondId == thirdSecId) {// 判断第二级表中的ID与第三级表中的二级ID是否一样
							thirdTypeMap = new HashMap<>();
							thirdTypeMap.put("id", goodsThirdType.getId());
							thirdTypeMap.put("firstTypeId", goodsThirdType.getFirstTypeId());
							thirdTypeMap.put("secondTypeId", goodsThirdType.getSecondTypeId());
							thirdTypeMap.put("goodsThirdTypeName", goodsThirdType.getGoodsThirdTypeName());
							thirdTypeMap.put("vat", goodsThirdType.getVat());
							thirdTypeMap.put("consumptionTax", goodsThirdType.getConsumptionTax());
							thirdTypeMap.put("tariff", goodsThirdType.getTariff());
							thirdTypeMap.put("consolidatedTax", goodsThirdType.getConsolidatedTax());
							thirdTypeMap.put("createBy", firstType.getCreateBy());
							thirdTypeMap.put("createDate", firstType.getCreateDate());
							thirdTypeMap.put("updateBy", firstType.getUpdateBy());
							thirdTypeMap.put("updateDate", firstType.getUpdateDate());
							thirdTypeMap.put("deletFlag", firstType.getDeletFlag());
							thirdTypeMap.put("deleteBy", firstType.getDeleteBy());
							thirdTypeMap.put("deleteDate", firstType.getDeleteDate());
							secondTypeMap.put("thirdTypeMap" + thirdCount++, thirdTypeMap);
						}
					}
					firstTypeMap.put("secondType" + secCount++, secondTypeMap);
				}
			}
			reMap.put("firstTypeMap" + firstCount++, (HashMap<String, Object>) firstTypeMap);
		}
		return reMap;
	}
}
