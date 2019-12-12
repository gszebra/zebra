package com.guosen.zebra.core.grpc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.springframework.util.StringUtils;

import io.netty.util.CharsetUtil;


public class PropertiesContent {
	public static Properties p = new Properties();

	public final static Properties loadProperties(final String name) throws IOException {
		InputStream inStream = new FileInputStream(new File(name));
		p.load(new InputStreamReader(inStream, CharsetUtil.UTF_8));
		return p;
	}

	public static String getStrValue(String key, String defVal) {
		String value = "";
		try {
			value = p.getProperty(key);
			if (StringUtils.isEmpty(value))
				value = defVal;
		} catch (Exception e) {
			value = defVal;
		}
		return value;
	}

	public static boolean getbooleanValue(String key) {
		boolean value = false;
		try {
			String val = p.getProperty(key);
			if ("true".equals(val))
				value = true;
		} catch (Exception e) {

		}
		return value;
	}
	
	public static boolean getbooleanValue(String key,boolean def) {
		boolean value = def;
		try {
			String val = p.getProperty(key);
			if ("true".equals(val)){
				value = true;
			}else if("false".equals(val)){
				value = false;
			}
		} catch (Exception e) {

		}
		return value;
	}

	public static int getIntValue(String key, int defVal) {
		int value = 0;
		try {
			String val = p.getProperty(key);
			value = Integer.parseInt(val);
		} catch (Exception e) {
			value = defVal;
		}
		return value;
	}

	public static class ResourceBundleAdapter extends Properties {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ResourceBundleAdapter(ResourceBundle rb) {
			assert (rb instanceof java.util.PropertyResourceBundle);
			this.rb = rb;
			Enumeration<String> e = rb.getKeys();
			while (e.hasMoreElements()) {
				Object o = e.nextElement();
				this.put(o, rb.getObject((String) o));
			}
		}

		private ResourceBundle rb = null;

		public ResourceBundle getBundle(String baseName) {
			return ResourceBundle.getBundle(baseName);
		}

		public ResourceBundle getBundle(String baseName, Locale locale) {
			return ResourceBundle.getBundle(baseName, locale);
		}

		public ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) {
			return ResourceBundle.getBundle(baseName, locale, loader);
		}

		public Enumeration<?> getKeys() {
			return rb.getKeys();
		}

		public Locale getLocale() {
			return rb.getLocale();
		}

		public Object getObject(String key) {
			return rb.getObject(key);
		}

		public String getString(String key) {
			return rb.getString(key);
		}

		public String[] getStringArray(String key) {
			return rb.getStringArray(key);
		}

		protected Object handleGetObject(String key) {
			return ((PropertyResourceBundle) rb).handleGetObject(key);
		}
	}
	public static void refleshAll(Map<String,String> map){
		p.putAll(map);
	}
}