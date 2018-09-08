package org.silver.shop.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.organization.Member;
import org.springframework.stereotype.Repository;


/**
 * 提供数据访问层共用DAO方法
 */
@Repository("baseDao")
public class BaseDaoImpl<E> extends HibernateDaoImpl implements BaseDao<E> {
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
	public List<E> findAll(Class entity, int page, int size) {
		Session session = null;
		try {
			String entName = entity.getSimpleName();
			String hql = "from " + entName + " model ";
			session = getSession();
			Query query = session.createQuery(hql);
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<E> list = query.list();
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
	public List<E> findByProperty(Class entity, Map params, int page, int size) {
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
			List<E> results = query.list();
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
	public List<E> findBlurryProperty(Class entity, Map params, String startTime, String endTime, int page,
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
			List<E> results = query.list();
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
	public List<E> findByPropertyOr(Class entity, Map params, List orList, int page, int size) {
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
			List<E> results = query.list();
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
	 * <li>createDate-用于驼峰命名版本实体参数(createDate)</li>
	 * <li>startTime-用于兼容下划线版本实体参数(create_date)</li>
	 * <li>endTime-用于兼容下划线版本实体参数(create_date)</li>
	 * 
	 * @param hql
	 * @param property
	 */
	private void appendDate(StringBuilder hql, String property) {
		if (property.equals("startDate")) {
			hql.append("model.createDate" + " >= " + "? " + " AND ");
		} else if ("endDate".equals(property)) {
			hql.append("model.createDate" + " <= " + " ? " + " AND ");
		} else if ("startTime".equals(property)) {
			hql.append("model.create_date " + " >= " + " ? " + " AND ");
		} else if ("endTime".equals(property)) {
			hql.append("model.create_date " + " <= " + " ? " + " AND ");
		} else {
			hql.append("model." + property + " = " + " ? " + " AND ");
		}
	}

	@Override
	public List<E> findByPropertyOr2(Class entity, Map orMap, int page, int size) {
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
			List<E> results = query.list();
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

	public List<Object> testSql() {
		Session session = null;
		try {
			StringBuilder sbSQL = new StringBuilder(
					" SELECT t1.memberName FROM ym_shop_member t1 GROUP BY 	t1.memberName HAVING	COUNT(*) > 1");

			session = getSession();
			Query query = session.createSQLQuery(sbSQL.toString());
			List resources = query.list();
			session.connection().close();
			session.close();
			return resources;
			// return t;
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
	public List<E> find(Class<E> entity, Map<String,Object> params, Map<String,Object> blurryMap, int page, int size) {
		Session session = null;
		String entName = entity.getSimpleName();
		try {
			session = getSession();
			StringBuilder hql = new StringBuilder(" FROM " + entName + " model WHERE");
			List<Object> paramlist = new ArrayList<>();
			hql.append("  ");
			appendParameter(hql, params, paramlist);
			appendBlurry(hql, blurryMap, paramlist);
			hql.append(" 1=1 ORDER BY id DESC");
			Query query = session.createQuery(hql.toString());
			for (int i = 0; i < paramlist.size(); i++) {
				query.setParameter(i, paramlist.get(i));
			}
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<E> results = query.list();
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

	/**
	 * 拼接(精准“=”)查询参数
	 * 
	 * @param hql
	 * @param params
	 * @param paramlist
	 */
	private void appendParameter(StringBuilder hql, Map params, List<Object> paramlist) {
		if (params != null && params.size() > 0) {
			String property;
			Iterator<String> is = params.keySet().iterator();
			while (is.hasNext()) {
				property = is.next();
				appendDate(hql, property);
				paramlist.add(params.get(property));
			}
		}
	}

	/**
	 * 拼接模糊字符串的查询
	 * 
	 * @param hql
	 * @param blurryMap
	 *            模糊查询参数
	 * @param paramlist
	 */
	private void appendBlurry(StringBuilder hql, Map blurryMap, List<Object> paramlist) {
		if (blurryMap != null && !blurryMap.isEmpty()) {
			String property;
			Iterator<String> is = blurryMap.keySet().iterator();
			while (is.hasNext()) {
				property = is.next();
				hql.append("model." + property + " LIKE " + " ? " + " AND ");
				paramlist.add("%" + blurryMap.get(property) + "%");
			}
		}
	}

	@Override
	public long findCount(Class entity, Map params, Map blurryMap) {
		Session session = null;
		try {
			String entityName = entity.getSimpleName();
			StringBuilder hql = new StringBuilder("SELECT count(model) FROM " + entityName + " model WHERE ");
			List<Object> paramlist = new ArrayList<>();
			appendParameter(hql, params, paramlist);
			appendBlurry(hql, blurryMap, paramlist);
			hql.append(" 1=1 ");
			session = getSession();
			Query query = session.createQuery(hql.toString());
			for (int i = 0; i < paramlist.size(); i++) {
				query.setParameter(i, paramlist.get(i));
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

	public static void main(String[] args) {
		// ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
		Map<String, Object> params = new HashMap<>();
		BaseDaoImpl bd = new BaseDaoImpl();
		params.put("firstTypeId", Long.parseLong("29"));
		// List<Object> reThirdTypeList = bd.findByProperty(Proxy.class, null,
		// 0, 0);
		/*
		 * Map<String, Object> orMap = new HashMap<>();
		 * orMap.put("order_serial_no", "222"); orMap.put("order_id",
		 * "65478417");
		 */
		List<Morder> reList1 = bd.findByProperty(Morder.class, null, 1, 1);
		String str = reList1.get(0).getCreate_date() + "";
		System.out.println("-->>" + str.substring(0, 10));

		List<Member> l = bd.findByProperty(Member.class, params, 0, 0);

	}

}
