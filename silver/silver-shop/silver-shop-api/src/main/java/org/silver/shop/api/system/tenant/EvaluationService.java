package org.silver.shop.api.system.tenant;

import java.util.List;
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

	/**
	 * 用户根据商品Id进行商品评价
	 * @param goodsId 商品Id
	 * @param content 评价内容
	 * @param level 评分
	 * @param memberId 用户Id
	 * @param memberName 用户名称
	 * @param ipAddress Ip地址
	 * @return Map
	 */
	public Map<String, Object> addEvaluation(String entOrderNo,String goodsInfoPack,String memberId,String memberName, String ipAddress);

	/**
	 * 商户获取商品评价信息
	 * @param goodsName 商品名称
	 * @param memberName 用户名称
	 * @return Map
	 */
	public Map<String, Object> merchantGetInfo(String goodsName, String memberName,String merchantId);

	/**
	 * 管理员删除商品评论
	 * @param idList 评论流水Id集合
	 * @param managerName 
	 * @param managerId 
	 * @return Map
	 */
	public Map<String,Object> managerDeleteInfo(List<String> idList, String managerId, String managerName);


}
