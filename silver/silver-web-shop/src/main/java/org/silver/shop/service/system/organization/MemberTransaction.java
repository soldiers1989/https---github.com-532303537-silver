package org.silver.shop.service.system.organization;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.model.system.organization.Member;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;
import org.silver.util.StringEmptyUtils;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

/**
 * 用户事物层
 */
@Service("memberTransaction")
public class MemberTransaction {

	@Autowired
	private MemberService memberService;

	// 用户注册
	public Map<String, Object> memberRegister(String account, String loginPass, String memberIdCardName,
			String memberIdCard, String memberTel, int verificationCode) {
		Map<String, Object> datasMap = memberService.createMemberId();
		if (!datasMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return datasMap;
		}
		String memberId = datasMap.get(BaseCode.DATAS.toString()) + "";
		String redis = JedisUtil.get("Shop_Key_MemberRegisterCode_" + memberTel);
		if (StringEmptyUtils.isNotEmpty(redis)) {
			JSONObject json = JSONObject.fromObject(redis);
			long time = Long.parseLong(json.get("time")+"");
			if (time - new Date().getTime() < 9000) {
				return memberService.memberRegister(account, loginPass, memberIdCardName, memberIdCard, memberId,
						memberTel);
			}
		}
		datasMap.clear();
		datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
		datasMap.put(BaseCode.MSG.getBaseCode(), "手机验证码错误,请重试!");
		return datasMap;
	}

	// 用户登录
	public Map<String, Object> memberLogin(String account, String loginPassword) {
		MD5 md5 = new MD5();
		Map<String, Object> datasMap = new HashMap<>();
		List<Object> reList = memberService.findMemberBy(account);
		if (reList == null) {
			datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return datasMap;
		} else if (reList.size() > 0) {
			Member member = (Member) reList.get(0);
			String name = member.getMemberName();
			String loginpas = member.getLoginPass();
			String md5Pas = md5.getMD5ofStr(loginPassword);
			// 判断查询出的账号密码与前台登录的账号密码是否一致
			if (account.equals(name) && md5Pas.equals(loginpas)) {
				Subject currentUser = SecurityUtils.getSubject();
				// 获取用户登录时,shiro存入在session中的数据
				Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
				if (memberInfo == null) {
					WebUtil.getSession().setAttribute(LoginType.MEMBERINFO.toString(), reList.get(0));
				}
				datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
				datasMap.put(BaseCode.MSG.getBaseCode(), "登录成功");
				return datasMap;
			}
		} else {
			datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			datasMap.put(BaseCode.MSG.getBaseCode(), "用户不存在");
			return null;
		}
		return null;
	}

	public Map<String, Object> getMemberInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return memberService.getMemberInfo(memberId, memberName);
	}

	public Map<String, Object> editShopCartGoodsFlag(String goodsInfoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return memberService.editShopCartGoodsFlag(goodsInfoPack, memberId, memberName);
	}

	public Map<String, Object> getMemberWalletInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return memberService.getMemberWalletInfo(memberId, memberName);
	}

	public Map<String, Object> checkMerchantName(String account) {
		return memberService.checkMerchantName(account);
	}

}
