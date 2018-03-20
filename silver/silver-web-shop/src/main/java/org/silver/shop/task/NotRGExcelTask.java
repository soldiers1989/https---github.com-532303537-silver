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
	private GoodsRecordTransaction goodsRecordTransaction;//
	private Map<String, Object> params;

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
	public NotRGExcelTask(ExcelUtil excel, List<Map<String, Object>> errl,
			GoodsRecordTransaction goodsRecordTransaction, Map<String, Object> params) {
		this.excel = excel;
		this.errorList = errl;
		this.goodsRecordTransaction = goodsRecordTransaction;
		this.params = params;
	}

	@Override
	public Map<String, Object> call() {
		try {
			System.out.println("------call方法--------");
			excel.open();
			goodsRecordTransaction.batchAddNotRecordGoodsInfo(excel, errorList,  params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
