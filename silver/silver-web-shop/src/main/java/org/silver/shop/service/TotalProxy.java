package org.silver.shop.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;

public class TotalProxy {

	// 维护一个目标对象
	private Object target;

	public TotalProxy(Object target) {
		this.target = target;
	}

	// 给目标对象生成代理对象
	public Object getProxyInstance() {
		return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(),
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						System.out.println("开始事务2");
						String methodName = method.getName();
						// 执行目标对象方法
						System.out.println(method.getName());
						if("".equals(methodName)){
							
						}
						Parameter[] p = method.getParameters();
						
						System.out.println(p[0].getType().getSimpleName());
						System.out.println(args[1]);
						Object returnValue = method.invoke(target, args);
						System.out.println("提交事务2");

						return returnValue;
					}
				});
	}

	public static void main(String[] args) {

		/*MerchantWalletService target = new MerchantWalletServiceImpl();

		MerchantWalletService yds = (MerchantWalletService) new TotalProxy(target).getProxyInstance();
		System.out.println(yds.walletRecharge("MerchantId_00047", "钱包01", 0.68));*/

	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}
}
