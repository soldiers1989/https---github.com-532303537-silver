package org.silver.sys.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.silver.sys.component.ChooseDatasourceHandler;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;

@Repository("areaDao")
public class AreaDao extends HibernateDaoImpl{

	public Table findRecordByAreaCode(String areaId){
		
		Session session = null;
		try {
			String sqlString = "select n.*,t4.zip from (SELECT m.*, t3.provinceName FROM( SELECT t1.areaCode,t1.areaName,t2.cityCode,t2.cityName,t2.provinceCode FROM ym_cbsp_base_area t1 LEFT JOIN ym_cbsp_base_city t2 ON (t1.cityCode = t2.cityCode)) m LEFT JOIN ym_cbsp_base_province t3 ON (m.provinceCode = t3.provinceCode)) n LEFT JOIN zipcode t4 on (n.areaCode=t4.areaid) where n.areaCode=?" ;
			
			session = getSession();
		
		
			List<Object> params = new ArrayList<>();
			params.add(areaId);
			//ConnectionProvider cp = ((SessionFactoryImplementor)session.getSessionFactory()).getConnectionProvider();
			
			Table t=null;
			t = DataUtils.queryData(session.connection(), sqlString, params, null, null, null);
			
		
			session.close();
			return t;
		} catch (RuntimeException re) {
			re.printStackTrace();
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		
	}
	public static void main(String[] args) {
		ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
		AreaDao ad = new AreaDao();
	    Table t=ad.findRecordByAreaCode("110101");
	   //System.out.println( Transform.tableToJson(t));
	    System.out.println(t.getRows().get(0).getValue("cityName"));;
		
	}
}
