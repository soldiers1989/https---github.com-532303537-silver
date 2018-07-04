package org.silver.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class MapSortUtils {
	/**
	 * 
	 * @Title: sortMap
	 * @Description: 对集合内的数据按key的字母顺序做排序
	 */
	public Map<String, Object> sortMap(final Map<String, Object> map) {
		Map<String, Object> obj = new LinkedHashMap<>();
		final List<Map.Entry<String, Object>> infos = new ArrayList<Map.Entry<String, Object>>(map.entrySet());

		// 重写集合的排序方法：按字母顺序
		Collections.sort(infos, new Comparator<Map.Entry<String, Object>>() {
			@Override
			public int compare(final Entry<String, Object> o1, final Entry<String, Object> o2) {
				return (o1.getKey().toString().compareTo(o2.getKey()));
			}
		});

		for (final Map.Entry<String, Object> m : infos) {
			obj.put(m.getKey(), m.getValue());
		}

		return obj;
	}
}
