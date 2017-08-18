package org.silver.sys.impl;

import org.silver.sys.api.WXService;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass =WXService.class)
public class WXServiceImpl implements WXService{

	@Override
	public int sum(int a, int b) {
		System.out.println(a+b);
		return a+b;
	}

	@Override
	public boolean update(Object object) {
		System.out.println("object");
		return false;
	}

}
