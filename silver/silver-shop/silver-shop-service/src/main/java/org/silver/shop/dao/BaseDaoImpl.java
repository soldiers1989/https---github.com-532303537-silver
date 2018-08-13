package org.silver.shop.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.silver.shop.model.common.base.IdCard;
import org.silver.shop.model.system.log.StockReviewLog;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.tenant.MerchantIdCardCostContent;
import org.silver.util.DateUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

/**
 * 提供数据访问层共用DAO方法
 */
@Repository("baseDao")
public class BaseDaoImpl<T> extends HibernateDaoImpl implements BaseDao {
	protected static final Logger logger = LogManager.getLogger();

	@Override
	public boolean add(Object entity) {
		Session session = null;
		try {
			session = getSession();
			Transaction tra = session.beginTransaction();
			session.save(entity);
			tra.commit();
			session.flush();
			session.close();
			return true;
		} catch (Exception re) {
			re.printStackTrace();
			return false;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}

		}
	}

	@Override
	public boolean delete(Object entity) {
		Session session = null;
		try {
			session = getSession();
			Transaction tra = session.beginTransaction();
			session.delete(entity);
			tra.commit();
			session.flush();
			session.close();
			return true;
		} catch (Exception re) {
			re.printStackTrace();
			return false;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}

		}
	}

	@Override
	public boolean update(Object entity) {
		Session session = null;
		try {
			session = getSession();
			Transaction tx = session.beginTransaction();
			session.update(entity);
			tx.commit();
			session.flush();
			session.close();
			return true;
		} catch (Exception re) {
			re.printStackTrace();
			return false;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public Member findMailboxbyId(long id) {
		Session session = null;
		try {
			session = getSession();
			Member instance = (Member) session.get(Member.class, id);
			session.close();
			return instance;
		} catch (Exception re) {
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public Long findAllCount(Class entity) {
		Session session = null;
		try {
			String entName = entity.getSimpleName();
			String hql = "select count(model) from " + entName + " model ";
			session = getSession();
			Query query = session.createQuery(hql);
			Long count = (Long) query.uniqueResult();
			session.close();
			return count;
		} catch (Exception re) {
			re.printStackTrace();
			return (long) -1;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<Object> findAll(Class entity, int page, int size) {
		Session session = null;
		try {
			String entName = entity.getSimpleName();
			String hql = "from " + entName + " model ";
			session = getSession();
			Query query = session.createQuery(hql);
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<Object> list = query.list();
			session.close();
			return list;
		} catch (Exception re) {
			re.printStackTrace();
			// log.error("查询数据出错！", re);
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<Object> findByProperty(Class entity, Map params, int page, int size) {
		Session session = null;
		String entName = entity.getSimpleName();
		try {
			session = getSession();
			StringBuilder sbHql = new StringBuilder(" FROM " + entName + " MODEL WHERE ");
			List<Object> list = new ArrayList<>();
			if (params != null && params.size() > 0) {
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					sbHql.append(" MODEL." + property + " = " + " ? " + " AND ");
					list.add(params.get(property));
				}
			}
			sbHql.append(" 1=1 ORDER By id DESC");
			Query query = session.createQuery(sbHql.toString());
			for (int i = 0; i < list.size(); i++) {
				query.setParameter(i, list.get(i));
			}
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<Object> results = query.list();
			session.close();
			return results;
		} catch (Exception re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public long findLastId(Class entity) {
		Session session = null;
		try {
			String entName = entity.getSimpleName();
			String hql = "select model.id from " + entName + " model order by model.id desc ";
			session = getSession();
			Query query = session.createQuery(hql);

			// 设置查询结果分页
			query.setFirstResult(0).setMaxResults(1);
			if (query.uniqueResult() == null) {
				return (long) 0;
			}
			long count = (long) query.uniqueResult();
			session.close();
			return count;
		} catch (Exception re) {
			re.printStackTrace();
			return (long) -1;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<Object> findBlurryProperty(Class entity, Map params, String startTime, String endTime, int page,
			int size) {
		Session session = null;
		String entName = entity.getSimpleName();
		try {
			session = getSession();
			StringBuilder sbHql = new StringBuilder(" FROM " + entName + " MODEL WHERE ");
			List<Object> list = new ArrayList<>();
			if (params != null && params.size() > 0) {
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					sbHql.append("model." + property + " like " + " ? " + " and ");
					list.add(params.get(property));
				}
				if (startTime != null && !"".equals(startTime.trim())) {
					sbHql.append("model.createDate >= '" + startTime + " 00:00:00'" + " and ");
				}
				if (endTime != null && !"".equals(endTime.trim())) {
					sbHql.append("model.createDate <= '" + endTime + " 23:59:59'" + " and ");
				}
			}
			sbHql.append(" 1=1 ORDER By id DESC");
			Query query = session.createQuery(sbHql.toString());
			for (int i = 0; i < list.size(); i++) {
				query.setParameter(i, "%" + list.get(i) + "%");
			}
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<Object> results = query.list();
			session.close();
			return results;
		} catch (Exception re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List findByPropertyDesc(Class entity, Map params, String descParams, int page, int size) {
		Session session = null;
		String entName = entity.getSimpleName();
		try {
			session = getSession();
			String hql = "from " + entName + " model ";
			List<Object> list = new ArrayList<>();
			if (params != null && params.size() > 0) {
				hql += "where ";
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					hql = hql + "model." + property + "=" + "?" + " and  ";
					list.add(params.get(property));
				}
				hql += " 1=1 order by model." + descParams + "  DESC";
			}
			Query query = session.createQuery(hql);
			if (list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<Object> results = query.list();
			session.close();
			return results;
		} catch (Exception re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public long findSerialNoCount(Class entity, String property, int year) {
		Session session = null;
		try {
			String entName = entity.getSimpleName();
			String hql = "select count(model." + property + ") from " + entName + " model  ";
			if (year > 0) {
				hql += "WHERE model.createDate >='" + year + "-01-01 00:00:00'" + "and model.createDate <= '" + year
						+ "-12-31 23:59:59' ";
			}
			session = getSession();
			Query query = session.createQuery(hql);
			// 设置查询结果分页
			// query.setFirstResult(0).setMaxResults(1);
			// if (query.uniqueResult() == null) {
			// return null;
			// }
			long count = (long) query.uniqueResult();
			session.close();
			return count;
		} catch (Exception re) {
			re.printStackTrace();
			return (long) -1;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public long findByPropertyCount(Class entity, Map params) {
		Session session = null;
		try {
			String entityName = entity.getSimpleName();
			String hql = "select count(model) from " + entityName + " model ";
			List<Object> list = new ArrayList<>();
			if (params != null && params.size() > 0) {
				hql += "where ";
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					hql = hql + "model." + property + "=" + "?" + " and ";
					list.add(params.get(property));
				}
				hql += " 1=1 ";
			}
			session = getSession();
			Query query = session.createQuery(hql);
			if (list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			Long count = (Long) query.uniqueResult();
			session.close();
			return count;
		} catch (Exception re) {
			return (long) -1;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List findByPropertyLike(Class entity, Map params, Map blurryMap, int page, int size) {
		Session session = null;
		String entName = entity.getSimpleName();
		try {
			session = getSession();
			StringBuilder hql = new StringBuilder(" from " + entName + " model WHERE");
			List<Object> list = new ArrayList<>();
			hql.append("  ");
			if (params != null && params.size() > 0) {
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					if ("tradeNoFlag".equals(property)) {
						hql.append("model.trade_no " + params.get(property) + " AND ");
						continue;
					} else {
						appendDate(hql, property);
					}
					list.add(params.get(property));
				}
			}
			if (blurryMap != null && !blurryMap.isEmpty()) {
				String property;
				Iterator<String> is = blurryMap.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					hql.append("model." + property + " LIKE " + " ? " + " and ");
					list.add("%" + blurryMap.get(property) + "%");
				}
			}
			hql.append(" 1=1 Order By id DESC");
			Query query = session.createQuery(hql.toString());
			for (int i = 0; i < list.size(); i++) {
				query.setParameter(i, list.get(i));
			}
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<Object> results = query.list();
			session.close();
			return results;
		} catch (Exception re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public long findByPropertyLikeCount(Class entity, Map params, Map blurryMap) {
		Session session = null;
		try {
			String entityName = entity.getSimpleName();
			StringBuilder hql = new StringBuilder("SELECT count(model) FROM " + entityName + " model WHERE ");
			List<Object> list = new ArrayList<>();
			if (params != null && params.size() > 0) {
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					if ("tradeNoFlag".equals(property)) {
						hql.append("model.trade_no " + params.get(property) + " and ");
						continue;
					} else {
						appendDate(hql, property);
					}
					list.add(params.get(property));
				}
			}
			if (blurryMap != null && !blurryMap.isEmpty()) {
				String property;
				Iterator<String> is = blurryMap.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					hql.append("model." + property + " LIKE " + " ? " + " and ");
					list.add("%" + blurryMap.get(property) + "%");
				}
			}
			hql.append(" 1=1 ");
			session = getSession();
			Query query = session.createQuery(hql.toString());
			if (list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			Long count = (Long) query.uniqueResult();
			session.close();
			return count;
		} catch (Exception re) {
			return (long) 0;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<T> findByPropertyOr(Class entity, Map params, List orList, int page, int size) {
		Session session = null;
		String entName = entity.getSimpleName();
		try {
			session = getSession();
			StringBuilder hql = new StringBuilder(" from " + entName + " model WHERE ");
			List<Object> list = new ArrayList<>();
			appendParams(params, hql, list);
			appendOrParams(orList, hql, list);
			Query query = session.createQuery(hql.toString());
			if (!list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<T> results = query.list();
			session.close();
			return results;
		} catch (Exception re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	private void appendOrParams(List orList, StringBuilder hql, List<Object> list) {
		if (orList != null && !orList.isEmpty()) {
			// hql.delete(hql.length() - 4,hql.length());
			hql.append(" ( ");
			for (int i = 0; i < orList.size(); i++) {
				Map<String, Object> map = (Map<String, Object>) orList.get(i);
				String property;
				Iterator<String> isKey = map.keySet().iterator();
				while (isKey.hasNext()) {
					property = isKey.next();
					hql.append("model." + property + " = " + " ? " + " or ");
					list.add(map.get(property));
				}
			}
			// 截取掉最后的 or 防止出错
			hql.delete(hql.length() - 3, hql.length());
			hql.append(" ) Order By id DESC");
		} else {
			hql.append(" 1=1 Order By id DESC");
		}
	}

	@Override
	public long findByPropertyOrCount(Class entity, Map params, List orList) {
		Session session = null;
		String entityName = entity.getSimpleName();
		try {
			session = getSession();
			StringBuilder hql = new StringBuilder(" SELECT count(model) FROM " + entityName + " model WHERE ");
			List<Object> list = new ArrayList<>();
			appendParams(params, hql, list);
			appendOrParams(orList, hql, list);
			hql.append(" 1=1 ");
			session = getSession();
			Query query = session.createQuery(hql.toString());
			if (list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			Long count = (Long) query.uniqueResult();
			session.close();
			return count;
		} catch (Exception re) {
			re.printStackTrace();
			return -1;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	private void appendParams(Map params, StringBuilder hql, List<Object> list) {
		if (params != null && params.size() > 0) {
			String property;
			Iterator<String> is = params.keySet().iterator();
			while (is.hasNext()) {
				property = is.next();
				if ("tradeNoFlag".equals(property)) {
					hql.append(" model.trade_no " + params.get(property) + " AND ");
					continue;
				} else {
					appendDate(hql, property);
				}
				list.add(params.get(property));
			}
		}
	}

	/**
	 * 拼接通用型日期时间
	 * 
	 * @param hql
	 * @param property
	 */
	private void appendDate(StringBuilder hql, String property) {
		if (property.equals("startDate")) {
			hql.append("model.createDate" + " >= " + "? " + " AND ");
		} else if ("endDate".equals(property)) {
			hql.append("model.createDate" + " <= " + " ? " + " AND ");
		} else if ("startTime".equals(property)) {// 用于兼容下划线版本实体
			hql.append("model.create_date " + " >= " + " ? " + " AND ");
		} else if ("endTime".equals(property)) {// 用于兼容下划线版本实体
			hql.append("model.create_date " + " <= " + " ? " + " AND ");
		} else {
			hql.append("model." + property + " = " + " ? " + " AND ");
		}
	}

	@Override
	public List<T> findByPropertyOr2(Class entity, Map orMap, int page, int size) {
		Session session = null;
		String entName = entity.getSimpleName();
		try {
			session = getSession();
			String hql = "from " + entName + " model WHERE ";
			String orHql = "";
			List<Object> list = new ArrayList<>();
			hql += "  ";
			if (orMap != null && orMap.size() > 0) {
				Iterator<String> isKey = orMap.keySet().iterator();
				orHql = " ( ";
				while (isKey.hasNext()) {
					String property;
					property = isKey.next();
					orHql = orHql + " model." + property + " = " + " ? " + " or ";
					list.add(orMap.get(property));
				}
				// 截取掉最后的 or 防止出错
				orHql = orHql.substring(0, orHql.length() - 3);
				orHql += " ) Order By id DESC";
			} else {
				hql += " 1=1 Order By id DESC";
			}
			// 合并两个sql语句
			hql += orHql;
			Query query = session.createQuery(hql);
			if (!list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<T> results = query.list();
			session.close();
			return results;
		} catch (Exception re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public List<Object> testSql(List<Map<String, Object>> paramsList) {
		if (paramsList == null) {
			return null;
		}
		Session session = null;
		try {
			List<Object> sqlParams = new ArrayList<>();
			StringBuilder sbSQL = new StringBuilder(
					" SELECT * FROM ym_shop_base_id_card t1 WHERE (t1.merchantId,t1.name,t1.idNumber) IN ( ");
			if (!paramsList.isEmpty()) {
				for (int i = 0; i < paramsList.size(); i++) {
					sbSQL.append(" ( ? , ? , ? ) , ");
					Map<String, Object> map = paramsList.get(i);
					sqlParams.add(map.get("merchantId"));
					sqlParams.add(map.get("name"));
					sqlParams.add(map.get("idNumber"));
				}
				// 删除结尾的逗号
				sbSQL.deleteCharAt(sbSQL.length() - 2);
				sbSQL.append(" )  AND t1.status != 'failure' ");
			}
			session = getSession();
			Query query = session.createSQLQuery(sbSQL.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				query.setString(i, sqlParams.get(i) + "");
			}
			List resources = query.list();
			System.out.println("--query.list()->>" + query.list().toString());
			session.connection().close();
			session.close();
			return null;
			// return t;
		} catch (Exception re) {
			re.printStackTrace();
			// log.error("查询数据出错！", re);
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public static void main(String[] args) {
		// ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
		Map<String, Object> paramMap = new HashMap<>();
		BaseDaoImpl bd = new BaseDaoImpl();
		paramMap.put("firstTypeId", Long.parseLong("29"));
		// List<Object> reThirdTypeList = bd.findByProperty(Proxy.class, null,
		// 0, 0);
		/*
		 * Map<String, Object> orMap = new HashMap<>();
		 * orMap.put("order_serial_no", "222"); orMap.put("order_id",
		 * "65478417");
		 */
		List<Morder> reList1 = bd.findByProperty(Morder.class, null, 1, 1);
		  ExecutorService threadPool = Executors.newCachedThreadPool();
		  
		
		Map<String, Object> map = null;
		List<Map<String, Object>> list = new ArrayList<>();
		long startTime = System.currentTimeMillis();
		System.out.println("--开始查询----");
		Map<String, Object> params = new HashMap<>();
		params.put("merchant_no", "MerchantId_00057");
		params.put("startTime", DateUtil.parseDate2("2018-07-01 00:00:00"));
		params.put("endTime", DateUtil.parseDate2("2018-08-10 23:50:50"));
		int size = 1;
		for (int i = 0; i < 301; i++) {
			List<Morder> reList = bd.findByPropertyLike(Morder.class, params, null, size, 1);
			if (reList != null && !reList.isEmpty()) {
				map = new HashMap<>();
				map.put("merchantId", reList.get(0).getMerchant_no());
				map.put("name", reList.get(0).getOrderDocName());
				map.put("idNumber", reList.get(0).getOrderDocId());
				list.add(map);
			}
			// System.out.println("--page->>>"+ size);
			size++;
		}
		System.out.println("--list->>" + list.size());
		bd.testSql(list);
		long endTime = System.currentTimeMillis();
		System.out.println("---耗时->>" + (endTime - startTime) + "ms");
	}

}
