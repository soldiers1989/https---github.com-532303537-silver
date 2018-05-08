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
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;
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
		if (!IdcardValidator.validate18Idcard(memberIdCard)) {
			return ReturnInfoUtils.errorInfo("身份证号输入错误,请重新输入!");
		}
		if (loginPass.length() < 6 || loginPass.length() > 18) {
			return ReturnInfoUtils.errorInfo("密码输入错误,请重新输入!");
		}
		if (!StringUtil.isContainChinese(memberIdCardName)) {
			return ReturnInfoUtils.errorInfo("姓名输入错误,请重新输入!");
		}
		// 获取缓存中用户注册手机验证码
		String redis = JedisUtil.get("Shop_Key_MemberRegisterCode_" + memberTel);
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
		MD5 md5 = new MD5();
		List<Member> reList = memberService.findMemberBy(account);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("服务器繁忙!");
		} else if (!reList.isEmpty()) {
			Member member = reList.get(0);
			// String name = member.getMemberName();
			String loginpas = member.getLoginPass();
			String md5Pas = md5.getMD5ofStr(loginPassword);
			// 判断查询出的账号密码与前台登录的账号密码是否一致
			if (md5Pas.equals(loginpas)) {
				Subject currentUser = SecurityUtils.getSubject();
				// 获取用户登录时,shiro存入在session中的数据
				Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
				if (memberInfo == null) {
					WebUtil.getSession().setAttribute(LoginType.MEMBERINFO.toString(), reList.get(0));
				}
				return ReturnInfoUtils.successInfo();
			}
		}
		return null;
	}

	public Map<String, Object> getMemberInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		// String memberName = memberInfo.getMemberName();
		return memberService.getMemberInfo(memberId);
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

	//会员修改密码
	public Object editPassword(String memberId, String oldPassword, String newPassword) {
		return memberService.editPassword(memberId,oldPassword,newPassword);
	}

	//用户修改信息
	public Object editInfo(Map<String, Object> datasMap) {
		return memberService.editInfo(datasMap);
	}

}
