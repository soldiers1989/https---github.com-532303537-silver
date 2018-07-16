package org.silver.shop.api.system.tenant;

import java.util.Map;

public interface OfflineRechargeService {

	/**
	 * 商户线下加款申请
	 * @param datasMap 
	 * @return Map
	 */
	public Map<String, Object> merchantApplication(Map<String, Object> datasMap);

	/**
	 * 查询线下加款信息
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> getApplication(Map<String, Object> datasMap,int page,int size);

	/**
	 * 获取线下加款申请详情
	 * @param offlineRechargeId
	 * @return
	 */
	public Map<String, Object> getApplicationDetail(String offlineRechargeId);

	/**
	 * 管理员初审线下加款信息
	 * @param offlineRechargeId 流水id
	 * @param managerName 管理员名称
	 * @param managerId 
	 * @param reviewerFlag 
	 * @return
	 */
	public Map<String, Object> managerReview(String offlineRechargeId, String managerName, String managerId, int reviewerFlag, String note);

	/**
	 * 
	 * @param offlineRechargeId
	 * @param managerName
	 * @param managerId
	 * @param reviewerFlag
	 * @param note
	 * @return
	 */
	public Map<String, Object> financialReview(String offlineRechargeId, String managerName, String managerId,
			int reviewerFlag, String note);

}
