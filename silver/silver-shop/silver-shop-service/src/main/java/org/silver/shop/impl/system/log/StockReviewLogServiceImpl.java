package org.silver.shop.impl.system.log;

import java.util.HashMap;
import java.util.Map;

import org.silver.shop.api.system.log.StockReviewLogService;
import org.silver.shop.dao.system.log.StockReviewLogDao;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = StockReviewLogService.class)
public class StockReviewLogServiceImpl implements StockReviewLogService{

	@Autowired
	private StockReviewLogDao stockReviewLogDao;
	
	@Override
	public Map<String, Object> getLog(String merchantId, String entGoodsNo) {
		if(StringEmptyUtils.isEmpty(merchantId) ||StringEmptyUtils.isEmpty(entGoodsNo)){
			return ReturnInfoUtils.errorInfo("请求参数不能为空！");
		}
		Map<String,Object> params = new HashMap<>();
		return null;
	}
	
}
