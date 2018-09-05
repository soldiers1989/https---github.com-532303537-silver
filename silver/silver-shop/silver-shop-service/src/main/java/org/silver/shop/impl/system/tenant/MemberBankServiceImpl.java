package org.silver.shop.impl.system.tenant;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.tenant.MemberBankService;
import org.silver.shop.dao.system.tenant.MemberBankDao;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.tenant.MemberBankContent;
import org.silver.shop.util.IdUtils;
import org.silver.shop.util.VerifiedUtils;
import org.silver.util.BankCardUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = MemberBankService.class)
public class MemberBankServiceImpl implements MemberBankService {

	@Autowired
	private MemberBankDao memberBankDao;
	@Autowired
	private IdUtils idUtils;
	@Autowired
	private VerifiedUtils verifiedUtils;

	@Override
	public Map<String, Object> addInfo(Member memberInfo, Map<String, Object> datasMap) {
		if (memberInfo == null || datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		// 银行卡账户类型 私人(personal) 对公(corporate)
		String bankAccountType = datasMap.get("bankAccountType") + "";
		if ("corporate".equals(bankAccountType)) {
			return ReturnInfoUtils.errorInfo("暂只支持私人账号！");
		}
		Map<String, Object> checkMap = checkIdentity(memberInfo, datasMap);
		if (!"1".equals(checkMap.get(BaseCode.STATUS.toString()) + "")) {
			return checkMap;
		}
		datasMap.put("memberId", memberInfo.getMemberId());
		datasMap.put("memberName", memberInfo.getMemberName());
		datasMap.put("createBy", memberInfo.getMemberName());
		return saveMemberBank(datasMap);
	}

	@Override
	public Map<String, Object> saveMemberBank(Map<String, Object> datasMap) {
		Map<String, Object> reCheckMap = checkData("add", datasMap);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		Map<String, Object> reIdMap = idUtils.createId(MemberBankContent.class, "memberBankId_");
		if (!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))) {
			return reIdMap;
		}
		String memberBankId = reIdMap.get(BaseCode.DATAS.toString()) + "";
		MemberBankContent entity = new MemberBankContent();
		entity.setMemberBankId(memberBankId);
		entity.setMemberId(datasMap.get("memberId") + "");
		entity.setMemberName(datasMap.get("memberName") + "");
		entity.setBankProvince(datasMap.get("bankProvince") + "");
		entity.setBankCity(datasMap.get("bankCity") + "");
		entity.setBankName(datasMap.get("bankName") + "");
		// 银行卡号
		String bankAccountNo = datasMap.get("bankAccountNo") + "";
		Map<String, Object> reBankAccountNoMap = checkOnlyBankAccountNo(bankAccountNo);
		if (!"1".equals(reBankAccountNoMap.get(BaseCode.STATUS.toString()))) {
			return reBankAccountNoMap;
		}
		entity.setBankAccountNo(bankAccountNo);
		entity.setBankAccountName(datasMap.get("bankAccountName") + "");
		// 银行卡账户类型 私人(personal) 对公(corporate)
		String bankAccountType = datasMap.get("bankAccountType") + "";
		entity.setBankAccountType(bankAccountType);
		// 银行卡类别 借记卡(debit) 信用卡(credit) 单位结算卡(unit)
		String bankCardType = datasMap.get("bankCardType") + "";
		if ("debit".equals(bankCardType) || "credit".equals(bankCardType) || "unit".equals(bankCardType)) {
			entity.setBankCardType(bankCardType);
		} else {
			return ReturnInfoUtils.errorInfo("银行卡类别错误，请重新输入！");
		}
		Map<String, Object> reFlagMap = firstBankDefault(datasMap.get("memberId") + "", entity);
		if (!"1".equals(reFlagMap.get(BaseCode.STATUS.toString()))) {
			return reFlagMap;
		}
		entity.setCreateBy(datasMap.get("createBy") + "");
		entity.setCreateDate(new Date());
		if (!memberBankDao.add(entity)) {
			return ReturnInfoUtils.errorInfo("添加失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 校验银行卡是否唯一
	 * 
	 * @param bankAccountNo
	 *            银行卡号
	 * @return Map
	 */
	private Map<String, Object> checkOnlyBankAccountNo(String bankAccountNo) {
		if (!BankCardUtils.checkBankCard(bankAccountNo)) {
			return ReturnInfoUtils.errorInfo("银行卡号错误！");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("bankAccountNo", bankAccountNo);
		Map<String, Object> reMap = getInfo(params, 0, 0);
		if ("-1".equals(reMap.get(BaseCode.ERROR_CODE.toString()))) {
			return ReturnInfoUtils.successInfo();
		} else if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		List<MemberBankContent> reList = (List<MemberBankContent>) reMap.get(BaseCode.DATAS.toString());
		if (!reList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("银行卡已存在，请勿重复添加");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 根据用户id查询用户下是否已有银行卡，没有则将新卡设定为默认卡
	 * @param memberId 用户id
	 * @param entity 银行卡实体信息
	 * @return Map
	 */
	public Map<String, Object> firstBankDefault(String memberId, MemberBankContent entity) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		Map<String, Object> reMap = getInfo(params, 0, 0);
		if ("-1".equals(reMap.get(BaseCode.ERROR_CODE.toString()))) {
			if (entity == null) {
				return ReturnInfoUtils.errorInfo("实体信息错误！");
			}
			// 选中标识：1-默认选中,2-备用
			entity.setDefaultFlag(1);
			return ReturnInfoUtils.successInfo();
		}
		return reMap;
	}

	/**
	 * 根据用户id查询用户银行卡，判断用户银行卡是否为第一张银行卡
	 * 
	 * @param entity
	 *            实体信息
	 * @param type
	 *            是否设置默认卡：1-就近设置默认卡、2-将所有银行卡设为备用
	 */
	private Map<String, Object> judgmentDefaultFlag(String memberId, String type) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		Map<String, Object> reMap = getInfo(params, 0, 0);
		if ("-1".equals(reMap.get(BaseCode.ERROR_CODE.toString()))) {
			//没有银行卡数据时，直接返回即可
			return ReturnInfoUtils.successInfo();
		}else if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		} else {
			List<MemberBankContent> reList = (List<MemberBankContent>) reMap.get(BaseCode.DATAS.toString());
			if ("1".equals(type)) {//
				MemberBankContent bank = reList.get(0);
				bank.setDefaultFlag(1);
				return updateBank(bank);
			} else if ("2".equals(type)) {
				for (int i = 0; i < reList.size(); i++) {
					MemberBankContent bank = reList.get(i);
					// 选中标识：1-默认选中,2-备用
					bank.setDefaultFlag(2);
					Map<String, Object> reUpdateMap = updateBank(bank);
					if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
						return reUpdateMap;
					}
				}
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 更新银行卡实体信息
	 * 
	 * @param entity
	 *            用户银行卡实体
	 * @return Map
	 */
	private Map<String, Object> updateBank(MemberBankContent entity) {
		if (entity == null) {
			return ReturnInfoUtils.errorInfo("更新失败，请求参数不能为null");
		}
		entity.setUpdateDate(new Date());
		if (memberBankDao.update(entity)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("更新失败，服务器繁忙！");
	}

	/**
	 * 根据不同银行卡业务类型，校验数据
	 * 
	 * @param type
	 * @param datasMap
	 * @return
	 */
	private Map<String, Object> checkData(String type, Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("校验参数时，请求参数不能为空！");
		}
		List<String> noNullKeys = new ArrayList<>();
		JSONArray jsonList = null;
		switch (type) {
		case "add":// 添加用户银行卡
			noNullKeys.add("memberId");
			noNullKeys.add("memberName");
			noNullKeys.add("bankProvince");
			noNullKeys.add("bankCity");
			noNullKeys.add("bankName");
			noNullKeys.add("bankAccountNo");
			noNullKeys.add("bankAccountName");
			noNullKeys.add("bankAccountType");
			noNullKeys.add("createBy");
			jsonList = new JSONArray();
			jsonList.add(datasMap);
			return CheckDatasUtil.checkData(jsonList, noNullKeys);
		default:
			return ReturnInfoUtils.errorInfo("校验银行卡信息时，[" + type + "]类型错误！");
		}

	}

	/**
	 * 验证身份
	 * 
	 * @param memberInfo
	 * @param datasMap
	 * @return
	 */
	private Map<String, Object> checkIdentity(Member memberInfo, Map<String, Object> datasMap) {
		if (memberInfo == null || datasMap == null) {
			return ReturnInfoUtils.errorInfo("验证身份时，请求参数不能为null");
		}
		// 用户实名标识：1-未实名、2-已实名
		if (memberInfo.getRealNameFlag() == 1) {
			return ReturnInfoUtils.errorInfo("您还未实名认证，不能绑定银行卡！");
		}
		if (!memberInfo.getMemberIdCardName().equals(datasMap.get("bankAccountName") + "")) {
			return ReturnInfoUtils.errorInfo("真实姓名与开户姓名不符合！");
		}
		return verifiedUtils.sendFourElementsVerification(memberInfo.getMemberIdCardName(),
				memberInfo.getMemberIdCard(), datasMap.get("bankAccountNo") + "", memberInfo.getMemberTel());
	}

	@Override
	public Map<String, Object> getInfo(String type, Map<String, Object> params, int page, int size) {
		switch (type) {
		case "hide":
			Map<String, Object> reMap = getInfo(params, page, size);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
			List<MemberBankContent> reList = (List<MemberBankContent>) reMap.get(BaseCode.DATAS.toString());
			List<MemberBankContent> newList = new ArrayList<>();
			for (MemberBankContent bank : reList) {
				String bankAccountNo = bank.getBankAccountNo();
				// 截取后4位
				String bankAccountNoEnd = bankAccountNo.substring(bankAccountNo.length() - 4, bankAccountNo.length());
				bank.setBankAccountNo("**** **** **** " + bankAccountNoEnd);
				bank.setBankAccountName(StringUtil.reservedInitialsReplaceOther(bank.getBankAccountName()));
				newList.add(bank);
			}
			return ReturnInfoUtils.successDataInfo(newList);
		case "display":
			return getInfo(params, page, size);
		default:
			return ReturnInfoUtils.errorInfo("查询失败,[" + type + "]类型错误！");
		}
	}

	/**
	 * 根据指定的参数查询银行卡信息
	 * 
	 * @param params
	 *            查询条件
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @return Map
	 */
	private Map<String, Object> getInfo(Map<String, Object> params, int page, int size) {
		List<MemberBankContent> reList = memberBankDao.findByPropertyLike(MemberBankContent.class, params, null, page,
				size);
		if (reList == null) {
			return ReturnInfoUtils.warnInfo();
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList);
		} else {
			return ReturnInfoUtils.noDatas();
		}
	}

	@Override
	public Map<String, Object> deleteInfo(String memberBankId) {
		if (StringEmptyUtils.isEmpty(memberBankId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空！");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("memberBankId", memberBankId);
		Map<String, Object> reMap = getInfo(params, 0, 0);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		List<MemberBankContent> reList = (List<MemberBankContent>) reMap.get(BaseCode.DATAS.toString());
		MemberBankContent entity = reList.get(0);
		Map<String, Object> reDeleteMap = delete(entity);
		if (!"1".equals(reDeleteMap.get(BaseCode.STATUS.toString()))) {
			return reDeleteMap;
		}
		return judgmentDefaultFlag(entity.getMemberId(), "1");
	}

	/**
	 * 真实删除用户银行卡信息
	 * 
	 * @param entity
	 *            实体信息
	 * @return boolean
	 */
	private Map<String, Object> delete(MemberBankContent entity) {
		if (entity == null) {
			return ReturnInfoUtils.errorInfo("删除失败，请求参数不能为null");
		}
		if (memberBankDao.delete(entity)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("删除失败，服务器繁忙！");
	}

	@Override
	public Map<String, Object> setDefaultBankCard(String memberBankId, String memberId) {
		if (StringEmptyUtils.isEmpty(memberBankId) || StringEmptyUtils.isEmpty(memberId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空！");
		}
		Map<String, Object> reDefaultMap = judgmentDefaultFlag(memberId, "2");
		if (!"1".equals(reDefaultMap.get(BaseCode.STATUS.toString()))) {
			return reDefaultMap;
		}
		Map<String, Object> params = new HashMap<>();
		params.put("memberBankId", memberBankId);
		Map<String, Object> reMap = getInfo(params, 0, 0);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		List<MemberBankContent> reList = (List<MemberBankContent>) reMap.get(BaseCode.DATAS.toString());
		MemberBankContent bank = reList.get(0);
		// 选中标识：1-默认选中,2-备用
		bank.setDefaultFlag(2);
		return updateBank(bank);
	}

}
