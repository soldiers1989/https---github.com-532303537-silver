package org.silver.shop.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.silver.shop.component.ChooseDatasourceHandler;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;

/**
 * 提供数据访问层共用DAO方法
 */
public class BaseDaoImpl<T> extends HibernateDaoImpl implements BaseDao {
	// 将实体数据插入数据库
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

	/*
	 * public List findByProperty(String propertyName, Object value) { return
	 * findByProperty(propertyName, value, 0, 0); }
	 */

	// 共用findByProperty,此方法暂时不用,
	/*
	 * public List<Object> findByProperty(Class entity,String propertyName,
	 * Object value, int page, int size) { Session session = null; String enName
	 * = entity.getSimpleName(); try { String queryString =
	 * "from "+enName+" as model where model." + propertyName + "= ?"; session =
	 * getSession(); System.out.println(session); Query queryObject =
	 * session.createQuery(queryString); queryObject.setParameter(0, value); if
	 * (page > 0 && size > 0) { queryObject.setFirstResult((page - 1) *
	 * size).setMaxResults(size); } List<Object> l = queryObject.list();
	 * session.close(); return l; } catch (RuntimeException re) {
	 * re.printStackTrace(); return null; } finally { if (session != null &&
	 * session.isOpen()) { session.close(); } } }
	 */

	// 待说明
	/*
	 * public Long findByPropertyCount(Map<String, Object> params) { Session
	 * session = null; try { String hql =
	 * "select count(model) from Member model "; List<Object> list = new
	 * ArrayList<Object>(); if (params != null && params.size() > 0) { hql +=
	 * "where "; String property; Iterator<String> is =
	 * params.keySet().iterator(); while (is.hasNext()) { property = is.next();
	 * hql = hql + "model." + property + "=" + "?" + " and ";
	 * list.add(params.get(property)); } hql += " 1=1 "; } session =
	 * getSession(); Query query = session.createQuery(hql); if (list.size() >
	 * 0) { for (int i = 0; i < list.size(); i++) { query.setParameter(i,
	 * list.get(i)); } } Long count = (Long) query.uniqueResult();
	 * session.close(); return count; } catch (Exception re) { return (long) 0;
	 * } finally { if (session != null && session.isOpen()) { session.close(); }
	 * } }
	 */

	// 根据实体查询实体表中所有数据
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

	// 根据传递进来的Map查询数据
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
				hql += " 1=1 ";
			}
			System.out.println(session);
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
	public Long findLastId(Class entity) {
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
			Long count = (long) query.uniqueResult();
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

	public static void main(String[] args) {
		ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
		BaseDaoImpl ud = new BaseDaoImpl();
		Merchant merchant = new Merchant();
		merchant.setMerchantId("测试ID");
		merchant.setMerchantCusNo("商户编码");
		// List reList= ud.findByProperty(Merchant.class, "merchantName",
		// "dang_Star@live.com", 0,0);
		/*
		 * for(int x =0 ; x<reList.size();x++){ Merchant list = (Merchant)
		 * reList.get(x); System.out.println(list.getMerchantName());
		 * System.out.println(list.getLoginPassword()); }
		 */
		System.out.println();

	}
}
