package org.silver.shop.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.api.system.manual.MpayService;
import org.silver.shop.service.system.manual.ManualService;
import org.silver.util.ExcelUtil;
import org.silver.util.TaskUtils;

/**
 * 企邦Excel导入实现类
 *
 */
public class QBExcelTask extends TaskUtils {
	
	private int sheet;//
	private ExcelUtil excel;//
	private List<Map<String, Object>> errorList;//
	private String merchantId;// 商户Id
	private int startCount;// 开始行数
	private int endCount;// 结束行数
	private ManualService manualService;//
	private String serialNo;// 流水号
	private List dataList; //
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
	public QBExcelTask(int sheet, ExcelUtil excel, List<Map<String, Object>> errl, String merchantId, int startCount,
			int endCount, ManualService manualService, String serialNo,  int realRowCount) {
		this.sheet = sheet;
		this.excel = excel;
		this.errorList = errl;
		this.merchantId = merchantId;
		this.startCount = startCount;
		this.endCount = endCount;
		this.manualService = manualService;
		this.serialNo = serialNo;
		this.realRowCount = realRowCount;
	}

	/*public ExcelTask(List dataList, String merchantId, PaymentService paymentService,String serialNo,int realRowCount) {
		this.dataList = dataList;
		this.merchantId = merchantId;
		this.paymentService = paymentService;
		this.serialNo = serialNo;
		this.realRowCount = realRowCount;
	}*/

	@Override
	public Map<String, Object> call() {
	/*	
		if (flag == 1) {
			excel.open();
			return manualService.readQBSheet(sheet, excel, errorList, merchantId,serialNo, startCount, endCount, realRowCount);
		} else if (flag == 2) {
			excel.open();
			return manualService.readGZSheet(sheet, excel, errorList, merchantId, startCount, endCount, serialNo,realRowCount);
		}
		if (paymentService != null) {
			return paymentService.groupCreateMpay(merchantId, dataList,serialNo,realRowCount);
		}*/
		try{
			System.out.println("------call方法--------");
			excel.open();
			return manualService.readQBSheet(sheet, excel, errorList, merchantId,serialNo, startCount, endCount, realRowCount);
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
