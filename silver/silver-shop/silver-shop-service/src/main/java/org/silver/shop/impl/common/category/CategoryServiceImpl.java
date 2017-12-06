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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Row;
import com.justep.baas.data.Table;

@Service(interfaceClass = CategoryService.class)
public class CategoryServiceImpl implements CategoryService {
	private Logger logger = LoggerFactory.getLogger(getClass());
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
				String thirdId = String.valueOf(lr.get(i).getValue("thirdId"));
				String thirdName = String.valueOf(lr.get(i).getValue("goodsThirdTypeName"));
				// 增值税
				String vat = String.valueOf(lr.get(i).getValue("vat"));
				// 消费税
				String consumptionTax = String.valueOf(lr.get(i).getValue("consumptionTax"));
				// 综合税 跨境电商综合税率 = （消费税率+增值税率）/（1-消费税率）×70%
				String consolidatedTax = String.valueOf(lr.get(i).getValue("consolidatedTax"));
				if (firstMap != null && firstMap.get(firstId + "_" + firstName) != null) {
					if (secondMap != null && secondMap.get(secId + "_" + secName) != null) {
						if (StringEmptyUtils.isNotEmpty(thirdId) && StringEmptyUtils.isNotEmpty(thirdName)
								&& StringEmptyUtils.isNotEmpty(vat) && StringEmptyUtils.isNotEmpty(consumptionTax)
								&& StringEmptyUtils.isNotEmpty(consolidatedTax)) {
							thirdMap = new HashMap<>();
							thirdMap.put("thirdId", thirdId);
							thirdMap.put("thirdName", thirdName);
							thirdMap.put("vat", vat);
							thirdMap.put("consumptionTax", consumptionTax);
							thirdMap.put("consolidatedTax", consolidatedTax);
							List<Map<String, Object>> thirdList = new ArrayList<>();
							thirdList.add(thirdMap);
							firstMap.get(firstId + "_" + firstName).get(secId + "_" + secName).addAll(thirdList);
						}
					} else {
						if (StringEmptyUtils.isNotEmpty(thirdId) && StringEmptyUtils.isNotEmpty(thirdName)
								&& StringEmptyUtils.isNotEmpty(vat) && StringEmptyUtils.isNotEmpty(consumptionTax)
								&& StringEmptyUtils.isNotEmpty(consolidatedTax)) {
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
					}
				} else {
					secondMap = new HashMap<>();
					List<Map<String, Object>> thirdList = new ArrayList<>();

					thirdMap = new HashMap<>();
					thirdMap.put("thirdId", thirdId);
					thirdMap.put("thirdName", thirdName);
					thirdMap.put("vat", vat);
					thirdMap.put("consumptionTax", consumptionTax);
					thirdMap.put("consolidatedTax", consolidatedTax);
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
		// type 类型:1-第一商品类型,2-第二商品类型,3-第三商品类型
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
			return refreshCache();
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
			statusMap.put(BaseCode.MSG.toString(), "税费输入错误,请重试!");
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
		Iterator<String> isKey = paramMap.keySet().iterator();
		List<Map<String, Object>> errorList = new ArrayList<>();
		while (isKey.hasNext()) {
			String key = isKey.next();
			String value = String.valueOf(paramMap.get(key)).trim();
			switch (key.trim()) {
			case "firstTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					Map<String, Object> reAllCategoryMap = deleteAllCategory(value, errorList);
					if (!"1".equals(reAllCategoryMap.get(BaseCode.STATUS.toString()))) {
						return reAllCategoryMap;
					}
				}
				break;
			case "secondTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					Map<String, Object> reSecondCategoryMap = deleteSecondCategory(value, errorList);
					if (!"1".equals(reSecondCategoryMap.get(BaseCode.STATUS.toString()))) {
						return reSecondCategoryMap;
					}
				}
				break;
			case "thirdTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					Map<String, Object> reThirdCategoryMap = deleteThirdCategory(value, errorList);
					if (!"1".equals(reThirdCategoryMap.get(BaseCode.STATUS.toString()))) {
						return reThirdCategoryMap;
					}
				}
				break;
			default:
				break;
			}
		}
		return refreshCache();
	}

	// 删除第三类型
	private Map<String, Object> deleteThirdCategory(String value, List<Map<String, Object>> errorList) {
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> statusMap = new HashMap<>();
		params.put("goodsThirdTypeId", value);
		List<Object> reGoodsContentList = categoryDao.findByProperty(GoodsContent.class, params, 0, 0);
		params.clear();
		params.put("spareGoodsThirdTypeId", value);
		List<Object> reGoodsRecordDetailList = categoryDao.findByProperty(GoodsRecordDetail.class, params, 0, 0);
		params.clear();
		params.put("id", Long.parseLong(value));
		List<Object> reThirdTypeList = categoryDao.findByProperty(GoodsThirdType.class, params, 0, 0);
		if (reGoodsRecordDetailList == null || reGoodsContentList == null || reThirdTypeList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reGoodsRecordDetailList.isEmpty() || !reGoodsContentList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "该类型已关联商品,无法进行删除");
			return statusMap;
		} else {
			if (!reThirdTypeList.isEmpty()) {
				GoodsThirdType thirdTypeInfo = (GoodsThirdType) reThirdTypeList.get(0);
				if (!categoryDao.delete(thirdTypeInfo)) {
					Map<String, Object> errorMap = new HashMap<>();
					errorMap.put(BaseCode.MSG.toString(), thirdTypeInfo.getGoodsThirdTypeName() + "删除失败,请重试!");
					errorList.add(errorMap);
				}
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "该类型不存在,请重试!");
			return statusMap;
		}
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
		} else if (!reGoodsRecordDetailList.isEmpty() || !reGoodsContentList.isEmpty()) {
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
			if (reSecondTypeList != null) {
				GoodsSecondType secondTypeInfo = (GoodsSecondType) reSecondTypeList.get(0);
				// 删除标识：0-未删除,1-已删除
				secondTypeInfo.setDeleteFlag(1);
				if (!categoryDao.delete(secondTypeInfo)) {
					Map<String, Object> errorMap = new HashMap<>();
					errorMap.put(BaseCode.MSG.toString(), secondTypeInfo.getGoodsSecondTypeName() + "删除失败,请重试!");
					errorList.add(errorMap);
				}
				if (!reThirdTypeList.isEmpty()) {
					for (int t = 0; t < reThirdTypeList.size(); t++) {
						GoodsThirdType thirdTypeInfo = (GoodsThirdType) reThirdTypeList.get(0);
						thirdTypeInfo.setDeleteFlag(1);
						if (!categoryDao.delete(thirdTypeInfo)) {
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
	}

	/**
	 * 删除该商品大类型
	 * 
	 * @param value
	 *            Id
	 * @param errorList
	 * @return
	 */
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
		} else if (!reGoodsRecordDetailList.isEmpty() || !reGoodsContentList.isEmpty()) {
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
			} else if (!reFirstTypeList.isEmpty()) {
				if (!reSecondTypeList.isEmpty()) {
					for (int s = 0; s < reSecondTypeList.size(); s++) {
						GoodsSecondType secondTypeInfo = (GoodsSecondType) reSecondTypeList.get(s);
						if (!categoryDao.delete(secondTypeInfo)) {
							Map<String, Object> errorMap = new HashMap<>();
							errorMap.put(BaseCode.MSG.toString(),
									secondTypeInfo.getGoodsSecondTypeName() + "删除失败,请重试!");
							errorList.add(errorMap);
						}
					}
				}
				if (!reThirdTypeList.isEmpty()) {
					for (int t = 0; t < reThirdTypeList.size(); t++) {
						GoodsThirdType thirdTypeInfo = (GoodsThirdType) reThirdTypeList.get(0);
						if (!categoryDao.delete(thirdTypeInfo)) {
							Map<String, Object> errorMap = new HashMap<>();
							errorMap.put(BaseCode.MSG.toString(), thirdTypeInfo.getGoodsThirdTypeName() + "删除失败,请重试!");
							errorList.add(errorMap);
						}
					}
				}
				GoodsFirstType firstTypeInfo = (GoodsFirstType) reFirstTypeList.get(0);
				if (!categoryDao.delete(firstTypeInfo)) {
					Map<String, Object> errorMap = new HashMap<>();
					errorMap.put(BaseCode.MSG.toString(), firstTypeInfo.getFirstTypeName() + "删除失败,请重试");
					errorList.add(errorMap);
				}
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			return statusMap;
		}

	}

	@Override
	public Map<String, Object> editGoodsCategory(String managerId, String managerName, Map<String, Object> paramMap) {
		Map<String, Object> statusMap = new HashMap<>();
		List<Map<String, Object>> errorList = new ArrayList<>();
		String type = paramMap.get("type") + "";
		switch (type.trim()) {
		case "1":
			Map<String, Object> reAllCategoryMap = editFirstCategory(paramMap, errorList, managerId, managerName);
			if (!"1".equals(reAllCategoryMap.get(BaseCode.STATUS.toString()))) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
				return statusMap;
			}
			break;
		case "2":
			Map<String, Object> reSecondCategoryMap = editSecondCategory(paramMap, errorList, managerId, managerName);
			if (!"1".equals(reSecondCategoryMap.get(BaseCode.STATUS.toString()))) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
				return statusMap;
			}
			break;
		case "3":
			Map<String, Object> reThirdCategoryMap = editThirdCategory(paramMap, errorList, managerId, managerName);
			if (!"1".equals(reThirdCategoryMap.get(BaseCode.STATUS.toString()))) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
				return statusMap;
			}
			break;
		default:
			break;
		}

		return null;
	}

	/**
	 * 修改第三类型名称
	 * 
	 * @param paramMap
	 * @param errorList
	 * @param managerId
	 * @param managerName
	 * @return
	 */
	private Map<String, Object> editThirdCategory(Map<String, Object> paramMap, List<Map<String, Object>> errorList,
			String managerId, String managerName) {
		Date date = new Date();
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> statusMap = new HashMap<>();
		long goodsThirdTypeId = Long.parseLong(paramMap.get("goodsThirdTypeId") + "");
		// 根据Id查询第一商品类型
		params.put("id", goodsThirdTypeId);
		List<Object> goodsThirdList = categoryDao.findByProperty(GoodsThirdType.class, params, 1, 1);
		params.clear();
		// 根据Id查询商品基本信息表中是否有商品第一类型Id存在
		params.put("goodsThirdTypeId", goodsThirdTypeId);
		List<Object> reGoodsContentList = categoryDao.findByProperty(GoodsContent.class, params, 0, 0);
		params.clear();
		// 根据Id查询商品备案信息表中管理的商品第一类型Id
		params.put("spareGoodsThirdTypeId", goodsThirdTypeId);
		List<Object> reGoodsRecordDetailList = categoryDao.findByProperty(GoodsRecordDetail.class, params, 0, 0);
		if (reGoodsRecordDetailList == null || reGoodsContentList == null || goodsThirdList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!goodsThirdList.isEmpty()) {
			String goodsThirdTypeName = paramMap.get("goodsThirdTypeName") + "";
			GoodsThirdType goodsThirdType = (GoodsThirdType) goodsThirdList.get(0);
			double vat = 0.0;
			double consumptionTax = 0.0;
			double consolidatedTax = 0.0;
			double tariff = 0.0;
			try {
				vat = Double.parseDouble(paramMap.get("vat") + "");
				consumptionTax = Double.parseDouble(paramMap.get("consumptionTax") + "");
				consolidatedTax = Double.parseDouble(paramMap.get("consolidatedTax") + "");
				tariff = Double.parseDouble(paramMap.get("tariff") + "");
			} catch (Exception e) {
				logger.error("-------------");
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "税费格式错误,请重新输入");
				return statusMap;
			}
			goodsThirdType.setGoodsThirdTypeName(goodsThirdTypeName);
			goodsThirdType.setVat(vat);
			goodsThirdType.setConsumptionTax(consumptionTax);
			goodsThirdType.setConsolidatedTax(consolidatedTax);
			goodsThirdType.setTariff(tariff);
			goodsThirdType.setUpdateBy(managerName);
			goodsThirdType.setUpdateDate(date);
			if (!categoryDao.update(goodsThirdType)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "修改商品第三类型错误,请重试！");
				return statusMap;
			}
			for (int i = 0; i < reGoodsContentList.size(); i++) {
				GoodsContent goodsBaseInfo = (GoodsContent) reGoodsContentList.get(i);
				goodsBaseInfo.setGoodsThirdTypeName(goodsThirdTypeName);
				if (!categoryDao.update(goodsBaseInfo)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "修改商品第三类型错误,请重试！");
					return statusMap;
				}
			}
			for (int i = 0; i < reGoodsContentList.size(); i++) {
				GoodsRecordDetail goodsRecordInfo = (GoodsRecordDetail) reGoodsContentList.get(i);
				goodsRecordInfo.setSpareGoodsThirdTypeName(goodsThirdTypeName);
				if (!categoryDao.update(goodsRecordInfo)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "修改商品第三类型错误,请重试！");
					return statusMap;
				}
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	/**
	 * 修改第二类型商品
	 * 
	 * @param paramMap
	 * @param errorList
	 * @param managerId
	 * @param managerName
	 * @return
	 */
	private Map<String, Object> editSecondCategory(Map<String, Object> paramMap, List<Map<String, Object>> errorList,
			String managerId, String managerName) {
		Date date = new Date();
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> statusMap = new HashMap<>();
		long goodsSecondTypeId = Long.parseLong(paramMap.get("goodsSecondTypeId") + "");
		// 根据Id查询第一商品类型
		params.put("id", goodsSecondTypeId);
		List<Object> goodsSecondList = categoryDao.findByProperty(GoodsSecondType.class, params, 1, 1);
		params.clear();
		// 根据Id查询商品基本信息表中是否有商品第一类型Id存在
		params.put("goodsSecondTypeId", goodsSecondTypeId);
		List<Object> reGoodsContentList = categoryDao.findByProperty(GoodsContent.class, params, 0, 0);
		params.clear();
		// 根据Id查询商品备案信息表中管理的商品第一类型Id
		params.put("spareGoodsSecondTypeId", goodsSecondTypeId);
		List<Object> reGoodsRecordDetailList = categoryDao.findByProperty(GoodsRecordDetail.class, params, 0, 0);
		if (reGoodsRecordDetailList == null || reGoodsContentList == null || goodsSecondList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!goodsSecondList.isEmpty()) {
			String goodsSecondTypeName = paramMap.get("goodsSecondTypeName") + "";
			GoodsSecondType goodsSecondType = (GoodsSecondType) goodsSecondList.get(0);
			goodsSecondType.setGoodsSecondTypeName(goodsSecondTypeName);
			goodsSecondType.setUpdateBy(managerName);
			goodsSecondType.setUpdateDate(date);
			if (!categoryDao.update(goodsSecondType)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "修改商品第二类型错误,请重试！");
				return statusMap;
			}
			for (int i = 0; i < reGoodsContentList.size(); i++) {
				GoodsContent goodsBaseInfo = (GoodsContent) reGoodsContentList.get(i);
				goodsBaseInfo.setGoodsSecondTypeName(goodsSecondTypeName);
				if (!categoryDao.update(goodsBaseInfo)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "修改商品第二类型错误,请重试！");
					return statusMap;
				}
			}
			for (int i = 0; i < reGoodsContentList.size(); i++) {
				GoodsRecordDetail goodsRecordInfo = (GoodsRecordDetail) reGoodsContentList.get(i);
				goodsRecordInfo.setSpareGoodsSecondTypeName(goodsSecondTypeName);
				if (!categoryDao.update(goodsRecordInfo)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "修改商品第二类型错误,请重试！");
					return statusMap;
				}
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	/**
	 * 修改商品第一类型
	 * 
	 * @param paramMap
	 * @param errorList
	 * @return
	 */
	private Map<String, Object> editFirstCategory(Map<String, Object> paramMap, List<Map<String, Object>> errorList,
			String managerId, String managerName) {
		Date date = new Date();
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> statusMap = new HashMap<>();
		long goodsFirstTypeId = Long.parseLong(paramMap.get("goodsFirstTypeId") + "");
		// 根据Id查询第一商品类型
		params.put("id", goodsFirstTypeId);
		List<Object> goodsFirstList = categoryDao.findByProperty(GoodsFirstType.class, params, 1, 1);
		params.clear();
		// 根据Id查询商品基本信息表中是否有商品第一类型Id存在
		params.put("goodsFirstTypeId", goodsFirstTypeId);
		List<Object> reGoodsContentList = categoryDao.findByProperty(GoodsContent.class, params, 0, 0);
		params.clear();
		// 根据Id查询商品备案信息表中管理的商品第一类型Id
		params.put("spareGoodsFirstTypeId", goodsFirstTypeId);
		List<Object> reGoodsRecordDetailList = categoryDao.findByProperty(GoodsRecordDetail.class, params, 0, 0);
		if (reGoodsRecordDetailList == null || reGoodsContentList == null || goodsFirstList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!goodsFirstList.isEmpty()) {
			String goodsFirstTypeName = paramMap.get("goodsFirstTypeName") + "";
			GoodsFirstType goodsFirstType = (GoodsFirstType) goodsFirstList.get(0);
			goodsFirstType.setFirstTypeName(goodsFirstTypeName);
			goodsFirstType.setUpdateBy(managerName);
			goodsFirstType.setUpdateDate(date);
			if (!categoryDao.update(goodsFirstType)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "修改商品第一类型错误,请重试！");
				return statusMap;
			}
			for (int i = 0; i < reGoodsContentList.size(); i++) {
				GoodsContent goodsBaseInfo = (GoodsContent) reGoodsContentList.get(i);
				goodsBaseInfo.setGoodsFirstTypeName(goodsFirstTypeName);
				if (!categoryDao.update(goodsBaseInfo)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "修改商品第一类型错误,请重试！");
					return statusMap;
				}
			}
			for (int i = 0; i < reGoodsContentList.size(); i++) {
				GoodsRecordDetail goodsRecordInfo = (GoodsRecordDetail) reGoodsContentList.get(i);
				goodsRecordInfo.setSpareGoodsFirstTypeName(goodsFirstTypeName);
				if (!categoryDao.update(goodsRecordInfo)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "修改商品第一类型错误,请重试！");
					return statusMap;
				}
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());

			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	/**
	 * 查询最新的商品类型,重新放入至缓存中
	 * 
	 * @return
	 */
	private final Map<String, Object> refreshCache() {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> datasMap = findGoodsType();
		String status = datasMap.get(BaseCode.STATUS.toString()) + "";
		if ("1".equals(status)) {
			datasMap = (Map) datasMap.get(BaseCode.DATAS.getBaseCode());
			// 将已查询出来的商品类型存入redis,有效期为1小时
			System.out.println("-重新放入缓存-------------------");
			JedisUtil.setListDatas("Shop_Key_GoodsCategory_Map", 3600, datasMap);
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> getCategoryInfo(int type, String id) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		Class entity = null;
		switch (type) {
		case 1:
			entity = GoodsFirstType.class;
			break;
		case 2:
			entity = GoodsSecondType.class;
			break;
		case 3:
			entity = GoodsThirdType.class;
			break;
		default:
			break;
		}
		paramMap.put("id", Long.parseLong(id));
		List<Object> reList= categoryDao.findByProperty(entity, paramMap, 0, 0);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> searchCategoryInfo(int type) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		Class entity = null;
		switch (type) {
		case 1:
			entity = GoodsFirstType.class;
			break;
		case 2:
			entity = GoodsSecondType.class;
			break;
		case 3:
			entity = GoodsThirdType.class;
			break;
		default:
			break;
		}
		List<Object> reList= categoryDao.findByProperty(entity, paramMap, 0, 0);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}
}
