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
			String memberIdCard, String memberId,String memberTel);

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
	 * 
	 * @param memberId 用户ID
	 * @param memberName 用户名称
	 * @param goodsId 商品名称
	 * @param count 数量
	 */
	public Map<String, Object> addGoodsToShopCart(String memberId, String memberName, String goodsId, int count);

	/**
	 * 用户查询购物车
	 * @param memberId 用户ID
	 * @param memberName 用户名称
	 * @return
	 */
	public Map<String, Object> getGoodsToShopCartInfo(String memberId, String memberName);

	/**
	 * 删除信息
	 * @param goodsId
	 * @param memberId
	 * @param memberName
	 * @return
	 */
	public Map<String,Object> deleteShopCartGoodsInfo(String goodsId, String memberId, String memberName);

	/**
	 * 用户确认订单时设置购物车标识
	 * @param goodsId
	 * @param memberId
	 * @param memberName
	 * @param flag  用户选中标识1-为选择,2-已选择
	 * @return
	 */
	public Map<String, Object> editShopCartGoodsFlag(String goodsInfoPack, String memberId, String memberName);

	/**
	 * 用户查询订单信息
	 * @param memberId
	 * @param memberName
	 * @return
	 */
	public Map<String, Object> getMemberOrderInfo(String memberId, String memberName,int page,int size);

	public Map<String, Object> getMemberWalletInfo(String memberId, String memberName);
}
