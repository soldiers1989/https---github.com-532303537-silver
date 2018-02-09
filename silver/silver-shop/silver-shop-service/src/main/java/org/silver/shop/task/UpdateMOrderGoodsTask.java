package org.silver.shop.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.silver.shop.impl.system.cross.PaymentServiceImpl;
import org.silver.shop.impl.system.manual.MorderServiceImpl;

import com.justep.baas.data.Row;

import net.sf.json.JSONArray;

public class UpdateMOrderGoodsTask implements Callable<Object> {
	private List<Row> dataList;//
	private String merchantId;// 商户Id
	private String merchantName; // 商户名称
	private List<Map<String, Object>> errorList;// 错误信息
	private String serialNo;// 批次号
	private int totalCount;//
	private MorderServiceImpl morderServiceImpl;//
	private String goodsRecordHeadSerialNo;// 商品备案流水号

	/**
	 * 推送支付单信息
	 * 
	 * @param dataList
	 *            订单信息
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param errorList
	 *            错误信息
	 * @param customsMap
	 *            海关口岸信息
	 * @param tok
	 * @param totalCount
	 *            总数
	 * @param serialNo
	 *            批次号
	 * @param paymentServiceImpl
	 *            实现类
	 */
	public UpdateMOrderGoodsTask(List<Row> dataList, String merchantId, String merchantName,
			List<Map<String, Object>> errorList, int totalCount, String serialNo, MorderServiceImpl morderServiceImpl,
			String goodsRecordHeadSerialNo) {
		this.dataList = dataList;
		this.merchantId = merchantId;
		this.merchantName = merchantName;
		this.errorList = errorList;
		this.totalCount = totalCount;
		this.serialNo = serialNo;
		this.morderServiceImpl = morderServiceImpl;
		this.goodsRecordHeadSerialNo = goodsRecordHeadSerialNo;
	}

	@Override
	public Object call() throws Exception {
		morderServiceImpl.saveGoodsRecordContent(dataList, merchantId, merchantName, errorList, totalCount, serialNo,goodsRecordHeadSerialNo);
		return null;
	}
	
	
}
