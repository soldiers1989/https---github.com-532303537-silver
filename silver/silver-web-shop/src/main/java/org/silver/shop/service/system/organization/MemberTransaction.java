package org.silver.shop.service.system.organization;

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
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.MD5;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户事物层
 */
@Service("memberTransaction")
public class MemberTransaction {

	@Autowired
	private MemberService memberService;

	// 用户注册
	public Map<String, Object> memberRegister(String account, String loginPass, String memberIdCardName,
			String memberIdCard) {
		Map<String, Object> datasMap = new HashMap<>();

		datasMap = memberService.createMemberId();
		if (!datasMap.get(BaseCode.STATUS.toString()).equals("1")) {
			return datasMap;
		}
		String memberId = datasMap.get(BaseCode.DATAS.toString()) + "";
		return memberService.memberRegister(account, loginPass, memberIdCardName, memberIdCard, memberId);
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
				// 获取商户登录时,shiro存入在session中的数据
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
		// 获取商户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return memberService.getMemberInfo(memberId,memberName);
	}

	//用户添加商品至购物车
	public Map<String,Object> addGoodsToShopCart(String goodsId, int count) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return memberService.addGoodsToShopCart(memberId,memberName,goodsId,count);
	}

}
