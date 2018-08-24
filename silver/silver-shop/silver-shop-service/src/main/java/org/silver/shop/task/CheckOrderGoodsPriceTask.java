package org.silver.shop.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.dao.system.manual.MorderDao;
import org.silver.shop.impl.system.manual.MpayServiceImpl;
import org.silver.shop.model.common.base.IdCard;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.util.DoubleOperationUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SplitListUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.common.status.Status;

import net.sf.json.JSONObject;

/**
 * 校验订单商品金额与是否上架
 */
public class CheckOrderGoodsPriceTask implements Callable<Object> {

	private static Logger logger = LogManager.getLogger(Object.class);
	/**
	 * 驼峰命名：商户Id
	 */
	private static final String MERCHANT_ID = "merchantId";
	/**
	 * 下划线命名：订单Id
	 */
	private static final String ORDER_ID = "order_id";

	private List datasList;//
	private List<Object> correctList;// 正确的订单集合
	private MorderDao morderDao;
	private List<Map<String, Object>> errorList;

	public CheckOrderGoodsPriceTask(List<Map<String, Object>> datasList, List<Object> correctList, MorderDao morderDao,
			List<Map<String, Object>> errorList) {
		this.datasList = datasList;
		this.correctList = correctList;
		this.morderDao = morderDao;
		this.errorList = errorList;
	}

	@Override
	public Object call() {
		try {
			System.out.println("--开始检查商品---");
			Map<String, Object> params = new HashMap<>();
			for (int i = 0; i < datasList.size(); i++) {
				Map<String, Object> map = (Map<String, Object>) datasList.get(i);
				params.clear();
				params.put(ORDER_ID, map.get("orderNo"));
				// 查询订单商品信息
				List<MorderSub> reGoodsList = morderDao.findByProperty(MorderSub.class, params, 0, 0);
				// 通过标识、默认为true
				boolean flag = true;
				if (reGoodsList != null && !reGoodsList.isEmpty()) {
					for (int y = 0; y < reGoodsList.size(); y++) {
						MorderSub goods = reGoodsList.get(y);
						Map<String, Object> reMap = checkGoods(goods);
						if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
							// 将通过标识改为false、不添加失败的订单id
							flag = false;
							errorList.add(reMap);
						}
					}
					if (flag) {// 当全部通过后、添加校验通过的信息
						correctList.add(map);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 校验商品价格是否符合要求
	 * 
	 * @param goods
	 *            商品信息
	 * @return Map
	 */
	private Map<String, Object> checkGoods(MorderSub goods) {
		goods.getSpareParams();
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		blurryMap.put("entGoodsNo", goods.getEntGoodsNo());
		// 删除标识:0-未删除,1-已删除
		params.put("deleteFlag", 0);
		params.put(MERCHANT_ID, goods.getMerchant_no());
		// 根据商品信息拼接商品自编号与商家代码，查询库存表
		System.out.println("--查询库存--");
		List<StockContent> reStockList = morderDao.findByPropertyLike(StockContent.class, params, blurryMap, 0, 0);
		if (reStockList == null) {
			return ReturnInfoUtils
					.errorInfo("订单号[" + goods.getOrder_id() + "]中商品编号[" + goods.getEntGoodsNo() + "]查询失败，服务器繁忙！");
		} else if (reStockList.isEmpty()) {
			return ReturnInfoUtils
					.errorInfo("订单号[" + goods.getOrder_id() + "]中商品编号[" + goods.getEntGoodsNo() + "]对应的商品未入库！");
		} else {
			return checkStock(goods,reStockList);
		}
	}

	/**
	 * 校验库存商品是否已在商城上架、并且商品单价不低于售卖价的50%
	 * @param goods 订单商品信息
	 * @param reStockList 库存商品信息
	 * @return Map
	 */
	private Map<String, Object> checkStock(MorderSub goods, List<StockContent> reStockList) {
		if(goods == null || reStockList == null){
			return ReturnInfoUtils.errorInfo("校验商品库存信息参数错误！");
		}
		Map<String, Object> item = new HashMap<>();
		for (int i = 0; i < reStockList.size(); i++) {
			StockContent stock = reStockList.get(i);
			// 上/下架标识：1-上架,2-下架,3-审核中,4-审核不通过
			if (stock.getSellFlag() == 1) {
				// regPrice
				double price = stock.getRegPrice();
				// 订单商品价格 (/)除已 上架价格 结果 小于 65%则算不通过、2018-08-23暂定50%
				if (DoubleOperationUtil.div(goods.getPrice(), price, 2) < 0.5) {//第二级报错信息
					item.put(goods.getEntGoodsNo() + "_2", "订单号[" + goods.getOrder_id() + "]中商品编号["
							+ goods.getEntGoodsNo() + "]商品单价大幅度低于售卖价，不允许申报！");
				} else {
					item.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
					break;
				}
			} else {//第三级报错信息
				item.put(goods.getEntGoodsNo() + "_3",
						"订单号[" + goods.getOrder_id() + "]中商品编号[" + goods.getEntGoodsNo() + "]对应的商品未上架！");
			}
		}
		//
		if (!"1".equals(item.get(BaseCode.STATUS.toString()))) {
			if (StringEmptyUtils.isNotEmpty(item.get(goods.getEntGoodsNo() + "_2"))) {
				return ReturnInfoUtils.errorInfo(item.get(goods.getEntGoodsNo() + "_2")+"");
			} else {
				return ReturnInfoUtils.errorInfo(item.get(goods.getEntGoodsNo()+ "_3")+"") ;
			}
		} else {
			return ReturnInfoUtils.successInfo();
		}
	}

	public static void main(String[] args) {
		double shang = 80.14;
		double dingdan = 78;
		System.out.println("-->>>" + DoubleOperationUtil.div(dingdan, shang, 2));
		if (DoubleOperationUtil.div(dingdan, shang, 2) < 0.85) {

		}

		JSONObject json = JSONObject.fromObject("");
		String entGoodsNo = null;
		String marCode = json.get("marCode") + "";
	}
}
