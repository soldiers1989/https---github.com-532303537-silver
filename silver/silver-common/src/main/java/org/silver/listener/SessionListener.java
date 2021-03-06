package org.silver.listener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.Constants;
import org.silver.util.JedisUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * session监听器
 * 
 */
public class SessionListener implements HttpSessionListener {
	private Logger logger = LogManager.getLogger(SessionListener.class);

	

	public void sessionCreated(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		/*session.setAttribute(Constants.WEBTHEME, "default");
		logger.info("创建了一个Session连接:[" + session.getId() + "]");
		setAllUserNumber(1);*/
	}


	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		/*if (getAllUserNumber() > 0) {
			logger.info("销毁了一个Session连接:[" + session.getId() + "]");
		}
		session.removeAttribute(Constants.CURRENT_USER);*/
	//	sessionService.deleteBySessionId(session.getId());
	//	setAllUserNumber(-1);
	}

	private void setAllUserNumber(int n) {
		Long number = getAllUserNumber() + n;
		/*if (number >= 0) {
			logger.info("用户数：" + number);
			JedisUtil.set(Constants.ALLUSER_NUMBER, 60 * 60 * 24, number);
		}*/
	}

	/** 获取在线用户数量 */
	public static Long getAllUserNumber() {
	  String v = JedisUtil.get(Constants.ALLUSER_NUMBER);
		if (v != null) {
			return Long.valueOf(v);
		}
		return 0L;
	}
}
