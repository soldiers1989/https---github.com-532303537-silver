package org.silver.shop.impl.system.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.log.FenZhangLogService;
import org.silver.shop.dao.system.log.FenZhangLogDao;
import org.silver.shop.model.system.log.FenZhangLog;
import org.silver.shop.util.IdUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = FenZhangLogService.class )
public class FenZhangLogServiceImpl implements FenZhangLogService{
	
	@Autowired
	private FenZhangLogDao fenZhangLogDao;
	@Autowired
	private IdUtils idUtils;
	
	@Override
	public Map<String,Object> saveFenZhangLog(Map<String,Object> datasMap) {
		Map<String,Object> reCheckMap = checkDatas("add",datasMap);
		if(!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))){
			return reCheckMap;
		}
		FenZhangLog entity = new FenZhangLog();
		Map<String,Object> reIdMap = idUtils.createId(FenZhangLog.class, "FZ");
		if(!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))){
			return reIdMap;
		}
		entity.setSerialNo(reIdMap.get(BaseCode.DATAS.toString())+"");
		entity.setOrderId(datasMap.get("orderId")+"");
		String tradeNo = datasMap.get("tradeNo")+"";
		if(StringEmptyUtils.isNotEmpty(tradeNo)){
			entity.setTradeNo(tradeNo);
		}
		entity.setYsPartnerNo(datasMap.get("ysPartnerNo")+"");
		try{
			double originalAmount = Double.parseDouble(datasMap.get("originalAmount")+"");
			entity.setOriginalAmount(originalAmount);
		}catch (Exception e) {
			return ReturnInfoUtils.errorInfo("原始订单金额错误！");
		}
		
		try{
			double platformFee = Double.parseDouble(datasMap.get("platformFee")+"");
			entity.setPlatformFee(platformFee);
		}catch (Exception e) {
			return ReturnInfoUtils.errorInfo("平台手续费金额错误！");
		}
		try{
			double masterReceiptAmount = Double.parseDouble(datasMap.get("masterReceiptAmount")+"");
			entity.setMasterReceiptAmount(masterReceiptAmount);
		}catch (Exception e) {
			return ReturnInfoUtils.errorInfo("主商户收款金额错误！");
		}
		entity.setDivPartnerParams(datasMap.get("divPartnerNo")+"");
		entity.setCreateBy(datasMap.get("createBy")+"");
		entity.setCreateDate(new Date());
		if(!fenZhangLogDao.add(entity)){
			return ReturnInfoUtils.errorInfo("保存失败，服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 校验数据
	 * @param type
	 * @param datasMap
	 * @return
	 */
	private Map<String, Object> checkDatas(String type, Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("校验参数时，请求参数不能为空！");
		}
		List<String> noNullKeys = new ArrayList<>();
		JSONArray jsonList = null;
		switch (type) {
		case "add":// 添加
			noNullKeys.add("orderId");
			noNullKeys.add("ysPartnerNo");
			noNullKeys.add("originalAmount");
			noNullKeys.add("platformFee");
			noNullKeys.add("masterReceiptAmount");
			noNullKeys.add("tradingStatus");
			
			noNullKeys.add("createBy");
			jsonList = new JSONArray();
			jsonList.add(datasMap);
			return CheckDatasUtil.checkData(jsonList, noNullKeys);
		default:
			return ReturnInfoUtils.errorInfo("校验信息时，[" + type + "]类型错误！");
		}
	}
	
}
