package org.silver.shop.task;

import java.util.List;
import java.util.Map;

import org.silver.shop.service.system.manual.ManualOrderTransaction;
import org.silver.shop.service.system.manual.ManualService;
import org.silver.util.ExcelUtil;
import org.silver.util.StringEmptyUtils;
import org.silver.util.TaskUtils;

public class GZExcelTaskMQ extends TaskUtils {
	private ExcelUtil excel;//
	private List<Map<String, Object>> errorList;//
	private ManualOrderTransaction manualOrderTransaction;//
	private Map<String, Object> params;//

	public GZExcelTaskMQ(ExcelUtil excel, List<Map<String, Object>> errl, ManualOrderTransaction manualOrderTransaction,
			Map<String, Object> params) {
		this.excel = excel;
		this.errorList = errl;
		this.manualOrderTransaction = manualOrderTransaction;
		this.params = params;
	}

	@Override
	public Map<String, Object> call() {
		try {
			excel.open();
			manualOrderTransaction.readGZSheet(excel, errorList, params);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
