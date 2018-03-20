package org.silver.shop.task;

import java.util.List;
import java.util.Map;
import org.silver.shop.service.system.manual.ManualService;
import org.silver.util.ExcelUtil;
import org.silver.util.StringEmptyUtils;
import org.silver.util.TaskUtils;

/**
 * 国宗Excel导入实现类
 *
 */
public class GZExcelTask extends TaskUtils {

	private ExcelUtil excel;//
	private List<Map<String, Object>> errorList;//
	private ManualService manualService;//
	private Map<String, Object> params;//

	/**
	 * excel多任务读取
	 * 
	 */
	public GZExcelTask(ExcelUtil excel, List<Map<String, Object>> errl, ManualService manualService,
			Map<String, Object> params) {
		this.excel = excel;
		this.errorList = errl;
		this.manualService = manualService;
		this.params = params;
	}


	@Override
	public Map<String, Object> call() {
		try {
			excel.open();
			if (StringEmptyUtils.isNotEmpty(params.get("merchantId") + "")
					&& StringEmptyUtils.isNotEmpty(params.get("merchantName") + "")) {
				// excel表索引
				params.put("sheet", 0);
				manualService.readGZSheet(excel, errorList, params);
			} else {
				manualService.pretreatmentGZTable(excel, errorList, params);
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
