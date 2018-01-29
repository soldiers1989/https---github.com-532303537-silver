package org.silver.shop.service.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.EPortService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

/**
 * 商品备案的口岸 Transaction
 *
 */
@Service
public class EPortTransaction {

	@Reference
	private EPortService ePortService;

	// 检查口岸名是否重复
	public Map<String, Object> checkEPortName(String customsPortName) {
		Map<String, Object> datasMap = new HashMap<>();
		List<Object> reList = ePortService.checkEportName(customsPortName);
		if (reList != null && reList.size() > 0) {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			datasMap.put(BaseCode.MSG.toString(), "口岸名已存在,请重新输入！");
		} else {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), "口岸名可以使用！");
		}
		return datasMap;
	}

	// 添加开通的口岸
	public Map<String, Object> addEPort(String customsPort, String customsPortName, String cityCode, String cityName,
			String provinceCode, String provinceName) {
		Map<String, Object> datasMap = new HashMap<>();
		datasMap = ePortService.addEPort(customsPort, customsPortName, cityCode, cityName, provinceCode, provinceName);
		String status = datasMap.get(BaseCode.STATUS.toString()) + "";
		if (status.equals("1")) {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			datasMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		}
		return datasMap;
	}

	// 查询全部口岸信息
	public Map<String, Object> findEPort() {
		Map<String, Object> reStatusMap = null;
		reStatusMap = ePortService.findAllEPort();
		String status = reStatusMap.get(BaseCode.STATUS.toString()) + "";
		if (status.equals("1")) {
			return reStatusMap;
		} else {
			reStatusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return reStatusMap;
	}

	// 修改口岸信息
	public Map<String, Object> editEPot(long id, String customsPort, String customsPortName, String cityCode,
			String cityName, String provinceCode, String provinceName) {
		Map<String, Object> reStatusMap = new HashMap<>();
		reStatusMap = ePortService.editEPotInfo(id, customsPort, customsPortName, cityCode, cityName, provinceCode,
				provinceName);
		String status = reStatusMap.get(BaseCode.STATUS.toString()) + "";
		
		if (status.equals("1")) {
			reStatusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			reStatusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			reStatusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getStatus());
			reStatusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		}
		return reStatusMap;
	}
}
