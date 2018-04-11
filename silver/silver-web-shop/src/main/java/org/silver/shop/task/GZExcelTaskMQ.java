package org.silver.shop.task;

import java.util.List;
import java.util.Map;

import org.silver.shop.service.system.manual.ManualOrderTransaction;
import org.silver.util.ExcelUtil;
import org.silver.util.TaskUtils;

/**
 * 国宗Excel导入实现类
 * MQ版
 */
public class GZExcelTaskMQ extends TaskUtils {
	private ExcelUtil excel;//
	private ManualOrderTransaction manualOrderTransaction;//
	private Map<String, Object> params;//

	public GZExcelTaskMQ(ExcelUtil excel, ManualOrderTransaction manualOrderTransaction, Map<String, Object> params) {
		this.excel = excel;
		this.manualOrderTransaction = manualOrderTransaction;
		this.params = params;
	}

	@Override
	public Map<String, Object> call() {
		try {
			excel.open();
			params.put("counter", 0);
			params.put("statusCounter", 0);
			manualOrderTransaction.readGuoZongSheet(excel, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
