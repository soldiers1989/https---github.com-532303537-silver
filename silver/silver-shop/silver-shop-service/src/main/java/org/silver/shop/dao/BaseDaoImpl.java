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
import org.silver.shop.component.ChooseDatasourceHandler;
import org.silver.shop.model.common.base.CustomsPort;
import org.silver.shop.model.common.category.GoodsThirdType;
import org.silver.shop.model.system.organization.Member;

/**
 * 提供数据访问层共用DAO方法
 */
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
			String hql = "from " + entName + " model ";
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
				hql += " 1=1 Order By id DESC";
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
			String hql = "from " + entName + " model ";
			List<Object> list = new ArrayList<>();
			if (params != null && params.size() > 0) {
				hql += "where ";
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					hql = hql + "model." + property + " like " + " ? " + " and ";
					list.add(params.get(property));
				}
				if (startTime != null && !"".equals(startTime.trim())) {
					hql = hql + "model.createDate >= '" + startTime + " 00:00:00'" + " and ";
				}
				if (endTime != null && !"".equals(endTime.trim())) {
					hql = hql + "model.createDate <= '" + endTime + " 23:59:59'" + " and ";
				}
				hql += " 1=1 Order By id DESC";
			}
			Query query = session.createQuery(hql);
			if (list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, "%" + list.get(i) + "%");
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
			return (long) 0;
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
			String hql = "from " + entName + " model ";
			List<Object> list = new ArrayList<>();
			hql += "where ";
			if (params != null && params.size() > 0) {
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					if ("startDate".equals(property)) {
						hql = hql + "model.createDate " + " > " + "? " + " and ";
					} else if ("endDate".equals(property)) {
						hql = hql + "model.createDate " + " < " + "?" + " and ";
					} else {
						hql = hql + "model." + property + " = " + "?" + " and ";
					}
					list.add(params.get(property));
				}
			}
			if (blurryMap != null && !blurryMap.isEmpty()) {
				String property;
				Iterator<String> is = blurryMap.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					hql = hql + "model." + property + " LIKE " + "?" + " and ";
					list.add(blurryMap.get(property));
				}
			}
			hql += " 1=1 Order By id DESC";
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
	public long findByPropertyLikeCount(Class entity, Map params, Map blurryMap) {
		Session session = null;
		try {
			String entityName = entity.getSimpleName();
			String hql = "select count(model) from " + entityName + " model ";
			List<Object> list = new ArrayList<>();
			hql += "where ";
			if (params != null && params.size() > 0) {
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					if (property.equals("startDate")) {
						hql = hql + "model.createDate" + " >= " + "?" + " and ";
					} else if ("endDate".equals(property)) {
						hql = hql + "model.createDate" + " <= " + "?" + " and ";
					} else {
						hql = hql + "model." + property + " = " + "?" + " and ";
					}
					list.add(params.get(property));
				}
			}
			if (blurryMap != null && !blurryMap.isEmpty()) {
				String property;
				Iterator<String> is = blurryMap.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					hql = hql + "model." + property + " LIKE " + "?" + " and ";
					list.add(blurryMap.get(property));
				}
			}
			hql += " 1=1 ";
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
			return (long) 0;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<Object> findByPropertyOr(Class entity, Map params, Map orMap, int page, int size) {
		Session session = null;
		String entName = entity.getSimpleName();
		try {
			session = getSession();
			String hql = "from " + entName + " model ";
			String orHql = "";
			List<Object> list = new ArrayList<>();
			hql += " where ";
			if (params != null && params.size() > 0) {
				String property;
				Iterator<String> is = params.keySet().iterator();
				while (is.hasNext()) {
					property = is.next();
					hql = hql + "model." + property + " = " + "?" + " and ";
					list.add(params.get(property));
				}
			}
			if (orMap != null && orMap.size() > 0) {
				String property2;
				Iterator<String> isKey = orMap.keySet().iterator();
				orHql = " ( ";
				while (isKey.hasNext()) {
					property2 = isKey.next();
					List<Object> lt = (List<Object>) orMap.get(property2);
					for (int y = 0; y < lt.size(); y++) {
						orHql = orHql + "model." + property2 + " = " + " ? " + " or ";
						list.add(lt.get(y));
					}
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

	public static void main(String[] args)  {
		ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
		Map<String, Object> paramMap = new HashMap<>();
		Map<String, List<Object>> orMap = new HashMap<>();
		BaseDaoImpl bd =  new BaseDaoImpl();
		paramMap.put("firstTypeId", Long.parseLong("29"));
		List<Object> reThirdTypeList = bd.findByProperty(CustomsPort.class, null, 0, 0);
		System.out.println("0----->>>>"+reThirdTypeList.size());
	}

}
