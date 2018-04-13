package org.silver.shop.service.system.manual;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.shop.api.system.manual.MorderService;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.mq.ShopQueueSender;
import org.silver.shop.service.system.commerce.GoodsRecordTransaction;
import org.silver.shop.utils.ExcelBufferUtils;
import org.silver.shop.utils.InvokeTaskUtils;
import org.silver.shop.utils.RedisInfoUtils;
import org.silver.util.AppUtil;
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

import net.sf.json.JSONObject;

@Service
public class ManualOrderTransaction {

	private static Logger logger = Logger.getLogger(Object.class);

	@Reference
	private MorderService morderService;
	@Autowired
	private FileUpLoadService fileUpLoadService;
	@Autowired
	private InvokeTaskUtils invokeTaskUtils;
	@Autowired
	private GoodsRecordTransaction goodsRecordTransaction;
	@Autowired
	private ManualService manualService;
	@Autowired
	private ExcelBufferUtils excelBufferUtils;
	@Autowired
	private ShopQueueSender shopQueueSender;

	/**
	 * 商户Id
	 */
	private static final String MERCHANT_ID = "merchantId";
	/**
	 * 商户名称
	 */
	private static final String MERCHANT_NAME = "merchantName";
	/**
	 * 开始行数
	 */
	private static final String START_COUNT = "startCount";
	/**
	 * 总行数
	 */
	private static final String TOTAL_COUNT = "totalCount";
	/**
	 * 错误标识
	 */
	private static final String ERROR = "error";

	public Map<String, Object> excelImportOrder(HttpServletRequest req) {
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
			if (columnTotalCount == 14) { // 企邦模板表格长度
				// 用于前台区分哪家批次号
				serialNo = "QB_" + SerialNoUtils.getSerialNo("QB");
				// 多了一行说明
				realRowCount += 1;
				invokeTaskUtils.startTask(11, realRowCount, file, merchantId, serialNo, merchantName);
				statusMap.put("serialNo", serialNo);
			} else if (columnTotalCount == 71) {// 国宗订单模板长度(加上序号列)
				// 用于前台区分哪家批次号
				serialNo = "GZ_" + SerialNoUtils.getSerialNo("GZ");
				invokeTaskUtils.startTask(10, realRowCount, file, merchantId, serialNo, merchantName);
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
	 * 读取国宗订单表
	 * 
	 * @param excel
	 *            文件
	 * @param errl
	 *            错误信息List
	 * @return Map
	 */
	public final void readGuoZongSheet(ExcelUtil excel, Map<String, Object> params) {
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
		String provinceCode = "";// 省份编码
		String cityCode = "";// 城市编码
		String areaCode = "";// 区域编码
		String provinceName = "";// 省份名称
		String cityName = "";// 城市名称
		String areaName = "";// 区域名称
		String unit = "";// 计量单位
		String currCode = "";// 币制
		// FCY订单商品总金额
		double orderTotalAmount;
		//
		double actualAmountPaid;
		// 税费
		double tax = 0.0;
		int goodsCount = 0;// 商品数量
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
		int totalColumnCount = excel.getColumnCount(0);
		String merchantId = params.get(MERCHANT_ID) + "";
		String serialNo = params.get("serialNo") + "";
		int startCount = Integer.parseInt(params.get(START_COUNT) + "");
		int endCount = Integer.parseInt(params.get("endCount") + "");
		String merchantName = params.get(MERCHANT_NAME) + "";
		//
		params.put("name", "orderImport");
		for (int r = startCount; r <= endCount; r++) {
			try {
				if (excel.getColumnCount(0, r) == 0) {
					break;
				}
				for (int c = 0; c < totalColumnCount; c++) {
					String value = excel.getCell(0, r, c);
					if (c == 0 && "".equals(value)) {
						break;
					}
					if (c == 0) {
						// 序号
						if (StringEmptyUtils.isNotEmpty(value)) {
							seqNo = Integer.parseInt(value);
						} else {
							String msg = "【表格】第" + (r + 1) + "行-->表单序号错误,请核对信息!";
							RedisInfoUtils.errorInfoMq(msg, ERROR, params);
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
							RedisInfoUtils.errorInfoMq(msg, ERROR, params);
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
					} else if (c == 17) {
						// 海关关区代码
						customsCode = value;
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
						goodsCount = Integer.parseInt(value);
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
							value = goodsRecordTransaction.findUnit(value);
							if (StringEmptyUtils.isNotEmpty(value)) {
								stdUnit = value;
							}
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
				Map<String, Object> provinceMap = manualService.searchProvinceCityArea(RecipientAddr);
				if (provinceMap == null) {
					String msg = "【表格】第" + (r + 1) + "行-->运单号[" + waybill + "]收货人地址填写有误,请核对信息!";
					RedisInfoUtils.errorInfoMq(msg, "address", params);
				} else {
					Map<String, Object> reProvinceCityAreaMap = manualService.doProvinceCityArea(provinceMap);
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
				// 目前都暂用下单人姓名
				// OrderDocAcount = getOrderDocAcount();// 平台账号
				OrderDocAcount = "";
				orderTotalAmount = goodsCount * price;
				actualAmountPaid = orderTotalAmount + tax;
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
				goodsInfo.put("Qty", goodsCount);
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
				goodsInfo.put(MERCHANT_ID, merchantId);
				goodsInfo.put(MERCHANT_NAME, merchantName);
				goodsInfo.put("customsCode", customsCode);
				String[] str = serialNo.split("_");
				int serial = Integer.parseInt(str[1]);
				//
				Map<String, Object> reGoodsMap = manualService.checkGoodsInfo(goodsInfo);
				if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
					String msg = "【表格】第" + (r + 1) + "行-->" + reGoodsMap.get(BaseCode.MSG.toString()) + "";
					RedisInfoUtils.errorInfoMq(msg, ERROR, params);
					continue;
				}
				JSONObject orderInfo = new JSONObject();
				orderInfo.put("waybill", waybill);
				orderInfo.put("serial", serial);
				orderInfo.put("orderTotalAmount", orderTotalAmount);
				orderInfo.put("tax", tax);
				orderInfo.put("actualAmountPaid", actualAmountPaid);
				orderInfo.put("recipientName", RecipientName);
				orderInfo.put("recipientID", RecipientID);
				orderInfo.put("recipientTel", RecipientTel);
				orderInfo.put("recipientAddr", RecipientAddr);
				orderInfo.put("orderDocAcount", OrderDocAcount);
				orderInfo.put("orderDocName", OrderDocName);
				orderInfo.put("orderDocId", OrderDocId);
				orderInfo.put("orderDocTel", OrderDocTel);
				orderInfo.put("senderName", senderName);
				orderInfo.put("senderCountry", senderCountry);
				orderInfo.put("senderAreaCode", senderAreaCode);
				orderInfo.put("senderAddress", senderAddress);
				orderInfo.put("senderTel", senderTel);
				orderInfo.put("provinceCode", provinceCode);
				orderInfo.put("cityCode", cityCode);
				orderInfo.put("areaCode", areaCode);
				orderInfo.put("postal", postal);
				orderInfo.put("provinceName", provinceName);
				orderInfo.put("cityName", cityName);
				orderInfo.put("areaName", areaName);
				orderInfo.put("orderId", orderId);
				orderInfo.put("dateSign", dateSign);
				orderInfo.put(MERCHANT_ID, merchantId);
				orderInfo.put(MERCHANT_NAME, merchantName);
				orderInfo.put("currCode", currCode);
				// 商品信息
				orderInfo.put("goodsInfo", goodsInfo);
				// 其他缓存参数
				orderInfo.put("other", params);
				// 标识区分
				orderInfo.put("type", "guoZongExcelOrderImpl");
				AtomicInteger mqCounter = (AtomicInteger) params.get("mqCounter");
				// 发起队列,开始创建国宗订单
				shopQueueSender.send("excel-channel-" + mqCounter.get(), orderInfo.toString());
				params.put("type", "success");
				excelBufferUtils.writeRedisMq(null, params);
			} catch (Exception e) {
				logger.error("--国宗订单导入错误---线程--->" + Thread.currentThread().getName(), e);
				String msg = "【表格】第" + (r + 1) + "行-->数据不符合规范,请核对数据排序或格式是否正确!";
				//
				RedisInfoUtils.errorInfoMq(msg, ERROR, params);
				//
				excelBufferUtils.writeCompletedRedisMq(null, params);
			}
		}
		excel.closeExcel();
	}

	/**
	 * 读取企邦表单,暂默认为14列数据
	 * 
	 * @param excel
	 *            工具类
	 * @param errl
	 *            错误信息
	 * @param 参数
	 * 
	 * @return Map
	 */
	public void readQiBangSheet(ExcelUtil excel, Map<String, Object> params) {
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
		int totalColumnCount = excel.getColumnCount(0);
		String merchantId = params.get(MERCHANT_ID) + "";
		String serialNo = params.get("serialNo") + "";
		int startCount = Integer.parseInt(params.get(START_COUNT) + "");
		int endCount = Integer.parseInt(params.get("endCount") + "");
		int totalCount = Integer.parseInt(params.get(TOTAL_COUNT) + "");
		// 由于启邦属于银盟自定义模板,故而多了一行说明，总数需要减1
		params.put(TOTAL_COUNT, (totalCount - 1));
		String merchantName = params.get(MERCHANT_NAME) + "";
		//
		params.put("name", "orderImport");
		for (int r = startCount; r <= endCount; r++) {
			if (excel.getColumnCount(0, r) == 0) {
				break;
			}
			try {
				for (int c = 0; c < totalColumnCount; c++) {
					String value = excel.getCell(0, r, c);
					if (c == 0 && "".equals(value)) {
						break;
					}
					if (c == 0) {
						// 序号
						if (StringEmptyUtils.isNotEmpty(value)) {
							seqNo = Integer.parseInt(value);
						} else {
							String msg = "【表格】第" + (r + 1) + "行-->表单序号错误,请核对信息!";
							RedisInfoUtils.errorInfoMq(msg, ERROR, params);
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
							String msg = "【表格】第" + (r + 1) + "行,商品单价输入错误!";
							RedisInfoUtils.errorInfoMq(msg, ERROR, params);
							break;
						}
					} else if (c == 5) {
						// 数量
						try {
							if (StringEmptyUtils.isNotEmpty(value)) {
								count = Integer.parseInt(value);
							}
						} catch (Exception e) {
							String msg = "【表格】第" + (r + 1) + "行,商品数量输入错误!";
							RedisInfoUtils.errorInfoMq(msg, ERROR, params);
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
				JSONObject item = new JSONObject();
				Map<String, Object> provinceMap = manualService.searchProvinceCityArea(recipientAddr);
				if (provinceMap == null) {
					String msg = "【表格】第" + (r + 1) + "行-->订单号[" + orderId + "]收货人地址不符合规范,请核对信息!";
					RedisInfoUtils.errorInfoMq(msg, "address", params);
				}
				Map<String, Object> reProvinceCityAreaMap = manualService.doProvinceCityArea(provinceMap);
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
				item.put(MERCHANT_ID, merchantId);
				item.put(MERCHANT_NAME, merchantName);
				// 其他缓存参数
				item.put("other", params);
				item.put("type", "qiBangExcelOrderImpl");
				// 发起队列,开始创建启邦订单
				AtomicInteger mqCounter = (AtomicInteger) params.get("mqCounter");
				shopQueueSender.send("excel-channel-" + mqCounter.get(), item.toString());
				params.put("type", "success");
				excelBufferUtils.writeRedisMq(null, params);
			} catch (Exception e) {
				logger.error("--启邦订单导入错误--线程-->" + Thread.currentThread().getName(), e);
				String msg = "【表格】第" + (r + 1) + "行-->数据不符合规范,请核对数据排序或格式是否正确!";
				//
				RedisInfoUtils.errorInfoMq(msg, ERROR, params);
				//
				excelBufferUtils.writeCompletedRedisMq(null, params);
			}
		}
		excel.closeExcel();
	}

}
