package com.mybatis.mapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;

public class MybatisMapperMiddle {
	private Map<Class<?>, MapperTemplate> mapperTemplates = new ConcurrentHashMap<>();
	private List<Class<?>> mapperInterfaceClass = new ArrayList<>();
	private Map<String, MapperTemplate> cache = new ConcurrentHashMap<>();
	
	public void registerMapper() {
		registerMapper("com.mybatis.mapper.Mapper");
	}
	
	private void registerMapper(String className) {
		try {
			registerMapper(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void registerMapper(Class<?> class1) {
		if(!mapperTemplates.containsKey(class1)) {
			mapperInterfaceClass.add(class1);
			mapperTemplates.put(class1, parseClassMethod(class1));
		}
		//自动注册继承的接口
        Class<?>[] interfaces = class1.getInterfaces();
        if (interfaces != null && interfaces.length > 0) {
            for (Class<?> anInterface : interfaces) {
                registerMapper(anInterface);
            }
        }
	}
	
	public boolean isMybatisMappper(Class<?> classInterface) {
		for (Class<?> mapperClass : mapperInterfaceClass) {
            if (mapperClass.isAssignableFrom(classInterface)) {
                return true;
            }
        }
		return false;
	}
	
	private MapperTemplate parseClassMethod(Class<?> mapperInterface) {
		Method[] methods = mapperInterface.getDeclaredMethods();
		MapperTemplate mapperTemplate = new MapperTemplate();
		List<String> methodNames = new ArrayList<>();
		for (Method method : methods) {
			if(method.isAnnotationPresent(Select.class)) {
				methodNames.add(method.getName());
			}
		}
		for (String methodName : methodNames) {
			try {
				mapperTemplate.addMethod(methodName, mapperTemplate.getClass().getMethod(methodName, MappedStatement.class));
			} catch (Exception e) {
				throw new RuntimeException("缺少"+methodName+"方法");
			}
		}
		return mapperTemplate;
	}
	
	public void handlerSqlSource(Configuration configuration,Class<?> mapperInterface) {
		String prefix;
        if (mapperInterface != null) {
            prefix = mapperInterface.getCanonicalName();
        } else {
            prefix = "";
        }
        for (Object object : new ArrayList<Object>(configuration.getMappedStatements())) {
            if (object instanceof MappedStatement) {
                MappedStatement ms = (MappedStatement) object;
                if (ms.getId().startsWith(prefix) && isMapperMethod(ms.getId())) {
                	setSqlSource(ms);
                }
            }
        }
	}
	
	private void setSqlSource(MappedStatement ms) {
		try {
			MapperTemplate mapperTemplate = cache.get(ms.getId());
			if(mapperTemplate!=null) {
				mapperTemplate.setSqlSource(ms);
			}
		} catch (Exception e) {
			throw new RuntimeException("该方法找不到");
		}
	}
	
	private boolean isMapperMethod(String methodName) {
		String mName = getMethodName(methodName);
		MapperTemplate mapperTemplate;
		for(Map.Entry<Class<?>, MapperTemplate> entry : mapperTemplates.entrySet()) {
			mapperTemplate = entry.getValue();
			if(mapperTemplate.isMethod(mName)) {
				cache.put(methodName, mapperTemplate);
				return true;
			}
		}
		return false;
	}
	
	public static String getMethodName(String msId) {
        return msId.substring(msId.lastIndexOf(".") + 1);
    }
}
