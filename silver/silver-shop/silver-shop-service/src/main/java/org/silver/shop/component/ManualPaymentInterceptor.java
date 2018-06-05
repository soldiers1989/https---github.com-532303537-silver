package org.silver.shop.component;


import org.aspectj.lang.JoinPoint;

public class ManualPaymentInterceptor {
	public void methodBefore(JoinPoint joinPoint) {

		System.out.println(
				joinPoint.getTarget().getClass().getName() + ".|" + joinPoint.getSignature().getName() + " Start");
	}

	public void methodAfter(JoinPoint joinPoint) {
		Class<?> targetClass = joinPoint.getTarget().getClass();
		String methodName = joinPoint.getSignature().getName();
		//sendMpayByRecord
		if("computingCostsManualPayment".equals(methodName)){
			// 拦截方法所传入的参数
			Object[] args = joinPoint.getArgs();
			System.out.println("p---args-?>??"+args.toString());
		}
		System.out.println(
				joinPoint.getTarget().getClass().getName() + ".|" + joinPoint.getSignature().getName() + " end");
	}

	public void methodException(JoinPoint joinPoint) {

		System.out.println(
				joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + " mett Error");
	}
}
