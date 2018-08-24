package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.tenant.MemberBankService;
import org.silver.shop.dao.system.tenant.MemberBankDao;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.tenant.MemberBankContent;
import org.silver.shop.model.system.tenant.MerchantBankContent;
import org.silver.shop.util.IdUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MemberBankService.class)
public class MemberBankServiceImpl implements MemberBankService {

	@Autowired
	private MemberBankDao memberBankDao;
	@Autowired
	private IdUtils idUtils;
	
	@Override
	public Map<String, Object> addInfo(Member memberInfo, Map<String, Object> datasMap) {
		if (memberInfo == null || datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		Map<String, Object> reIdMap = idUtils.createId(MemberBankContent.class, "memberBankId_");
		if (!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))) {
			return reIdMap;
		}
		String memberBankId = reIdMap.get(BaseCode.DATAS.toString()) + "";
		MemberBankContent entity = new MemberBankContent();
		entity.setMemberBankId(memberBankId);
		entity.setMemberId(memberInfo.getMemberId());
		entity.setMemberName(memberInfo.getMemberName());
		entity.setBankProvince(datasMap.get("bankProvince") + "");
		entity.setBankCity(datasMap.get("bankCity") + "");
		entity.setBankName(datasMap.get("bankName") + "");
		entity.setBankAccountNo(datasMap.get("bankAccountNo") + "");
		entity.setBankAccountName(datasMap.get("bankAccountName") + "");
		// 银行卡账户类型 私人(personal) 对公(corporate)
		entity.setBankAccountType(datasMap.get("bankAccountType") + "");
		// 银行卡类别 借记卡(debit) 信用卡(credit) 单位结算卡(unit)
		String bankCardType = datasMap.get("bankCardType") + "";
		if ("debit".equals(bankCardType) || "credit".equals(bankCardType) || "unit".equals(bankCardType)) {
			entity.setBankCardType(bankCardType);
		} else {
			return ReturnInfoUtils.errorInfo("银行卡类别错误，请重新输入！");
		}
		int defaultFlag = 0;
		try {
			defaultFlag = Integer.parseInt(datasMap.get("defaultFlag") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("选中标识错误！");
		}
		entity.setDefaultFlag(defaultFlag);
		entity.setCreateBy(memberInfo.getMemberName());
		entity.setCreateDate(new Date());
		if (!memberBankDao.add(entity)) {
			return ReturnInfoUtils.errorInfo("添加失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> getInfo(Member memberInfo, int page, int size) {
		if(memberInfo == null){
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
					
		}
		Map<String,Object> params = new HashMap<>();
		params.put("memberId", memberInfo.getMemberId());
		List<MerchantBankContent> reList = memberBankDao.findByProperty(MemberBankContent.class, params, page,
				size);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询银行卡信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}
	


}
