package com.guosen.zebra.database.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;


public final class OperateProperties {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperateProperties.class);

	public static Properties p = new Properties();

	private OperateProperties(){}

    public static void loadProperties(final String name) throws IOException {
    	try (InputStream inStream = new FileInputStream(new File(name+".properties"))){
            p.load(new InputStreamReader(inStream, StandardCharsets.UTF_8.name()));
    	}
    	catch (Exception e) {
			LOGGER.info("[error] get default properties err,begin get properties from classpath");
    		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    		Resource[] rs = resolver.getResources("classpath*:*.properties");
			LOGGER.info("get properties from classpath file is {}", Arrays.toString(rs));
    		if (ArrayUtils.isEmpty(rs)) {
    			return;
			}
    		for (Resource eachRs : rs) {
				p.load(eachRs.getInputStream());
			}
    	}
    }

    public static String getStrValue(String key,String defVal) {
    	String value = "";
    	try {
    		value = p.getProperty(key);
    		if (StringUtils.isEmpty(value)) {
    			value=defVal;
    		}
    	} catch (Exception e) {
    		value=defVal;
    	}
    	return value;
    }
    
    
    public static boolean getBooleanValue(String key) {
    	boolean value = false;
    	try {
    		String val = p.getProperty(key);
    		if ("true".equals(val)) {
    			value = true;
			}
    	} catch(Exception e) {
			LOGGER.error("Failed to parse key, key is {}", key, e);
    	}
    	return value;
    }
    
    public static int getIntValue(String key,int defVal) {
    	int value = 0;
    	try {
    		String val =p.getProperty(key);
    		value = Integer.parseInt(val);
    	} catch(Exception e){
    		value=defVal;
    	}
    	return value;
    }
}