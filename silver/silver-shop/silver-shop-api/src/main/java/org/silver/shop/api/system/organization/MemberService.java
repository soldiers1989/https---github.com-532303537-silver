package org.silver.shop.api.system.organization;

import java.util.List;
import java.util.Map;

import org.silver.shop.model.system.manual.Morder;
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
	 * 根据账号名称、手机号码、身份证号码，查询用户
	 * 
	 * @param account  账号名称
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
	 * 根据用户id，获取用户信息
	 * 
	 * @param memberId 用户Id
	 * @return Map datas-用户信息实体
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
	 * 用户身份证-实名认证
	 * @param memberInfo 用户信息
	 * @return Map
	 */
	public Map<String, Object> realName(Member memberInfo);

	/**
	 * 根据用户信息，修改会员登陆密码
	 * @param memberInfo 用户信息
	 * @param loginPassword 登录密码
	 * @return 
	 */
	public Map<String,Object> updateLoginPassword(Member memberInfo,  String loginPassword);

	/**
	 * 修改用户信息
	 * @param datasMap 参数
	 * @return Map
	 */
	public Map<String,Object> editInfo(Map<String, Object> datasMap);
	
	/**
	 * 检查身份证号码是否已注册
	 * 
	 * @param idcard
	 * @return
	 */
	public Map<String, Object> checkIdCard(String idcard);
	
	/**
	 * 根据(手工)订单信息注册会员信息
	 * 
	 * @param Morder
	 *            手工订单实体信息
	 * @return Map
	 */
	public Map<String, Object> registerMember(Morder order);

	/**
	 * 设置用户支付密码
	 * @param memberInfo 用户实体信息
	 * @param paymentPassword 
	 * @return Map
	 */
	public Map<String, Object> setPaymentPassword(Member memberInfo, String paymentPassword);

	/**
	 * 根据用户名称，查询对应的用户信息
	 * @param accountName 用户名称/手机号码
	 * @return Map
	 */
	public Map<String, Object> retrieveLoginPassword(String accountName);

	/**
	 * 更新用户手机号码
	 * @param memberId 用户id
	 * @param phone 手机号码
	 * @return Map
	 */
	public Map<String, Object> updatePhone(String memberId, String phone);
	
	/**
	 * 更新用户信息
	 * 
	 * @param entity
	 *            用户信息实体类
	 * @return Map
	 */
	public Map<String, Object> updateMemberInfo(Member entity);

	/**
	 * 更新用户支付密码
	 * @param memberId 用户id
	 * @param newPayPassword 新支付密码
	 * @param oldPayPassword 旧支付密码
	 * @return Map
	 */
	public Map<String, Object> updatePayPwd(String memberId, String newPayPassword, String oldPayPassword);

	/**
	 * 查询用户信息
	 * @param params 查询参数
	 * @param page 页数
	 * @param size 数目
	 * @return Map 
	 */
	public Map<String, Object> getInfo(Map<String, Object> params, int page, int size);

	/**
	 * 设置支付密码
	 * @param newPayPassword 新支付密码
	 * @return
	 */
	public Map<String, Object> updatePayPwd(String newPayPassword);

}
