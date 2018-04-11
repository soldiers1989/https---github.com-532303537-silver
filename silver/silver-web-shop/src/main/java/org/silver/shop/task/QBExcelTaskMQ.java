package org.silver.shop.task;

import java.util.Map;

import org.silver.shop.service.system.manual.ManualOrderTransaction;
import org.silver.util.ExcelUtil;
import org.silver.util.TaskUtils;

/**
 * 企邦Excel导入实现类
 * MQ版
 */
public class QBExcelTaskMQ extends TaskUtils {

	private ExcelUtil excel;//
	private ManualOrderTransaction manualOrderTransaction;// 调用服务
	private Map<String, Object> params; // 参数

	/**
	 * excel多任务读取
	 */
	public QBExcelTaskMQ(ExcelUtil excel, ManualOrderTransaction manualOrderTransaction, Map<String, Object> params) {
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
			manualOrderTransaction.readQiBangSheet(excel, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
