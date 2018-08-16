package org.silver.shop.impl.system.organization;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hibernate.loader.custom.Return;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.dao.system.organization.MemberDao;
import org.silver.shop.impl.system.tenant.MerchantWalletServiceImpl;
import org.silver.shop.model.common.base.IdCard;
import org.silver.shop.model.system.commerce.ShopCarContent;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.quartz.CreatePaymentQtz;
import org.silver.shop.util.WalletUtils;
import org.silver.util.DateUtil;
import org.silver.util.IdcardValidator;
import org.silver.util.MD5;
import org.silver.util.PinyinUtil;
import org.silver.util.RandomPasswordUtils;
import org.silver.util.RandomUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.silver.util.YmMallOauth;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = MemberService.class)
public class MemberServiceImpl implements MemberService {

	@Autowired
	private MemberDao memberDao;
	@Autowired
	private MemberService memberService;
	@Autowired
	private WalletUtils walletUtils;
	@Autowired
	private CreatePaymentQtz createPaymentQtz;
	private static final Object LOCK = "lock";

	@Override
	public Map<String, Object> memberRegister(String account, String loginPass, String memberIdCardName,
			String memberIdCard, String memberId, String memberTel) {
		Date date = new Date();
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
			return ReturnInfoUtils.errorInfo("注册失败,服务器繁忙!");
		}
		// 创建用户钱包
		Map<String, Object> reWalletMap = walletUtils.checkWallet(2, memberId, account);
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			return reWalletMap;
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public List<Member> findMemberBy(String account) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberName", account);
		List<Member> reMemberList = memberDao.findByProperty(Member.class, params, 0, 0);
		if (reMemberList != null && !reMemberList.isEmpty()) {
			return reMemberList;
		} else {
			params.clear();
			params.put("memberTel", account);
			List<Member> reMemberList2 = memberDao.findByProperty(Member.class, params, 0, 0);
			if (reMemberList2 != null && !reMemberList2.isEmpty()) {
				return reMemberList2;
			}
			params.clear();
			params.put("memberIdCard", account);
			List<Member> reMemberList3 = memberDao.findByProperty(Member.class, params, 0, 0);
			if (reMemberList3 != null && !reMemberList3.isEmpty()) {
				return reMemberList3;
			}
		}
		return null;
	}

	@Override
	public Map<String, Object> createMemberId() {
		synchronized (LOCK) {
			//
			long memberIdCount = memberDao.findLastId();
			// 当返回-1时,则查询数据库失败
			if (memberIdCount < 0) {
				return ReturnInfoUtils.errorInfo("查询自增Id失败,服务器繁忙！");
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
			return ReturnInfoUtils.successDataInfo(memberId);
		}
	}

	@Override
	public Map<String, Object> getMemberInfo(String memberId) {
		if (StringEmptyUtils.isEmpty(memberId)) {
			return ReturnInfoUtils.errorInfo("用户Id不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		List<Member> reList = memberDao.findByProperty(Member.class, params, 1, 1);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询用户信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			Member member = reList.get(0);
			return ReturnInfoUtils.successDataInfo(member);
		} else {
			return ReturnInfoUtils.errorInfo("未找到用户信息!");
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
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> getMemberWalletInfo(String memberId, String memberName) {
		return walletUtils.checkWallet(2, memberId, memberName);
	}

	@Override
	public Map<String, Object> checkRegisterInfo(String datas, String type) {
		if (StringEmptyUtils.isEmpty(datas) || StringEmptyUtils.isEmpty(type)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		switch (type) {
		case "memberName":
			return checkMemberNameExist(datas);
		case "memberTel":
			return checkMemberMobileExist(datas);
		case "memberIdCard":
			return checkMemberIdCardExist(datas);
		default:
			return ReturnInfoUtils.errorInfo("类型错误,请重新输入!");
		}
	}

	/**
	 * 校验用户身份证号码是否已存在
	 * 
	 * @param memberIdCard
	 *            身份证号码
	 * @return Map
	 */
	private Map<String, Object> checkMemberIdCardExist(String memberIdCard) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("memberIdCard", memberIdCard);
		List<Member> reList = memberDao.findByProperty(Member.class, paramMap, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("身份证已注册,请重新输入!");
		} else {
			return ReturnInfoUtils.successInfo();
		}
	}

	/**
	 * 校验用户手机号码是否已存在
	 * 
	 * @param memberTel
	 *            手机号码
	 * @return Map
	 */
	private Map<String, Object> checkMemberMobileExist(String memberTel) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("memberTel", memberTel);
		List<Member> reList = memberDao.findByProperty(Member.class, paramMap, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("手机号码已存在,请重新输入!");
		} else {
			return ReturnInfoUtils.successInfo();
		}
	}

	/**
	 * 校验用户名称是否已存在
	 * 
	 * @param memberName
	 *            用户名称
	 * @return Map
	 */
	private Map<String, Object> checkMemberNameExist(String memberName) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("memberName", memberName);
		List<Member> reList = memberDao.findByProperty(Member.class, paramMap, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("用户名已存在,请重新输入!");
		} else {
			return ReturnInfoUtils.successInfo();
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
				// 只有当订单状态为备案成功时才创建用户账号
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

	
	@Override
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
		member.setLoginPass(md.getMD5ofStr(RandomPasswordUtils.createPassWord(8)));
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

	@Override
	public Map<String, Object> checkIdCard(String idcard) {
		if (StringEmptyUtils.isEmpty(idcard)) {
			return ReturnInfoUtils.errorInfo("身份证号码不能为空");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("memberIdCard", idcard);
		List<Member> memberList = memberDao.findByProperty(Member.class, params, 0, 0);
		if (memberList == null) {
			return ReturnInfoUtils.errorInfo("查询会员信息失败！");
		} else if (!memberList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("身份证号码[" + idcard + "]已注册过会员,请勿重复注册!");
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

	@Override
	public Map<String, Object> realName(String memberId) {
		if (StringEmptyUtils.isEmpty(memberId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> reMemberMap = getMemberInfo(memberId);
		if (!"1".equals(reMemberMap.get(BaseCode.STATUS.toString()))) {
			return reMemberMap;
		}
		Member member = (Member) reMemberMap.get(BaseCode.DATAS.toString());
		if (member.getMemberRealName() == 2) {
			return ReturnInfoUtils.errorInfo("用户已实名认证,无需重复认证!");
		}
		if (!StringUtil.isContainChinese(member.getMemberIdCardName())) {
			return ReturnInfoUtils.errorInfo("姓名必须为中文!");
		}
		if (!IdcardValidator.validate18Idcard(member.getMemberIdCard())) {
			return ReturnInfoUtils.errorInfo("身份证号码错误!");
		}
		Map<String, Object> reIdCardMap = getIdCardInfo(member.getMemberIdCardName(), member.getMemberIdCard());
		if (!"1".equals(reIdCardMap.get(BaseCode.STATUS.toString()))) {
			return reIdCardMap;
		}
		return updateRealFlag(member);
	}

	/**
	 * 根据身份证号码查询实名库中是否已存在
	 * 
	 * @param name
	 *            用户姓名
	 * @param idCard
	 *            用户身份证号码
	 * @return Map
	 */
	private Map<String, Object> getIdCardInfo(String name, String idCard) {
		if (StringEmptyUtils.isEmpty(name) || StringEmptyUtils.isEmpty(idCard)) {
			return ReturnInfoUtils.errorInfo("姓名或身份证号码不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("idNumber", idCard);
		List<IdCard> reList = memberDao.findByProperty(IdCard.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("实名库查询身份证信息失败,服务器繁忙!");
		} else if (reList.isEmpty()) {
			// 当实名库中没有该身份证号码时,发起验证
			Map<String, Object> reMap = createPaymentQtz.sendIdCardCertification(name, idCard);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()) + "")) {
				return reMap;
			}
			// 存入实名库
			String msgId = reMap.get("messageID") + "";
			String msg = reMap.get("msg") + "";
			return createPaymentQtz.addIdCardInfo(msgId, "", "", name, idCard, "success", msg);
		} else {
			IdCard idCardEntity = reList.get(0);
			int type = idCardEntity.getType();
			// 类型：1-未验证,2-手工验证,3-海关认证,4-第三方认证,5-错误
			if (type == 5) {
				return ReturnInfoUtils.errorInfo("身份证号码有误,请核对信息!");
			} else if (type == 1) {
				Map<String, Object> reMap = createPaymentQtz.sendIdCardCertification(name, idCard);
				if (!"1".equals(reMap.get(BaseCode.STATUS.toString()) + "")) {
					return reMap;
				}
				idCardEntity.setCertifiedNo(reMap.get("messageID") + "");
				idCardEntity.setNote(reMap.get("msg") + "");
				return updateIdCardInfo(idCardEntity);
			} else {
				return ReturnInfoUtils.successInfo();
			}
		}
	}

	/**
	 * 更新身份证实名标识
	 * 
	 * @param idCardEntity
	 *            身份证实体类
	 * @return Map
	 */
	private Map<String, Object> updateIdCardInfo(IdCard idCardEntity) {
		if (idCardEntity == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		// 类型：1-未验证,2-手工验证,3-海关认证,4-第三方认证,5-错误
		idCardEntity.setType(4);
		idCardEntity.setUpdateDate(new Date());
		if (!memberDao.update(idCardEntity)) {
			return ReturnInfoUtils.errorInfo("更新身份证信息失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 更新用户实名标识
	 * 
	 * @param member
	 *            用户信息实体类
	 * @return Map
	 */
	private Map<String, Object> updateRealFlag(Member member) {
		member.setMemberRealName(2);
		member.setUpdateDate(new Date());
		member.setUpdateBy("system");
		if (!memberDao.update(member)) {
			return ReturnInfoUtils.errorInfo("实名认证状态更新失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Object updateLoginPassword(String memberId, String newPassword) {
		if (StringEmptyUtils.isEmpty(memberId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> reMemberMap = getMemberInfo(memberId);
		if (!"1".equals(reMemberMap.get(BaseCode.STATUS.toString()))) {
			return reMemberMap;
		}
		Member member = (Member) reMemberMap.get(BaseCode.DATAS.toString());
		MD5 md5 = new MD5();
		member.setLoginPass(md5.getMD5ofStr(newPassword));
		if (memberDao.update(member)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("修改密码失败,服务器繁忙!");
	}

	@Override
	public Map<String, Object> editInfo(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		String memberId = String.valueOf(datasMap.get("memberId"));
		Map<String, Object> reMemberMap = getMemberInfo(memberId);
		if (!"1".equals(reMemberMap.get(BaseCode.STATUS.toString()))) {
			return reMemberMap;
		}
		Member member = (Member) reMemberMap.get(BaseCode.DATAS.toString());
		return updateInfo(member, datasMap);

	}

	private Map<String, Object> updateInfo(Member member, Map<String, Object> datasMap) {
		for (Map.Entry<String, Object> entry : datasMap.entrySet()) {
			String key = entry.getKey();
			switch (key) {
			case "memberTel":
				member.setMemberTel(String.valueOf(entry.getValue()));
				break;
			case "memberIdCardName":
				if (member.getMemberRealName() == 2) {
					return ReturnInfoUtils.errorInfo("该用户已实名,不允许修改身份证信息,请联系管理员！");
				}
				if (!StringUtil.isContainChinese(String.valueOf(entry.getValue()).replace("·", ""))) {
					return ReturnInfoUtils.errorInfo("姓名输入错误,请重新输入!");
				}
				member.setMemberIdCardName(String.valueOf(entry.getValue()));
				break;
			case "memberIdCard":
				if (member.getMemberRealName() == 2) {
					return ReturnInfoUtils.errorInfo("该用户已实名,不允许修改身份证信息,请联系管理员！");
				}
				if (!IdcardValidator.validate18Idcard(String.valueOf(entry.getValue()))) {
					return ReturnInfoUtils.errorInfo("身份证号码输入错误,请重新输入!");
				}
				member.setMemberIdCard(String.valueOf(entry.getValue()));
				break;
			default:
				break;
			}
		}
		if (!memberDao.update(member)) {
			return ReturnInfoUtils.errorInfo("修改失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}
}
