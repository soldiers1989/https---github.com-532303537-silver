package org.silver.sys.dao;

import org.hibernate.Session;

public class HibernateDaoImpl {
	public Session getSession() {
		return SessionFactory.getSession();
	}
}
