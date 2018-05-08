package org.silver.shop.api.system.organization;

import java.util.List;
import java.util.Map;

import org.silver.shop.model.system.organization.Member;

import net.sf.json.JSONArray;

public interface MemberService {

	/**
	 * 用户注册
	 * 
	 * @param account 用户名称
	 * @param loginPass 登陆密码
	 * @param memberIdCardName 姓名
	 * @param memberIdCard 身份证号码
	 * @param memberId 用户Id
	 * @return Map
	 */
	public Map<String, Object> memberRegister(String account, String loginPass, String memberIdCardName,
			String memberIdCard, String memberId, String memberTel);

	/**
	 * 根据用户名查询数据
	 * 
	 * @param account  用户名
	 * @return
	 */
	public List<Member> findMemberBy(String account);

	/**
	 * 创建用户ID
	 * 
	 * @return Map
	 */
	public Map<String, Object> createMemberId();

	/**
	 * 查询用户基本信息
	 * 
	 * @param memberId 用户Id
	 * @return Map
	 */
	public Map<String, Object> getMemberInfo(String memberId);

	/**
	 * 用户确认订单时设置购物车标识
	 * 
	 * @param goodsId
	 * @param memberId
	 * @param memberName
	 * @param flag
	 *            用户选中标识1-为选择,2-已选择
	 * @return
	 */
	public Map<String, Object> editShopCartGoodsFlag(String goodsInfoPack, String memberId, String memberName);

	/**
	 * 获取用户钱包信息
	 * 
	 * @param memberId 用户Id
	 * @param memberName 用户名称
	 * @return Map
	 */
	public Map<String, Object> getMemberWalletInfo(String memberId, String memberName);

	/**
	 * 检查用户注册信息是否已存在
	 * 
	 * @param datas
	 *            参数
	 * @param type
	 *            类型：memberName(用户名称),memberTel(手机号码),memberIdCard(身份证号)
	 * @return
	 */
	public Map<String, Object> checkRegisterInfo(String datas, String type);

	/**
	 * 管理员根据已备案成功的订单Id批量注册会员
	 * 
	 * @param josnArr
	 *            订单Id集合
	 * @return Map
	 */
	public Map<String, Object> batchRegisterMember(JSONArray josnArr);

	/**
	 * 会员实名认证
	 * @param memberId 会员Id
	 * @return Map
	 */
	public Map<String, Object> realName(String memberId);

	/**
	 * 根据用户Id，修改密码
	 * @param memberId 用户Id
	 * @param newPassword 新密码
	 * @param oldPassword 原密码
	 * @return 
	 */
	public Object editPassword(String memberId, String oldPassword, String newPassword);

	/**
	 * 修改用户信息
	 * @param datasMap 参数
	 * @return Map
	 */
	public Map<String,Object> editInfo(Map<String, Object> datasMap);
}
