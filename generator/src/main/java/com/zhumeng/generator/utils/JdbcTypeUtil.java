package com.zhumeng.generator.utils;

import java.util.HashMap;
import java.util.Map;

public class JdbcTypeUtil {

    public static Map<String,String> dataTypeToJdbcType= new HashMap<String,String>();

    static {
        dataTypeToJdbcType.put("","");
    }
}
