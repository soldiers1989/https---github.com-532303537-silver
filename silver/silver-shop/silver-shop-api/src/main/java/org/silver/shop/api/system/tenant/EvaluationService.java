package org.silver.shop.api.system.tenant;

import java.util.Map;

public interface EvaluationService {

	/**
	 * 商城前台获取商品评价信息
	 * 
	 * @param goodsId
	 *            商品Id
	 * @param size
	 *            数目
	 * @param page
	 *            页数
	 * @return Map
	 */
	public Map<String, Object> getInfo(String goodsId, int page, int size);

	/**
	 * 添加评论
	 * 
	 * @param goodsId
	 *            商品自编号
	 * @param content
	 *            内容
	 * @param level
	 *            评分数
	 * @param memberName
	 *            用户名称
	 * @param memberId
	 *            用户Id
	 * @return Map
	 */
	public Map<String, Object> addInfo(String goodsId, String content, double level, String memberId,
			String memberName);

	/**
	 * 
	 * @return
	 */
	public Map<String, Object> randomMember();

}
