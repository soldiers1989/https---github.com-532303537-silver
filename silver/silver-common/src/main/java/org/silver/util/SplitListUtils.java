package org.silver.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;

/**
 * List拆分工具类
 */
public class SplitListUtils {

	/**
	 * 将List集合拆分为多个
	 * @param dataList 
	 * @param size 拆分的数量
	 * @return Map
	 */
	public static Map<String, Object> batchList(List dataList, int size) {
		// 分批处理
		if (dataList != null && !dataList.isEmpty()) {
			Map<String, Object> datasMap = new HashMap<>();
			List<Object> list = new ArrayList<>();
			Integer totalSize = dataList.size();
			int start = 0;
			int end = 0;
			// 判断是否有必要分批
			if ( size > 1 && size < totalSize) {
				int part = totalSize / size;// 分批数
				//遍历需要创建List的长度
				for (int i = 0; i < size; i++) {
					if (i > 0 && size > 0) {
						start = i * part;
						end = (i + 1) * part;
						if(i == (size -1)){
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
			}else{
				list.add(dataList);
				return ReturnInfoUtils.successDataInfo(list);
			}
		}
		return ReturnInfoUtils.errorInfo("拆分List错误！");
	}

	public static void main(String[] args) {
		long startTime =System.currentTimeMillis();
		List<Object> l = new ArrayList();
		for (int i = 0; i < 5; i++) {
			l.add(i);
		}
		Map<String, Object> map = batchList(l, 1);
		
		System.out.println(map);
		long endTime =System.currentTimeMillis();
		System.out.println("耗时："+(endTime - startTime)+"ms");
	}
}
