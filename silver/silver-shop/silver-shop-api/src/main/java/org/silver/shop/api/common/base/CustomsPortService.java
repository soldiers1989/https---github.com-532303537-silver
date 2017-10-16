package org.silver.shop.api.common.base;

import java.util.List;
import java.util.Map;

public interface CustomsPortService {
	
	/**
	 * 添加口岸下已开通的 海关及国检名称与编码
	 * @param provinceName
	 *            省份名称
	 * @param provinceCode
	 *            省份编码
	 * @param cityName
	 *            城市名称
	 * @param cityCode
	 *            城市编码
	 * @param customsPort
	 *            口岸编码：1-电子口岸，2-智检
	 * @param customsPortName
	 *            口岸名称
	 * @param customsCode
	 *            主管海关代码(同仓库编码)
	 * @param customsName
	 *            主管海关代码名称
	 * @param ciqOrgCode
	 *            检验检疫机构代码
	 * @param ciqOrgName
	 *            检验检疫机构名称
	 * @return
	 */
	public Map<String,Object> addCustomsPort(String provinceName, String provinceCode, String cityName, String cityCode, int customsPort, String customsPortName, String customsCode, String customsName, String ciqOrgCode, String ciqOrgName);

	/**
	 * 查询所有口岸下已开通的 海关及国检名称与编码
	 * @return
	 */
	public Map<String, Object> findAllCustomsPort();
}
