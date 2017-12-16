package org.silver.shop.api.system.manual;

import java.util.Map;

public interface MuserService {

	public Map<String,Object> addEntity(String merchant_no,String[] strs);

	public Map<String, Object> loadMuserDatas(String merchant_no, int page, int size);

	public Map<String, Object> delMubySysno(String merchant_no, String muser_sys_no);
	
}
