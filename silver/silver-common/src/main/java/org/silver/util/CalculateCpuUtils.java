package org.silver.util;

/**
 * 计算Cpu数量工具类
 */
public class CalculateCpuUtils {

	/**
	 * 根据总数计算出对应需要开辟线程的CPU数量
	 * 
	 * @param totalCount
	 *            总数
	 * @return int CPU数量
	 */
	public static final int calculateCpu(int totalCount) {
		// 判断当前计算机CPU线程个数
		int cpuCount = Runtime.getRuntime().availableProcessors();
		// 默认计算单位为服务器CPU数量32
		if (totalCount <= cpuCount) {
			return 1;
		} else if (totalCount <= (cpuCount * 2)) {
			// cpuCount=3
			return cpuCount / 9;
		} else if (totalCount >= (cpuCount * 2) && totalCount <= (cpuCount * 4)) {
			// cpuCount = 6
			return cpuCount / 5;
		} else if (totalCount >= (cpuCount * 4) && totalCount <= (cpuCount * 6)) {
			// cpuCount = 8
			return cpuCount / 4;
		} else if (totalCount >= (cpuCount * 6) && totalCount <= (cpuCount * 10)) {
			// cpuCount = 10
			return cpuCount / 3;
		} else if (totalCount >= (cpuCount * 10) && totalCount <= (cpuCount * 20)) {
			// cpuCount = 16
			return cpuCount / 2;
		} else {
			return cpuCount;
		}
	}

	public static void main(String[] args) {
		System.out.println("--->>>"+calculateCpu(811));
	}
}
