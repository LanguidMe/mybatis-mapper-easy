package com.mybatis.mapper;

import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

public class MybatisMapperScannerConfigurer extends MapperScannerConfigurer{
	private MybatisMapperMiddle mapperMiddle = new MybatisMapperMiddle();
	
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
		super.postProcessBeanDefinitionRegistry(registry);
		mapperMiddle.registerMapper();
		String[] definitionNames = registry.getBeanDefinitionNames();
		GenericBeanDefinition genericBeanDefinition;
		for (String beanName : definitionNames) {
			BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
			if(beanDefinition instanceof GenericBeanDefinition) {
				genericBeanDefinition = (GenericBeanDefinition)beanDefinition;
				if(genericBeanDefinition.getBeanClassName().equals("org.mybatis.spring.mapper.MapperFactoryBean")) {
					genericBeanDefinition.setBeanClass(MybatisMapperFactoryBean.class);
					genericBeanDefinition.getPropertyValues().add("mapperMiddle", this.mapperMiddle);
				}
			}
		}
	}
	
}
