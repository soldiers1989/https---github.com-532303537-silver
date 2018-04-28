package org.silver.shop.dao.system.tenant;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDao;
import org.silver.shop.model.system.tenant.EvaluationContent;

public interface EvaluationDao extends BaseDao{

	public List<EvaluationContent> findByCreateDate(Class entity, Map params, int page, int size) ;
}
