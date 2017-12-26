package org.silver.shop.task;

import java.util.List;
import java.util.Map;
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

public  class CallableAndFuture {
	
	/*static Integer read(int ccc) {
		return ccc;
	}*/

    
	
	public static  void futureTask(int sheet, ExcelUtil excel, List<Map<String, Object>> errl, String merchantId) {
		final int sheet2 = sheet;
		final ExcelUtil excel2 = excel;
		final List<Map<String, Object>> errl2 = errl;
		final String merchantId2 = merchantId;
		
		ExecutorService threadPool = Executors.newCachedThreadPool();
		FutureTask<Map<String,Object>> futureTask1 = new FutureTask<>(new Callable<Map<String,Object>>() {
			@Override
			public Map<String,Object> call() throws Exception {
				ManualService manualService = new ManualService();
				return manualService.readGZSheet(sheet2, excel2, errl2, merchantId2);
			}
		});

		/*FutureTask<Integer> futureTask2 = new FutureTask<>(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return read(20);
			}
		});*/
		
		threadPool.submit(futureTask1);
		//threadPool.submit(futureTask2);
		//try {
		//System.out.println(futureTask1.get());
		//	threadPool.shutdown();
		//} catch (InterruptedException e) {
		//	e.printStackTrace();
		//} catch (ExecutionException e) {
		//	e.printStackTrace();
		//}
	}
}
