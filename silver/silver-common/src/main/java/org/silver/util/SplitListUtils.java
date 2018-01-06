package org.silver.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;

/**
 * List拆分工具类
 */
public class SplitListUtils {

	/**
	 * 
	 * @param dataList
	 * @param page
	 * @return
	 */
	public static Map<String, Object> batchList(List dataList, int length) {
		Map<String, Object> statusMap = new HashMap<>();
		// 分批处理
		if (null != dataList && !dataList.isEmpty()) {
			Map<String, Object> datasMap = new HashMap<>();
			List<Object> list = new ArrayList<>();
			Integer totalSize = dataList.size();
			int start = 0;
			int end = 0;
			// 判断是否有必要分批
			if (length < totalSize) {
				int part = totalSize / length;// 分批数
				System.out.println("共有 ： " + totalSize + "条，！" + " 每一次的长度 ：" + part + "");
				//遍历需要创建List的长度
				for (int i = 0; i < length; i++) {					
					if (i > 0 && length > 0) {
						start = i * part;
						end = (i + 1) * part;
						if(i == (length -1)){
							// 当是最后一次的时候
							start = i * part;
							end = totalSize;
						}
					} else {
						end = (i + 1) * part;
					}
					List<Object> listPage = dataList.subList(start, end);
					list.add(listPage);
				}
				datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				datasMap.put(BaseCode.DATAS.toString(), list);
				return datasMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		return statusMap;
	}

	public static void main(String[] args) {
		List<Object> l = new ArrayList<>();
		for (int i = 0; i <= 103; i++) {
			l.add(i);
		}
		Map<String, Object> map = batchList(l, 6);
		System.out.println(map);
	}
}
