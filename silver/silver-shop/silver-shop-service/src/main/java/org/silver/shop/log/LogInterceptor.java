package org.silver.shop.log;

import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@Component
public class LogInterceptor {
	
	
	public void myMethod() {};

	
	public void before() {
		System.out.println("method start");
	}

	
	public void after() {
		System.out.println("method after");
	}

	/*@AfterReturning("execution(public * org.silver.shop.impl..*.*(..))")
	public void AfterReturning() {
		System.out.println("method AfterReturning");
	}

	@AfterThrowing("execution(public * org.silver.shop.impl..*.*(..))")
	public void AfterThrowing() {
		System.out.println("method AfterThrowing");
	}*/
}
