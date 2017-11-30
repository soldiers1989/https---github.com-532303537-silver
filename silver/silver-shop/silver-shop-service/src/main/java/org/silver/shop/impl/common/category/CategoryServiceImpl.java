package org.silver.shop.impl.common.category;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.category.CategoryService;
import org.silver.shop.dao.common.category.CategoryDao;
import org.silver.shop.model.common.category.GoodsFirstType;
import org.silver.shop.model.common.category.GoodsSecondType;
import org.silver.shop.model.common.category.GoodsThirdType;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.util.JedisUtil;
import org.silver.util.StringEmptyUtils;
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
		Map<String, Object> datasMap = new HashMap<>();
		Map<String, Map<String, List<Map<String, Object>>>> firstMap = new HashMap<>();
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
						List<Map<String, Object>> thirdList = new ArrayList<>();
						thirdList.add(thirdMap);
						firstMap.get(firstId + "_" + firstName).get(secId + "_" + secName).addAll(thirdList);
					} else {
						thirdMap = new HashMap<>();
						thirdMap.put("thirdId", thirdId);
						thirdMap.put("thirdName", thirdName);
						thirdMap.put("vat", vat);
						thirdMap.put("consumptionTax", consumptionTax);
						thirdMap.put("consolidatedTax", consolidatedTax);
						List<Map<String, Object>> thirdList = new ArrayList<>();
						thirdList.add(thirdMap);
						firstMap.get(firstId + "_" + firstName).put(secId + "_" + secName, thirdList);
					}
				} else {
					secondMap = new HashMap<>();
					thirdMap = new HashMap<>();
					thirdMap.put("thirdId", thirdId);
					thirdMap.put("thirdName", thirdName);
					thirdMap.put("vat", vat);
					thirdMap.put("consumptionTax", consumptionTax);
					thirdMap.put("consolidatedTax", consolidatedTax);
					List<Map<String, Object>> thirdList = new ArrayList<>();
					thirdList.add(thirdMap);
					secondMap.put(secId + "_" + secName, thirdList);
					firstMap.put(firstId + "_" + firstName, secondMap);
				}
			}
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.DATAS.toString(), firstMap);
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return datasMap;
	}

	@Override
	public Map<String, Object> addGoodsCategory(String managerId, String managerName, Map<String, Object> paramMap) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		int type = 0;
		try {
			type = Integer.parseInt(paramMap.get("type") + "");
		} catch (Exception e) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NOTICE.getMsg());
			return statusMap;
		}
		if (type == 1 || type == 2 || type == 3) {
			switch (type) {
			case 1:
				Map<String, Object> reFirstMap = saveFirstType(paramMap, managerName, date);
				if (!"1".equals(reFirstMap.get(BaseCode.STATUS.toString()))) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
					return statusMap;
				}
				break;
			case 2:
				String firstTypeId = paramMap.get("firstTypeId") + "";
				String goodsSecondTypeName = paramMap.get("goodsSecondTypeName") + "";
				Map<String, Object> reSecondMap = saveSecondType(Long.parseLong(firstTypeId), goodsSecondTypeName,
						managerName, date);
				if (!"1".equals(reSecondMap.get(BaseCode.STATUS.toString()))) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
					return statusMap;
				}
				break;
			case 3:
				Map<String, Object> reThirdMap = saveThirdType(paramMap, managerName, date);
				if (!"1".equals(reThirdMap.get(BaseCode.STATUS.toString()))) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
					return statusMap;
				}
				break;
			default:
				break;
			}
			Map<String, Object> datasMap = findGoodsType();
			String status = datasMap.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {
				datasMap = (Map) datasMap.get(BaseCode.DATAS.getBaseCode());
				// 将已查询出来的商品类型存入redis,有效期为1小时
				JedisUtil.setListDatas("Shop_Key_GoodsCategory_Map", 3600, datasMap);
			}
			System.out.println("-重新放入缓存-------------------");
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.NOTICE.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.NOTICE.getMsg());
		return statusMap;
	}

	/**
	 * 保存第三级商品类型
	 * 
	 * @param paramMap
	 *            参数
	 * @param managerName
	 *            管理员名称
	 * @param date
	 *            日期
	 * @return Map
	 */
	private Map<String, Object> saveThirdType(Map<String, Object> paramMap, String managerName, Date date) {
		Map<String, Object> statusMap = new HashMap<>();
		long firstTypeId2 = 0;
		long secondTypeId = 0;
		double vat = 0;
		double consumptionTax = 0;
		double consolidatedTax = 0;
		double tariff = 0;
		try {
			firstTypeId2 = Long.parseLong(paramMap.get("firstTypeId") + "");
			secondTypeId = Long.parseLong(paramMap.get("secondTypeId") + "");
			vat = Double.parseDouble(paramMap.get("vat") + "");
			consumptionTax = Double.parseDouble(paramMap.get("consumptionTax") + "");
			consolidatedTax = Double.parseDouble(paramMap.get("consolidatedTax") + "");
			tariff = Double.parseDouble(paramMap.get("tariff") + "");
		} catch (Exception e) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NOTICE.getMsg());
			return statusMap;
		}
		String goodsThirdTypeName = paramMap.get("goodsThirdTypeName") + "";
		GoodsThirdType thirdType = new GoodsThirdType();
		thirdType.setFirstTypeId(firstTypeId2);
		thirdType.setSecondTypeId(secondTypeId);
		thirdType.setGoodsThirdTypeName(goodsThirdTypeName);
		thirdType.setVat(vat);
		thirdType.setConsumptionTax(consumptionTax);
		thirdType.setConsolidatedTax(consolidatedTax);
		thirdType.setTariff(tariff);
		thirdType.setCreateBy(managerName);
		thirdType.setCreateDate(date);
		thirdType.setDeleteFlag(0);
		if (!categoryDao.add(thirdType)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	/**
	 * 保存第二级商品类型
	 * 
	 * @param firstTypeId
	 *            第一级商品类型Id
	 * @param goodsSecondTypeName
	 *            第二级类型名称
	 * @param managerName
	 *            管理员名称
	 * @param date
	 *            日期
	 * @return Map
	 */
	private Map<String, Object> saveSecondType(long firstTypeId, String goodsSecondTypeName, String managerName,
			Date date) {
		Map<String, Object> statusMap = new HashMap<>();
		GoodsSecondType secondType = new GoodsSecondType();
		secondType.setGoodsSecondTypeName(goodsSecondTypeName);
		secondType.setFirstTypeId(firstTypeId);
		secondType.setCreateBy(managerName);
		secondType.setCreateDate(date);
		secondType.setDeleteFlag(0);
		if (!categoryDao.add(secondType)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	/**
	 * 保存第一商品类型
	 * 
	 * @param paramMap
	 *            参数
	 * @param managerName
	 *            管理员名称
	 * @param date
	 *            日期
	 * @return Map
	 */
	private Map<String, Object> saveFirstType(Map<String, Object> paramMap, String managerName, Date date) {
		Map<String, Object> statusMap = new HashMap<>();
		GoodsFirstType firstType = new GoodsFirstType();
		String firstTypeName = paramMap.get("firstTypeName") + "";
		firstType.setFirstTypeName(firstTypeName);
		firstType.setCreateBy(managerName);
		firstType.setCreateDate(date);
		firstType.setDeleteFlag(0);
		if (!categoryDao.add(firstType)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	@Override
	public Map<String, Object> deleteGoodsCategory(String managerId, String managerName, Map<String, Object> paramMap) {
		Map<String, Object> statusMap = new HashMap<>();
		Iterator<String> isKey = paramMap.keySet().iterator();
		List<Map<String, Object>> errorList = new ArrayList<>();
		while (isKey.hasNext()) {
			String key = isKey.next();
			String value = paramMap.get(key) + "";
			switch (key) {
			case "firstTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					Map<String, Object> reAllCategoryMap = deleteAllCategory(value, errorList);
					if (!"1".equals(reAllCategoryMap.get(BaseCode.STATUS.toString()))) {
						statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
						statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
						return statusMap;
					}
				}
				break;
			case "secondTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					Map<String, Object> reSecondCategoryMap = deleteSecondCategory(value, errorList);
					if (!"1".equals(reSecondCategoryMap.get(BaseCode.STATUS.toString()))) {
						statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
						statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
						return statusMap;
					}
				}
				break;
			case "thirdTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					Map<String, Object> reThirdCategoryMap = deleteThirdCategory(value, errorList);
					if (!"1".equals(reThirdCategoryMap.get(BaseCode.STATUS.toString()))) {
						statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
						statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
						return statusMap;
					}
				}
				break;
			default:
				break;
			}
		}
		Map<String, Object> datasMap = findGoodsType();
		String status = datasMap.get(BaseCode.STATUS.toString()) + "";
		if ("1".equals(status)) {
			datasMap = (Map) datasMap.get(BaseCode.DATAS.getBaseCode());
			// 将已查询出来的商品类型存入redis,有效期为1小时
			JedisUtil.setListDatas("Shop_Key_GoodsCategory_Map", 3600, datasMap);
		}
		System.out.println("-重新放入缓存-------------------");
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		statusMap.put(BaseCode.ERROR.toString(), errorList);
		return statusMap;
	}

	//删除第三类型
	private Map<String, Object> deleteThirdCategory(String value, List<Map<String, Object>> errorList) {
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> statusMap = new HashMap<>();
		params.put("goodsThirdTypeId", value);
		List<Object> reGoodsContentList = categoryDao.findByProperty(GoodsContent.class, params, 0, 0);
		params.clear();
		params.put("spareGoodsThirdTypeId", value);
		List<Object> reGoodsRecordDetailList = categoryDao.findByProperty(GoodsRecordDetail.class, params, 0, 0);
		if (reGoodsRecordDetailList == null || reGoodsContentList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reGoodsRecordDetailList.isEmpty() && !reGoodsContentList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "该类型已关联商品,无法进行删除");
			return statusMap;
		} else {
			params.clear();
			params.put("id", Long.parseLong(value));
			List<Object> reThirdTypeList = categoryDao.findByProperty(GoodsThirdType.class, params, 0, 0);
			if (reThirdTypeList != null && !reThirdTypeList.isEmpty()) {
				for (int t = 0; t < reThirdTypeList.size(); t++) {
					GoodsThirdType thirdTypeInfo = (GoodsThirdType) reThirdTypeList.get(0);
					thirdTypeInfo.setDeleteFlag(1);
					if (!categoryDao.update(thirdTypeInfo)) {
						Map<String, Object> errorMap = new HashMap<>();
						errorMap.put(BaseCode.MSG.toString(), thirdTypeInfo.getGoodsThirdTypeName() + "删除失败,请重试!");
						errorList.add(errorMap);
					}
				}
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	// 删除第二类型
	private Map<String, Object> deleteSecondCategory(String value, List<Map<String, Object>> errorList) {
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> statusMap = new HashMap<>();
		params.put("spareGoodsSecondTypeId", value);
		List<Object> reGoodsRecordDetailList = categoryDao.findByProperty(GoodsRecordDetail.class, params, 0, 0);
		params.clear();
		params.put("goodsSecondTypeId", value);
		List<Object> reGoodsContentList = categoryDao.findByProperty(GoodsContent.class, params, 0, 0);
		if (reGoodsRecordDetailList == null || reGoodsContentList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reGoodsRecordDetailList.isEmpty() && !reGoodsContentList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "该类型已关联商品,无法进行删除");
			return statusMap;
		} else {
			params.clear();
			params.put("id", Long.parseLong(value));
			List<Object> reSecondTypeList = categoryDao.findByProperty(GoodsSecondType.class, params, 0, 0);
			params.clear();
			params.put("firstTypeId", Long.parseLong(value));
			List<Object> reThirdTypeList = categoryDao.findByProperty(GoodsThirdType.class, params, 0, 0);
			if (reSecondTypeList != null && !reSecondTypeList.isEmpty()) {
				GoodsSecondType secondTypeInfo = (GoodsSecondType) reSecondTypeList.get(0);
				// 删除标识：0-未删除,1-已删除
				secondTypeInfo.setDeleteFlag(1);
				if (!categoryDao.update(secondTypeInfo)) {
					Map<String, Object> errorMap = new HashMap<>();
					errorMap.put(BaseCode.MSG.toString(), secondTypeInfo.getGoodsSecondTypeName() + "删除失败,请重试!");
					errorList.add(errorMap);
				}
				for (int t = 0; t < reThirdTypeList.size(); t++) {
					GoodsThirdType thirdTypeInfo = (GoodsThirdType) reThirdTypeList.get(0);
					thirdTypeInfo.setDeleteFlag(1);
					if (!categoryDao.update(thirdTypeInfo)) {
						Map<String, Object> errorMap = new HashMap<>();
						errorMap.put(BaseCode.MSG.toString(), thirdTypeInfo.getGoodsThirdTypeName() + "删除失败,请重试!");
						errorList.add(errorMap);
					}
				}

			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	// 删除该商品大类型
	private Map<String, Object> deleteAllCategory(String value, List<Map<String, Object>> errorList) {
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> statusMap = new HashMap<>();
		params.put("spareGoodsFirstTypeId", value);
		List<Object> reGoodsRecordDetailList = categoryDao.findByProperty(GoodsRecordDetail.class, params, 0, 0);
		params.clear();
		params.put("goodsFirstTypeId", value);
		List<Object> reGoodsContentList = categoryDao.findByProperty(GoodsContent.class, params, 0, 0);
		if (reGoodsRecordDetailList == null || reGoodsContentList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reGoodsRecordDetailList.isEmpty() && !reGoodsContentList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "该类型已关联商品,无法进行删除");
			return statusMap;
		} else {
			params.clear();
			params.put("id", Long.parseLong(value));
			List<Object> reFirstTypeList = categoryDao.findByProperty(GoodsFirstType.class, params, 0, 0);
			params.clear();
			params.put("firstTypeId", Long.parseLong(value));
			List<Object> reSecondTypeList = categoryDao.findByProperty(GoodsSecondType.class, params, 0, 0);
			params.clear();
			params.put("firstTypeId", Long.parseLong(value));
			List<Object> reThirdTypeList = categoryDao.findByProperty(GoodsThirdType.class, params, 0, 0);
			if (reFirstTypeList == null || reSecondTypeList == null || reThirdTypeList == null) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
				return statusMap;
			} else if (!reFirstTypeList.isEmpty() && !reSecondTypeList.isEmpty() && !reThirdTypeList.isEmpty()) {
				GoodsFirstType firstTypeInfo = (GoodsFirstType) reFirstTypeList.get(0);
				for (int s = 0; s < reSecondTypeList.size(); s++) {
					GoodsSecondType secondTypeInfo = (GoodsSecondType) reSecondTypeList.get(s);
					secondTypeInfo.setDeleteFlag(1);
					if (!categoryDao.update(secondTypeInfo)) {
						Map<String, Object> errorMap = new HashMap<>();
						errorMap.put(BaseCode.MSG.toString(), secondTypeInfo.getGoodsSecondTypeName() + "删除失败,请重试!");
						errorList.add(errorMap);
					}
				}
				for (int t = 0; t < reThirdTypeList.size(); t++) {
					GoodsThirdType thirdTypeInfo = (GoodsThirdType) reThirdTypeList.get(0);
					thirdTypeInfo.setDeleteFlag(1);
					if (!categoryDao.update(thirdTypeInfo)) {
						Map<String, Object> errorMap = new HashMap<>();
						errorMap.put(BaseCode.MSG.toString(), thirdTypeInfo.getGoodsThirdTypeName() + "删除失败,请重试!");
						errorList.add(errorMap);
					}
				}
				// 删除标识：0-未删除,1-已删除
				firstTypeInfo.setDeleteFlag(1);
				if (!categoryDao.update(firstTypeInfo)) {
					Map<String, Object> errorMap = new HashMap<>();
					errorMap.put(BaseCode.MSG.toString(), firstTypeInfo.getFirstTypeName() + "删除失败,请重试");
					errorList.add(errorMap);
				}
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	@Override
	public Map<String, Object> editGoodsCategory(String managerId, String managerName, Map<String, Object> paramMap) {
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> statusMap = new HashMap<>();
		Iterator<String> isKey = paramMap.keySet().iterator();
		List<Map<String, Object>> errorList = new ArrayList<>();
		while (isKey.hasNext()) {
			String key = isKey.next();
			String value = paramMap.get(key) + "";
			switch (key) {
			case "firstTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					Map<String, Object> reAllCategoryMap = editFirstCategory(value, errorList);
					if (!"1".equals(reAllCategoryMap.get(BaseCode.STATUS.toString()))) {
						statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
						statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
						return statusMap;
					}
				}
				break;
			case "secondTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					Map<String, Object> reSecondCategoryMap = deleteSecondCategory(value, errorList);
					if (!"1".equals(reSecondCategoryMap.get(BaseCode.STATUS.toString()))) {
						statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
						statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
						return statusMap;
					}
				}
				break;
			case "thirdTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					Map<String, Object> reThirdCategoryMap = deleteThirdCategory(value, errorList);
					if (!"1".equals(reThirdCategoryMap.get(BaseCode.STATUS.toString()))) {
						statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
						statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
						return statusMap;
					}
				}
				break;
			default:
				break;
			}
		}
		return null;
	}

	/**
	 * 修改商品第一类型
	 * @param value 
	 * @param errorList
	 * @return
	 */
	private Map<String, Object> editFirstCategory(String value, List<Map<String, Object>> errorList) {
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> statusMap = new HashMap<>();
		List<Object> goodsFirstList= categoryDao.findByProperty(GoodsFirstType.class, params, 0, 0);
		params.put("spareGoodsFirstTypeId", value);
		List<Object> reGoodsRecordDetailList = categoryDao.findByProperty(GoodsRecordDetail.class, params, 0, 0);
		params.clear();
		params.put("goodsFirstTypeId", value);
		List<Object> reGoodsContentList = categoryDao.findByProperty(GoodsContent.class, params, 0, 0);
		if (reGoodsRecordDetailList == null || reGoodsContentList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!goodsFirstList.isEmpty()) {
			GoodsFirstType goodsFirstType= (GoodsFirstType) goodsFirstList.get(0);
			
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
		return null;
	}
}
