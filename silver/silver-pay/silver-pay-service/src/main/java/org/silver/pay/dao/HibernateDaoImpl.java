package org.silver.pay.dao;

import org.hibernate.Session;
import org.silver.pay.component.ChooseDatasourceHandler;

public class HibernateDaoImpl {
	 Session session;
	

	public Session getSession() {
		return ChooseDatasourceHandler.hibernateDaoImpl.session;
	}
	public void setSession(Session session) {
		this.session = session;
	}
	
}
