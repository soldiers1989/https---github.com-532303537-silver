package org.silver.sys.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.silver.common.GZEportCode;
import org.silver.sys.component.ChooseDatasourceHandler;
import org.silver.sys.model.goods.GoodsRecord;
import org.springframework.stereotype.Repository;

@Repository("goodsRecordDao")
public class GoodsRecordDao extends HibernateDaoImpl {

	public boolean add(GoodsRecord entity) {
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

	public boolean delete(GoodsRecord entity) {
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

	public boolean update(GoodsRecord entity) {
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

	public GoodsRecord findbyId(long id) {

		Session session = null;
		try {
			session = getSession();
			GoodsRecord instance = (GoodsRecord) session.get(GoodsRecord.class, id);
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

	public Long findAllCount() {
		Session session = null;
		try {
			String hql = "select count(model) from GoodsRecord model ";
			session = getSession();
			Query query = session.createQuery(hql);
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

	public List<GoodsRecord> findAll(int page, int size) {
		Session session = null;
		try {
			String hql = "from GoodsRecord model ";
			session = getSession();
			Query query = session.createQuery(hql);
			if (page > 0 && size > 0) {
				query.setFirstResult((page - 1) * size).setMaxResults(size);
			}
			List<GoodsRecord> list = query.list();
			session.close();
			return list;
		} catch (Exception re) {
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
			String queryString = "from GoodsRecord as model where model." + propertyName + "= ?";
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

	public List<GoodsRecord> findByProperty(Map<String, Object> params, int page, int size) {
		Session session = null;
		try {
			session = getSession();
			
			String hql = "from GoodsRecord model ";
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
			List<GoodsRecord> results = query.list();
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
			String hql = "select count(model) from GoodsRecord model ";
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
		GoodsRecordDao ed = new GoodsRecordDao();
		GoodsRecord gr =new GoodsRecord();
		gr.setDeclEntNo(GZEportCode.DECL_ENT_NO);//申报企业编号
		gr.setDeclEntName(GZEportCode.DECL_ENT_NAME);//申报企业名称
		gr.setEBEntNo(GZEportCode.DECL_ENT_NO);//电商企业编号
		gr.setEBEntName(GZEportCode.DECL_ENT_NAME);//电商企业名称
		gr.setOpType("A");//操作方式
		gr.setCustomsCode("5208");//主管海关代码
		gr.setCIQOrgCode("442300");//检验检疫机构代码
		gr.setEBPEntNo(GZEportCode.DECL_ENT_NO);//电商平台企业编号
		gr.setEBPEntName(GZEportCode.DECL_ENT_NAME);//电商平台名称
		gr.setCurrCode("142");//币制
		gr.setBusinessType("3");//跨境业务类型
		gr.setInputDate("20170905143522");//录入日期
		gr.setDeclTime("20170905143522");//备案申请时间
		gr.setIeFlag("I");//进出口标识
		gr.setOrgMessageID("4566456");
		gr.setCiqStatus("0");
		gr.setCusStatus("0");
		gr.setStatus(0);
		gr.setCount(0);
		gr.setCreate_date(new Date());
		gr.setDel_flag(0);
		ed.add(gr);
//		System.out.println(ed.findAllCount());
//		JedisUtil.set("message_id"+"_resendCount",0,60*60*3);
//		JedisUtil.get("message_id"+"_resendCount");
	}
	
}
