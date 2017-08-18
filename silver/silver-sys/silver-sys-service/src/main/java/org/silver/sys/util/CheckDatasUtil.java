package org.silver.sys.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CheckDatasUtil {

	public static Map<String, Object> checkData(JSONArray datas, List<String> noNullKeys ) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<JSONObject> dataList = new ArrayList<>();
		if (datas != null && datas.size() > 0) {
			JSONObject jsonObject = null;
			String key, value, result;
			for (int i = 0; i < datas.size(); i++) {
				jsonObject = (JSONObject) datas.get(i);
				Iterator it = jsonObject.keys();
				while (it.hasNext()) {
					key = it.next() + "";
					value = jsonObject.get(key) + "";
						if (("null".equals(value) || "".equals(value.trim())) && noNullKeys.contains(key)) {
							map.put("status", -1);
							map.put("msg", "第" + (i+1) + "条数据 " + key + "不能为空");
							return map;
						}
					noNullKeys.remove(key);
				}
				dataList.add(jsonObject);
			}
			if (noNullKeys.size() > 0) {
				map.put("status", -2);
				map.put("msg", noNullKeys.get(0) + ":不能为空");
				return map;
			}
			map.put("status", 1);
			map.put("datas", dataList);
			map.put("msg", "数据校验通过");
			return map;

		}
		map.put("status", -3);
		map.put("msg", "非法数据");
		return map;
	}

	public static void main(String[] args) {
	
	}
}
