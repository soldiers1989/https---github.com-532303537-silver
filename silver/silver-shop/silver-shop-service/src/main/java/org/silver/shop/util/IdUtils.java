package org.silver.shop.util;

import java.util.Map;

import org.silver.shop.dao.BaseDao;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *	商城Id工具类 
 */
@Component
public class IdUtils<E> {

	private static final Object  LOCK = new Object();

	@Autowired
	private BaseDao<E> baseDao;

	/**
	 * 根据类名查询当前数据库表中最后一行自增Id值,然后根据自定义抬头字符串生成流水Id
	 * @param calss 实体类
	 * @param topStr 自定义抬头
	 * @return Map
	 */
	public Map<String, Object> createId(Class<E>calss, String topStr) {
		synchronized (LOCK) {
			//
			long idCount = baseDao.findLastId(calss);
			// 当返回-1时,则查询数据库失败
			if (idCount < 0) {
				return ReturnInfoUtils.errorInfo("查询自增Id失败,服务器繁忙！");
			}
			// 得出的总数上+1
			long count = idCount + 1;
			String id = String.valueOf(count);
			// 当商户ID没有5位数时,前面补0
			while (id.length() < 5) {
				id = "0" + id;
			}
			// 生成返回ID
			String resultId = topStr + id;
			return ReturnInfoUtils.successDataInfo(resultId);
		}
	}
}
