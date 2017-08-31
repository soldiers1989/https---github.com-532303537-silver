package org.silver.sys.api;

import java.util.Map;

public interface EBPentRecordService {
   
   Object findByProperty(Map<String, Object> params, int page, int size);
}
