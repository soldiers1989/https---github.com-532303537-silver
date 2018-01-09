package org.silver.shop.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.silver.shop.api.system.manual.MpayService;
import org.silver.shop.service.system.manual.ManualService;
import org.silver.shop.utils.ExcelUtil;

public class ExcelTask implements Callable<Object> {

	private int sheet;//
	private ExcelUtil excel;//
	private List<Map<String, Object>> errorList;//
	private String merchantId;// 商户Id
	private int startCount;// 开始行数
	private int endCount;// 结束行数
	private ManualService manualService;//
	private int serialNo;// 订单批次号
	private MpayService mpayService;//
	private List dataList; //
	private int flag;// 标识：1-企邦,2-国宗
	private int realRowCount;//总行数
	/**
	 * excel多任务读取
	 * 
	 * @param sheet
	 * @param excel
	 * @param errl
	 * @param merchantId
	 * @param startCount
	 * @param endCount
	 * @param manualService
	 */
	public ExcelTask(int sheet, ExcelUtil excel, List<Map<String, Object>> errl, String merchantId, int startCount,
			int endCount, ManualService manualService, int serialNo, int flag,int realRowCount ) {
		this.sheet = sheet;
		this.excel = excel;
		this.errorList = errl;
		this.merchantId = merchantId;
		this.startCount = startCount;
		this.endCount = endCount;
		this.manualService = manualService;
		this.serialNo = serialNo;
		this.flag = flag;
		this.realRowCount = realRowCount;
	}

	/**
	 * 
	 * @param dataList
	 * @param merchantId
	 * @param mpayService
	 */
	public ExcelTask(List dataList, String merchantId, MpayService mpayService) {
		this.dataList = dataList;
		this.merchantId = merchantId;
		this.mpayService = mpayService;
	}

	@Override
	public Map<String, Object> call() {
		if (flag == 1) {
			excel.open();
			return manualService.readQBSheet(sheet, excel, errorList, merchantId,serialNo, startCount, endCount, realRowCount);
		} else if (flag == 2) {
			excel.open();
			return manualService.readGZSheet(sheet, excel, errorList, merchantId, startCount, endCount, serialNo,realRowCount);
		}
		if (mpayService != null) {
			return mpayService.groupCreateMpay(merchantId, dataList);
		}
		return null;
	}

}
