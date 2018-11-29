package com.lxpfunny.enterprise.util;

import com.fasterxml.jackson.databind.JavaType;

public class JsonUtils {

    public final static JsonMapper jsonMapper = JsonMapper.alwaysMapper();

    /**
     * Object可以是POJO，也可以是Collection或数组。
     * 如果对象为Null, 返回"null".
     * 如果集合为空集合, 返回"[]".
     */
    public static String toJson(Object object) {
        return jsonMapper.toJson(object);
    }

    /**
     * 反序列化POJO或简单Collection如List<String>.
     */
    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        return jsonMapper.fromJson(jsonString, clazz);
    }


    public static <T> T fromJson(String jsonString, JavaType javaType) {
        return jsonMapper.fromJson(jsonString, javaType);
    }

}
