package org.silver.shop.task;

import java.util.List;
import java.util.Map;

import org.silver.shop.service.system.commerce.GoodsRecordTransaction;
import org.silver.shop.service.system.manual.ManualService;
import org.silver.util.ExcelUtil;
import org.silver.util.TaskUtils;

/**
 * 未备案商品子任务类
 *
 */
public class NotRGExcelTask extends TaskUtils {
	private ExcelUtil excel;//
	private List<Map<String, Object>> errorList;//
	private String merchantId;// 商户Id
	private int startCount;// 开始行数
	private int endCount;// 结束行数
	private GoodsRecordTransaction goodsRecordTransaction;//
	private String serialNo;// 流水号
	private int realRowCount;// 总行数
	private String merchantName;// 商户名称

	/**
	 * excel多任务读取未备案商品
	 * 
	 * @param sheet
	 * @param excel
	 * @param errl
	 * @param merchantId
	 * @param startCount
	 * @param endCount
	 * @param goodsRecordTransaction
	 * @param merchantName
	 */
	public NotRGExcelTask(  ExcelUtil excel, List<Map<String, Object>> errl, String merchantId, int startCount,
			int endCount, GoodsRecordTransaction goodsRecordTransaction, String serialNo, int realRowCount, String merchantName) {
		this.excel = excel;
		this.errorList = errl;
		this.merchantId = merchantId;
		this.startCount = startCount;
		this.endCount = endCount;
		this.goodsRecordTransaction = goodsRecordTransaction;
		this.serialNo = serialNo;
		this.realRowCount = realRowCount;
		this.merchantName = merchantName;
	}

	@Override
	public Map<String, Object> call() {
		try {
			System.out.println("------call方法--------");
			excel.open();
			goodsRecordTransaction.batchAddNotRecordGoodsInfo( excel, errorList, merchantId, serialNo, startCount, endCount, realRowCount,
					merchantName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
