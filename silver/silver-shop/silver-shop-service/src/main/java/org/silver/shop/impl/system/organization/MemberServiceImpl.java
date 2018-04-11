package org.silver.shop.impl.system.organization;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.dao.system.organization.MemberDao;
import org.silver.shop.impl.system.tenant.MerchantWalletServiceImpl;
import org.silver.shop.model.system.commerce.ShopCarContent;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.util.DateUtil;
import org.silver.util.MD5;
import org.silver.util.PinyinUtil;
import org.silver.util.RandomUtils;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = MemberService.class)
public class MemberServiceImpl implements MemberService {

	@Autowired
	private MemberDao memberDao;
	@Autowired
	private MerchantWalletServiceImpl merchantWalletServiceImpl;
	@Autowired
	private MemberService memberService;

	private static final Object LOCK = "lock";

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
		// 用户状态1-审核2-启用3-禁用
		member.setMemberStatus(1);
		// 用户实名1-未实名,2-已实名
		member.setMemberRealName(1);
		if (!memberDao.add(member)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		// 创建用户钱包
		Map<String, Object> reMap = merchantWalletServiceImpl.checkWallet(2, memberId, account);
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
		synchronized (LOCK) {
			//
			long memberIdCount = memberDao.findLastId();
			// 当返回-1时,则查询数据库失败
			if (memberIdCount < 0) {
				datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
				return datasMap;
			}
			// 得出的总数上+1
			long count = memberIdCount + 1;
			String memberId = String.valueOf(count);
			// 当商户ID没有5位数时,前面补0
			while (memberId.length() < 5) {
				memberId = "0" + memberId;
			}
			// 生成用户ID
			memberId = "Member_" + memberId;
			datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			datasMap.put(BaseCode.DATAS.getBaseCode(), memberId);
			return datasMap;
		}
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
			params.put("entGoodsNo", reMap.get("entGoodsNo"));
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
				statusMap.put(BaseCode.MSG.toString(), "该商品未找到,请核对后在提交!");
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
		Map<String, Object> reMap = merchantWalletServiceImpl.checkWallet(2, memberId, memberName);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "创建钱包失败!");
			return statusMap;
		}
		MemberWalletContent wallet = (MemberWalletContent) reMap.get(BaseCode.DATAS.toString());
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		statusMap.put(BaseCode.DATAS.toString(), wallet);
		return statusMap;

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

	@Override
	public Map<String, Object> batchRegisterMember(JSONArray jsonArr) {
		if (jsonArr == null || jsonArr.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数错误!");
		}
		List<String> errorList = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();
		int successCount = 0;
		for (int i = 0; i < jsonArr.size(); i++) {
			String orderId = jsonArr.get(i) + "";
			params.clear();
			params.put("order_id", orderId);
			List<Morder> reList = memberDao.findByProperty(Morder.class, params, 0, 0);
			if (reList == null) {
				errorList.add("订单[" + orderId + "]查询失败,服务器繁忙!");
			} else if (!reList.isEmpty()) {
				Morder order = reList.get(0);
				if (order.getOrder_record_status() == 3) {
					Map<String, Object> reMemberMap = registerMember(order);
					if (!"1".equals(reMemberMap.get(BaseCode.STATUS.toString()))) {
						errorList.add(reMemberMap.get(BaseCode.MSG.toString()) + "");
					} else {
						successCount++;
					}
				} else {
					errorList.add("订单[" + orderId + "]状态不允许生成会员信息!");
				}
			} else {
				errorList.add("订单[" + orderId + "]未找到对应的订单信息!");
			}
		}
		if (errorList.isEmpty()) {
			return ReturnInfoUtils.successInfo();
		}
		params.clear();
		params.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
		params.put(BaseCode.ERROR.toString(), errorList);
		params.put("successCount", successCount);
		return params;
	}

	/**
	 * 根据(手工)订单信息注册会员信息
	 * 
	 * @param Morder
	 *            手工订单实体信息
	 * @return Map
	 */
	public Map<String, Object> registerMember(Morder order) {
		if (order == null) {
			return ReturnInfoUtils.errorInfo("注册会员信息失败,请求参数错误!");
		}
		String orderDocId = order.getOrderDocId();
		Map<String, Object> checkIdMap = checkIdCard(orderDocId);
		if (!"1".equals(checkIdMap.get(BaseCode.STATUS.toString()))) {
			return checkIdMap;
		}
		Member member = new Member();
		Map<String, Object> reMemberIdMap = memberService.createMemberId();
		if (!"1".equals(reMemberIdMap.get(BaseCode.STATUS.toString()))) {
			return reMemberIdMap;
		}
		member.setMemberId(reMemberIdMap.get(BaseCode.DATAS.toString()) + "");
		//
		member.setMemberName(randomCreateName(order));
		MD5 md = new MD5();
		member.setLoginPass(md.getMD5ofStr(Integer.toString(RandomUtils.getRandom(6))));
		member.setMemberTel(order.getOrderDocTel());
		member.setMemberIdCardName(order.getOrderDocName());
		member.setMemberIdCard(order.getOrderDocId());
		member.setMemberRealName(2);
		member.setCreateBy(order.getOrderDocName());
		// 获取随机下单(生成)订单时间
		String date = order.getOrderDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(DateUtil.parseDate(date, "yyyyMMddHHddss"));
		// 在订单下单日期之前30分钟创建会员信息
		calendar.add(Calendar.MINUTE, -30);
		member.setCreateDate(calendar.getTime());
		if (!memberDao.add(member)) {
			return ReturnInfoUtils.errorInfo("保存用户失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 检查身份证号码是否已注册
	 * 
	 * @param orderDocId
	 * @return
	 */
	private Map<String, Object> checkIdCard(String orderDocId) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberIdCard", orderDocId);
		List<Member> memberList = memberDao.findByProperty(Member.class, params, 0, 0);
		if (memberList == null) {
			return ReturnInfoUtils.errorInfo("查询会员信息失败！");
		} else if (!memberList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("身份证号码[" + orderDocId + "]已注册过会员,请勿重复注册!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 采用随机算法生成会员名称,规则：随机生成一个1-9之间的数,当小于2时采用拼音全称+1-5位随机数,当大于2小于5时使用下单人电话号码,大于5时采用身份证号码
	 * 
	 * @param order
	 *            手工订单信息实体
	 * @String name
	 */
	private String randomCreateName(Morder order) {
		int count = RandomUtils.getRandom(1);
		if (count <= 2) {// 当小于2时采用拼音全称+1-5位随机数
			String orderDocName = order.getOrderDocName();
			return randomMemberName(orderDocName);
		} else if (count > 2 && count <= 5) {// 使用下单人电话号码
			return order.getOrderDocTel();
		} else {// 使用身份证
			return order.getOrderDocId();
		}
	}

	/**
	 * 根据递归生成用户账号,避免重复
	 * 
	 * @param orderDocName
	 *            下单人姓名
	 * @return String 下单人账号
	 */
	private String randomMemberName(String orderDocName) {
		Random rand = new Random();
		// 下单人姓名
		String pinyin = PinyinUtil.getPinYin(orderDocName.trim());
		String memberName = pinyin + Integer.toString(rand.nextInt(10000) + 1);
		Map<String, Object> params = new HashMap<>();
		params.put("memberName", memberName);
		List<Member> reList = memberDao.findByProperty(Member.class, params, 0, 0);
		if (reList == null) {
			return randomMemberName(orderDocName);
		} else if (reList.isEmpty()) {
			return memberName;
		} else {
			return randomMemberName(orderDocName);
		}
	}

	public static void main(String[] args) {
		System.out.println(PinyinUtil.getPinYin("周婷婷"));
		System.out.println(PinyinUtil.getPinYinHeadChar("何德志"));
	}
}
