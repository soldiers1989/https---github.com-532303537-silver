package org.silver.shop.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.silver.shop.component.ChooseDatasourceHandler;
import org.silver.shop.component.DataSourcesKey;
import org.silver.shop.model.User;
import org.springframework.stereotype.Repository;

@Repository("memberDao")
public class MemberDao extends HibernateDaoImpl{
	
	public boolean add(User entity) {
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

	public boolean delete(User entity) {
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

	public boolean update(User entity) {
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

	public User findMailboxbyId(long id) {

		Session session = null;
		try {
			session = getSession();
			User instance = (User) session.get(User.class, id);
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

	@DataSourcesKey("read")
	public Long findAllCount() {
		Session session = null;
		try {
			String hql = "select count(model) from User model ";
			session = getSession();
			Query query = session.createQuery(hql);
			Long count = (Long) query.uniqueResult();
			session.close();
			return count;
		} catch (Exception re) {
			re.printStackTrace();
			return (long) 0;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public List<User> findAll(int page, int size) {
		Session session = null;
		try {
			String hql = "from User model ";
			session = getSession();
			Query query = session.createQuery(hql);
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<User> list = query.list();
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

	public List findByProperty(String propertyName, Object value) {
		return findByProperty(propertyName, value, 0, 0);
	}

	public List findByProperty(String propertyName, Object value, int page, int size) {
		Session session = null;
		try {
			String queryString = "from User as model where model." + propertyName + "= ?";
			session = getSession();
			Query queryObject = session.createQuery(queryString);
			queryObject.setParameter(0, value);
			if (page > 0 && size > 0) {
				queryObject.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List l = queryObject.list();
			session.close();
			return l;
		} catch (RuntimeException re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public List<User> findByProperty(Map<String, Object> params, int page, int size) {
		Session session = null;
		try {
			session = getSession();
			String hql = "from User model ";
			List<Object> list = new ArrayList<Object>();
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
			Query query = session.createQuery(hql);
			if (list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i, list.get(i));
				}
			}
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<User> results = query.list();
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

	
	
	
	public Long findByPropertyCount(Map<String, Object> params) {
		Session session = null;
		try {
			String hql = "select count(model) from User model ";
			List<Object> list = new ArrayList<Object>();
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
	
	
	public static void main(String[] args) {
		ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
		MemberDao ud = new MemberDao();
		Map params = new HashMap();
		params.put("id", (long)33);
		params.put("del_flag", 1);//
		List<User> list=ud.findByProperty(params, 1, 5);
	//	System.out.println(ulist.get(0).getCreate_date());
	//	ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
	//	ud.update(ulist.get(0));
		//System.out.println(u.getCreate_date());
	}

	

}
