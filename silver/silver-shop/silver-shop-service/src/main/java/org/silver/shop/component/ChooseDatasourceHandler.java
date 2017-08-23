package org.silver.shop.component;

import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.hibernate.Session;
import org.silver.shop.dao.HibernateDaoImpl;
import org.silver.shop.dao.SessionFactory;
import org.silver.shop.dao.SessionFactory2;

public class ChooseDatasourceHandler {
	
	
	public static final HibernateDaoImpl hibernateDaoImpl = new HibernateDaoImpl();

	public void methodBefore(JoinPoint joinPoint) {
		Class<?> targetClass = joinPoint.getTarget().getClass();
		String methodName = joinPoint.getSignature().getName();
		// Object[] args = joinPoint.getArgs();//拦截方法所传入的参数
		for (Method method : targetClass.getDeclaredMethods()) {
			if (methodName.equals(method.getName())) {
				DataSourcesKey annotation = method.getAnnotation(DataSourcesKey.class);
				if (annotation != null && (annotation.value().equals("read"))) {
					hibernateDaoImpl.setSession(SessionFactory.getSession());
				} else {
					hibernateDaoImpl.setSession(SessionFactory2.getSession());
				}
			}

		}
		System.out.println(
				joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + " Start");
	}

	public void methodAfter(JoinPoint joinPoint) {
		System.out.println(
				joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + " end");
	}

	public void methodException(JoinPoint joinPoint) {
		System.out.println(
				joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + " mett Error");
	}

	/*
	 * public Object methodRound(ProceedingJoinPoint joinPoint) {
	 * 
	 * methodBefore(joinPoint); Object ob = null; try { ob =
	 * joinPoint.proceed(); } catch (Throwable error) {
	 * methodException(joinPoint); } methodAfter(joinPoint); return ob; }
	 */
}
