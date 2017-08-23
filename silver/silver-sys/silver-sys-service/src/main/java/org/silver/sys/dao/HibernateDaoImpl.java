package org.silver.sys.dao;

import org.hibernate.Session;
import org.silver.sys.component.ChooseDatasourceHandler;

public class HibernateDaoImpl {
	private Session session;
	

	public Session getSession() {
		return ChooseDatasourceHandler.hibernateDaoImpl.session;
	}
	public void setSession(Session session) {
		this.session = session;
	}
	
}
