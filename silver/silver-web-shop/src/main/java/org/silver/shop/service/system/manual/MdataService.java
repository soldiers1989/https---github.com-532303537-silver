package org.silver.shop.service.system.manual;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.silver.shop.utils.ExcelUtil;
import org.silver.util.DateUtil;
import org.silver.util.FileUpLoadService;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
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

	public Map<String, Object> groupCreateMpay(List<String> orderIDs) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		return mpayService.groupCreateMpay(merchantId, orderIDs);
	}

	public Object sendMorderRecord(Map<String, Object> recordMap, String orderNoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		String proxyParentId = merchantInfo.getProxyParentId();
		String proxyParentName = merchantInfo.getProxyParentName();
		return mpayService.sendMorderRecord(merchantId, recordMap, orderNoPack, proxyParentId, merchantName,
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
		com.alibaba.fastjson.JSONObject obj = com.alibaba.fastjson.JSONObject.parseObject(JSONObject
				.fromObject(
						mpayService.downOrderExcelByDateSerialNo(merchantId, merchantName, filePath, date, serialNo))
				.get("datas") + "");
		JSONArray jarr = JSONArray.parseArray(obj.get("rows") + "");
		return doWrite(jarr, filePath);
	}

	private Map<String, Object> doWrite(JSONArray arr, String filePath) {
		Map<String, Object> statusMap = new HashMap<>();
		if (arr != null && arr.size() > 0) {
			File f = new File(filePath);
			ExcelUtil excel = new ExcelUtil(f);
			excel.open();
			excel.writCell(0, 1, 0, "序号*");
			excel.writCell(0, 1, 1, "订单编号*");
			excel.writCell(0, 1, 2, "订单日期*");
			excel.writCell(0, 1, 3, "进出口日期*");
			excel.writCell(0, 1, 4, "订单运费*");
			excel.writCell(0, 1, 5, "收件人所在国家*");
			excel.writCell(0, 1, 6, "收件人所在省*");
			excel.writCell(0, 1, 7, "收件人所在市*");
			excel.writCell(0, 1, 8, "收件人所在区");
			excel.writCell(0, 1, 9, "收件人详细地址*");
			excel.writCell(0, 1, 10, "收件人姓名*");
			excel.writCell(0, 1, 11, "收件人电话*");
			excel.writCell(0, 1, 12, "发货人*");
			excel.writCell(0, 1, 13, "发货人所在国家*");
			excel.writCell(0, 1, 14, "发货人所在省");
			excel.writCell(0, 1, 15, "发货人所在市*");
			excel.writCell(0, 1, 16, "发货人所在区");
			excel.writCell(0, 1, 17, "发货人地址*");
			excel.writCell(0, 1, 18, "发货人电话*");
			excel.writCell(0, 1, 19, "订单人姓名*");
			excel.writCell(0, 1, 20, "订单人证件类型*");
			excel.writCell(0, 1, 21, "订单人证件号码*");
			excel.writCell(0, 1, 22, "订单人注册号*");
			excel.writCell(0, 1, 23, "订单人电话*");
			excel.writCell(0, 1, 24, "订单人所在国家（地区）代码*");
			excel.writCell(0, 1, 25, "订单人所在城市名称");
			excel.writCell(0, 1, 26, "运输方式*");
			excel.writCell(0, 1, 27, "运输工具名称*");
			excel.writCell(0, 1, 28, "运输工具代码");
			excel.writCell(0, 1, 29, "航班航次编号*");
			excel.writCell(0, 1, 30, "舱单号*");
			excel.writCell(0, 1, 31, "启运国*");
			excel.writCell(0, 1, 32, "启运港");
			excel.writCell(0, 1, 33, "集装箱号");
			excel.writCell(0, 1, 34, "集装箱尺寸");
			excel.writCell(0, 1, 35, "集装箱类型");
			excel.writCell(0, 1, 36, "是否转关*");
			excel.writCell(0, 1, 37, "支付企业代码*");
			excel.writCell(0, 1, 38, "支付企业名称*");
			excel.writCell(0, 1, 39, "支付流水号*");
			excel.writCell(0, 1, 40, "电子订单状态*");
			excel.writCell(0, 1, 41, "支付状态*");
			excel.writCell(0, 1, 42, "其他费用*");
			excel.writCell(0, 1, 43, "支付交易类型");
			excel.writCell(0, 1, 44, "出仓进境日期*");
			excel.writCell(0, 1, 45, "货物存放地");
			excel.writCell(0, 1, 46, "路由状态");
			excel.writCell(0, 1, 47, "电子运单状态*");
			excel.writCell(0, 1, 48, "运单二维码编号");
			excel.writCell(0, 1, 49, "备注");
			excel.writCell(0, 1, 50, "物流订单号");
			excel.writCell(0, 1, 51, "运单号*");
			excel.writCell(0, 1, 52, "进/出境口岸*");
			excel.writCell(0, 1, 53, "快递公司*");
			excel.writCell(0, 1, 54, "商品货号*");
			excel.writCell(0, 1, 55, "品牌");
			excel.writCell(0, 1, 56, "商品信息*");
			excel.writCell(0, 1, 57, "商品海关备案号*");
			excel.writCell(0, 1, 58, "商检备案号*");
			excel.writCell(0, 1, 59, "规格型号*");
			excel.writCell(0, 1, 60, "原产国*");
			excel.writCell(0, 1, 61, "包装种类*");
			excel.writCell(0, 1, 62, "计量单位*");
			excel.writCell(0, 1, 63, "申报数量*");
			excel.writCell(0, 1, 64, "净重*");
			excel.writCell(0, 1, 65, "毛重*");
			excel.writCell(0, 1, 66, "件数*");
			excel.writCell(0, 1, 67, "商品单价*");
			excel.writCell(0, 1, 68, "商品总价*");
			excel.writCell(0, 1, 69, "商品批次号*");
			excel.writCell(0, 1, 70, "抵付金额*");
			excel.writCell(0, 1, 71, "抵付说明");
			excel.writCell(0, 1, 72, "ERP订单号");
			excel.writCell(0, 1, 73, "ERP单价");
			excel.writCell(0, 1, 74, "ERP总价");
			excel.writCell(0, 1, 75, "ERP商品名称");
			excel.writCell(0, 1, 76, "第一数量*");
			excel.writCell(0, 1, 77, "第二数量*");
			excel.writCell(0, 1, 78, "HS编码");
			excel.writCell(0, 1, 79, "行邮税号");

			for (int i = 0; i < arr.size(); i++) {
				String order_Id, Fcode, RecipientName, RecipientAddr, RecipientID, RecipientTel, RecipientProvincesCode,
						RecipientCityCode, RecipientAreaCode, OrderDocAcount, OrderDocName, OrderDocType, OrderDocId,
						OrderDocTel, OrderDate, trade_no, dateSign, waybill, create_date, senderName, senderCountry,
						senderAreaCode, senderAddress, senderTel, postal, RecipientProvincesName, RecipientCityName,
						RecipientAreaName, EntGoodsNo, HSCode, GoodsName, CusGoodsNo, CIQGoodsNo, OriginCountry,
						GoodsStyle, BarCode, Brand, Unit, stdUnit, secUnit, transportModel, exit_date;
				double FCY = 0.0;
				double Tax = 0.0;
				double ActualAmountPaid = 0.0;
				int serial = 0;
				int Qty = 0;
				double Price = 0.0;
				double Total = 0.0;
				double netWt = 0.0;
				double grossWt = 0.0;
				double firstLegalCount = 0.0;
				double secondLegalCount = 0.0;
				int numOfPackages = 0;
				int packageType = 0;
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
				netWt = Double.parseDouble(strNetWt);
				String strGrossWt = rowIndex.getString("grossWt").replace("{\"value\":", "").replace("}", "");
				grossWt = Double.parseDouble(strGrossWt);
				String strPrice = rowIndex.getString("Price").replace("{\"value\":", "").replace("}", "");
				Price = Double.parseDouble(strPrice);
				String strTotal = rowIndex.getString("Total").replace("{\"value\":", "").replace("}", "");
				Total = Double.parseDouble(strTotal);
				String strFirstLegalCount = rowIndex.getString("firstLegalCount").replace("{\"value\":", "")
						.replace("}", "");
				firstLegalCount = Double.parseDouble(strFirstLegalCount);
				String strSecondLegalCount = rowIndex.getString("secondLegalCount").replace("{\"value\":", "")
						.replace("}", "");
				secondLegalCount = Double.parseDouble(strSecondLegalCount);
				HSCode = rowIndex.getString("HSCode").replace("{\"value\":\"", "").replace("\"}", "");
				for (int c = 0; c < 81; c++) {
					if (c == 0) {
						excel.writCell(0, i + 2, c, i + 1);
					} else if (c == 1) {
						// 订单Id
						excel.writCell(0, i + 2, c, order_Id);
					} else if (c == 2) {
						// 下单日期
						excel.writCell(0, i + 2, c, create_date);
					} else if (c == 3) {
						excel.writCell(0, i + 2, c, create_date);
					} else if (c == 6) {
						excel.writCell(0, i + 2, c, RecipientProvincesName);
					} else if (c == 7) {
						excel.writCell(0, i + 2, c, RecipientCityName);
					} else if (c == 8) {
						excel.writCell(0, i + 2, c, RecipientAreaName);
					} else if (c == 9) {
						excel.writCell(0, i + 2, c, RecipientAddr);
					} else if (c == 10) {
						excel.writCell(0, i + 2, c, RecipientName);
					} else if (c == 11) {
						excel.writCell(0, i + 2, c, RecipientTel);
					} else if (c == 12) {
						excel.writCell(0, i + 2, c, senderName);
					} else if (c == 13) {
						excel.writCell(0, i + 2, c, senderCountry);
					} else if (c == 15) {
						excel.writCell(0, i + 2, c, senderAreaCode);
					} else if (c == 17) {
						excel.writCell(0, i + 2, c, senderAddress);
					} else if (c == 18) {
						excel.writCell(0, i + 2, c, senderTel);
					} else if (c == 19) {
						excel.writCell(0, i + 2, c, OrderDocName);
					} else if (c == 20) {
						excel.writCell(0, i + 2, c, OrderDocType);
					} else if (c == 21) {
						excel.writCell(0, i + 2, c, OrderDocId);
					} else if (c == 22) {
						excel.writCell(0, i + 2, c, OrderDocName);
					} else if (c == 23) {
						excel.writCell(0, i + 2, c, OrderDocTel);
					} else if (c == 25) {
						excel.writCell(0, i + 2, c, RecipientCityName);
					}else if(c == 31){
						excel.writCell(0, i + 2, c, senderCountry);
					}else if(c == 32){
						excel.writCell(0, i + 2, c, senderCountry);
					}			
					else if (c == 37) {
						excel.writCell(0, i + 2, c, "C000010000803304");
					} else if (c == 38) {
						excel.writCell(0, i + 2, c, "银盛支付服务股份有限公司");
					} else if (c == 39) {
						excel.writCell(0, i + 2, c, trade_no);
					} else if (c == 44) {
						// 出仓进境日期
						excel.writCell(0, i + 2, c, create_date);
					} else if (c == 50) {
						// 物流订单号
						excel.writCell(0, i + 2, c, order_Id);
					} else if (c == 51) {
						excel.writCell(0, i + 2, c, waybill);
					} else if (c == 54) {
						excel.writCell(0, i + 2, c, EntGoodsNo);
					} else if (c == 56) {
						// 品牌
						excel.writCell(0, i + 2, c, Brand);
					} else if (c == 55) {
						// 商品信息
						excel.writCell(0, i + 2, c, GoodsName);
					} else if (c == 57) {
						// 海关备案号=HS编码
						excel.writCell(0, i + 2, c, HSCode);
					} else if (c == 58) {
						// 商检备案号
						excel.writCell(0, i + 2, c, CIQGoodsNo);
					} else if (c == 59) {
						excel.writCell(0, i + 2, c, GoodsStyle);
					} else if (c == 60) {
						excel.writCell(0, i + 2, c, OriginCountry);
					} else if (c == 62) {
						excel.writCell(0, i + 2, c, Unit);
					} else if (c == 63) {
						excel.writCell(0, i + 2, c, Qty);
					} else if (c == 64) {
						excel.writCell(0, i + 2, c, netWt);
					} else if (c == 65) {
						excel.writCell(0, i + 2, c, grossWt);
					} else if (c == 67) {
						excel.writCell(0, i + 2, c, Price);
					} else if (c == 68) {
						excel.writCell(0, i + 2, c, Total);
					} else if (c == 78) {
						excel.writCell(0, i + 2, c, HSCode);
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

	// 商户修改手工订单信息
	public Map<String, Object> editMorderInfo(String morderInfoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return mpayService.editMorderInfo(merchantId, merchantName, morderInfoPack);
	}

	// 读取缓存中excel导入实时数据
	public Map<String, Object> readExcelRedisInfo() {
		Map<String,Object> datasMap = new HashMap<>();
		Map<String,Object> statusMap = new HashMap<>();
		byte[] redisByte = JedisUtil.get("Shop_Key_ExcelIng_Map".getBytes(), 3600);
		if (redisByte != null && redisByte.length > 0) {
			datasMap =  (Map<String, Object>) SerializeUtil.toObject(redisByte);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), JSONObject.fromObject(datasMap));
			return statusMap;
		} else {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			datasMap.put(BaseCode.MSG.toString(), "暂无数据,请等待!");
			return datasMap;
		}
	}
}
