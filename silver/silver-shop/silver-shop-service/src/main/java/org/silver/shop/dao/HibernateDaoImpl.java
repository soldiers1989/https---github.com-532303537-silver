package org.silver.shop.dao;

import org.hibernate.Session;
import org.silver.shop.component.ChooseDatasourceHandler;

public class HibernateDaoImpl {
	 Session session;
	

	public Session getSession() {
		return SessionFactory.getSession();
	}
	public void setSession(Session session) {
		this.session = session;
	}
	
}
