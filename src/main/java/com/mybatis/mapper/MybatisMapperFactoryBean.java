package com.mybatis.mapper;

import org.mybatis.spring.mapper.MapperFactoryBean;

public class MybatisMapperFactoryBean<T> extends MapperFactoryBean<T>{
	private MybatisMapperMiddle mapperMiddle;
	
	public MybatisMapperFactoryBean() {
	}
	
	public MybatisMapperFactoryBean(Class<T> mapperInterface) {
		super(mapperInterface);
	}
	@Override
	protected void checkDaoConfig() {
		super.checkDaoConfig();
		if(mapperMiddle.isMybatisMappper(getObjectType())) {
			mapperMiddle.handlerSqlSource(getSqlSession().getConfiguration(), getObjectType());
		}
	}

	public void setMapperMiddle(MybatisMapperMiddle mapperMiddle) {
		this.mapperMiddle = mapperMiddle;
	}
}
