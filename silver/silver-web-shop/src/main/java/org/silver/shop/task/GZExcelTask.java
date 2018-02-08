package org.silver.shop.task;

import java.util.List;
import java.util.Map;
import org.silver.shop.service.system.manual.ManualService;
import org.silver.util.ExcelUtil;
import org.silver.util.TaskUtils;

/**
 * 国宗Excel导入实现类
 *
 */
public class GZExcelTask extends TaskUtils {

	private int sheet;//
	private ExcelUtil excel;//
	private List<Map<String, Object>> errorList;//
	private String merchantId;// 商户Id
	private int startCount;// 开始行数
	private int endCount;// 结束行数
	private ManualService manualService;//
	private String serialNo;// 流水号
	private int realRowCount;// 总行数
	private String merchantName;// 商户名称

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
	 * @param merchantName
	 */
	public GZExcelTask(int sheet, ExcelUtil excel, List<Map<String, Object>> errl, String merchantId, int startCount,
			int endCount, ManualService manualService, String serialNo, int realRowCount, String merchantName) {
		this.sheet = sheet;
		this.excel = excel;
		this.errorList = errl;
		this.merchantId = merchantId;
		this.startCount = startCount;
		this.endCount = endCount;
		this.manualService = manualService;
		this.serialNo = serialNo;
		this.realRowCount = realRowCount;
		this.merchantName = merchantName;
	}

	@Override
	public Map<String, Object> call() {
		try {
			System.out.println("------call方法--------");
			excel.open();
			manualService.readGZSheet(sheet, excel, errorList, merchantId, startCount, endCount, serialNo, realRowCount,
					merchantName);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
