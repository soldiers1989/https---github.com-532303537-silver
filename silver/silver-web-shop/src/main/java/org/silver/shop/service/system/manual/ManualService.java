package org.silver.shop.service.system.manual;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.manual.MorderService;
import org.silver.shop.model.common.base.Metering;
import org.silver.shop.model.common.base.Province;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.service.common.base.MeteringTransaction;
import org.silver.shop.service.common.base.PostalTransaction;
import org.silver.shop.service.common.base.ProvinceCityAreaTransaction;
import org.silver.shop.task.ExcelTask;
import org.silver.shop.utils.ExcelUtil;
import org.silver.util.AppUtil;
import org.silver.util.BufferUtils;
import org.silver.util.DateUtil;
import org.silver.util.FileUpLoadService;
import org.silver.util.FileUtils;
import org.silver.util.JedisUtil;
import org.silver.util.SerialNoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.SortUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONObject;

@Service("manualService")
public class ManualService {

	@Reference
	private MorderService morderService;
	@Autowired
	private FileUpLoadService fileUpLoadService;
	@Autowired
	private MeteringTransaction meteringTransaction;
	@Autowired
	private ProvinceCityAreaTransaction provinceCityAreaTransaction;

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

	public Map<String, Object> deleteByOrderId(String marchant_no, String order_id) {

		return morderService.deleteByOrderId(marchant_no, order_id);
	}

	public Map<String, Object> groupAddOrder(HttpServletRequest req) {
		Map<String, Object> statusMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		Map<String, Object> reqMap = fileUpLoadService.universalDoUpload(req, "/gadd-excel/", ".xls", false, 400, 400,
				null);
		List<Map<String, Object>> errl = new ArrayList<>();
		String serialNo = "";
		if ((int) reqMap.get("status") == 1) {
			List<String> list = (List<String>) reqMap.get("datas");
			File file = new File("/gadd-excel/" + list.get(0));
			ExcelUtil excel = new ExcelUtil(file);
			excel.open();
			// 总列数
			int columnTotalCount = excel.getColumnCount(0, 0);
			// 有数据的总行数
			int realRowCount = excelRealRowCount(excel.getRowCount(0), excel);
			if (realRowCount <= 0) {
				statusMap.put("status", -1);
				statusMap.put("msg", "导入失败,请检查是否有数据或数据符合要求!");
				return statusMap;
			}
			if (columnTotalCount == 14) { // 企邦模板表格长度(增加了序号列)
				serialNo = "QB_" + SerialNoUtils.getSerialNo("QB");
				// 企邦表单多了一行说明
				realRowCount = realRowCount + 1;
				// readQBSheet(0, excel, errl, merchantId, serialNo);
				startTask(1, realRowCount, file, errl, merchantId, serialNo);
				// 用于前台区分哪家批次号
				statusMap.put("serialNo", serialNo);
			} else if (columnTotalCount == 70) {// 国宗订单模板长度(加上序号列)
				// readGZSheet(0, excel, errl, merchantId,0,0);
				serialNo = "GZ_" + SerialNoUtils.getSerialNo("GZ");
				startTask(2, realRowCount, file, errl, merchantId, serialNo);
				// 用于前台区分哪家批次号
				statusMap.put("serialNo", serialNo);
			} else {
				statusMap.put("status", -1);
				statusMap.put("msg", "导入失败,请检查订单模板是否符合规范!");
				return statusMap;
			}
			excel.closeExcel();
			file.delete();
			statusMap.put("status", 1);
			statusMap.put("msg", "执行成功,正在读取数据");
			return statusMap;
		}
		return null;
	}

	/**
	 * 通过逆向工程获取表单中真实数据行数(默认再总行数上减1)
	 * 
	 * @param rowTotalCount
	 *            总行数
	 * @param excel
	 */
	private int excelRealRowCount(int rowTotalCount, ExcelUtil excel) {
		int realRowCount = 0;
		for (int r = rowTotalCount; r > 0; r--) {
			if (excel.getColumnCount(r) == 0) {
				return r;
			}
			// 默认只需要读取表单中的第一列(序号)
			for (int c = 0; c < excel.getColumnCount(0); c++) {
				String value = excel.getCell(0, r, c);
				// 判断序号是否有值
				if (c == 0 && StringEmptyUtils.isNotEmpty(value)) {
					// 取最后一条数据的序号作为总行数
					realRowCount = Integer.parseInt(value);
					return realRowCount;
				}
			}
		}
		return -1;
	}

	/**
	 * 启动线程,根据启动线程数量对应CPU数
	 * 
	 * @param flag
	 *            1-企邦,2-国宗
	 * @param totalCount
	 *            总行数
	 * @param file
	 *            excel文件
	 * @param errl
	 *            错误List
	 * @param merchantId
	 *            商户Id
	 * @param serialNo
	 *            导入批次号
	 */
	private void startTask(int flag, int totalCount, File file, List errl, String merchantId, String serialNo) {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		// 判断当前计算机CPU线程个数
		int cpuCount = Runtime.getRuntime().availableProcessors();
		// int cpuCount = 1;
		// 开始行数
		int startCount = flag == 1 ? 2 : 1;
		// 结束行数
		int end = 0;
		ExcelTask excelTask = null;
		if (totalCount < cpuCount) {// 不需要开辟多线程
			ExcelUtil excelC = new ExcelUtil(file);
			excelTask = new ExcelTask(0, excelC, errl, merchantId, startCount, totalCount, this, serialNo, flag,
					totalCount);
			threadPool.submit(excelTask);
		} else {
			for (int i = 0; i < cpuCount; i++) {
				// 副本文件名
				String imgName = AppUtil.generateAppKey() + "_" + System.currentTimeMillis() + ".xlsx";
				File dest = new File(file.getParentFile() + "/" + imgName);
				try {
					FileUtils.copyFileUsingFileChannels(file, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// 打开副本文件
				ExcelUtil excelC = new ExcelUtil(dest);
				if (i == 0) {
					end = totalCount / cpuCount;
					excelTask = new ExcelTask(0, excelC, errl, merchantId, startCount, end, this, serialNo, flag,
							totalCount);
				} else {
					startCount = end + 1;
					end = startCount + (totalCount / cpuCount);
					if (i == (cpuCount - 1)) {// 最后一次
						excelTask = new ExcelTask(0, excelC, errl, merchantId, startCount, totalCount, this, serialNo,
								flag, totalCount);
					} else {
						excelTask = new ExcelTask(0, excelC, errl, merchantId, startCount, end, this, serialNo, flag,
								totalCount);
					}
				}

				threadPool.submit(excelTask);
			}
		}
		threadPool.shutdown();
	}

	/**
	 * 企邦表单,暂默认为14列数据
	 * 
	 * @param sheet
	 * @param excel
	 * @param errl
	 * @param merchantId
	 * @param serialNo
	 * @param startCount
	 * @param endCount
	 * @param realRowCount
	 * @return
	 */
	public Map<String, Object> readQBSheet(int sheet, ExcelUtil excel, List<Map<String, Object>> errl,
			String merchantId, String serialNo, int startCount, int endCount, int realRowCount) {
		Map<String, Object> statusMap = new HashMap<>();
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
		try {
			// for (int r = 2; r <= rowTotalCount; r++) {
			for (int r = startCount; r <= endCount; r++) {
				if (excel.getColumnCount(r) == 0) {
					break;
				}
				for (int c = 0; c < excel.getColumnCount(r); c++) {
					String value = excel.getCell(sheet, r, c);
					if (c == 0) {
						// 序号
						seqNo = Integer.parseInt(value);
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
							errMap.put(BaseCode.MSG.toString(), "第" + (r + 1) + "行,商品单价输入错误!");
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
							errMap.put(BaseCode.MSG.toString(), "第" + (r + 1) + "行,商品数量输入错误!");
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
						orderDocId = value;
					} else if (c == 12) {
						// 承运商
						ehsEntName = value;
					} else if (c == 13) {
						waybillNo = value;
					}
				}

				Map<String, Object> item = new HashMap<>();
				Map<String, Object> provinceMap = searchProvinceCityArea(recipientAddr);
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
				Map<String, Object> orderMap = morderService.createQBOrder(merchantId, item);
				if (!"1".equals(orderMap.get(BaseCode.STATUS.toString()))) {
					Map<String, Object> errMap = new HashMap<>();
					errMap.put(BaseCode.MSG.toString(),
							"第" + (r + 1) + "行,---->" + orderMap.get(BaseCode.MSG.toString()));
					errl.add(errMap);
				} else {
					Map<String, Object> orderSubMap = morderService.createQBOrderSub(merchantId, item);
					if ((int) orderSubMap.get("status") != 1) {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "第" + (r + 1) + "行-->" + orderSubMap.get("msg"));
						errl.add(errMap);
					}
				}
				BufferUtils.writeRedis("1", errl, (realRowCount - 1),  serialNo, "order");
			}
			if (!errl.isEmpty()) {
				errl = SortUtil.sortList(errl);
			}
			File file2 = excel.getFile();
			// excel.closeExcel();
			if (!file2.delete()) {
				System.out.println("-----------文件没有删除--------------");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BufferUtils.writeRedis("2", errl, (realRowCount - 1), serialNo, "order");
		return statusMap;
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
	 * @return Map
	 */
	public final Map<String, Object> readGZSheet(int sheet, ExcelUtil excel, List<Map<String, Object>> errl,
			String merchantId, int startCount, int endCount, String serialNo, int realRowCount) {
		Map<String, Object> statusMap = new HashMap<>();
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
		try {
			for (int r = startCount; r <= endCount; r++) {
				if (excel.getColumnCount(r) == 0) {
					break;
				}
				for (int c = 0; c < excel.getColumnCount(r); c++) {
					String value = excel.getCell(sheet, r, c);
					if (c == 0 && "".equals((value + "").trim())) {
						break;
					}
					if (c == 0) {
						// 序号
						// if (StringEmptyUtils.isNotEmpty(value)) {
						seqNo = Integer.parseInt(value);
						// }
					} else if (c == 1) {
						orderId = value.trim();
					} else if (c == 4) {
						System.out.println(value);// 运单号
						waybill = value;
					} else if (c == 16) {
						System.out.println(value);// 商品名
						goodsName = value.trim();
					} else if (c == 18) {
						System.out.println(value);// 收货人姓名
						RecipientName = value.trim().replaceAll("  ", "").replaceAll(" ", "");
					} else if (c == 20) {
						System.out.println(value);// 邮编代码
						if (StringEmptyUtils.isNotEmpty(value)) {
							postal = value;
						}
					} else if (c == 21) {
						System.out.println(value);// 收货地址
						RecipientAddr = value.trim();
					} else if (c == 22) {
						System.out.println(value);// 收货人电话
						RecipientTel = value.trim();
					} else if (c == 24) {
						System.out.println(value);// 收货人身份证
						RecipientID = value.trim().replace("x", "X");
					} else if (c == 33) {
						System.out.println(value);// 商品货号
						EntGoodsNo = value.replaceAll(" ", "");
					} else if (c == 36) {
						System.out.println(value);// 数量
						total_count = Integer.parseInt(value.trim());
					} else if (c == 40) {
						System.out.println(value);// 单价
						price = Double.parseDouble(value.trim());
					} else if (c == 56) {
						ciqGoodsNo = value;
						System.out.println(value);// 商品国检备案号
					} else if (c == 55) {
						// 商品规格
						goodsStyle = value;
						System.out.println(value);
					} else if (c == 39) {
						hsCode = value.trim();
						System.out.println(value);// HS编码
					} else if (c == 34) {// 原产国
						originCountry = value;
						System.out.println(value);
					} else if (c == 35) {
						// 计量单位
						System.out.println(value);
						if (StringEmptyUtils.isNotEmpty(value.trim())) {
							unit = findUnit(value.trim());
						}
					} else if (c == 41) {// 币制
						currCode = value;
						System.out.println(value);
					} else if (c == 13) {
						// 净重
						netWt = Double.parseDouble(value.trim());
					} else if (c == 14) {
						// 毛重
						grossWt = Double.parseDouble(value.trim());
					} else if (c == 51) {
						// 第一法定数量
						if (StringEmptyUtils.isNotEmpty(value)) {
							firstLegalCount = Double.parseDouble(value.trim());
						} else {
							firstLegalCount = 0.0;
						}
					} else if (c == 52) {
						// 第一法定计量单位
						if (StringEmptyUtils.isNotEmpty(value)) {
							stdUnit = value.trim();
						}
					} else if (c == 53) {
						if (StringEmptyUtils.isNotEmpty(value)) {
							// 第二法定数量
							secondLegalCount = Double.parseDouble(value.trim());
						} else {
							secondLegalCount = 0.0;
						}
					} else if (c == 54) {
						// 第二法定计量单位
						if (StringEmptyUtils.isNotEmpty(value)) {
							secUnit = value;
						}
					} else if (c == 15) {
						// 箱件数
						numOfPackages = Integer.parseInt(value.trim());
					} else if (c == 10) {
						// 包装种类
						packageType = Integer.parseInt(value.trim());
					} else if (c == 8) {
						// 运输方式
						transportModel = value;
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
					}
				}
				Map<String, Object> provinceMap = searchProvinceCityArea(RecipientAddr);
				Map<String, Object> reProvinceCityAreaMap = doProvinceCityArea(provinceMap);
				if (reProvinceCityAreaMap != null) {
					areaCode = reProvinceCityAreaMap.get("areaCode") + "";
					areaName = reProvinceCityAreaMap.get("areaName") + "";
					cityCode = reProvinceCityAreaMap.get("cityCode") + "";
					cityName = reProvinceCityAreaMap.get("cityName") + "";
					provinceCode = reProvinceCityAreaMap.get("provinceCode") + "";
					provinceName = reProvinceCityAreaMap.get("provinceName") + "";
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
				goodsInfo.put("EntGoodsNo", EntGoodsNo);
				goodsInfo.put("HSCode", hsCode);
				goodsInfo.put("Brand", goodsName);
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
				String[] str = serialNo.split("_");
				int serial = Integer.parseInt(str[1]);
				// 创建国宗订单
				Map<String, Object> item = morderService.guoCreateNew(merchantId, waybill, serial, dateSign,
						OrderDate, FCY, Tax, ActualAmountPaid, RecipientName, RecipientID, RecipientTel,
						RecipientProvincesCode, RecipientAddr, OrderDocAcount, OrderDocName, OrderDocId, OrderDocTel,
						senderName, senderCountry, senderAreaCode, senderAddress, senderTel, areaCode, cityCode,
						provinceCode, postal, provinceName, cityName, areaName, orderId, goodsInfo);
				if (!"1".equals(item.get("status") + "")) {
					Map<String, Object> errMap = new HashMap<>();
					errMap.put("msg", "【表格】第" + (r + 1) + "行订单--->" + item.get("msg"));
					errl.add(errMap);
					continue;
				}
				String order_id = item.get("order_id") + "";
				goodsInfo.put("order_id", order_id);
				// 根据订单Id创建订单商品
				Map<String, Object> goodsItem = morderService.createNewSub(goodsInfo);
				if ((int) goodsItem.get("status") != 1) {
					Map<String, Object> errMap = new HashMap<>();
					errMap.put("msg", "【表格】第" + (r + 1) + "行商品----->" + goodsItem.get("msg"));
					errl.add(errMap);
				}
				BufferUtils.writeRedis("1", errl, realRowCount,  serialNo, "order");
			}

			if (!errl.isEmpty()) {
				errl = SortUtil.sortList(errl);
			}
			File file2 = excel.getFile();
			// excel.closeExcel();
			if (!file2.delete()) {
				System.out.println("-----------文件没有删除--------------");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		BufferUtils.writeRedis("2", errl, realRowCount,  serialNo, "order");
		// 393行：81767ms ,第二次()：76202ms
		return statusMap;
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
					&& StringEmptyUtils.isNotEmpty(provinceMap.get("areaCode"))) {
				areaCode = provinceMap.get("areaCode") + "";
				areaName = provinceMap.get("areaName") + "";
			} else {// 如果未找到区域编码及区域名称,则沿用省份的编码
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
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.toString());
			statusMap.put(BaseCode.MSG.toString(), "查询省市区失败,请核对邮编号码!");
			return statusMap;
		}
		Map<String, Object> areaMap = (Map<String, Object>) provinceCityAreaMap.get(BaseCode.DATAS.getBaseCode());
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
			}
			// 地址中区域+省份未找到则把城市编码放入缓存
			reCityMap.put(cityCode, cityCode + "_" + cityName + "#" + provinceCode + "_" + provinceName);

		}
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

	private String getOrderDocAcount() {
		return AppUtil.generateAppKey().substring(0, 6);
	}

	// 根据计量单位的中文名称查询编码
	private String findUnit(String value) {
		List reList = meteringTransaction.findMetering();
		if (reList != null && !reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Metering metering = (Metering) reList.get(i);
				if (value.equals(metering.getMeteringName())) {
					return metering.getMeteringCode();
				}
			}
		}
		return null;
	}
}
