package org.silver.util;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 抽象多线程任务类
 *
 */
public abstract class TaskUtils implements Callable<Object> {

	@Override
	public abstract Map<String, Object> call() ;

	
}
