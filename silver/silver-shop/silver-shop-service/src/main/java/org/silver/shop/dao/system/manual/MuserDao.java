package org.silver.shop.dao.system.manual;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.silver.shop.dao.HibernateDaoImpl;
import org.silver.shop.model.system.manual.Muser;
import org.springframework.stereotype.Repository;

@Repository("muserDao")
public class MuserDao extends HibernateDaoImpl{
	
	
	public boolean add(Muser entity) {
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

	public boolean delete(Muser entity) {
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

	public boolean update(Muser entity) {
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

	public Muser findbyId(long id) {

		Session session = null;
		try {
			session = getSession();
			Muser instance = (Muser) session.get(Muser.class, id);
			session.close();
			return instance;
		} catch (Exception re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public Long findAllCount() {
		Session session = null;
		try {
			String hql = "select count(model) from Muser model ";
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

	public List<Muser> findAll(int page, int size) {
		Session session = null;
		try {
			String hql = "from Muser model ";
			session = getSession();
			Query query = session.createQuery(hql);
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<Muser> list = query.list();
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
			String queryString = "from Muser as model where model." + propertyName + "= ?";
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

	public List<Muser> findByProperty(Map<String, Object> params, int page, int size) {
		Session session = null;
		try {
			session = getSession();
			String hql = "from Muser model ";
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
			List<Muser> results = query.list();
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
			String hql = "select count(model) from Muser model ";
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
			re.printStackTrace();
			return (long) -1;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	

	public List<Muser> getLast() {
		Session session = null;
		
		try {
			String hql = "select model from Muser model order by id desc";
			session = getSession();
			Query query = session.createQuery(hql);
			query.setFirstResult(0).setMaxResults(1);
			List<Muser> ll=  query.list();
			session.close();
			
			
			return ll;
		} catch (Exception re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	public static void main(String[] args) {
		MuserDao md = new MuserDao();
		
	/*	Muser entity =new Muser();
		entity.setMuser_sys_no("muser_sys_no3");
		entity.setMuser_name("strs[0]");
		entity.setMuser_tel("strs[0]");
		entity.setMuser_cer_type("strs[0]");
		entity.setMuser_ID("strs[0]");
		entity.setBank_type("strs[0]");
		entity.setBank_card_no("strs[0]");
		entity.setAdm_area_code("strs[0]");
		entity.setMuser_addr("strs[0]");
		
		entity.setDel_flag(0);
		entity.setCreate_date(new Date());
		entity.setCreate_by("muser_sys_no3");*/
		//System.out.println(md.add(entity));;
		System.out.println(md.randomGet("YM20170000015078659178651922").get(0).getId());
	}

	public List<Muser> randomGet(String merchant_no) {
	Session session = null;
		try {
			String hql = "select model from Muser model where model.merchant_no=? order by id desc";
			session = getSession();
			Query query = session.createQuery(hql);
			query.setParameter(0, merchant_no);
			if(query.list().size()>0){
				Random r =new Random();
				query.setFirstResult(r.nextInt(query.list().size())).setMaxResults(1);
			}
			List<Muser> ll=  query.list();
			session.close();
			return ll;
		} catch (Exception re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	
}
