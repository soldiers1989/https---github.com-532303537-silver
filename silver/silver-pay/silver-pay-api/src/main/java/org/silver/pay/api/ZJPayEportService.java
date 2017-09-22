package org.silver.pay.api;

import java.util.Map;

public interface ZJPayEportService {

	public Map<String, Object> zjCreatePayRecord(Object obj, String path, String opType);
}
