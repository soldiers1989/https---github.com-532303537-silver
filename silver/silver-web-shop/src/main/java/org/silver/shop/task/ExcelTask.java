package org.silver.shop.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.silver.shop.service.system.manual.ManualService;
import org.silver.shop.utils.ExcelUtil;

public class ExcelTask implements Callable<Object> {

	private int sheet;//
	private ExcelUtil excel;//
	private List<Map<String, Object>> errorList;//
	private String merchantId;//商户Id
	private int startCount;//开始行数
	private int endCount;//结束行数
	private ManualService manualService;//
	private int serialNo ;//订单批次号
	/**
	 * excel多任务读取
	 * @param sheet
	 * @param excel
	 * @param errl
	 * @param merchantId
	 * @param startCount
	 * @param endCount
	 * @param manualService
	 */
	public ExcelTask(int sheet, ExcelUtil excel, List<Map<String, Object>> errl, String merchantId, int startCount,
			int endCount, ManualService manualService,int serialNo) {
		this.sheet = sheet;
		this.excel = excel;
		this.errorList = errl;
		this.merchantId = merchantId;
		this.startCount = startCount;
		this.endCount = endCount;
		this.manualService = manualService;
		this.serialNo = serialNo;
	}
	/**
	 * 
	 * @param sheet
	 * @param excel
	 * @param errl
	 * @param merchantId
	 * @param startCount
	 * @param endCount
	 * @param manualService
	 */
	/*public createTask(int sheet, ExcelUtil excel, List<Map<String, Object>> errl, String merchantId, int startCount,
			int endCount, Object manualService) {
		this.sheet = sheet;
		this.excel = excel;
		this.errorList = errl;
		this.merchantId = merchantId;
		this.startCount = startCount;
		this.endCount = endCount;
		this.manualService = manualService;
	}*/
	@Override
	public Map<String, Object> call() {
		
		excel.open();
		return manualService.readGZSheet(sheet, excel, errorList, merchantId, startCount, endCount,serialNo);
	}

}
