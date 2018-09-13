package org.silver.shop.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDao;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 统一查询工具
 * @param <T>
 */
@Service
public class InquireHelperService<T> {
	
	@Autowired
	private BaseDao<T> baseDao;
	
	
	/**
	 * 
	 * <li>注：当page=1、size=1 时则直接传回实体信息<li>
	 * @param entity 实体类
	 * @param datasMap
	 * 		<li>查询参数中的键必须与数据库中的键名一致</li>
	 * @param page 页数
	 * @param size 数目
	 * @return Map
	 */
	public Map<String, Object> findInfo(Class<T> entity,Map<String, Object> datasMap, int page, int size) {
		if(StringEmptyUtils.isNotEmpty(datasMap.get("startDate"))){
			Date startDate= DateUtil.parseDate(datasMap.get("startDate")+"", "yyyy-MM-dd HH:mm:ss");
			if(startDate == null){
				return ReturnInfoUtils.errorInfo("日期格式错误！");
			}
			datasMap.put("startDate", startDate);
		}
		if(StringEmptyUtils.isNotEmpty(datasMap.get("endDate"))){
			Date endDate= DateUtil.parseDate(datasMap.get("endDate")+"", "yyyy-MM-dd HH:mm:ss");
			if(endDate == null){
				return ReturnInfoUtils.errorInfo("日期格式错误！");
			}
			datasMap.put("endDate", endDate);
		}
		Map<String,Object> blurryMap = new HashMap<>();
		//校验前台参数是否带模糊查询字样
		for(Map.Entry<String, Object> entry : datasMap.entrySet()){
			if(entry.getKey().contains("blurry")){
				blurryMap.put(entry.getKey().replace("blurry", ""), entry.getValue());
			}
		}
		List<T> reList = baseDao.find(entity, datasMap, blurryMap, page, size);
		Long count = baseDao.findCount(entity, datasMap, blurryMap);
		return uniteResult(reList,count,page,size);
	}
	
	/**
	 * 统一处理查询结果返回
	 * @param reList 查询结果集合
	 * @param count 数量
	 * @param page 页数
 	 * @param size 数目
	 * @return Map
	 */
	private Map<String, Object> uniteResult(List reList, Long count, int page, int size) {
		if(reList == null){
			return ReturnInfoUtils.warnInfo();
		}else if(reList.isEmpty()){
			return ReturnInfoUtils.noDatas();
		}else{
			if(page == 1 && size == 1){
				return ReturnInfoUtils.successDataInfo(reList.get(0));
			}else{
				return ReturnInfoUtils.successDataInfo(reList,count);
			}
		}
	}
	

	
	
}
