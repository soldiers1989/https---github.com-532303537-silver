package org.silver.shop.api.system.organization;

import java.util.List;
import java.util.Map;

public interface MemberService {

	/**
	 * 用户注册
	 * 
	 * @param account
	 * @param loginPass
	 * @param memberIdCardName
	 * @param memberIdCard
	 * @param memberId
	 * @return
	 */
	public Map<String, Object> memberRegister(String account, String loginPass, String memberIdCardName,
			String memberIdCard, String memberId);

	/**
	 * 根据用户名查询商户数据
	 * 
	 * @param account
	 * @return
	 */
	public List<Object> findMemberBy(String account);

	/**
	 * 创建用户ID
	 * 
	 * @return Map
	 */
	public Map<String, Object> createMemberId();

	/**
	 * 查询商户基本信息
	 * 
	 * @param memberId
	 * @param memberName
	 * @return
	 */
	public Map<String, Object> getMemberInfo(String memberId, String memberName);

	/**
	 * 用户添加商品至购物车
	 * @param memberId
	 * @param memberName
	 * @param goodsId
	 * @param count
	 */
	public Map<String,Object> addGoodsToShopCart(String memberId, String memberName, String goodsId, int count);
}
