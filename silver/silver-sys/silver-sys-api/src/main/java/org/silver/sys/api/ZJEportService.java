package org.silver.sys.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import net.sf.json.JSONArray;

public interface ZJEportService {
    /**
     * 智检平台商品备案
     * @param list
     * @param path
     * @param opType
     * @param businessType
     * @param ieFlag
     * @return
     */
	public Map<String, Object> zjCreateGoodsRecord(Object obj, String path, String opType, String businessType,
			String ieFlag);
	
	/**
	 * 智检平台订单备案
	 * @param list
	 * @param path
	 * @param opType
	 * @param ieFlag
	 * @return
	 */
	public Map<String, Object> zjCreateOrderRecord(Object obj, String path, String opType, String ieFlag);
}
