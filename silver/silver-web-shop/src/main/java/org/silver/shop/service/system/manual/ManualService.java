package org.silver.shop.service.system.manual;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.manual.MorderService;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.service.common.base.MeteringTransaction;
import org.silver.shop.service.common.base.ProvinceCityAreaTransaction;
import org.silver.shop.service.system.commerce.GoodsRecordTransaction;
import org.silver.shop.task.GZExcelTask;
import org.silver.shop.task.QBExcelTask;
import org.silver.shop.utils.ExcelBufferUtils;
import org.silver.shop.utils.InvokeTaskUtils;
import org.silver.shop.utils.RedisInfoUtils;
import org.silver.util.AppUtil;
import org.silver.util.CalculateCpuUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.ExcelUtil;
import org.silver.util.FileUpLoadService;
import org.silver.util.FileUtils;
import org.silver.util.IdcardValidator;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service("manualService")
public class ManualService {

	@Reference
	private MorderService morderService;
	@Autowired
	private FileUpLoadService fileUpLoadService;
	@Autowired
	private ProvinceCityAreaTransaction provinceCityAreaTransaction;
	@Autowired
	private GoodsRecordTransaction goodsRecordTransaction;
	@Autowired
	private ExcelBufferUtils excelBufferUtils;
	@Autowired
	private InvokeTaskUtils invokeTaskUtils;

	public boolean saveDatas(String merchant_no, String[] head, int length, String[][] body) {

		return morderService.saveRecord(merchant_no, head, length, body);
	}

	public Map<String, Object> loadDatas(int page, int size, HttpServletRequest req) {
		Map<String, Object> params = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		params.remove("page");
		params.remove("size");
		params.put("merchant_no", merchantId);
		return morderService.pageFindRecords(params, page, size);
	}

	public Map<String, Object> groupAdd(String merchant_no, HttpServletRequest req, String autoMSHR, String autoMXDR) {
		Map<String, Object> reqMap = fileUpLoadService.universalDoUpload(req, "/gadd-excel/", ".xls", false, 400, 400,
				null);
		List<Map<String, Object>> errl = new ArrayList<Map<String, Object>>();
		if ((int) reqMap.get("status") == 1) {
			List<String> list = (List<String>) reqMap.get("datas");
			File f = new File("/gadd-excel/" + list.get(0));
			ExcelUtil excel = new ExcelUtil();
			excel.open(f);
			readBySheetIndex(0, excel, errl, autoMSHR, autoMXDR);// 读取订单工作表
			readBySheetIndex(1, excel, errl, autoMSHR, autoMXDR);// 读取订单货品工作表
			excel.closeExcel();
			f.delete();
			reqMap.clear();
			reqMap.put("status", 1);
			reqMap.put("msg", "导入完成");
			reqMap.put("err", errl);
			return reqMap;
		}
		reqMap.put("status", -5);
		reqMap.put("msg", "导入文件出错，请重试");
		return reqMap;
	}

	private Map<String, Object> readBySheetIndex(int sheet, ExcelUtil excel, List<Map<String, Object>> errl,
			String autoMSHR, String autoMXDR) {
		switch (sheet) {
		case 0:
			return readHeadSheet(sheet, excel, errl, autoMSHR, autoMXDR);
		case 1:
			return readContentSheet(sheet, excel, errl);
		default:
			return null;
		}

	}

	private Map<String, Object> readHeadSheet(int sheet, ExcelUtil excel, List<Map<String, Object>> errl,
			String autoMSHR, String autoMXDR) {
		Map<String, Object> headMap = new HashMap<>();
		String OrderDate = null;
		String order_id = null;
		Double FCY = null;
		Double Tax = null;
		Double ActualAmountPaid = null;
		String RecipientName = null;
		String RecipientID = null;
		String RecipientTel = null;
		String RecipientProvincesCode = null;
		String RecipientAddr = null;
		String OrderDocAcount = null;
		String OrderDocName = null;
		String OrderDocId = null;
		String OrderDocTel = null;

		for (int r = 1; r <= excel.getRowCount(0); r++) {
			if (excel.getColumnCount(r) == 0) {
				break;
			}

			for (int c = 0; c < excel.getColumnCount(r); c++) {
				String value = excel.getCell(sheet, r, c);
				if (c == 0 && "".equals(value)) {

					headMap.put("status", 1);
					headMap.put("msg", "导入完成");
					headMap.put("err", errl);
					return headMap;
				}
				switch (c) {
				case 0:
					OrderDate = value;
					break;
				case 1:
					order_id = value;
					break;
				case 2:
					try {
						FCY = Double.parseDouble(value);
					} catch (Exception e) {
						e.printStackTrace();
						Map<String, Object> errMap = new HashMap<String, Object>();
						errMap.put("msg", "【订单工作表】第" + (r + 1) + "行-->" + "商品总额数值有误");
						errl.add(errMap);
					}
					break;
				case 3:
					try {
						Tax = Double.parseDouble(value);
					} catch (Exception e) {
						e.printStackTrace();
						Map<String, Object> errMap = new HashMap<String, Object>();
						errMap.put("msg", "【订单工作表】第" + (r + 1) + "行-->" + "税费数值有误");
						errl.add(errMap);
					}
					break;
				case 4:
					try {
						ActualAmountPaid = Double.parseDouble(value);
					} catch (Exception e) {
						e.printStackTrace();
						Map<String, Object> errMap = new HashMap<String, Object>();
						errMap.put("msg", "【订单工作表】第" + (r + 1) + "行-->" + "实际支付金额数值有误");
						errl.add(errMap);
					}
					break;
				case 5:
					RecipientName = value;
					break;
				case 6:
					RecipientID = value;
					break;
				case 7:
					RecipientTel = value;
					break;
				case 8:
					RecipientProvincesCode = value;
					break;
				case 9:
					RecipientAddr = value;
					break;
				case 10:
					OrderDocAcount = value;
					break;
				case 11:
					OrderDocName = value;
					break;
				case 12:
					OrderDocId = value;
					break;
				case 13:
					OrderDocTel = value;
					break;
				default:
					break;
				}
			}

			Map<String, Object> item = morderService.createNew("123", OrderDate, order_id, FCY, Tax, ActualAmountPaid,
					RecipientName, RecipientID, RecipientTel, RecipientProvincesCode, RecipientAddr, OrderDocAcount,
					OrderDocName, OrderDocId, OrderDocTel, autoMSHR, autoMXDR);
			if ((int) item.get("status") != 1) {
				Map<String, Object> errMap = new HashMap<String, Object>();
				errMap.put("msg", "【订单工作表】第" + (r + 1) + "行-->" + item.get("msg"));
				errl.add(errMap);
			}
		}
		headMap.clear();
		headMap.put("status", 1);
		headMap.put("msg", "导入完成");
		headMap.put("err", errl);
		return headMap;
	}

	private Map<String, Object> readContentSheet(int sheet, ExcelUtil excel, List<Map<String, Object>> errl) {
		Map<String, Object> contentMap = new HashMap<>();
		String order_id = null;
		String EntGoodsNo = null;
		String HSCode = null;
		String Brand = null;
		String BarCode = null;
		String GoodsName = null;
		String OriginCountry = null;
		String CusGoodsNo = null;
		String CIQGoodsNo = null;
		String GoodsStyle = null;
		String Unit = null;
		Double Price = null;
		Integer Qty = null;
		for (int r = 1; r <= excel.getRowCount(sheet); r++) {
			if (excel.getColumnCount(sheet, r) == 0) {
				break;
			}
			for (int c = 0; c < excel.getColumnCount(sheet, r); c++) {
				String value = excel.getCell(sheet, r, c);
				if (c == 0 && "".equals(value)) {
					contentMap.put("status", 1);
					contentMap.put("msg", "导入完成");
					contentMap.put("err", errl);
					return contentMap;
				}
				switch (c) {
				case 0:
					order_id = value;
					break;
				case 1:
					EntGoodsNo = value;
					break;
				case 2:
					HSCode = value;
					break;
				case 3:
					Brand = value;
					break;
				case 4:
					BarCode = value;
					break;
				case 5:
					GoodsName = value;
					break;
				case 6:
					OriginCountry = value;
					break;
				case 7:
					CusGoodsNo = value;
					break;
				case 8:
					CIQGoodsNo = value;
					break;
				case 9:
					GoodsStyle = value;
					break;
				case 10:
					Unit = value;
					break;
				case 11:
					try {
						Price = Double.parseDouble(value);
					} catch (Exception e) {
						e.printStackTrace();
						Map<String, Object> errMap = new HashMap<String, Object>();
						errMap.put("msg", "【货品工作表】第" + (r + 1) + "行-->" + "货品单价数值有误");
						errl.add(errMap);
					}
					break;
				case 12:
					try {
						if (value != null) {
							Qty = (int) Double.parseDouble(value);
						} else {
							Map<String, Object> errMap = new HashMap<String, Object>();
							errMap.put("msg", "【货品工作表】第" + (r + 1) + "行-->" + "请填写货品数量");
							errl.add(errMap);
						}

					} catch (Exception e) {
						e.printStackTrace();
						Map<String, Object> errMap = new HashMap<String, Object>();
						errMap.put("msg", "【货品工作表】第" + (r + 1) + "行-->" + "货品数量有误");
						errl.add(errMap);
					}

					break;
				default:
					break;
				}
			}
			JSONObject params = new JSONObject();
			params.put("order_id", order_id);
			params.put("EntGoodsNo", EntGoodsNo);
			params.put("HSCode", HSCode);
			params.put("Brand", Brand);
			params.put("BarCode", BarCode);
			params.put("GoodsName", GoodsName);
			params.put("OriginCountry", OriginCountry);
			params.put("CusGoodsNo", CusGoodsNo);
			params.put("CIQGoodsNo", CIQGoodsNo);
			params.put("GoodsStyle", GoodsStyle);
			params.put("Unit", Unit);
			params.put("Price", Price);
			params.put("Qty", Qty);

			Map<String, Object> item = morderService.createNewSub(params);
			if ((int) item.get("status") != 1) {
				Map<String, Object> errMap = new HashMap<String, Object>();
				errMap.put("msg", "【货品工作表】第" + (r + 1) + "行-->" + item.get("msg"));
				errl.add(errMap);
			}
		}
		contentMap.clear();
		contentMap.put("status", 1);
		contentMap.put("msg", "导入完成");
		contentMap.put("err", errl);
		return contentMap;

	}

	public Map<String, Object> deleteByOrderId(String orderIdPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return morderService.deleteByOrderId(merchantId, merchantName, orderIdPack);
	}

	public Map<String, Object> groupAddOrder(HttpServletRequest req) {
		Map<String, Object> statusMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Map<String, Object> reqMap = fileUpLoadService.universalDoUpload(req, "/gadd-excel/", ".xls", false, 400, 400,
				null);
		String serialNo = "";
		if ((int) reqMap.get("status") == 1) {
			List<String> list = (List<String>) reqMap.get("datas");
			if (list.isEmpty()) {
				return ReturnInfoUtils.errorInfo("文件上传失败,请重试!");
			}
			File file = new File("/gadd-excel/" + list.get(0));
			File dest = copyFile(file);
			ExcelUtil excel = new ExcelUtil(dest);
			excel.open();
			// 总列数
			int columnTotalCount = excel.getColumnCount(0, 0);
			// 有数据的总行数
			int realRowCount = excelRealRowCount(excel.getRowCount(0), excel);
			if (realRowCount <= 0) {
				return ReturnInfoUtils.errorInfo("导入失败,请检查是否有数据或序号符合要求!");
			}
			if (columnTotalCount == 14) { // 企邦模板表格长度(增加了序号列)
				// 用于前台区分哪家批次号
				serialNo = "QB_" + SerialNoUtils.getSerialNo("QB");
				// 多了一行说明
				realRowCount += 1;
				invokeTaskUtils.startTask(1, realRowCount, file, merchantId, serialNo, merchantName);
				statusMap.put("serialNo", serialNo);
			} else if (columnTotalCount == 71) {// 国宗订单模板长度(加上序号列)
				// 用于前台区分哪家批次号
				serialNo = "GZ_" + SerialNoUtils.getSerialNo("GZ");
				invokeTaskUtils.startTask(2, realRowCount, file, merchantId, serialNo, merchantName);
				statusMap.put("serialNo", serialNo);
			} else {
				return ReturnInfoUtils.errorInfo("导入失败,请检查订单模板是否符合规范!");
			}
			excel.closeExcel();
			statusMap.put("status", 1);
			statusMap.put("msg", "执行成功,正在读取数据....");
			return statusMap;
		}
		return ReturnInfoUtils.errorInfo("上传文件失败,服务器繁忙!");
	}

	/**
	 * 复制原始文件
	 * 
	 * @param file
	 * @return
	 */
	private File copyFile(File file) {
		// 副本文件名
		String imgName = AppUtil.generateAppKey() + "_" + System.currentTimeMillis() + ".xlsx";
		File dest = new File(file.getParentFile() + "/" + imgName);
		try {
			FileUtils.copyFileUsingFileChannels(file, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dest;
	}

	/**
	 * 通过逆向工程获取表单中真实数据行数(默认再总行数上减1)
	 * 
	 * @param rowTotalCount
	 *            总行数
	 * @param excel
	 */
	public static int excelRealRowCount(int rowTotalCount, ExcelUtil excel) {
		int realRowCount = 0;
		int totalColumnCount = excel.getColumnCount(0);
		for (int r = rowTotalCount; r > 0; r--) {
			if (excel.getColumnCount(0) == 0) {
				continue;
			}
			// 默认只需要读取表单中的第一列(序号)
			for (int c = 0; c < totalColumnCount; c++) {
				String value = null;
				try {
					value = excel.getCell(0, r, c);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}

				// 判断序号是否有值
				if (c == 0 && StringEmptyUtils.isNotEmpty(value)) {
					try {
						// 取最后一条数据的序号作为总行数
						realRowCount = Integer.parseInt(value);
					} catch (Exception e) {
						e.printStackTrace();
						return -1;
					}
					return realRowCount;
				}
			}
		}
		return -1;
	}

	/**
	 * 企邦表单,暂默认为14列数据
	 * 
	 * @param sheet
	 *            工作表索引
	 * @param excel
	 *            工具类
	 * @param errl
	 *            错误信息
	 * @param merchantId
	 *            商户Id
	 * @param serialNo
	 *            批次号
	 * @param startCount
	 *            开始行数
	 * @param endCount
	 *            结束行数
	 * @param realRowCount
	 *            总行数
	 * @param merchantName
	 *            商户名称
	 * @return Map
	 */
	public void readQBSheet(int sheet, ExcelUtil excel, List<Map<String, Object>> errl, String merchantId,
			String serialNo, int startCount, int endCount, int realRowCount, String merchantName) {
		int seqNo = 0;
		String orderId = "";// 订单Id
		String entGoodsNo = "";// 商品自编号
		String goodsName = "";// 商品名称
		double price = 0;// 单价
		int count = 0;// 商品数量
		String recipientName = "";// 收货人名称
		String recipientTel = "";// 收货人电话
		String recipientAddr = "";// 收货人详细地址
		String marCode = "";// (启邦客户)商品归属商家代码
		String orderDocName = "";// 下单人名称
		String orderDocId = "";// 下单人身份证号码
		String ehsEntName = "";// 承运商
		String waybillNo = "";// 运单编号
		System.out.println("--------------开始循环表单中数据-----------");
		int totalColumnCount = excel.getColumnCount(0);
		// for (int r = 2; r <= rowTotalCount; r++) {
		try {
			for (int r = startCount; r <= endCount; r++) {
				if (excel.getColumnCount(sheet, r) == 0) {
					break;
				}
				for (int c = 0; c < totalColumnCount; c++) {
					String value = excel.getCell(sheet, r, c);
					if (c == 0 && "".equals(value)) {
						break;
					}
					if (c == 0) {
						// 序号
						if (StringEmptyUtils.isNotEmpty(value)) {
							seqNo = Integer.parseInt(value);
						} else {
							String msg = "【表格】第" + (r + 1) + "行-->表单序号错误,请核对信息!";
							RedisInfoUtils.commonErrorInfo(msg, errl, (realRowCount - 1), serialNo, "orderImport", 1);
							break;
						}
					} else if (c == 1) {
						// 订单Id
						orderId = value;
					} else if (c == 2) {
						// 商品自编号
						if (StringEmptyUtils.isNotEmpty(value)) {
							entGoodsNo = value;
						}
					} else if (c == 3) {
						// 商品名称
						if (StringEmptyUtils.isNotEmpty(value)) {
							goodsName = value;
						}
					} else if (c == 4) {
						// 商品单价
						try {
							if (StringEmptyUtils.isNotEmpty(value)) {
								price = Double.parseDouble(value);
							}
						} catch (Exception e) {
							Map<String, Object> errMap = new HashMap<>();
							errMap.put(BaseCode.MSG.toString(), "【表格】第" + (r + 1) + "行,商品单价输入错误!");
							errl.add(errMap);
							break;
						}
					} else if (c == 5) {
						// 数量
						try {
							if (StringEmptyUtils.isNotEmpty(value)) {
								count = Integer.parseInt(value);
							}
						} catch (Exception e) {
							Map<String, Object> errMap = new HashMap<>();
							errMap.put(BaseCode.MSG.toString(), "【表格】第" + (r + 1) + "行,商品数量输入错误!");
							errl.add(errMap);
							break;
						}
					} else if (c == 6) {
						// 收货人姓名
						recipientName = value;
					} else if (c == 7) {
						// 收件人电话
						recipientTel = value;
					} else if (c == 8) {
						// 收货人详细地址
						recipientAddr = value;
					} else if (c == 9) {
						// (启邦客户)商品归属商家代码
						marCode = value;
					} else if (c == 10) {
						orderDocName = value;
					} else if (c == 11) {
						// 身份证号码
						orderDocId = value.replace("x", "X").replaceAll("  ", "").replaceAll(" ", "");
					} else if (c == 12) {
						// 承运商
						ehsEntName = value;
					} else if (c == 13) {
						waybillNo = value;
					}
				}
				if (!IdcardValidator.validate18Idcard(orderDocId)) {
					String msg = "【表格】第" + (r + 1) + "行-->订单号[" + orderId + "]实名认证不通过,请核实身份证与姓名信息!";
					RedisInfoUtils.commonErrorInfo(msg, errl, (realRowCount - 1), serialNo, "orderImport", 4);
				}
				Map<String, Object> item = new HashMap<>();
				Map<String, Object> provinceMap = searchProvinceCityArea(recipientAddr);
				if (provinceMap == null) {
					String msg = "【表格】第" + (r + 1) + "行-->订单号[" + orderId + "]收货人地址不符合规范,请核对信息!";
					RedisInfoUtils.commonErrorInfo(msg, errl, (realRowCount - 1), serialNo, "orderImport", 3);
				}
				Map<String, Object> reProvinceCityAreaMap = doProvinceCityArea(provinceMap);
				if (reProvinceCityAreaMap != null) {
					item.put("areaCode", reProvinceCityAreaMap.get("areaCode") + "");
					item.put("areaName", reProvinceCityAreaMap.get("areaName") + "");
					item.put("cityCode", reProvinceCityAreaMap.get("cityCode") + "");
					item.put("cityName", reProvinceCityAreaMap.get("cityName") + "");
					item.put("provinceCode", reProvinceCityAreaMap.get("provinceCode") + "");
					item.put("provinceName", reProvinceCityAreaMap.get("provinceName") + "");
				}
				item.put("seqNo", seqNo);
				item.put("orderId", orderId);
				item.put("entGoodsNo", entGoodsNo);
				item.put("goodsName", goodsName);
				item.put("price", price);
				item.put("count", count);
				double orderTotalPrice = count * price;
				item.put("orderTotalPrice", orderTotalPrice);
				// 实际支付金额
				item.put("actualAmountPaid", orderTotalPrice);
				// 税费
				double tax = 0;
				item.put("tax", tax);
				item.put("recipientName", recipientName);
				item.put("recipientTel", recipientTel);
				item.put("recipientAddr", recipientAddr);
				item.put("orderDocAcount", recipientName);
				item.put("orderDocName", orderDocName);
				item.put("orderDocId", orderDocId);
				// 下单人电话
				item.put("orderDocTel", recipientTel);
				// 企邦订单承运商
				item.put("ehsEntName", ehsEntName);
				item.put("waybillNo", waybillNo);
				String[] str = serialNo.split("_");
				int serial = Integer.parseInt(str[1]);
				// 企邦批次号
				item.put("serial", serial);
				item.put("marCode", marCode);
				Map<String, Object> orderMap = morderService.createQBOrder(merchantId, item, merchantName);
				if ("10".equals(orderMap.get("status") + "")) {// 当遇到超额时,继续将剩下的订单商品导入
					String msg = "【表格】第" + (r + 1) + "行-->" + orderMap.get("msg") + "";
					RedisInfoUtils.commonErrorInfo(msg, errl, (realRowCount - 1), serialNo, "orderImport", 2);
				} else if (!"1".equals(orderMap.get("status") + "")) {
					String msg = "【表格】第" + (r + 1) + "行-->" + orderMap.get("msg") + "";
					RedisInfoUtils.commonErrorInfo(msg, errl, (realRowCount - 1), serialNo, "orderImport", 1);
					continue;
				}
				Map<String, Object> orderSubMap = morderService.createQBOrderSub(merchantId, item, merchantName);
				if (!"1".equals(orderSubMap.get("status") + "")) {
					String msg = "【表格】第" + (r + 1) + "行-->" + orderSubMap.get("msg") + "";
					RedisInfoUtils.commonErrorInfo(msg, errl, (realRowCount - 1), serialNo, "orderImport", 1);
				}
				excelBufferUtils.writeRedis(errl, (realRowCount - 1), serialNo, "orderImport");
			}
		} catch (Exception e) {
			e.printStackTrace();
			String msg = "表格数据不符合规范,请核对所有数据排序是否正确!";
			RedisInfoUtils.commonErrorInfo(msg, errl, (realRowCount - 1), serialNo, "orderImport", 1);
		} finally {
			excel.closeExcel();
			excelBufferUtils.writeCompletedRedis(errl, (realRowCount - 1), serialNo, "orderImport");
		}
	}

	/**
	 * 读取国宗订单表
	 * 
	 * @param sheet
	 *            子表数
	 * @param excel
	 *            文件
	 * @param errl
	 *            错误信息List
	 * @param merchantId
	 *            商户Id
	 * @param startCount
	 *            表单开始行数
	 * @param endCount
	 *            表单结束行数
	 * @param serialNo
	 *            批次号
	 * @param realRowCount
	 *            总行数
	 * @param merchantName
	 * @return Map
	 */
	public final void readGZSheet(int sheet, ExcelUtil excel, List<Map<String, Object>> errl, String merchantId,
			int startCount, int endCount, String serialNo, int realRowCount, String merchantName) {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHMMSS");
		String OrderDate = null, waybill = null, dateSign, RecipientName = null, RecipientID = null,
				RecipientTel = null, RecipientAddr = null, OrderDocAcount, OrderDocName, OrderDocId, OrderDocTel,
				goodsName = "";
		int seqNo = 0;
		String orderId = "";//
		String EntGoodsNo = "";// 商品货号、条形码
		String ciqGoodsNo = "";// 检验检疫商品备案编号
		String cusGoodsNo = "";// 海关正式备案编号
		String hsCode = "";// HS编码
		String goodsStyle = "";// 规格型号
		String originCountry = "";// 原产国
		String RecipientProvincesCode = "";
		String provinceCode = "";// 省份编码
		String cityCode = "";// 城市编码
		String areaCode = "";// 区域编码
		String provinceName = "";// 省份名称
		String cityName = "";// 城市名称
		String areaName = "";// 区域名称
		String unit = "";// 计量单位
		String currCode = "";// 币制
		double ActualAmountPaid, FCY = 0, Tax = 0;
		int total_count = 0;// 商品数量
		double price = 0;
		double netWt = 0.0; // 净重
		double grossWt = 0.0;// 毛重
		double firstLegalCount = 0.0;// 第一法定数量
		double secondLegalCount = 0.0;// 第二法定数量
		String stdUnit = "";// 第一法定计量单位
		String secUnit = "";// 第二法定计量单位
		int numOfPackages = 0;// 箱件数 （同一订单商品总件数，例如同一订单有6支护手霜和4瓶钙片则填写10，即6+4=10。)
		int packageType = 0;// 包装种类
		String transportModel = "";// 运输方式
		String postal = "";// 邮编
		String senderName = ""; // 发货人姓名
		String senderCountry = "";// 发货人国家代码
		String senderAreaCode = "";// 发货人区域代码 国外填 000000
		String senderAddress = "";// 发货人地址
		String senderTel = "";// 发货人电话
		String brand = "";// 品牌
		String customsCode = "";// (海关关区)码头代码
		System.out.println("--------------开始循环表单中数据-----------");
		int totalColumnCount = excel.getColumnCount(0);
		try {
			for (int r = startCount; r <= endCount; r++) {
				if (excel.getColumnCount(sheet, r) == 0) {
					break;
				}
				for (int c = 0; c < totalColumnCount; c++) {
					String value = excel.getCell(sheet, r, c);
					if (c == 0 && "".equals(value)) {
						break;
					}
					if (c == 0) {
						// 序号
						if (StringEmptyUtils.isNotEmpty(value)) {
							seqNo = Integer.parseInt(value);
						} else {
							String msg = "【表格】第" + (r + 1) + "行-->表单序号错误,请核对信息!";
							RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "orderImport", 1);
							break;
						}
					} else if (c == 1) {
						if (StringEmptyUtils.isNotEmpty(value)) {
							orderId = value;
						}
					} else if (c == 4) {
						// 运单号
						if (StringEmptyUtils.isNotEmpty(value)) {
							waybill = value.replaceAll(" ", "");
						} else {
							String msg = "【表格】第" + (r + 1) + "行-->运单号不能为空,请核对信息!";
							RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "orderImport", 1);
							break;
						}
					} else if (c == 8) {
						// 运输方式
						if (StringEmptyUtils.isNotEmpty(value)) {
							transportModel = value;
						} else {
							transportModel = "4";
						}
					} else if (c == 10) {
						// 包装种类
						if (StringEmptyUtils.isNotEmpty(value)) {
							packageType = Integer.parseInt(value);
						} else {
							packageType = 4;
						}
					} else if (c == 13) {
						// 净重
						if (StringEmptyUtils.isNotEmpty(value)) {
							netWt = Double.parseDouble(value);
						}
					} else if (c == 14) {
						// 毛重
						if (StringEmptyUtils.isNotEmpty(value)) {
							grossWt = Double.parseDouble(value);
						}
					} else if (c == 15) {
						// 箱件数
						if (StringEmptyUtils.isNotEmpty(value)) {
							numOfPackages = Integer.parseInt(value);
						}
					} else if (c == 16) {
						// 商品名
						goodsName = value;
					} else if(c  == 17){
						//海关关区代码
						customsCode = value;
					}	else if (c == 18) {
						// 收货人姓名
						RecipientName = value.replaceAll("  ", "").replaceAll(" ", "");
					} else if (c == 20) {
						// 邮编代码
						if (StringEmptyUtils.isNotEmpty(value)) {
							postal = value;
						}
					} else if (c == 21) {
						// 收货地址
						RecipientAddr = value;
					} else if (c == 22) {
						// 收货人电话
						RecipientTel = value;
					} else if (c == 24) {
						// 收货人身份证
						RecipientID = value.trim().replace("x", "X").replaceAll("  ", "").replaceAll(" ", "");
					} else if (c == 26) {
						senderName = value; // 发货人姓名
					} else if (c == 27) {
						senderCountry = value;// 发货人国家代码
					} else if (c == 28) {
						senderAreaCode = value;// 发货人区域代码 国外填 000000
					} else if (c == 29) {
						senderAddress = value;// 发货人地址
					} else if (c == 30) {
						senderTel = value;// 发货人电话
					} else if (c == 33) {
						// 商品货号
						EntGoodsNo = value.replaceAll(" ", "");
					} else if (c == 34) {// 原产国
						originCountry = value;
					} else if (c == 35) {
						// 计量单位
						if (StringEmptyUtils.isNotEmpty(value)) {
							value = goodsRecordTransaction.findUnit(value);
							if (StringEmptyUtils.isNotEmpty(value)) {
								unit = value;
							}
						}
					} else if (c == 36) {
						// 数量
						total_count = Integer.parseInt(value);
					} else if (c == 39) {
						// HS编码
						hsCode = value;
					} else if (c == 40) {
						// 单价
						if (StringEmptyUtils.isNotEmpty(value)) {
							price = Double.parseDouble(value);
						}
					} else if (c == 41) {// 币制
						currCode = value;
					} else if (c == 51) {
						// 第一法定数量
						if (StringEmptyUtils.isNotEmpty(value)) {
							firstLegalCount = Double.parseDouble(value);
						} else {
							firstLegalCount = 0.0;
						}
					} else if (c == 52) {
						// 第一法定计量单位
						if (StringEmptyUtils.isNotEmpty(value)) {
							stdUnit = value;
						}
					} else if (c == 53) {
						if (StringEmptyUtils.isNotEmpty(value)) {
							// 第二法定数量
							secondLegalCount = Double.parseDouble(value);
						} else {
							secondLegalCount = 0.0;
						}
					} else if (c == 54) {
						// 第二法定计量单位
						if (StringEmptyUtils.isNotEmpty(value)) {
							secUnit = value;
						}
					} else if (c == 55) {
						// 品牌
						if (StringEmptyUtils.isNotEmpty(value)) {
							brand = value;
						}
					} else if (c == 56) {
						// 商品规格
						goodsStyle = value;
					} else if (c == 57) {
						// 商品国检备案号
						ciqGoodsNo = value;
					}
				}

				if (!checkEntGoodsNoLength(EntGoodsNo)) {
					String msg = "【表格】第" + (r + 1) + "行-->运单号[" + waybill + "]商品货号长度超过20,请核对商品货号是否正确!";
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "orderImport", 1);
					continue;
				}
				if (netWt > grossWt) {
					String msg = "【表格】第" + (r + 1) + "行-->运单号[" + waybill + "]商品净重大于毛重,请核实信息!";

					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "orderImport", 5);
				}
				if (!IdcardValidator.validate18Idcard(RecipientID)) {
					String str = "运单号[" + waybill + "]实名认证不通过,请核实身份证与姓名信息!";
					String msg = "【表格】第" + (r + 1) + "行-->" + str;
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "orderImport", 4);
				}
				Map<String, Object> provinceMap = searchProvinceCityArea(RecipientAddr);
				if (provinceMap == null) {
					String msg = "【表格】第" + (r + 1) + "行-->运单号[" + waybill + "]收货人地址填写有误,请核对信息!";
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "orderImport", 3);
				} else {
					Map<String, Object> reProvinceCityAreaMap = doProvinceCityArea(provinceMap);
					if (reProvinceCityAreaMap != null) {
						areaCode = reProvinceCityAreaMap.get("areaCode") + "";
						areaName = reProvinceCityAreaMap.get("areaName") + "";
						cityCode = reProvinceCityAreaMap.get("cityCode") + "";
						cityName = reProvinceCityAreaMap.get("cityName") + "";
						provinceCode = reProvinceCityAreaMap.get("provinceCode") + "";
						provinceName = reProvinceCityAreaMap.get("provinceName") + "";
					}
				}
				OrderDocName = RecipientName;
				OrderDocId = RecipientID;
				OrderDocTel = RecipientTel;
				OrderDocAcount = getOrderDocAcount();// 平台账号
				FCY = total_count * price;
				ActualAmountPaid = FCY + Tax;
				OrderDate = sdf.format(new Date());
				dateSign = OrderDate.substring(0, 8);
				// 根据 商品名 商品货号 查找出已备案商品信息，生成订单关联的商品数据
				JSONObject goodsInfo = new JSONObject();
				goodsInfo.put("entGoodsNo", EntGoodsNo);
				goodsInfo.put("HSCode", hsCode);
				goodsInfo.put("Brand", brand);
				goodsInfo.put("BarCode", EntGoodsNo);
				goodsInfo.put("GoodsName", goodsName);
				goodsInfo.put("OriginCountry", originCountry);
				if (StringEmptyUtils.isNotEmpty(cusGoodsNo)) {
					goodsInfo.put("CusGoodsNo", cusGoodsNo);
				} else {// 当海关备案编码为空时,则默认填写*
					goodsInfo.put("CusGoodsNo", "*");
				}
				goodsInfo.put("CIQGoodsNo", ciqGoodsNo);
				goodsInfo.put("GoodsStyle", goodsStyle);
				goodsInfo.put("Unit", unit);
				goodsInfo.put("Price", price);
				goodsInfo.put("Qty", total_count);
				goodsInfo.put("netWt", netWt);
				goodsInfo.put("grossWt", grossWt);
				goodsInfo.put("firstLegalCount", firstLegalCount);
				goodsInfo.put("secondLegalCount", secondLegalCount);
				goodsInfo.put("stdUnit", stdUnit);
				goodsInfo.put("secUnit", secUnit);
				goodsInfo.put("numOfPackages", numOfPackages);
				goodsInfo.put("packageType", packageType);
				goodsInfo.put("transportModel", transportModel);
				goodsInfo.put("seqNo", seqNo);
				goodsInfo.put("merchantId", merchantId);
				goodsInfo.put("merchantName", merchantName);
				goodsInfo.put("customsCode", customsCode);
				String[] str = serialNo.split("_");
				int serial = Integer.parseInt(str[1]);
				Map<String, Object> reGoodsMap = checkGoodsInfo(goodsInfo);
				if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
					String msg = "【表格】第" + (r + 1) + "行-->" + reGoodsMap.get(BaseCode.MSG.toString()) + "";
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "orderImport", 1);
					continue;
				}
				// 创建国宗订单
				Map<String, Object> item = morderService.guoCreateNew(merchantId, waybill, serial, dateSign, OrderDate,
						FCY, Tax, ActualAmountPaid, RecipientName, RecipientID, RecipientTel, RecipientProvincesCode,
						RecipientAddr, OrderDocAcount, OrderDocName, OrderDocId, OrderDocTel, senderName, senderCountry,
						senderAreaCode, senderAddress, senderTel, areaCode, cityCode, provinceCode, postal,
						provinceName, cityName, areaName, orderId, goodsInfo);
				if ("10".equals(item.get("status") + "")) {// 当遇到超额时
					String msg = "【表格】第" + (r + 1) + "行-->" + item.get("msg") + "";
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "orderImport", 2);
				} else if (!"1".equals(item.get("status") + "")) {
					String msg = "【表格】第" + (r + 1) + "行-->" + item.get("msg") + "";
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "orderImport", 1);
					continue;
				}
				String order_id = item.get("order_id") + "";
				goodsInfo.put("order_id", order_id);
				// 根据订单Id创建订单商品
				Map<String, Object> goodsItem = morderService.createNewSub(goodsInfo);
				if ((int) goodsItem.get("status") != 1) {
					String msg = "【表格】第" + (r + 1) + "行商品-->" + goodsItem.get("msg");
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "orderImport", 1);
				}
				excelBufferUtils.writeRedis(errl, realRowCount, serialNo, "orderImport");
			}
		} catch (Exception e) {
			e.printStackTrace();
			String msg = "表格数据不符合规范,请核对所有数据排序是否正确!";
			RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "orderImport", 1);
		} finally {
			excel.closeExcel();
			excelBufferUtils.writeCompletedRedis(errl, realRowCount, serialNo, "orderImport");
		}
	}

	/**
	 * 校验商品信息
	 * 
	 * @param goodsInfo
	 * @return Map
	 */
	private Map<String, Object> checkGoodsInfo(JSONObject goodsInfo) {
		JSONArray datas = new JSONArray();
		datas.add(goodsInfo);
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("entGoodsNo");
		noNullKeys.add("HSCode");
		noNullKeys.add("Brand");
		// noNullKeys.add("BarCode");
		noNullKeys.add("GoodsName");
		noNullKeys.add("OriginCountry");
		noNullKeys.add("CusGoodsNo");
		noNullKeys.add("CIQGoodsNo");
		noNullKeys.add("GoodsStyle");
		noNullKeys.add("Unit");
		noNullKeys.add("Price");
		noNullKeys.add("Qty");
		return CheckDatasUtil.changeMsg(datas, noNullKeys);
	}

	/**
	 * 校验商品自编号是否超过海关要求长度
	 * 
	 * @param entGoodsNo
	 *            商品自编号
	 * @return boolean
	 */
	private boolean checkEntGoodsNoLength(String entGoodsNo) {
		return StringEmptyUtils.isNotEmpty(entGoodsNo) && entGoodsNo.length() <= 20;
	}

	/**
	 * 根据返回的省市区放入对应的属性中
	 * 
	 * @param provinceMap
	 */
	private Map<String, Object> doProvinceCityArea(Map<String, Object> provinceMap) {
		Map<String, Object> statusMap = new HashMap<>();
		String areaCode = "";
		String areaName = "";
		if (provinceMap != null && !provinceMap.isEmpty()) {
			if (StringEmptyUtils.isNotEmpty(provinceMap.get("areaName"))
					&& StringEmptyUtils.isNotEmpty(provinceMap.get("areaCode"))) {// 如果未找到区域编码及区域名称,则沿用省份的编码
				areaCode = provinceMap.get("areaCode") + "";
				areaName = provinceMap.get("areaName") + "";
			} else if (StringEmptyUtils.isNotEmpty(provinceMap.get("cityCode"))
					&& StringEmptyUtils.isNotEmpty(provinceMap.get("cityName"))) {
				areaCode = provinceMap.get("cityCode") + "";
				areaName = provinceMap.get("cityName") + "";
			} else {
				areaCode = provinceMap.get("provinceCode") + "";
				areaName = provinceMap.get("provinceName") + "";

			}
			statusMap.put("areaCode", areaCode);
			statusMap.put("areaName", areaName);
			statusMap.put("cityCode", provinceMap.get("cityCode") + "");
			statusMap.put("cityName", provinceMap.get("cityName") + "");
			statusMap.put("provinceCode", provinceMap.get("provinceCode") + "");
			statusMap.put("provinceName", provinceMap.get("provinceName") + "");
			return statusMap;
		}
		return null;
	}

	/**
	 * 根据邮编查询关联的省市区编码
	 * 
	 * @param recipientAddr
	 *            收货人详细地址
	 * @return Map
	 */
	public Map<String, Object> searchProvinceCityArea(String recipientAddr) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> provinceCityAreaMap = (Map<String, Object>) provinceCityAreaTransaction
				.getProvinceCityArea();
		if (!"1".equals(provinceCityAreaMap.get(BaseCode.STATUS.toString()))) {
			return provinceCityAreaMap;
		}
		Map<String, Object> areaMap = (Map<String, Object>) provinceCityAreaMap.get(BaseCode.DATAS.getBaseCode());

		Map<String, Object> reAreaMap = areaGetProvinceCityAreaInfo(areaMap, recipientAddr);
		if ("1".equals(reAreaMap.get(BaseCode.STATUS.toString()))) {
			return reAreaMap;
		}
		// 返回的城市信息
		Map<String, Object> reCityMap = (Map<String, Object>) reAreaMap.get(BaseCode.DATAS.toString());
		Map<String, Object> reProvinceMap = new HashMap<>();
		// 根据省份+城市查询
		for (Map.Entry<String, Object> entry : reCityMap.entrySet()) {
			String str = entry.getValue() + "";
			String[] c = str.split("#");
			String cityCode = c[0].split("_")[0];
			String cityName = c[0].split("_")[1];
			String provinceCode = c[1].split("_")[0];
			String provinceName = c[1].split("_")[1];
			// 上海市杨浦许昌路1588弄2号804室
			if (recipientAddr.contains(provinceName)) {
				if (recipientAddr.contains(cityName)) {
					statusMap.put("cityCode", cityCode);
					statusMap.put("cityName", cityName);
					statusMap.put("provinceCode", provinceCode);
					statusMap.put("provinceName", provinceName);
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
					return statusMap;
				} else if (provinceName.contains("上海市")) {
					statusMap.put("cityCode", "310100");
					statusMap.put("cityName", "市辖区");
					statusMap.put("provinceCode", provinceCode);
					statusMap.put("provinceName", provinceName);
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
					return statusMap;
				} else if (provinceName.contains("北京市")) {
					statusMap.put("cityCode", "110100");
					statusMap.put("cityName", "市辖区");
					statusMap.put("provinceCode", provinceCode);
					statusMap.put("provinceName", provinceName);
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
					return statusMap;
				} else if (provinceName.contains("天津市")) {
					statusMap.put("cityCode", "120100");
					statusMap.put("cityName", "市辖区");
					statusMap.put("provinceCode", provinceCode);
					statusMap.put("provinceName", provinceName);
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
					return statusMap;
				} else if (provinceName.contains("重庆市")) {
					statusMap.put("cityCode", "500100");
					statusMap.put("cityName", "市辖区");
					statusMap.put("provinceCode", provinceCode);
					statusMap.put("provinceName", provinceName);
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
					return statusMap;
				}
				// 如果地址中省份+城市未找到则把省份编码放入缓存
				reProvinceMap.put(provinceCode, provinceCode + "_" + provinceName);
			}
		}
		// 根据省份查询
		for (Map.Entry<String, Object> entry : reProvinceMap.entrySet()) {
			String p = entry.getValue() + "";
			String[] pStr = p.split("_");
			String provinceCode = pStr[0];
			String provinceName = pStr[1];
			if (recipientAddr.contains(provinceName)) {
				statusMap.put("cityCode", "");
				statusMap.put("cityName", "");
				statusMap.put("provinceCode", provinceCode);
				statusMap.put("provinceName", provinceName);
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
				return statusMap;
			}
		}
		return null;
	}

	/**
	 * 根据全国所有区域,查询对应的省市区信息
	 * 
	 * @param areaMap
	 *            全国所有区域信息集合
	 * @param recipientAddr
	 *            收货人详细地址
	 * @return Map
	 */
	private Map<String, Object> areaGetProvinceCityAreaInfo(Map<String, Object> areaMap, String recipientAddr) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reCityMap = new HashMap<>();
		// 遍历全国所有区域
		for (Map.Entry<String, Object> entry : areaMap.entrySet()) {
			String str = entry.getValue() + "";
			String[] a = str.split("#");
			String provinceCode = a[0].split("_")[0].trim();
			String provinceName = a[0].split("_")[1].trim();
			String cityCode = a[1].split("_")[0].trim();
			String cityName = a[1].split("_")[1].trim();
			String areaCode = a[2].split("_")[0].trim();
			String areaName = a[2].split("_")[1].trim();
			if (recipientAddr.contains(areaName) && recipientAddr.contains(provinceName)) {
				statusMap.put("areaCode", areaCode);
				statusMap.put("areaName", areaName);
				statusMap.put("cityCode", cityCode);
				statusMap.put("cityName", cityName);
				statusMap.put("provinceCode", provinceCode);
				statusMap.put("provinceName", provinceName);
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
				return statusMap;
			} else if (recipientAddr.contains(areaName) && recipientAddr.contains(cityName)) {
				statusMap.put("areaCode", areaCode);
				statusMap.put("areaName", areaName);
				statusMap.put("cityCode", cityCode);
				statusMap.put("cityName", cityName);
				statusMap.put("provinceCode", provinceCode);
				statusMap.put("provinceName", provinceName);
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
				return statusMap;
			}
			// 地址中区域+省份未找到则把城市编码放入缓存
			reCityMap.put(cityCode, cityCode + "_" + cityName + "#" + provinceCode + "_" + provinceName);
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
		statusMap.put(BaseCode.DATAS.toString(), reCityMap);
		return statusMap;
	}

	private String getOrderDocAcount() {
		return AppUtil.generateAppKey().substring(0, 6);
	}

	// 删除订单商品信息
	public Map<String, Object> deleteOrderGoodsInfo(String idPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取管理员登录时,shiro存入在session中的数据
		// Manager managerInfo = (Manager)
		// currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String id = "";
		String name = "";
		// 获取登录后的商户账号
		if (merchantInfo != null) {
			id = merchantInfo.getMerchantId();
			name = merchantInfo.getMerchantName();
		}
		// else if (managerInfo != null) {
		// id = managerInfo.getManagerId();
		// name = managerInfo.getManagerName();
		// }
		return morderService.deleteOrderGoodsInfo(id, name, idPack);
	}

	// 商户修改手工订单信息
	public Map<String, Object> editMorderInfo(JSONObject json, int length, int flag) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		String[] strArr = new String[length];
		for (int i = 0; i < length; i++) {
			String value = json.getString(Integer.toString(i));
			strArr[i] = value;
		}
		return morderService.editMorderInfo(merchantId, merchantName, strArr, flag);
	}

	public Map<String, Object> addOrderGoodsInfo(int length, HttpServletRequest req) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		String[] strArr = new String[length];
		// 0是长度,参数从1开始
		int i = 0;
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			strArr[i] = req.getParameter(key);
			i++;
		}
		return morderService.addOrderGoodsInfo(merchantId, merchantName, strArr);
	}

	public Map<String, Object> updateOldCreateBy() {
		return morderService.updateOldCreateBy();
	}

	public Map<String, Object> updateOldPaymentCreateBy() {
		return morderService.updateOldPaymentCreateBy();
	}

	public Map<String, Object> updateManualOrderGoodsInfo(String startTime, String endTime,
			Map<String, Object> customsMap) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return morderService.updateManualOrderGoodsInfo(startTime, endTime, merchantId, merchantName, customsMap);
	}

	public Map<String, Object> managerLoadMorderDatas(int page, int size, HttpServletRequest req) {
		Map<String, Object> params = new HashMap<>();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		params.remove("page");
		params.remove("size");
		return morderService.managerLoadMorderDatas(params, page, size);
	}

	public Map<String, Object> pretreatmentGroupAddOrder(HttpServletRequest req) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reqMap = fileUpLoadService.universalDoUpload(req, "/gadd-excel/", ".xls", false, 400, 400,
				null);
		String serialNo = "";
		if ((int) reqMap.get("status") == 1) {
			List<String> list = (List<String>) reqMap.get("datas");
			if (list.isEmpty()) {
				return ReturnInfoUtils.errorInfo("文件上传失败,请重试!");
			}
			File file = new File("/gadd-excel/" + list.get(0));
			ExcelUtil excel = new ExcelUtil(file);
			excel.open();
			// 总列数
			int columnTotalCount = excel.getColumnCount(0, 0);
			// 有数据的总行数
			int realRowCount = excelRealRowCount(excel.getRowCount(0), excel);
			if (realRowCount <= 0) {
				return ReturnInfoUtils.errorInfo("导入失败,请检查是否有数据或序号符合要求!");
			}
			if (columnTotalCount == 14) { // 企邦模板表格长度(增加了序号列)
				Subject currentUser = SecurityUtils.getSubject();
				// 获取商户登录时,shiro存入在session中的数据
				Merchant merchantInfo = (Merchant) currentUser.getSession()
						.getAttribute(LoginType.MERCHANTINFO.toString());
				String merchantId = merchantInfo.getMerchantId();
				String merchantName = merchantInfo.getMerchantName();
				// 用于前台区分哪家批次号
				serialNo = "QB_" + SerialNoUtils.getSerialNo("QB");
				// 多了一行说明
				realRowCount += 1;
				invokeTaskUtils.startTask(4, realRowCount, file, merchantId, serialNo, merchantName);
				statusMap.put("serialNo", serialNo);
			} else if (columnTotalCount == 71 && checkGZTable(excel)) {// 国宗订单模板长度(加上序号列)
				// 获取预处理导入订单批次号
				serialNo = "GZ_" + SerialNoUtils.getSerialNo("TGZ");
				invokeTaskUtils.startTask(5, realRowCount, file, null, serialNo, null);
				statusMap.put("serialNo", serialNo);
			} else {
				return ReturnInfoUtils.errorInfo("导入失败,请检查订单模板是否符合规范!");
			}
			excel.closeExcel();
			statusMap.put("status", 1);
			statusMap.put("msg", "执行成功,正在读取数据....");
			return statusMap;
		}
		return ReturnInfoUtils.errorInfo("上传文件失败,服务器繁忙!");
	}

	/**
	 * 根据最初的国宗订单导入模板进行表单校验
	 * 
	 * @param excel
	 * @return boolean
	 */
	private boolean checkGZTable(ExcelUtil excel) {
		// 总列数
		if (excel.getColumnCount(0, 0) != 71) {
			return false;
		}
		if (!excel.getCell(0, 0, 0).contains("序号")) {
			return false;
		}
		if (!excel.getCell(0, 0, 1).contains("订单")) {
			return false;
		}
		if (!excel.getCell(0, 0, 2).contains("物流企业备案号")) {
			return false;
		}
		if (!excel.getCell(0, 0, 3).contains("电商平台备案号")) {
			return false;
		}
		if (!excel.getCell(0, 0, 4).contains("企业运单编号")) {
			return false;
		}
		if (!excel.getCell(0, 0, 5).contains("物流状态")) {
			return false;
		}
		if (!excel.getCell(0, 0, 6).contains("运费")) {
			return false;
		}
		if (!excel.getCell(0, 0, 7).contains("运费币制")) {
			return false;
		}
		if (!excel.getCell(0, 0, 8).contains("运输方式")) {
			return false;
		}
		if (!excel.getCell(0, 0, 9).contains("运输工具名称")) {
			return false;
		}
		if (!excel.getCell(0, 0, 10).contains("包装种类")) {
			return false;
		}
		if (!excel.getCell(0, 0, 11).contains("保价费")) {
			return false;
		}
		if (!excel.getCell(0, 0, 12).contains("保价费币制")) {
			return false;
		}
		if (!excel.getCell(0, 0, 13).contains("分运单净重")) {
			return false;
		}
		if (!excel.getCell(0, 0, 14).contains("分运单毛重")) {
			return false;
		}
		if (!excel.getCell(0, 0, 16).contains("主要商品")) {
			return false;
		}

		if (!excel.getCell(0, 0, 17).contains("进出口岸代码")) {
			return false;
		}
		if (!excel.getCell(0, 0, 18).contains("收件人姓名")) {
			return false;
		}
		if (!excel.getCell(0, 0, 19).contains("收件人所在国家")) {
			return false;
		}
		if (!excel.getCell(0, 0, 20).contains("收件人省市区代码")) {
			return false;
		}
		if (!excel.getCell(0, 0, 21).contains("收件人地址")) {
			return false;
		}
		if (!excel.getCell(0, 0, 22).contains("收件人电话")) {
			return false;
		}

		if (!excel.getCell(0, 0, 23).contains("收件人证件类型")) {
			return false;
		}
		if (!excel.getCell(0, 0, 24).contains("收件人证件号码")) {
			return false;
		}
		if (!excel.getCell(0, 0, 25).contains("包裹单号")) {
			return false;
		}
		if (!excel.getCell(0, 0, 26).contains("发货人名称")) {
			return false;
		}
		if (!excel.getCell(0, 0, 27).contains("发货人所在国家")) {
			return false;
		}
		if (!excel.getCell(0, 0, 28).contains("发货人省市区代码")) {
			return false;
		}
		if (!excel.getCell(0, 0, 29).contains("发货人地址")) {
			return false;
		}
		if (!excel.getCell(0, 0, 30).contains("发货人电话")) {
			return false;
		}
		if (!excel.getCell(0, 0, 31).contains("子订单编号")) {
			return false;
		}
		if (!excel.getCell(0, 0, 32).contains("电商商户企业备案号")) {
			return false;
		}
		if (!excel.getCell(0, 0, 33).contains("商品货号")) {
			return false;
		}
		if (!excel.getCell(0, 0, 34).contains("原产国")) {
			return false;
		}
		if (!excel.getCell(0, 0, 35).contains("计量单位")) {
			return false;
		}
		if (!excel.getCell(0, 0, 36).contains("数量")) {
			return false;
		}
		if (!excel.getCell(0, 0, 37).contains("商品总价")) {
			return false;
		}
		if (!excel.getCell(0, 0, 38).contains("载货清单号")) {
			return false;
		}

		if (!excel.getCell(0, 0, 39).contains("商品")) {
			return false;
		}
		if (!excel.getCell(0, 0, 40).contains("单价")) {
			return false;
		}
		if (!excel.getCell(0, 0, 41).contains("商品币制")) {
			return false;
		}
		if (!excel.getCell(0, 0, 43).contains("进出口标识")) {
			return false;
		}
		if (!excel.getCell(0, 0, 48).contains("代码")) {
			return false;
		}
		if (!excel.getCell(0, 0, 49).contains("毛重")) {
			return false;
		}
		if (!excel.getCell(0, 0, 50).contains("仓单申报类型")) {
			return false;
		}
		if (!excel.getCell(0, 0, 51).contains("第一法定数量")) {
			return false;
		}
		if (!excel.getCell(0, 0, 52).contains("第一法定计量单位")) {
			return false;
		}
		if (!excel.getCell(0, 0, 53).contains("第二")) {
			return false;
		}
		if (!excel.getCell(0, 0, 54).contains("第二法定计量单位")) {
			return false;
		}
		if (!excel.getCell(0, 0, 55).contains("品牌")) {
			return false;
		}
		if (!excel.getCell(0, 0, 56).contains("规格")) {
			return false;
		}
		if (!excel.getCell(0, 0, 57).contains("国检备案号")) {
			return false;
		}

		return true;
	}

	public final void pretreatmentGZTable(int sheet, ExcelUtil excel, List<Map<String, Object>> errl, int startCount,
			int endCount, String serialNo, int realRowCount) {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHMMSS");
		String OrderDate = null, waybill = null, dateSign, RecipientName = null, RecipientID = null,
				RecipientTel = null, RecipientAddr = null, OrderDocAcount, OrderDocName, OrderDocId, OrderDocTel,
				goodsName = "";
		int seqNo = 0;
		String orderId = "";//
		String EntGoodsNo = "";// 商品货号、条形码
		String ciqGoodsNo = "";// 检验检疫商品备案编号
		String cusGoodsNo = "";// 海关正式备案编号
		String hsCode = "";// HS编码
		String goodsStyle = "";// 规格型号
		String originCountry = "";// 原产国
		String RecipientProvincesCode = "";
		String provinceCode = "";// 省份编码
		String cityCode = "";// 城市编码
		String areaCode = "";// 区域编码
		String provinceName = "";// 省份名称
		String cityName = "";// 城市名称
		String areaName = "";// 区域名称
		String unit = "";// 计量单位
		String currCode = "";// 币制
		double ActualAmountPaid, FCY = 0, Tax = 0;
		int total_count = 0;// 商品数量
		double price = 0;
		double netWt = 0.0; // 净重
		double grossWt = 0.0;// 毛重
		double firstLegalCount = 0.0;// 第一法定数量
		double secondLegalCount = 0.0;// 第二法定数量
		String stdUnit = "";// 第一法定计量单位
		String secUnit = "";// 第二法定计量单位
		int numOfPackages = 0;// 箱件数 （同一订单商品总件数，例如同一订单有6支护手霜和4瓶钙片则填写10，即6+4=10。)
		int packageType = 0;// 包装种类
		String transportModel = "";// 运输方式
		String postal = "";// 邮编
		String senderName = ""; // 发货人姓名
		String senderCountry = "";// 发货人国家代码
		String senderAreaCode = "";// 发货人区域代码 国外填 000000
		String senderAddress = "";// 发货人地址
		String senderTel = "";// 发货人电话
		String brand = "";// 品牌
		System.out.println("--------------开始循环表单中数据-----------");
		int totalColumnCount = excel.getColumnCount(0);
		try {
			for (int r = startCount; r <= endCount; r++) {
				if (excel.getColumnCount(sheet, r) == 0) {
					break;
				}
				for (int c = 0; c < totalColumnCount; c++) {

					String value = excel.getCell(sheet, r, c);
					if (c == 0 && "".equals(value)) {
						break;
					}
					if (c == 0) {
						// 序号
						if (StringEmptyUtils.isNotEmpty(value)) {
							seqNo = Integer.parseInt(value);
						} else {
							String msg = "【表格】第" + (r + 1) + "行-->表单序号错误,请核对信息!";
							RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "orderImport", 1);
							break;
						}
					} else if (c == 1) {
						if (StringEmptyUtils.isNotEmpty(value)) {
							orderId = value;
						}
					} else if (c == 4) {
						// 运单号
						if (StringEmptyUtils.isNotEmpty(value)) {
							waybill = value.replaceAll(" ", "");
						} else {
							String msg = "【表格】第" + (r + 1) + "行-->运单号不能为空,请核对信息!";
							RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "orderImport", 1);
							break;
						}
					} else if (c == 8) {
						// 运输方式
						if (StringEmptyUtils.isNotEmpty(value)) {
							transportModel = value;
						} else {
							transportModel = "4";
						}
					} else if (c == 10) {
						// 包装种类
						if (StringEmptyUtils.isNotEmpty(value)) {
							packageType = Integer.parseInt(value);
						} else {
							packageType = 4;
						}
					} else if (c == 13) {
						// 净重
						if (StringEmptyUtils.isNotEmpty(value)) {
							netWt = Double.parseDouble(value);
						}
					} else if (c == 14) {
						// 毛重
						if (StringEmptyUtils.isNotEmpty(value)) {
							grossWt = Double.parseDouble(value);
						}
					} else if (c == 15) {
						// 箱件数
						if (StringEmptyUtils.isNotEmpty(value)) {
							numOfPackages = Integer.parseInt(value);
						}
					} else if (c == 16) {
						// 商品名
						goodsName = value;
					} else if (c == 18) {
						// 收货人姓名
						RecipientName = value.replaceAll("  ", "").replaceAll(" ", "");
					} else if (c == 20) {
						// 邮编代码
						if (StringEmptyUtils.isNotEmpty(value)) {
							postal = value;
						}
					} else if (c == 21) {
						// 收货地址
						RecipientAddr = value;
					} else if (c == 22) {
						// 收货人电话
						RecipientTel = value;
					} else if (c == 24) {
						// 收货人身份证
						RecipientID = value.replace("x", "X").replaceAll("  ", "").replaceAll(" ", "");
					} else if (c == 26) {
						senderName = value; // 发货人姓名
					} else if (c == 27) {
						senderCountry = value;// 发货人国家代码
					} else if (c == 28) {
						senderAreaCode = value;// 发货人区域代码 国外填 000000
					} else if (c == 29) {
						senderAddress = value;// 发货人地址
					} else if (c == 30) {
						senderTel = value;// 发货人电话
					} else if (c == 33) {
						// 商品货号
						EntGoodsNo = value.replaceAll(" ", "");
					} else if (c == 34) {// 原产国
						originCountry = value;
					} else if (c == 35) {
						// 计量单位
						if (StringEmptyUtils.isNotEmpty(value)) {
							value = goodsRecordTransaction.findUnit(value);
							if (StringEmptyUtils.isNotEmpty(value)) {
								unit = value;
							}
						}
					} else if (c == 36) {
						// 数量
						total_count = Integer.parseInt(value);
					} else if (c == 40) {
						// 单价
						if (StringEmptyUtils.isNotEmpty(value)) {
							price = Double.parseDouble(value);
						}
					} else if (c == 39) {
						// HS编码
						hsCode = value;
					} else if (c == 41) {// 币制
						currCode = value;
					} else if (c == 51) {
						// 第一法定数量
						if (StringEmptyUtils.isNotEmpty(value)) {
							firstLegalCount = Double.parseDouble(value);
						} else {
							firstLegalCount = 0.0;
						}
					} else if (c == 52) {
						// 第一法定计量单位
						if (StringEmptyUtils.isNotEmpty(value)) {
							stdUnit = value;
						}
					} else if (c == 53) {
						if (StringEmptyUtils.isNotEmpty(value)) {
							// 第二法定数量
							secondLegalCount = Double.parseDouble(value);
						} else {
							secondLegalCount = 0.0;
						}
					} else if (c == 54) {
						// 第二法定计量单位
						if (StringEmptyUtils.isNotEmpty(value)) {
							secUnit = value;
						}
					} else if (c == 55) {
						// 品牌
						if (StringEmptyUtils.isNotEmpty(value)) {
							brand = value;
						}
					} else if (c == 56) {
						// 商品规格
						goodsStyle = value;
					} else if (c == 57) {
						// 商品国检备案号
						ciqGoodsNo = value;
					}
				}
				if (!checkEntGoodsNoLength(EntGoodsNo)) {
					String msg = "【表格】第" + (r + 1) + "行-->运单号[" + waybill + "]商品货号长度超过20,请核对商品货号是否正确!";
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "checkGZOrderImport", 1);
					continue;
				}
				if (netWt > grossWt) {
					String msg = "【表格】第" + (r + 1) + "行-->运单号[" + waybill + "]商品净重大于毛重,请核实信息!";
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "checkGZOrderImport", 5);
				}
				if (!IdcardValidator.validate18Idcard(RecipientID)) {
					String msg = "【表格】第" + (r + 1) + "行-->运单号[" + waybill + "]实名认证不通过,请核实身份证与姓名信息!";
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "checkGZOrderImport", 4);
				}
				Map<String, Object> provinceMap = searchProvinceCityArea(RecipientAddr);
				if (provinceMap == null) {
					String msg = "【表格】第" + (r + 1) + "行-->运单号[" + waybill + "]收货人地址填写有误,请核对信息!";
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "checkGZOrderImport", 3);
				} else {
					Map<String, Object> reProvinceCityAreaMap = doProvinceCityArea(provinceMap);
					if (reProvinceCityAreaMap != null) {
						areaCode = reProvinceCityAreaMap.get("areaCode") + "";
						areaName = reProvinceCityAreaMap.get("areaName") + "";
						cityCode = reProvinceCityAreaMap.get("cityCode") + "";
						cityName = reProvinceCityAreaMap.get("cityName") + "";
						provinceCode = reProvinceCityAreaMap.get("provinceCode") + "";
						provinceName = reProvinceCityAreaMap.get("provinceName") + "";
					}
				}
				OrderDocName = RecipientName;
				OrderDocId = RecipientID;
				OrderDocTel = RecipientTel;
				OrderDocAcount = getOrderDocAcount();// 平台账号
				FCY = total_count * price;
				ActualAmountPaid = FCY + Tax;
				OrderDate = sdf.format(new Date());
				dateSign = OrderDate.substring(0, 8);
				// 根据 商品名 商品货号 查找出已备案商品信息，生成订单关联的商品数据
				JSONObject goodsInfo = new JSONObject();
				goodsInfo.put("entGoodsNo", EntGoodsNo);
				goodsInfo.put("HSCode", hsCode);
				goodsInfo.put("Brand", brand);
				goodsInfo.put("BarCode", EntGoodsNo);
				goodsInfo.put("GoodsName", goodsName);
				goodsInfo.put("OriginCountry", originCountry);
				if (StringEmptyUtils.isNotEmpty(cusGoodsNo)) {
					goodsInfo.put("CusGoodsNo", cusGoodsNo);
				} else {// 当海关备案编码为空时,则默认填写*
					goodsInfo.put("CusGoodsNo", "*");
				}
				goodsInfo.put("CIQGoodsNo", ciqGoodsNo);
				goodsInfo.put("GoodsStyle", goodsStyle);
				goodsInfo.put("Unit", unit);
				goodsInfo.put("Price", price);
				goodsInfo.put("Qty", total_count);
				goodsInfo.put("netWt", netWt);
				goodsInfo.put("grossWt", grossWt);
				goodsInfo.put("firstLegalCount", firstLegalCount);
				goodsInfo.put("secondLegalCount", secondLegalCount);
				goodsInfo.put("stdUnit", stdUnit);
				goodsInfo.put("secUnit", secUnit);
				goodsInfo.put("numOfPackages", numOfPackages);
				goodsInfo.put("packageType", packageType);
				goodsInfo.put("transportModel", transportModel);
				goodsInfo.put("seqNo", seqNo);
				String[] str = serialNo.split("_");
				int serial = Integer.parseInt(str[1]);
				Map<String, Object> reGoodsMap = checkGoodsInfo(goodsInfo);
				if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
					String msg = "【表格】第" + (r + 1) + "行-->" + reGoodsMap.get(BaseCode.MSG.toString()) + "";
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "checkGZOrderImport", 1);
					continue;
				}
				// 检验国宗订单信息
				Map<String, Object> item = morderService.checkGZOrder(waybill, serial, dateSign, OrderDate, FCY, Tax,
						ActualAmountPaid, RecipientName, RecipientID, RecipientTel, RecipientProvincesCode,
						RecipientAddr, OrderDocAcount, OrderDocName, OrderDocId, OrderDocTel, senderName, senderCountry,
						senderAreaCode, senderAddress, senderTel, areaCode, cityCode, provinceCode, postal,
						provinceName, cityName, areaName, orderId, goodsInfo);
				if ("10".equals(item.get("status") + "")) {// 当遇到超额时
					String msg = "【表格】第" + (r + 1) + "行-->" + item.get("msg") + "";
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "checkGZOrderImport", 2);
				} else if (!"1".equals(item.get("status") + "")) {
					String msg = "【表格】第" + (r + 1) + "行-->" + item.get("msg") + "";
					RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "checkGZOrderImport", 1);
					continue;
				}
				excelBufferUtils.writeRedis(errl, realRowCount, serialNo, "checkGZOrderImport");
			}
		} catch (Exception e) {
			e.printStackTrace();
			String msg = "表格数据不符合规范,请核对所有数据排序是否正确!";
			RedisInfoUtils.commonErrorInfo(msg, errl, realRowCount, serialNo, "checkGZOrderImport", 1);
		} finally {
			excel.closeExcel();
			excelBufferUtils.writeCompletedRedis(errl, realRowCount, serialNo, "checkGZOrderImport");
		}
	}

	/**
	 * (预处理)企邦表单,暂默认为14列数据
	 * 
	 * @param sheet
	 *            工作表索引
	 * @param excel
	 *            工具类
	 * @param errl
	 *            错误信息
	 * @param merchantId
	 *            商户Id
	 * @param serialNo
	 *            批次号
	 * @param startCount
	 *            开始行数
	 * @param endCount
	 *            结束行数
	 * @param realRowCount
	 *            总行数
	 * @param merchantName
	 *            商户名称
	 * @return Map
	 */
	public void pretreatmentQBTable(int sheet, ExcelUtil excel, List<Map<String, Object>> errl, String merchantId,
			String serialNo, int startCount, int endCount, int realRowCount, String merchantName) {
		int seqNo = 0;
		String orderId = "";// 订单Id
		String entGoodsNo = "";// 商品自编号
		String goodsName = "";// 商品名称
		double price = 0;// 单价
		int count = 0;// 商品数量
		String recipientName = "";// 收货人名称
		String recipientTel = "";// 收货人电话
		String recipientAddr = "";// 收货人详细地址
		String marCode = "";// (启邦客户)商品归属商家代码
		String orderDocName = "";// 下单人名称
		String orderDocId = "";// 下单人身份证号码
		String ehsEntName = "";// 承运商
		String waybillNo = "";// 运单编号
		System.out.println("--------------开始循环表单中数据-----------");
		int totalColumnCount = excel.getColumnCount(0);
		// for (int r = 2; r <= rowTotalCount; r++) {
		try {
			for (int r = startCount; r <= endCount; r++) {
				if (excel.getColumnCount(sheet, r) == 0) {
					break;
				}
				for (int c = 0; c < totalColumnCount; c++) {
					String value = excel.getCell(sheet, r, c);
					if (c == 0 && "".equals(value)) {
						break;
					}
					if (c == 0) {
						// 序号
						if (StringEmptyUtils.isNotEmpty(value)) {
							seqNo = Integer.parseInt(value);
						} else {
							String msg = "【表格】第" + (r + 1) + "行-->表单序号错误,请核对信息!";
							RedisInfoUtils.commonErrorInfo(msg, errl, (realRowCount - 1), serialNo,
									"checkQBOrderImport", 1);
							break;
						}
					} else if (c == 1) {
						// 订单Id
						orderId = value;
					} else if (c == 2) {
						// 商品自编号
						if (StringEmptyUtils.isNotEmpty(value)) {
							entGoodsNo = value;
						}
					} else if (c == 3) {
						// 商品名称
						if (StringEmptyUtils.isNotEmpty(value)) {
							goodsName = value;
						}
					} else if (c == 4) {
						// 商品单价
						try {
							if (StringEmptyUtils.isNotEmpty(value)) {
								price = Double.parseDouble(value);
							}
						} catch (Exception e) {
							Map<String, Object> errMap = new HashMap<>();
							errMap.put(BaseCode.MSG.toString(), "【表格】第" + (r + 1) + "行,商品单价输入错误!");
							errl.add(errMap);
							break;
						}
					} else if (c == 5) {
						// 数量
						try {
							if (StringEmptyUtils.isNotEmpty(value)) {
								count = Integer.parseInt(value);
							}
						} catch (Exception e) {
							Map<String, Object> errMap = new HashMap<>();
							errMap.put(BaseCode.MSG.toString(), "【表格】第" + (r + 1) + "行,商品数量输入错误!");
							errl.add(errMap);
							break;
						}
					} else if (c == 6) {
						// 收货人姓名
						recipientName = value;
					} else if (c == 7) {
						// 收件人电话
						recipientTel = value;
					} else if (c == 8) {
						// 收货人详细地址
						recipientAddr = value;
					} else if (c == 9) {
						// (启邦客户)商品归属商家代码
						marCode = value;
					} else if (c == 10) {
						orderDocName = value;
					} else if (c == 11) {
						// 身份证号码
						orderDocId = value.replace("x", "X");
					} else if (c == 12) {
						// 承运商
						ehsEntName = value;
					} else if (c == 13) {
						waybillNo = value;
					}
				}
				if (!IdcardValidator.validate18Idcard(orderDocId)) {
					String msg = "【表格】第" + (r + 1) + "行-->订单号[" + orderId + "]实名认证不通过,请核实身份证与姓名信息!";
					RedisInfoUtils.commonErrorInfo(msg, errl, (realRowCount - 1), serialNo, "checkQBOrderImport", 4);
				}
				Map<String, Object> item = new HashMap<>();
				Map<String, Object> provinceMap = searchProvinceCityArea(recipientAddr);
				if (provinceMap == null) {
					String msg = "【表格】第" + (r + 1) + "行-->订单号[" + orderId + "]收货人地址不符合规范,请核对信息!";
					RedisInfoUtils.commonErrorInfo(msg, errl, (realRowCount - 1), serialNo, "checkQBOrderImport", 3);
				}
				Map<String, Object> reProvinceCityAreaMap = doProvinceCityArea(provinceMap);
				if (reProvinceCityAreaMap != null) {
					item.put("areaCode", reProvinceCityAreaMap.get("areaCode") + "");
					item.put("areaName", reProvinceCityAreaMap.get("areaName") + "");
					item.put("cityCode", reProvinceCityAreaMap.get("cityCode") + "");
					item.put("cityName", reProvinceCityAreaMap.get("cityName") + "");
					item.put("provinceCode", reProvinceCityAreaMap.get("provinceCode") + "");
					item.put("provinceName", reProvinceCityAreaMap.get("provinceName") + "");
				}
				item.put("seqNo", seqNo);
				item.put("orderId", orderId);
				item.put("entGoodsNo", entGoodsNo);
				item.put("goodsName", goodsName);
				item.put("price", price);
				item.put("count", count);
				double orderTotalPrice = count * price;
				item.put("orderTotalPrice", orderTotalPrice);
				// 实际支付金额
				item.put("actualAmountPaid", orderTotalPrice);
				// 税费
				double tax = 0;
				item.put("tax", tax);
				item.put("recipientName", recipientName);
				item.put("recipientTel", recipientTel);
				item.put("recipientAddr", recipientAddr);
				item.put("orderDocAcount", recipientName);
				item.put("orderDocName", orderDocName);
				item.put("orderDocId", orderDocId);
				// 下单人电话
				item.put("orderDocTel", recipientTel);
				// 企邦订单承运商
				item.put("ehsEntName", ehsEntName);
				item.put("waybillNo", waybillNo);
				String[] str = serialNo.split("_");
				int serial = Integer.parseInt(str[1]);
				// 企邦批次号
				item.put("serial", serial);
				item.put("marCode", marCode);
				Map<String, Object> orderMap = morderService.checkQBOrder(merchantId, item, merchantName);
				if ("10".equals(orderMap.get("status") + "")) {// 当遇到超额时,继续将剩下的订单商品导入
					String msg = "【表格】第" + (r + 1) + "行-->" + orderMap.get("msg") + "";
					RedisInfoUtils.commonErrorInfo(msg, errl, (realRowCount - 1), serialNo, "checkQBOrderImport", 2);
				} else if (!"1".equals(orderMap.get("status") + "")) {
					String msg = "【表格】第" + (r + 1) + "行-->" + orderMap.get("msg") + "";
					RedisInfoUtils.commonErrorInfo(msg, errl, (realRowCount - 1), serialNo, "checkQBOrderImport", 1);
					continue;
				}
				excelBufferUtils.writeRedis(errl, (realRowCount - 1), serialNo, "checkQBOrderImport");
			}
		} catch (Exception e) {
			e.printStackTrace();
			String msg = "表格数据不符合规范,请核对所有数据排序是否正确!";
			RedisInfoUtils.commonErrorInfo(msg, errl, (realRowCount - 1), serialNo, "checkQBOrderImport", 1);
		} finally {
			excel.closeExcel();
			excelBufferUtils.writeCompletedRedis(errl, (realRowCount - 1), serialNo, "checkQBOrderImport");
		}
	}

	public Map<String, Object> managerDeleteMorderDatas(JSONArray json) {

		return morderService.managerDeleteMorderDatas(json);
	}

}
