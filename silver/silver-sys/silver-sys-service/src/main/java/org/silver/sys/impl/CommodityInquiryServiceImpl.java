package org.silver.sys.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.silver.sys.api.CommodityInquiryService;
import org.silver.sys.api.EBPentRecordService;
import org.silver.sys.component.ChooseDatasourceHandler;
import org.silver.sys.dao.GoodsInfoDao;
import org.silver.sys.dao.GoodsRecordDao;
import org.silver.sys.dao.OrderGoodsDao;
import org.silver.sys.dao.OrderHeadDao;
import org.silver.sys.dao.OrderRecordDao;
import org.silver.sys.dao.SessionFactory;
import org.silver.sys.model.goods.GoodsInfo;
import org.silver.sys.model.goods.GoodsRecord;
import org.silver.sys.model.order.OrderGoods;
import org.silver.sys.model.order.OrderHead;
import org.silver.sys.model.order.OrderRecord;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONObject;

@Service(interfaceClass=CommodityInquiryService.class)
public class CommodityInquiryServiceImpl implements CommodityInquiryService {

	@Resource
	private GoodsRecordDao goodsRecordDao =new GoodsRecordDao();
	@Resource
	private GoodsInfoDao goodsInfoDao= new GoodsInfoDao();
	@Resource
	private OrderHeadDao orderHeadDao= new OrderHeadDao();
	@Resource
	private OrderRecordDao orderRecordDao= new OrderRecordDao() ;
	@Resource
	private OrderGoodsDao orderGoodsDao= new OrderGoodsDao();
	
	@Override
	public Map<String, Object> findAllRecordsByAppkey(String tenantNo, int type,int page,int size) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> datas = new HashMap<>();
		Map<String, Object> datas1 = new HashMap<>();
		List list =new ArrayList<>();
		List orderList =new ArrayList<>();
		long count =0;
		String orgMessageID,entOrderNo ="";
		switch (type) {
		case 0://商品备案查询
			params.put("tenantNo", tenantNo);
			params.put("del_flag", 0);
			ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
			count =goodsRecordDao.findByPropertyCount(params);
			ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
			List<GoodsRecord> goodsList=goodsRecordDao.findByProperty(params, page, size);
			if(null!=goodsList&&goodsList.size()>0){
				for (int i = 0; i < goodsList.size(); i++) {
					params.clear();
					orgMessageID=goodsList.get(i).getOrgMessageID();
					params.put("OrgMessageID", orgMessageID);
					ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
					List<GoodsInfo> goodsInfoList=goodsInfoDao.findByProperty(params, 0, 0);
					if(null!=goodsInfoList&&goodsInfoList.size()>0){
						datas.put("head", goodsList.get(i));
						datas.put("content", goodsInfoList);
						list.add(datas);
					}
				}
				resultMap.put("status", 1);
				resultMap.put("msg", "有相关记录！");
				resultMap.put("count", count);
				resultMap.put("datas", list);
				return resultMap;
			}else{
				resultMap.put("status", -1);
				resultMap.put("msg", "没有查到相关记录！");
				return resultMap;
			}
		case 1://订单备案查询
			params.put("tenantNo", tenantNo);
			params.put("del_flag", 0);
			ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
			count =orderHeadDao.findByPropertyCount(params);
			System.out.println(count);
			ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
			List<OrderHead> orderHeadList =orderHeadDao.findByProperty(params, page, size);
			if(null!=orderHeadList&&orderHeadList.size()>0){
				for(int i = 0; i < orderHeadList.size(); i++){//根据报文编号查订单
					params.clear();
					orgMessageID=orderHeadList.get(i).getOrgMessageID();
					params.put("OrgMessageID", orgMessageID);
					ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
					List<OrderRecord> orderRecordList =orderRecordDao.findByProperty(params, 0, 0);
					if(null!=orderRecordList&&orderRecordList.size()>0){
						for (int j = 0; j < orderRecordList.size(); j++) {//根据订单编号查商品
							params.clear();
							entOrderNo=orderRecordList.get(j).getEntOrderNo();
							params.put("EntOrderNo", entOrderNo);
							ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
							List<OrderGoods> orderGoodsList=orderGoodsDao.findByProperty(params, 0, 0);
							if(orderGoodsList!=null&&orderGoodsList.size()>0){//将订单头和订单商品加入orderList
								datas1.put("head", orderRecordList.get(j));
								datas1.put("content", orderGoodsList);
								orderList.add(datas1);
							}
						}
					}
					//将报文头和orderList，加入list 这样就是一条完整的记录
					datas.put("head", orderHeadList.get(i));
					datas.put("content", orderList);
					list.add(datas);
				}
				resultMap.put("status", 1);
				resultMap.put("msg", "有相关记录！");
				resultMap.put("count", count);
				resultMap.put("datas", list);
				return resultMap;
			}else{
				resultMap.put("status", -1);
				resultMap.put("msg", "没有查到相关记录！");
				return resultMap;
			}
		}
		return null;
	}
	
	
	
	

	public static void main(String[] args) {
		ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
//		Map<String, Object> params = new HashMap<>();
//		params.put("tenantNo", "YM20170000015078659178651922");
//		params.put("del_flag", 0);
//		List<GoodsRecord> goodsList=goodsRecordDao.findByProperty(params, 0, 0);
//		System.out.println(goodsList);
		
		
		CommodityInquiryServiceImpl cs= new CommodityInquiryServiceImpl();
		System.out.println(JSONObject.fromObject(cs.findAllRecordsByAppkey("YM20170000015078659178651922", 1, 1, 1)));
	}
}
