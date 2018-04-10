package org.silver.shop.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.api.system.manual.MpayService;
import org.silver.shop.service.system.manual.ManualOrderTransaction;
import org.silver.shop.service.system.manual.ManualService;
import org.silver.util.ExcelUtil;
import org.silver.util.TaskUtils;

/**
 * 企邦Excel导入实现类
 *
 */
public class QBExcelTaskMQ extends TaskUtils {

	private ExcelUtil excel;//
	private List<Map<String, Object>> errorList;//错误信息
	private ManualOrderTransaction manualOrderTransaction;// 调用服务
	private Map<String, Object> params; // 参数

	/**
	 * excel多任务读取
	 */
	public QBExcelTaskMQ(ExcelUtil excel, List<Map<String, Object>> errl, ManualOrderTransaction manualOrderTransaction,
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
			manualOrderTransaction.readQBSheet(excel, errorList, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
