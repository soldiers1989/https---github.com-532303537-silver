package org.silver.shop.task;

import java.util.List;
import java.util.Map;

import org.silver.shop.service.system.manual.ManualService;
import org.silver.util.ExcelUtil;
import org.silver.util.TaskUtils;

/**
 * 预处理启邦订单子任务类
 */
public class PretreatmentQBExcelTask extends TaskUtils {
	private ExcelUtil excel;//
	private List<Map<String, Object>> errorList;//
	private ManualService manualService;//
	private Map<String, Object> params;//

	
	public PretreatmentQBExcelTask(ExcelUtil excel, List<Map<String, Object>> errl, ManualService manualService,
			Map<String, Object> params) {
		this.excel = excel;
		this.errorList = errl;
		this.manualService = manualService;
		this.params = params;
	}

	@Override
	public Map<String, Object> call() {
		try {
			excel.open();
			// excel表索引
			params.put("sheet", 0);
			manualService.pretreatmentQBTable(excel, errorList, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
