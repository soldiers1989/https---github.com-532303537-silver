package org.silver.shop.impl.system.organization;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.dao.system.organization.MemberDao;
import org.silver.shop.impl.system.tenant.MerchantWalletServiceImpl;
import org.silver.shop.model.system.commerce.ShopCarContent;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.util.MD5;
import org.silver.util.SerialNoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = MemberService.class)
public class MemberServiceImpl implements MemberService {

	@Autowired
	private MemberDao memberDao;

	@Autowired
	private MerchantWalletServiceImpl merchantWalletServiceImpl;
	
	@Override
	public Map<String, Object> memberRegister(String account, String loginPass, String memberIdCardName,
			String memberIdCard, String memberId, String memberTel) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		MD5 md = new MD5();
		Member member = new Member();
		member.setMemberId(memberId);
		member.setMemberName(account);
		member.setLoginPass(md.getMD5ofStr(loginPass));
		member.setMemberIdCard(memberIdCard);
		member.setMemberIdCardName(memberIdCardName);
		member.setCreateBy(account);
		member.setMemberTel(memberTel);
		member.setCreateDate(date);
		if (!memberDao.add(member)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		//创建用户钱包
		Map<String,Object> reMap = merchantWalletServiceImpl.checkWallet(2, memberId, account);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getMsg());
			statusMap.put(BaseCode.MSG.toString(), "用户创建钱包失败!");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public List<Object> findMemberBy(String account) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberName", account);
		return memberDao.findByProperty(Member.class, params, 0, 0);
	}

	@Override
	public Map<String, Object> createMemberId() {
		Map<String, Object> datasMap = new HashMap<>();
		Calendar cal = Calendar.getInstance();
		// 获取当前年份
		int year = cal.get(Calendar.YEAR);
		// 查询数据库字段名
		String property = "memberId";
		// 根据年份查询,当前年份下的id数量
		long memberIdCount = memberDao.findSerialNoCount(Member.class, property, year);
		// 当返回-1时,则查询数据库失败
		if (memberIdCount < 0) {
			datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return datasMap;
		}
		// 生成用户ID
		String memberId = SerialNoUtils.getSerialNotTimestamp("Member_", year, memberIdCount);
		datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
		datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
		datasMap.put(BaseCode.DATAS.getBaseCode(), memberId);
		return datasMap;
	}

	@Override
	public Map<String, Object> getMemberInfo(String memberId, String memberName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("memberName", memberName);
		List<Object> reList = memberDao.findByProperty(Member.class, params, 1, 1);
		if (reList != null && reList.size() > 0) {
			Member member = (Member) reList.get(0);
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.getBaseCode(), member);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "用戶不存在！");
			return statusMap;
		}
	}

	@Override
	// 临时使用
	public Map<String, Object> editShopCartGoodsFlag(String goodsInfoPack, String memberId, String memberName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 将所有标识为2的购物车中的商品,修改为1
		params.put("flag", 2);
		List<Object> re = memberDao.findByProperty(ShopCarContent.class, params, 0, 0);
		if (re != null && re.size() > 0) {
			for (int i = 0; i < re.size(); i++) {
				ShopCarContent cart = (ShopCarContent) re.get(i);
				cart.setFlag(1);
				if (!memberDao.update(cart)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
					return statusMap;
				}
			}
		}

		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> reMap = (Map<String, Object>) jsonList.get(i);
			params.clear();
			params.put("goodsBaseId", reMap.get("goodsId"));
			params.put("memberId", memberId);
			List<Object> reList = memberDao.findByProperty(ShopCarContent.class, params, 1, 1);
			if (reList != null && reList.size() > 0) {
				ShopCarContent cart = (ShopCarContent) reList.get(0);
				cart.setCount(Integer.parseInt(reMap.get("count") + ""));
				cart.setFlag(2);
				if (!memberDao.update(cart)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
					return statusMap;
				}

			} else {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
				return statusMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> getMemberWalletInfo(String memberId, String memberName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("memberName", memberName);
		List<Object> reList = memberDao.findByProperty(MemberWalletContent.class, params, 0, 0);
		if (reList != null && reList.size() > 0) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> checkMerchantName(String account) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("memberName", account);
		List<Object> reList = memberDao.findByProperty(Member.class, paramMap, 0, 0);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (reList.size() == 0) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "用户名可以使用!");
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.UNKNOWN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "用户名已存在!");
			return statusMap;
		}
	}

}
