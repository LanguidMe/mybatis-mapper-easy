package com.mybatis.mapper;

import org.apache.ibatis.annotations.Select;

public interface Mapper<T> {
	@Select(value="dynamicSQL")
	T selectOne(T param);
}
