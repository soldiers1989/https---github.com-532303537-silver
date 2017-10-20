package org.silver.sys.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.silver.sys.api.CallbackService;
import org.silver.sys.api.EBPentRecordService;
import org.silver.sys.dao.GoodsRecordDao;
import org.silver.sys.dao.OrderHeadDao;
import org.silver.sys.dao.OrderRecordDao;
import org.silver.sys.model.goods.GoodsRecord;
import org.silver.sys.model.order.OrderHead;
import org.silver.sys.util.YmHttpUtil;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONObject;

@Service(interfaceClass=CallbackService.class)
public class CallbackServiceImpl implements CallbackService {

	@Resource
	private GoodsRecordDao goodsRecordDao;
	@Resource
	private OrderHeadDao orderHeadDao;

	
	@Override
	public Map<String, Object> AsynchronousCallback(String messageID, int type, Map<String, Object> resultMap) {
		String url=findURL(messageID,type);
		Map<String , Object> params= new HashMap<String, Object>();
		params.put("datas",JSONObject.fromObject(resultMap).toString());
		System.out.println("params--->"+params);
	     if(url!=null&&"".equals(url.trim())){
			System.out.println("结果====>"+YmHttpUtil.HttpPost(url, params));
		}
		return null;
	}
    //根据报文类型以及报文ID查找到确切的数据，并返回回调URL
	public String findURL(String messageID, int type){
		Map<String, Object> params = new HashMap<>();
		String url="";
		params.put("OrgMessageID", messageID);
		switch (type) {
		case 0:
			List<GoodsRecord> goodsList=goodsRecordDao.findByProperty(params, 0, 0);
			url=goodsList.get(0).getUrl();
			return url;

		case 1:
			List<OrderHead> orderHeadList=orderHeadDao.findByProperty(params, 0, 0);
			url=orderHeadList.get(0).getUrl();
			return url;
		}
		return "";	
	}
}
