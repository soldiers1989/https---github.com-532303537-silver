package org.silver.util;

import java.util.Collection;
import java.util.Map;
import net.sf.json.JSONNull;

public class StringEmptyUtils {
	/**
	 * 判断对象是否Empty(null或元素为0)<br>
	 * 实用于对如下对象做判断:String Collection及其子类 Map及其子类
	 * 
	 * @param pObj
	 *            待检查对象 
	 * @return boolean 返回的布尔值
	 */
	public static boolean isEmpty(Object pObj) {
		if (pObj == null) {
			return true;
		} else if (pObj == "") {
			return true;
		} else if (pObj instanceof JSONNull) {
			return true;
		}else if((pObj+"").trim().equals("null")){
			return true;
		}else if (pObj instanceof String) {
			if (((String) pObj).length() == 0) {
				return true;
			}
		} else if (pObj instanceof Collection) {
			if (((Collection) pObj).isEmpty()) {
				return true;
			}
		} else if (pObj instanceof Map) {
			if (((Map) pObj).size() == 0) {
				return true;
			}
		} 
		return false;
	}

	/**
	 * 判断对象是否为NotEmpty(!null或元素>0)<br>
	 * 实用于对如下对象做判断:String Collection及其子类 Map及其子类
	 * 
	 * @param pObj
	 *            待检查对象
	 * @return boolean 返回的布尔值
	 */
	public static boolean isNotEmpty(Object pObj) {
		if (pObj == null) {
			return false;
		} else if (pObj == "") {
			return false;
		} else if (pObj instanceof JSONNull) {
			return false;
		}else if((pObj+"").trim().equals("null")){
			return false;
		}else if (pObj instanceof String) {
			if (((String) pObj).length() == 0) {
				return false;
			}
		} else if (pObj instanceof Collection) {
			if (((Collection) pObj).isEmpty()) {
				return false;
			}
		} else if (pObj instanceof Map) {
			if (((Map) pObj).size() == 0) {
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		String a = "2";
		if(isNotEmpty(a)){
			System.out.println("-----");
		}
				
	}
}
