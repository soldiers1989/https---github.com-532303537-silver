package org.silver.shop.api.system.commerce;

import java.util.HashMap;
import java.util.Map;

import org.silver.shop.model.system.commerce.GoodsRecordDetail;

public interface GoodsRecordService {

	/**
	 * 根据商户名称及商户ID、商品名称查询商户下商品备案信息,如未备案商品则查询商品基本信息
	 * 
	 * @param merchantName
	 *            商户名称
	 * @param goodsIdPack
	 *            商品信息包
	 * @return
	 */
	public Map<String, Object> getGoodsRecordInfo(String merchantName, String goodsIdPack);

	/**
	 * 商戶发起商品备案
	 * 
	 * @param merchantName
	 *            商户名称
	 * @param merchantId
	 *            商户ID
	 * @param customsPort
	 *            口岸编码
	 * @param customsCode
	 *            海关代码
	 * @param ciqOrgCode
	 *            国检代码
	 * @param recordGoodsInfoPack
	 *            备案商品信息
	 * @return
	 */
	public Map<String, Object> merchantSendGoodsRecord(String merchantName, String merchantId, String customsPort,
			String customsCode, String ciqOrgCode, String recordGoodsInfoPack);

	/**
	 * 异步回调,更新商品备案状态
	 * 
	 * @param datasMap
	 */
	public Map<String, Object> updateGoodsRecordInfo(Map<String, Object> datasMap);

	/**
	 * 根据商品自编号查询商品备案信息详情
	 * 
	 * @param entGoodsNo
	 *            商品备案(自编号)Id
	 * @return Map
	 */
	public Map<String, Object> getGoodsRecordDetail(String entGoodsNo);

	/**
	 * 商户修改备案商品中的商品基本信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param paramMap
	 *            参数
	 * @param type
	 * @return Map
	 */
	public Map<String, Object> editMerchantRecordGoodsDetailInfo(String merchantId, String merchantName,
			Map<String, Object> paramMap, int type);

	/**
	 * 商户添加已备案商品信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param paramMap
	 *            参数
	 * @return
	 */
	public Map<String, Object> merchantAddAlreadyRecordGoodsInfo(String merchantId, String merchantName,
			Map<String, Object> paramMap);

	/**
	 * 检索商品备案信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param datasMap
	 *            数据Map
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @return Map
	 */
	public Map<String, Object> searchGoodsRecordInfo(String merchantId, String merchantName,
			Map<String, Object> datasMap, int page, int size);

	/**
	 * 批量添加未备案商品
	 * 
	 * @param goodsRecordDetail
	 *            商品备案信息实体
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @return Map
	 */
	public Map<String, Object> batchCreateNotRecordGoods(GoodsRecordDetail goodsRecordDetail, String merchantId,
			String merchantName);

	/**
	 * 商户批量或单个商品备案
	 * 
	 * @param goodsRecordInfo
	 *            备案商品信息
	 * @param merchantId
	 *            商品Id
	 * @param merchantName
	 *            商品名称
	 * @return Map
	 */
	public Map<String, Object> merchantBatchOrSingleGoodsRecord(String goodsRecordInfo, String merchantId,
			String merchantName);

	/**
	 * 修改备案商品状态
	 * 
	 * @param managerId
	 *            管理员Id
	 * @param managerName
	 *            管理员名称
	 * @param goodsPack
	 *            商品备案Id
	 * @return Map
	 */
	public Map<String, Object> editGoodsRecordStatus(String managerId, String managerName, String goodsPack);

	/**
	 * 商户修改备案商品信息(局限于未备案的商品)
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param datasMap
	 *            参数
	 * @return
	 */
	public Map<String, Object> merchantEditGoodsRecordInfo(String merchantId, String merchantName,
			Map<String, Object> datasMap);

	/**
	 * 管理员查询商品备案信息
	 * 
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @return
	 */
	public Map<String, Object> managerGetGoodsRecordInfo(Map<String, Object> paramMap, int page, int size);

	/**
	 * 批量添加已备案商品头部(流水)信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param customsPort
	 *            口岸编码： 1-电子口岸,2-智检
	 * @param customsPortName
	 *            口岸中文名称
	 * @param customsCode
	 *            海关编码
	 * @param customsName
	 *            海关中文名称
	 * @param ciqOrgCode
	 *            检验检疫编码
	 * @param ciqOrgName
	 *            检验检疫名称
	 * @return
	 */
	public Map<String, Object> batchCreateRecordGoodsHead(String merchantId, String merchantName, int customsPort,
			String customsPortName, String customsCode, String customsName, String ciqOrgCode, String ciqOrgName);

	/**
	 * 批量创建备案商品详细
	 * 
	 * @param goodsRecordDetail
	 *            备案商品详情
	 * @return Map
	 */
	public Map<String, Object> batchCreateRecordGoodsDetail(GoodsRecordDetail goodsRecordDetail);

	/**
	 * 检查企业的商品货号是否重复
	 * 
	 * @param value
	 *            商品自编号
	 * @return Map
	 */
	public Map<String, Object> checkEntGoodsNoRepeat(String value);

	/**
	 * 商户删除商品备案信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param entGoodsNo
	 *            商品自编号
	 * @return Map
	 */
	public Map<String, Object> merchantDeleteGoodsRecordInfo(String merchantId, String merchantName, String entGoodsNo);

	/**
	 * 根据口岸代码、海关代码、国检代码 校验口岸对应的信息是否已在系统中存在
	 * 
	 * @param eport
	 *            1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
	 * @param customsCode
	 *            主管海关代码
	 * @param ciqOrgCode
	 *            检验检疫机构代码
	 * @return Map
	 */
	public  Map<String, Object> checkCustomsPort(int eport, String customsCode, String ciqOrgCode);

	/**
	 * 创建商品仓库
	 * 
	 * @param merchantId
	 *            商户ID
	 * @param merchantName
	 *            商户名称
	 * @param portInfo
	 *            口岸管理实体类
	 * @return Map
	 */
	public Map<String, Object> createWarehous(String merchantId, String merchantName, String customsCode, String customsName);
}
