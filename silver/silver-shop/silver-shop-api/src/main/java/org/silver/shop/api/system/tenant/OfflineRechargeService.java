package org.silver.shop.api.system.tenant;

import java.util.Map;

public interface OfflineRechargeService {

	/**
	 * 获取线下加款申请详情
	 * 
	 * @param offlineRechargeId
	 * @return
	 */
	public Map<String, Object> getApplicationDetail(String offlineRechargeId);

	/**
	 * 管理员初审线下加款信息
	 * 
	 * @param offlineRechargeId
	 *            流水id
	 * @param managerName
	 *            管理员名称
	 * @param managerId
	 *            管理员id
	 * @param reviewerFlag
	 *            审核标识：1-待审核、2-通过、3-不通过、
	 * @param note
	 *            批注说明信息
	 * @return
	 */
	public Map<String, Object> managerReview(String offlineRechargeId, String managerName, String managerId,
			int reviewerFlag, String note);

	/**
	 * 财务审核信息
	 * 
	 * @param offlineRechargeId
	 *            流水id
	 * @param managerName
	 *            管理员名称
	 * @param managerId
	 *            管理员id
	 * @param reviewerFlag
	 *            审核标识：1-待审核、2-通过、3-不通过
	 * @param note
	 *            批注说明信息
	 * @return
	 */
	public Map<String, Object> financialReview(String offlineRechargeId, String managerName, String managerId,
			int reviewerFlag, String note);

}
