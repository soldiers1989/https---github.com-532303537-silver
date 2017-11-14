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
	 * 用户确认订单时设置购物车标识
	 * @param goodsId
	 * @param memberId
	 * @param memberName
	 * @param flag  用户选中标识1-为选择,2-已选择
	 * @return
	 */
	public Map<String, Object> editShopCartGoodsFlag(String goodsInfoPack, String memberId, String memberName);


	/**
	 * 获取用户钱包信息
	 * @param memberId
	 * @param memberName
	 * @return
	 */
	public Map<String, Object> getMemberWalletInfo(String memberId, String memberName);

	/**
	 * 检查用户名称是否存在
	 * @param account
	 * @return
	 */
	public Map<String, Object> checkMerchantName(String account);
}
