package org.silver.shop.service.system.organization;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.model.system.organization.Member;
import org.silver.util.IdcardValidator;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;
import org.silver.util.RedisUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;

/**
 * 用户事物层
 */
@Service("memberTransaction")
public class MemberTransaction {

	@Autowired
	private MemberService memberService;

	/**
	 * 用户登录密码输入错误缓存计数KEY
	 */
	private static final String SHOP_LOGIN_MEMBER_ERROR_COUNT_INT = "SHOP_LOGIN_MEMBER_ERROR_COUNT_INT";

	// 用户注册
	public Map<String, Object> memberRegister(String account, String loginPass, String memberIdCardName,
			String memberIdCard, String memberTel, int verificationCode) {
		if (!StringUtil.isContainChinese(memberIdCardName)) {
			return ReturnInfoUtils.errorInfo("姓名输入错误,请重新输入!");
		}
		if (!IdcardValidator.validate18Idcard(memberIdCard)) {
			return ReturnInfoUtils.errorInfo("身份证号输入错误,请重新输入!");
		}
		if (loginPass.length() < 6 || loginPass.length() > 20) {
			return ReturnInfoUtils.errorInfo("密码长度只能在6-20个字符之间!");
		}
		// 密码的组成至少要包括大小写字母、数字及标点符号的其中两项
		String regex = "^(?![A-Za-z]+$)(?!\\d+$)(?![\\W_]+$)\\S{6,20}$";
		if (!loginPass.matches(regex)) {
			return ReturnInfoUtils.errorInfo("密码至少要由包括大小写字母、数字、特殊符号的其中两项!");
		}
		// 获取缓存中用户注册手机验证码
		String redis = JedisUtil.get("SHOP_KEY_MEMBER_REGISTER_CODE_" + memberTel);
		if (StringEmptyUtils.isNotEmpty(redis)) {
			JSONObject json = JSONObject.fromObject(redis);
			long time = Long.parseLong(json.get("time") + "");
			int code = Integer.parseInt(json.get("code") + "");
			if ((time - new Date().getTime()) < 9000 && code == verificationCode) {
				Map<String, Object> datasMap = memberService.createMemberId();
				if (!"1".equals(datasMap.get(BaseCode.STATUS.toString()))) {
					return datasMap;
				}
				String memberId = datasMap.get(BaseCode.DATAS.toString()) + "";
				return memberService.memberRegister(account, loginPass, memberIdCardName, memberIdCard, memberId,
						memberTel);
			}
		}
		return ReturnInfoUtils.errorInfo("验证码错误,请重新输入!");
	}

	// 用户登录
	public Map<String, Object> memberLogin(String account, String loginPassword) {
		if (StringEmptyUtils.isEmpty(account) || StringEmptyUtils.isEmpty(loginPassword)) {
			return ReturnInfoUtils.errorInfo("你输入的密码和账户名不匹配!");
		}
		String[] strArr = account.split("_");
		account = strArr[0];
		String ipAddress = strArr[1];
		MD5 md5 = new MD5();
		List<Member> reList = memberService.findMemberBy(account);
		if (reList != null && !reList.isEmpty()) {
			Member member = reList.get(0);
			String loginpas = member.getLoginPass();
			String md5Pas = md5.getMD5ofStr(loginPassword);
			// 判断查询出的账号密码与前台登录的账号密码是否一致
			if (md5Pas.equals(loginpas)) {
				String redis = JedisUtil
						.get(SHOP_LOGIN_MEMBER_ERROR_COUNT_INT + member.getMemberId() + "_" + ipAddress);
				if (StringEmptyUtils.isNotEmpty(redis) && Integer.parseInt(redis) >= 3) {
					return getRedisInfo(member.getMemberId(), ipAddress);
				}
				// 获取用户登录时,shiro存入在session中的数据
				Subject currentUser = SecurityUtils.getSubject();
				Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
				if (memberInfo == null) {
					WebUtil.getSession().setAttribute(LoginType.MEMBER_INFO.toString(), member);
				}
				return ReturnInfoUtils.successInfo();
			} else {
				return getRedisInfo(member.getMemberId(), ipAddress);
			}
		}
		return ReturnInfoUtils.errorInfo("你输入的密码和账户名不匹配!");
	}

	/**
	 * 根据用户Id获取缓存中用户输入错误密码次数
	 * 
	 * @param memberId
	 *            用户Id
	 * @param ipAddress
	 *            登陆账号的Ip地址
	 * @return
	 */
	private Map<String, Object> getRedisInfo(String memberId, String ipAddress) {
		int count = 1;
		String redis = JedisUtil.get(SHOP_LOGIN_MEMBER_ERROR_COUNT_INT + memberId + "_" + ipAddress);
		if (StringEmptyUtils.isNotEmpty(redis)) {
			count = Integer.parseInt(redis);
			// 当密码输入错误次数超过3次(含3次)时,直接返回
			if (count >= 3) {
				return ReturnInfoUtils.errorInfo("账号已被锁定", memberId);
			} else {
				count++;
				JedisUtil.set(SHOP_LOGIN_MEMBER_ERROR_COUNT_INT + memberId + "_" + ipAddress, 900, count);
				return ReturnInfoUtils.errorInfo("密码错误", memberId);
			}
		} else {// 缓存中没有数据,重新访问数据库读取数据
			JedisUtil.set(SHOP_LOGIN_MEMBER_ERROR_COUNT_INT + memberId + "_" + ipAddress, 900, count);
			return ReturnInfoUtils.errorInfo("密码错误", memberId);
		}

	}

	public Map<String, Object> getMemberInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		String memberId = memberInfo.getMemberId();
		// String memberName = memberInfo.getMemberName();
		return memberService.getMemberInfo(memberId);
	}

	public Map<String, Object> editShopCartGoodsFlag(String goodsInfoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return memberService.editShopCartGoodsFlag(goodsInfoPack, memberId, memberName);
	}

	public Map<String, Object> getMemberWalletInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return memberService.getMemberWalletInfo(memberId, memberName);
	}

	public Map<String, Object> checkRegisterInfo(String datas, String type) {
		return memberService.checkRegisterInfo(datas, type);
	}

	// 管理员批量注册会员
	public Object batchRegisterMember(JSONArray jsonArr) {
		return memberService.batchRegisterMember(jsonArr);
	}

	// 会员实名认证
	public Map<String, Object> realName(String memberId) {
		return memberService.realName(memberId);
	}

	// 会员修改登陆密码
	public Object updateLoginPassword(String memberId, String newPassword) {
		return memberService.updateLoginPassword(memberId, newPassword);
	}

	// 用户修改信息
	public Object editInfo(Map<String, Object> datasMap) {
		return memberService.editInfo(datasMap);
	}

	public static void main(String[] args) {
		Jedis j = new Jedis("150.242.58.22", 6380);
		j.auth("jugg");
		j.del("Shop_Login_MemberPasswordError_int_1Member_2017000025928");
		System.out.println("----------------");
	}
}
