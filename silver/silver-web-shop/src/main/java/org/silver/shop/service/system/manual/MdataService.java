package org.silver.shop.service.system.manual;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.manual.MpayService;
import org.silver.shop.api.system.manual.MuserService;
import org.silver.shop.model.common.base.Province;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.DateUtil;
import org.silver.util.ExcelUtil;
import org.silver.util.FileUpLoadService;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
import org.silver.util.SplitListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONArray;

import net.sf.json.JSONObject;

@Service("mdataService")
public class MdataService {

	@Reference
	private MuserService muserService;
	@Reference
	private MpayService mpayService;
	@Autowired
	private FileUpLoadService fileUpLoadService;

	public Map<String, Object> addEntity(String merchant_no, String[] strs) {
		return muserService.addEntity(merchant_no, strs);

	}

	public Map<String, Object> loadMuserDatas(String merchant_no, int page, int size) {

		return muserService.loadMuserDatas(merchant_no, page, size);
	}

	public Map<String, Object> delMubySysno(String merchant_no, String muser_sys_no) {

		return muserService.delMubySysno(merchant_no, muser_sys_no);
	}

	public Map<String, Object> groupAddmu(String merchant_no, HttpServletRequest req) {
		Map<String, Object> reqMap = fileUpLoadService.universalDoUpload(req, "/gadd-excel/", ".xls", false, 400, 400,
				null);
		List<Map<String, Object>> errl = new ArrayList<Map<String, Object>>();
		if ((int) reqMap.get("status") == 1) {
			List<String> list = (List<String>) reqMap.get("datas");
			File f = new File("/gadd-excel/" + list.get(0));
			ExcelUtil excel = new ExcelUtil();
			excel.open(f);
			readMuserSheet(0, excel, errl, merchant_no);// 读取订单工作表
			excel.closeExcel();
			// excel.getFile().delete();
			f.delete();
			reqMap.clear();
			reqMap.put("status", 1);
			reqMap.put("msg", "导入完成");
			reqMap.put("err", errl);
			return reqMap;
		}
		return null;
	}

	private Map<String, Object> readMuserSheet(int sheet, ExcelUtil excel, List<Map<String, Object>> errl,
			String merchant_no) {
		Map<String, Object> statusMap = new HashMap<>();
		String[] strs = new String[15];
		for (int r = 1; r <= excel.getRowCount(sheet); r++) {
			if (excel.getColumnCount(r) == 0) {
				break;
			}
			for (int c = 0; c < excel.getColumnCount(r); c++) {
				String value = excel.getCell(sheet, r, c);
				if (c == 0 && "".equals(value)) {
					statusMap.put("status", 1);
					statusMap.put("msg", "导入完成");
					statusMap.put("err", errl);
					return statusMap;
				}
				if (c <= 7) {
					strs[c] = value;
				} else {
					break;
				}

			}
			Map<String, Object> item = muserService.addEntity(merchant_no, strs);
			if ((int) item.get("status") != 1) {
				Map<String, Object> errMap = new HashMap<String, Object>();
				errMap.put("msg", "【人员录入工作表】第" + (r + 1) + "行-->" + item.get("msg"));
				errl.add(errMap);
			}
		}
		statusMap.put("status", 1);
		statusMap.put("msg", "导入完成");
		statusMap.put("err", errl);
		return statusMap;

	}

	public Object sendMorderRecord(Map<String, Object> customsMap, String orderNoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		String proxyParentId = merchantInfo.getAgentParentId();
		String proxyParentName = merchantInfo.getAgentParentName();
		return mpayService.sendMorderRecord(merchantId, customsMap, orderNoPack, proxyParentId, merchantName,
				proxyParentName);
	}

	// 更新订单信息
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap) {
		return mpayService.updateOrderRecordInfo(datasMap);
	}

	// 根据订单日期与批次号下载订单信息
	public Map<String, Object> downOrderExcelByDateSerialNo(HttpServletRequest req, String date, String serialNo) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		String filePath = req.getSession().getServletContext().getRealPath("/") + "WEB-INF/" + date.trim() + "_"
				+ serialNo + ".xls";
		Map<String, Object> reOrderMap = mpayService.downOrderExcelByDateSerialNo(merchantId, merchantName, filePath,
				date, serialNo);
		if (!"1".equals(reOrderMap.get(BaseCode.STATUS.toString()))) {
			return reOrderMap;
		}
		JSONArray jarr = JSONArray.parseArray(reOrderMap.get(BaseCode.DATAS.toString()) + "");
		return doWrite(jarr, filePath);
	}

	/**
	 * 按照国宗订单模板生成对应的excel模板
	 * 
	 * @param arr
	 * @param filePath
	 * @return
	 */
	private Map<String, Object> doWrite(JSONArray arr, String filePath) {
		Map<String, Object> statusMap = new HashMap<>();
		if (arr != null && !arr.isEmpty()) {
			File f = new File(filePath);
			ExcelUtil excel = new ExcelUtil(f);
			excel.open();
			excel.writCell(0, 0, 0, "序号*");
			excel.writCell(0, 0, 1, "订单编号*");
			excel.writCell(0, 0, 2, "订单日期*");
			excel.writCell(0, 0, 3, "进出口日期*");
			excel.writCell(0, 0, 4, "订单运费*");
			excel.writCell(0, 0, 5, "收件人所在国家*");
			excel.writCell(0, 0, 6, "收件人所在省*");
			excel.writCell(0, 0, 7, "收件人所在市*");
			excel.writCell(0, 0, 8, "收件人所在区");
			excel.writCell(0, 0, 9, "收件人详细地址*");
			excel.writCell(0, 0, 10, "收件人姓名*");
			excel.writCell(0, 0, 11, "收件人电话*");
			excel.writCell(0, 0, 12, "发货人*");
			excel.writCell(0, 0, 13, "发货人所在国家*");
			excel.writCell(0, 0, 14, "发货人所在省");
			excel.writCell(0, 0, 15, "发货人所在市*");
			excel.writCell(0, 0, 16, "发货人所在区");
			excel.writCell(0, 0, 17, "发货人地址*");
			excel.writCell(0, 0, 18, "发货人电话*");
			excel.writCell(0, 0, 19, "订单人姓名*");
			excel.writCell(0, 0, 20, "订单人证件类型*");
			excel.writCell(0, 0, 21, "订单人证件号码*");
			excel.writCell(0, 0, 22, "订单人注册号*");
			excel.writCell(0, 0, 23, "订单人电话*");
			excel.writCell(0, 0, 24, "订单人所在国家（地区）代码*");
			excel.writCell(0, 0, 25, "订单人所在城市名称");
			excel.writCell(0, 0, 26, "运输方式*");
			excel.writCell(0, 0, 27, "运输工具名称*");
			excel.writCell(0, 0, 28, "运输工具代码");
			excel.writCell(0, 0, 29, "航班航次编号*");
			excel.writCell(0, 0, 30, "舱单号*");
			excel.writCell(0, 0, 31, "启运国*");
			excel.writCell(0, 0, 32, "启运港");
			excel.writCell(0, 0, 33, "集装箱号");
			excel.writCell(0, 0, 34, "集装箱尺寸");
			excel.writCell(0, 0, 35, "集装箱类型");
			excel.writCell(0, 0, 36, "是否转关*");
			excel.writCell(0, 0, 37, "支付企业代码*");
			excel.writCell(0, 0, 38, "支付企业名称*");
			excel.writCell(0, 0, 39, "支付流水号*");
			excel.writCell(0, 0, 40, "电子订单状态*");
			excel.writCell(0, 0, 41, "支付状态*");
			excel.writCell(0, 0, 42, "其他费用*");
			excel.writCell(0, 0, 43, "支付交易类型");
			excel.writCell(0, 0, 44, "出仓进境日期*");
			excel.writCell(0, 0, 45, "货物存放地");
			excel.writCell(0, 0, 46, "路由状态");
			excel.writCell(0, 0, 47, "电子运单状态*");
			excel.writCell(0, 0, 48, "运单二维码编号");
			excel.writCell(0, 0, 49, "备注");
			excel.writCell(0, 0, 50, "物流订单号");
			excel.writCell(0, 0, 51, "运单号*");
			excel.writCell(0, 0, 52, "进/出境口岸*");
			excel.writCell(0, 0, 53, "快递公司*");
			excel.writCell(0, 0, 54, "商品货号*");
			excel.writCell(0, 0, 55, "品牌");
			excel.writCell(0, 0, 56, "商品信息*");
			excel.writCell(0, 0, 57, "商品海关备案号*");
			excel.writCell(0, 0, 58, "商检备案号*");
			excel.writCell(0, 0, 59, "规格型号*");
			excel.writCell(0, 0, 60, "原产国*");
			excel.writCell(0, 0, 61, "包装种类*");
			excel.writCell(0, 0, 62, "计量单位*");
			excel.writCell(0, 0, 63, "申报数量*");
			excel.writCell(0, 0, 64, "净重*");
			excel.writCell(0, 0, 65, "毛重*");
			excel.writCell(0, 0, 66, "件数*");
			excel.writCell(0, 0, 67, "商品单价*");
			excel.writCell(0, 0, 68, "商品总价*");
			excel.writCell(0, 0, 69, "商品批次号*");
			excel.writCell(0, 0, 70, "抵付金额*");
			excel.writCell(0, 0, 71, "抵付说明");
			excel.writCell(0, 0, 72, "ERP订单号");
			excel.writCell(0, 0, 73, "ERP单价");
			excel.writCell(0, 0, 74, "ERP总价");
			excel.writCell(0, 0, 75, "ERP商品名称");
			excel.writCell(0, 0, 76, "第一数量*");
			excel.writCell(0, 0, 77, "第二数量*");
			excel.writCell(0, 0, 78, "HS编码");
			excel.writCell(0, 0, 79, "行邮税号");

			for (int i = 0; i < arr.size(); i++) {
				String order_Id, Fcode, RecipientName, RecipientAddr, RecipientID, RecipientTel, RecipientProvincesCode,
						RecipientCityCode, RecipientAreaCode, OrderDocAcount, OrderDocName, OrderDocType, OrderDocId,
						OrderDocTel, OrderDate, trade_no, dateSign, waybill, create_date, senderName, senderCountry,
						senderAreaCode, senderAddress, senderTel, postal, RecipientProvincesName, RecipientCityName,
						RecipientAreaName, EntGoodsNo, HSCode, GoodsName, CusGoodsNo, CIQGoodsNo, OriginCountry,
						GoodsStyle, BarCode, Brand, Unit, stdUnit, secUnit, transportModel, exit_date, customsCode;
				int Qty = 0;
				double Price = 0.0;
				double Total = 0.0;
				double firstLegalCount = 0.0;
				double secLegalCount = 0.0;
				// Row rowIndex = lr.get(i);
				JSONObject rowIndex = JSONObject.fromObject(arr.get(i));
				rowIndex.getString("senderAddress").substring(10, rowIndex.getString("senderAddress").length() - 2);

				order_Id = rowIndex.getString("order_id") + "";
				order_Id = order_Id.substring(10, order_Id.length() - 2);

				create_date = rowIndex.getString("OrderDate").replace("{\"value\":\"", "").replace("\"}", "");
				create_date = create_date.replace("T", " ");
				create_date = DateUtil.toStringDate(create_date);
				RecipientProvincesName = rowIndex.getString("RecipientProvincesName") + "";
				RecipientProvincesName = RecipientProvincesName.substring(10, RecipientProvincesName.length() - 2);
				RecipientCityName = rowIndex.getString("RecipientCityName") + "";
				RecipientCityName = RecipientCityName.substring(10, RecipientCityName.length() - 2);
				RecipientAreaName = rowIndex.getString("RecipientAreaName") + "";
				RecipientAreaName = RecipientAreaName.substring(10, RecipientAreaName.length() - 2);
				RecipientAddr = rowIndex.getString("RecipientAddr") + "";
				RecipientAddr = RecipientAddr.substring(10, RecipientAddr.length() - 2);
				RecipientName = rowIndex.getString("RecipientName") + "";
				RecipientName = RecipientName.substring(10, RecipientName.length() - 2);
				RecipientTel = rowIndex.getString("RecipientTel") + "";
				RecipientTel = RecipientTel.substring(10, RecipientTel.length() - 2);
				senderName = rowIndex.getString("senderName") + "";
				senderName = senderName.substring(10, senderName.length() - 2);
				senderCountry = rowIndex.getString("senderCountry") + "";
				senderCountry = senderCountry.substring(10, senderCountry.length() - 2);
				senderAreaCode = rowIndex.getString("senderAreaCode") + "";
				senderAreaCode = senderAreaCode.substring(10, senderAreaCode.length() - 2);
				senderAddress = rowIndex.getString("senderAddress") + "";
				senderAddress = senderAddress.substring(10, senderAddress.length() - 2);
				senderTel = rowIndex.getString("senderTel") + "";
				senderTel = senderTel.substring(10, senderTel.length() - 2);
				OrderDocName = rowIndex.getString("OrderDocName") + "";
				OrderDocName = OrderDocName.substring(10, OrderDocName.length() - 2);
				OrderDocType = rowIndex.getString("OrderDocType") + "";
				OrderDocType = OrderDocType.substring(10, OrderDocType.length() - 2);
				OrderDocId = rowIndex.getString("OrderDocId") + "";
				OrderDocId = OrderDocId.substring(10, OrderDocId.length() - 2);
				OrderDocTel = rowIndex.getString("OrderDocTel") + "";
				OrderDocTel = OrderDocTel.substring(10, OrderDocTel.length() - 2);
				RecipientCityName = rowIndex.getString("RecipientCityName") + "";
				RecipientCityName = RecipientCityName.substring(10, RecipientCityName.length() - 2);
				trade_no = rowIndex.getString("trade_no").replace("{\"value\":\"", "").replace("\"}", "");
				waybill = rowIndex.getString("waybill") + "";
				waybill = waybill.substring(10, waybill.length() - 2);
				EntGoodsNo = rowIndex.getString("EntGoodsNo") + "";
				EntGoodsNo = EntGoodsNo.substring(10, EntGoodsNo.length() - 2);
				Brand = rowIndex.getString("Brand") + "";
				Brand = Brand.substring(10, Brand.length() - 2);
				GoodsName = rowIndex.getString("GoodsName") + "";
				GoodsName = GoodsName.substring(10, GoodsName.length() - 2);
				CusGoodsNo = rowIndex.getString("CusGoodsNo") + "";
				CusGoodsNo = CusGoodsNo.substring(10, CusGoodsNo.length() - 2);
				CIQGoodsNo = rowIndex.getString("CIQGoodsNo") + "";
				CIQGoodsNo = CIQGoodsNo.substring(10, CIQGoodsNo.length() - 2);
				GoodsStyle = rowIndex.getString("GoodsStyle") + "";
				GoodsStyle = GoodsStyle.substring(10, GoodsStyle.length() - 2);
				OriginCountry = rowIndex.getString("OriginCountry") + "";
				OriginCountry = OriginCountry.substring(10, OriginCountry.length() - 2);
				Unit = rowIndex.getString("Unit").replace("{\"value\":\"", "").replace("\"}", "");
				// 数量
				String strQty = rowIndex.getString("Qty").replace("{\"value\":", "").replace("}", "");
				Qty = Integer.parseInt(strQty);
				String strNetWt = rowIndex.getString("netWt").replace("{\"value\":", "").replace("}", "");
				// netWt = Double.parseDouble(strNetWt);
				String strGrossWt = rowIndex.getString("grossWt").replace("{\"value\":", "").replace("}", "");
				// grossWt = Double.parseDouble(strGrossWt);
				String strPrice = rowIndex.getString("Price").replace("{\"value\":", "").replace("}", "");
				Price = Double.parseDouble(strPrice);
				String strTotal = rowIndex.getString("Total").replace("{\"value\":", "").replace("}", "");
				Total = Double.parseDouble(strTotal);
				String strFirstLegalCount = rowIndex.getString("firstLegalCount").replace("{\"value\":", "")
						.replace("}", "");
				firstLegalCount = Double.parseDouble(strFirstLegalCount);
				String strSecondLegalCount = rowIndex.getString("secondLegalCount").replace("{\"value\":", "")
						.replace("}", "");
				secLegalCount = Double.parseDouble(strSecondLegalCount);

				HSCode = rowIndex.getString("HSCode").replace("{\"value\":\"", "").replace("\"}", "");
				//海关编码
				customsCode = rowIndex.getString("customsCode").replace("{\"value\":\"", "").replace("\"}", "");
				
				//
				stdUnit = rowIndex.getString("stdUnit").replace("{\"value\":\"", "").replace("\"}", "");
				
				for (int c = 0; c < 81; c++) {
					if (c == 0) {
						excel.writCell(0, i + 1, c, i + 1);
					} else if (c == 1) {
						// 订单Id
						excel.writCell(0, i + 1, c, order_Id);
					} else if (c == 2) {
						// 下单日期
						excel.writCell(0, i + 1, c, create_date);
					} else if (c == 3) {
						excel.writCell(0, i + 1, c, create_date);
					} else if (c == 4) {
						// 运费
						excel.writCell(0, i + 1, c, "0.00");
					} else if (c == 5) {
						// 收件人所在国家
						excel.writCell(0, i + 1, c, "142");
					} else if (c == 6) {
						excel.writCell(0, i + 1, c, RecipientProvincesName);
					} else if (c == 7) {
						excel.writCell(0, i + 1, c, RecipientCityName);
					} else if (c == 8) {
						excel.writCell(0, i + 1, c, RecipientAreaName);
					} else if (c == 9) {
						excel.writCell(0, i + 1, c, RecipientAddr);
					} else if (c == 10) {
						excel.writCell(0, i + 1, c, RecipientName);
					} else if (c == 11) {
						excel.writCell(0, i + 1, c, RecipientTel);
					} else if (c == 12) {
						excel.writCell(0, i + 1, c, senderName);
					} else if (c == 13) {
						// 发货人所在国家
						excel.writCell(0, i + 1, c, senderCountry);
					} else if (c == 15) {
						// 发货人所在市,2018-02-05客户要求填写本发货人国家相同编码
						// excel.writCell(0, i + 1, c, senderAreaCode);
						excel.writCell(0, i + 1, c, senderCountry);
					} else if (c == 17) {
						excel.writCell(0, i + 1, c, senderAddress);
					} else if (c == 18) {
						excel.writCell(0, i + 1, c, senderTel);
					} else if (c == 19) {
						excel.writCell(0, i + 1, c, OrderDocName);
					} else if (c == 20) {
						excel.writCell(0, i + 1, c, OrderDocType);
					} else if (c == 21) {
						excel.writCell(0, i + 1, c, OrderDocId);
					} else if (c == 22) {
						excel.writCell(0, i + 1, c, OrderDocName);
					} else if (c == 23) {
						excel.writCell(0, i + 1, c, OrderDocTel);
					} else if (c == 24) {
						// 订单人所在国家（地区）代码
						excel.writCell(0, i + 1, c, "142");
					} else if (c == 25) {
						excel.writCell(0, i + 1, c, RecipientCityName);
					} else if (c == 31) {
						excel.writCell(0, i + 1, c, senderCountry);
					} else if (c == 32) {
						excel.writCell(0, i + 1, c, senderCountry);
					} else if (c == 36) {
						// 是否转关
						excel.writCell(0, i + 1, c, "N");
					} else if (c == 37) {
						// 支付企业代码
						excel.writCell(0, i + 1, c, "C000010000803304");
					} else if (c == 38) {
						excel.writCell(0, i + 1, c, "银盛支付服务股份有限公司");
					} else if (c == 39) {
						excel.writCell(0, i + 1, c, trade_no);
					} else if (c == 40) {
						// 电子订单状态
						excel.writCell(0, i + 1, c, "1");
					} else if (c == 41) {
						// 支付状态
						excel.writCell(0, i + 1, c, "0");
					} else if (c == 42) {
						// 其他费用
						excel.writCell(0, i + 1, c, "0");
					} else if (c == 43) {
						// 支付交易类型
						excel.writCell(0, i + 1, c, "M");
					} else if (c == 44) {
						// 出仓进境日期
						excel.writCell(0, i + 1, c, create_date);
					} else if (c == 45) {
						// 货物存放地
						excel.writCell(0, i + 1, c, "国外直邮");
					} else if (c == 47) {
						// 电子运单状态
						excel.writCell(0, i + 1, c, "A");
					} else if (c == 50) {
						// 物流订单号
						excel.writCell(0, i + 1, c, order_Id);
					} else if (c == 51) {
						excel.writCell(0, i + 1, c, waybill);
					} else if (c == 52) {
						//海关编码
						excel.writCell(0, i + 1, c, customsCode);
					} else if (c == 53) {
						// 快递公司
						excel.writCell(0, i + 1, c, "邮政快递");
					} else if (c == 54) {
						excel.writCell(0, i + 1, c, EntGoodsNo);
					} else if (c == 55) {
						// 品牌
						excel.writCell(0, i + 1, c, Brand);
					} else if (c == 56) {
						// 商品信息
						excel.writCell(0, i + 1, c, GoodsName);
					} else if (c == 57) {
						// 海关备案号=HS编码
						excel.writCell(0, i + 1, c, HSCode);
					} else if (c == 58) {
						// 商检备案号
						excel.writCell(0, i + 1, c, CIQGoodsNo);
					} else if (c == 59) {
						excel.writCell(0, i + 1, c, GoodsStyle);
					} else if (c == 60) {
						excel.writCell(0, i + 1, c, OriginCountry);
					} else if (c == 61) {
						// 包装种类
						excel.writCell(0, i + 1, c, "2");
					} else if (c == 62) {
						excel.writCell(0, i + 1, c, Unit);
					} else if (c == 63) {
						excel.writCell(0, i + 1, c, Qty);
					} else if (c == 64) {
						// 净重
						excel.writCell(0, i + 1, c, strNetWt);
					} else if (c == 65) {
						// 毛重
						excel.writCell(0, i + 1, c, strGrossWt);
					} else if (c == 66) {
						// 件数
						excel.writCell(0, i + 1, c, "1");
					} else if (c == 67) {
						excel.writCell(0, i + 1, c, Price);
					} else if (c == 68) {
						excel.writCell(0, i + 1, c, Total);
					} else if (c == 69) {
						// 商品批次号
						excel.writCell(0, i + 1, c, "123456");
					} else if (c == 70) {
						// 抵付金额
						excel.writCell(0, i + 1, c, "0.0");
					} else if (c == 76) {
						// 第一法定数
						excel.writCell(0, i + 1, c, firstLegalCount);
					} else if (c == 77) {
						// 第二法定数
						excel.writCell(0, i + 1, c, secLegalCount);
					} else if (c == 78) {
						// HS编码
						excel.writCell(0, i + 1, c, HSCode);
					} else if (c == 79) {
						// 行邮税号
						excel.writCell(0, i + 1, c, "27000000");
					}
					
					//5157-顺德陈村港澳货柜车检查场
					if("5157".equals(customsCode)){
						excel.writCell(0, i + 1, 80, stdUnit);
						//excel.writCell(0, i + 1, 81, customsCode);
					}
				}
			}
			excel.closeExcel();
			excel.save();
			statusMap.put("status", StatusCode.SUCCESS.getStatus());
			statusMap.put("filePath", filePath);
			return statusMap;
		}
		return null;
	}

	// 读取缓存中excel导入实时数据
	public Map<String, Object> readExcelRedisInfo(String serialNo, String name) {
		Map<String, Object> statusMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo;
		// String key = "Shop_Key_ExcelIng_"+dateSign+"_"+serialNo;
		byte[] redisByte = JedisUtil.get(key.getBytes(), 3600);
		if (redisByte != null && redisByte.length > 0) {
			return (Map<String, Object>) SerializeUtil.toObject(redisByte);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "暂无数据,请等待!");
			return statusMap;
		}
	}

	public static void main(String[] args) {
		DecimalFormat df = new DecimalFormat("0.00");
		double d = 1.043352;
		System.out.println(df.format(d));
	}
}
