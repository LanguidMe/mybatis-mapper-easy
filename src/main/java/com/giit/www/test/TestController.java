package com.giit.www.test;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/test")
public class TestController {
	@Resource
	private DemosMapper demosMapper;
	@Resource
	private ProductMapper productMapper;
	
	@RequestMapping("/getOne")
	@ResponseBody
	public Demos getOne() {
		Demos demos = new Demos();
		demos.setId(1);
		return demosMapper.selectOne(demos);
	}
	
	@RequestMapping("/getProduct")
	@ResponseBody
	public Product getProduct() {
		Product product = new Product();
		product.setId(1);
		return productMapper.selectOne(product);
	}
}
