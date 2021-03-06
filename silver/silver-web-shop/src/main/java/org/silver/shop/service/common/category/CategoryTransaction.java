package org.silver.shop.service.common.category;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.RedisKey;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.category.CategoryService;
import org.silver.shop.model.system.organization.Manager;
import org.silver.util.JedisUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONObject;

/**
 * 商品类型Transaction(事物)处理层
 */
@Service("categoryTransaction")
public class CategoryTransaction {

	@Reference
	private CategoryService categoryService;

	/**
	 * 查询所有商品类型,并进行对应的级联封装到Map
	 * 
	 * @return Map
	 */
	public Map<String, Object> findAllCategory() {
		String redisList = JedisUtil.get(RedisKey.SHOP_KEY_GOODS_CATEGORY_MAP);
		if (StringEmptyUtils.isEmpty(redisList)) {// redis缓存没有数据
			Map<String, Object> datasMap = categoryService.findGoodsType();
			String status = datasMap.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {
				// 将已查询出来的商品类型存入redis,有效期为1小时
				JedisUtil.setListDatas(RedisKey.SHOP_KEY_GOODS_CATEGORY_MAP, 3600, datasMap.get(BaseCode.DATAS.getBaseCode()));
			}
			return datasMap;
		} else {// redis缓存中已有数据,直接返回数据
			return ReturnInfoUtils.successDataInfo(JSONObject.fromObject(redisList));
		}
	}

	// 添加商品类型
	public Map<String, Object> addGoodsCategory(HttpServletRequest req) {
		Map<String, Object> paramMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			paramMap.put(key, value);
		}
		return categoryService.addGoodsCategory(managerId, managerName, paramMap);

	}

	// 删除商品类型
	public Map<String, Object> deleteGoodsCategory(HttpServletRequest req) {
		Map<String, Object> paramMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			paramMap.put(key, value);
		}
		return categoryService.deleteGoodsCategory(managerId, managerName, paramMap);
	}

	// 修改商品类型
	public Map<String, Object> editGoodsCategory(HttpServletRequest req) {
		Map<String, Object> paramMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			paramMap.put(key, value);
		}
		if(paramMap.isEmpty()){
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		return categoryService.editGoodsCategory(managerId, managerName, paramMap);
	}

	// 获取商品类型详情
	public Map<String, Object> getCategoryInfo(int type, String id) {
		return categoryService.getCategoryInfo(type, id);
	}

	// 根据等级查询商品类型
	public Map<String, Object> searchCategoryInfo(int type) {
		return categoryService.searchCategoryInfo(type);
	}
}
