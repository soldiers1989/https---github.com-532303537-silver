package org.silver.shop.task;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.silver.shop.service.system.manual.ManualService;
import org.silver.shop.utils.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class ExcelTask implements Callable<Object> {
	private int sheet;
	private ExcelUtil excel;
	private List<Map<String, Object>> errorList;
	private String merchantId;
	private int startCount;
	private int endCount;
	private ManualService manualService;

	public ExcelTask(int sheet, ExcelUtil excel, List<Map<String, Object>> errl, String merchantId, int startCount,
			int endCount, ManualService manualService) {
		this.sheet = sheet;
		this.excel = excel;
		this.errorList = errl;
		this.merchantId = merchantId;
		this.startCount = startCount;
		this.endCount = endCount;
		this.manualService = manualService;
	}

	@Override
	public Map<String, Object> call() throws Exception {

		return manualService.readGZSheet(sheet, excel, errorList, merchantId, startCount, endCount);
	}

	public static void main(String args[]) {
		int i = 100;
		System.out.println(i/4);
	}
}
