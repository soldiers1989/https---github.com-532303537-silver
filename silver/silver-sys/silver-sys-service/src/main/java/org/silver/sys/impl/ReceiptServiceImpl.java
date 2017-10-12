package org.silver.sys.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.silver.sys.api.ReceiptService;
import org.silver.sys.dao.GoodsRecordDao;
import org.silver.sys.dao.OrderHeadDao;
import org.silver.sys.model.goods.GoodsRecord;
import org.silver.sys.model.order.OrderHead;
import org.silver.sys.util.YmHttpUtil;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass=ReceiptService.class)
public class ReceiptServiceImpl implements ReceiptService {

	@Resource
	private GoodsRecordDao goodsRecordDao ;
	@Resource
	private OrderHeadDao orderHeadDao ;
	
	@Override
	public Map<String, Object> createAccessToken(String messageID,String orgMessageType) {
		Map<String,Object>  reqMap = new HashMap<String,Object>();
		Map<String, Object> params = new HashMap<>();
		GoodsRecord goodsRecord=null;
		OrderHead orderHead=null;
		String url="";
		if("KJ881101".equals(orgMessageType)){//商品备案回执
			params.put("OrgMessageID", messageID);
			params.put("del_flag", 1);
			List<GoodsRecord> listGoods=goodsRecordDao.findByProperty(params, 1, 1);
			if(null!=listGoods&&listGoods.size()>0){
				goodsRecord=listGoods.get(0);
				url=goodsRecord.getUrl();
				reqMap.put("type", 0);
				YmHttpUtil.HttpGet(url, reqMap);//
			}
		}else if("KJ881111".equals(orgMessageType)){//订单回执
			params.put("OrgMessageID", messageID);
			params.put("del_flag", 1);
			List<OrderHead> orderHeadList=orderHeadDao.findByProperty(params, 1, 1);
			if(null!=orderHeadList&&orderHeadList.size()>0){
				orderHead=orderHeadList.get(0);
				url=orderHead.getUrl();
				reqMap.put("type", 1);
				YmHttpUtil.HttpGet(url, reqMap);//
			}
		}
		return null;
	}

}
