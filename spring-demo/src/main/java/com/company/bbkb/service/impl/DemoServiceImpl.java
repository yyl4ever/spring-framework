package com.company.bbkb.service.impl;

import com.company.bbkb.service.IDemoService;

/**
 * @Author: yangyl
 * @Date: 2020-05-02 15:07
 * @Description:
 */
public class DemoServiceImpl implements IDemoService {

	@Override
	public void sayHello() {
		System.out.println("hello spring");
	}
}
