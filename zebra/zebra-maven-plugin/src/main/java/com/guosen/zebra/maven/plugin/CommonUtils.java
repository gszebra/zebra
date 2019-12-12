package com.guosen.zebra.maven.plugin;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public final class CommonUtils {

    private CommonUtils(){

    }

    public static String findPojoTypeFromCache(String sourceType, Map<String, String> pojoTypes) {
    	if(sourceType.equals(".com.guosen.zebra.dto.RetMessage")){
    		return "com.guosen.zebra.dto.commondto.RetMessage";
    	}else if(sourceType.equals(".com.guosen.zebra.dto.ResultDTO")){
    		return "com.guosen.zebra.dto.commondto.ResultDTO";
    	}else if(sourceType.equals(".com.guosen.zebra.dto.ReturnDTO")){
    		return "com.guosen.zebra.dto.commondto.ResultDTO";
    	}
        String type = StringUtils.substring(sourceType, StringUtils.lastIndexOf(sourceType, ".") + 1);
        return pojoTypes.get(type);
    }

    public static String findNotIncludePackageType(String sourceType) {
        String type = StringUtils.substring(sourceType, StringUtils.lastIndexOf(sourceType, ".") + 1);
        return type;
    }

}
