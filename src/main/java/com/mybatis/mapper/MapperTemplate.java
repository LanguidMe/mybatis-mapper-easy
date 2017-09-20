package com.mybatis.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;

public class MapperTemplate {
	private Map<String, Method> mapperMethod = new ConcurrentHashMap<>();
	private static final LanguageDriver languageDriver = new XMLLanguageDriver();
	private Map<String, Class<?>> mapperClassMap = new ConcurrentHashMap<>();
	
	public MapperTemplate() {
		
	}
	public void addMethod(String methodName,Method method) {
		mapperMethod.put(methodName, method);
	}
	
	public String selectOne(MappedStatement ms) {
		Class<?> mapperEntityClass = getMapperEntityClass(ms);
		ResultMap resultMap = getResultMap(mapperEntityClass,ms);
		List<ResultMap> resultMaps = new ArrayList<>();
		resultMaps.add(resultMap);
		MetaObject metaObject = SystemMetaObject.forObject(ms);
		metaObject.setValue("resultMaps", resultMaps);
		StringBuilder sb = new StringBuilder();
		//这里根据需要自定义
		sb.append("SELECT * FROM ").append(mapperEntityClass.getSimpleName()).append(" ").append("where")
		.append(" id = #{id}");
		return sb.toString();
	}
	
	private ResultMap getResultMap(Class<?> mapperEntityClass,MappedStatement ms) {
		Field[] fields = mapperEntityClass.getDeclaredFields();
		List<ResultMapping> resultMappings = new ArrayList<>();
		for (Field field : fields) {
			field.setAccessible(true);
			//这里可以处理数据库字段和实体字段的对应
			ResultMapping mapping = new ResultMapping.Builder(ms.getConfiguration(),field.getName(),field.getName(),field.getType()).build();
			resultMappings.add(mapping);
		}
		ResultMap.Builder resultMap = new ResultMap.Builder(ms.getConfiguration(), "BaseResultMapMapper", mapperEntityClass, resultMappings);
		return resultMap.build();
	}
	
	private Class<?> getMapperEntityClass(MappedStatement ms){
		if(mapperClassMap.containsKey(ms.getId())) {
			return mapperClassMap.get(ms.getId());
		}
		Class<?> mapperClass = getMapperClass(ms.getId());
		Type[] types = mapperClass.getGenericInterfaces();
        for (Type type : types) {
            if (type instanceof ParameterizedType) {
                ParameterizedType t = (ParameterizedType) type;
                Class<?> returnType = (Class<?>) t.getActualTypeArguments()[0];
                mapperClassMap.put(ms.getId(), returnType);
                return returnType;
            }
        }
        throw new RuntimeException("无法获取泛型实体"+ms.getId());
	}
	
	public boolean isMethod(String methodName) {
		return mapperMethod.containsKey(methodName);
	}
	
	public void setSqlSource(MappedStatement ms) {
		String mName = getMethodName(ms.getId());
		if(!mapperMethod.containsKey(mName)) 
			throw new RuntimeException("找不到此方法");
		Method method = mapperMethod.get(mName);
		try {
			if(String.class.equals(method.getReturnType())) {
				String xmlSqlSource = (String)method.invoke(this, ms);
				SqlSource sqlSource = createSqlSource(ms,xmlSqlSource);
				setSqlSource(ms,sqlSource);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setSqlSource(MappedStatement ms,SqlSource sqlSource) {
		MetaObject metaObject = SystemMetaObject.forObject(ms);
		metaObject.setValue("sqlSource", sqlSource);
	}
	
	/**
     * 通过xmlSql创建sqlSource
     *
     * @param ms
     * @param xmlSql
     * @return
     */
    public SqlSource createSqlSource(MappedStatement ms, String xmlSql) {
        return languageDriver.createSqlSource(ms.getConfiguration(), "<script>\n\t" + xmlSql + "</script>", null);
    }
	
	public static String getMethodName(String msId) {
        return msId.substring(msId.lastIndexOf(".") + 1);
    }
	
	/**
     * 根据msId获取接口类
     *
     * @param msId
     * @return
     */
    public static Class<?> getMapperClass(String msId) {
        if (msId.indexOf(".") == -1) {
            throw new RuntimeException("当前MappedStatement的id=" + msId + ",不符合MappedStatement的规则!");
        }
        String mapperClassStr = msId.substring(0, msId.lastIndexOf("."));
        try {
            return Class.forName(mapperClassStr);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
