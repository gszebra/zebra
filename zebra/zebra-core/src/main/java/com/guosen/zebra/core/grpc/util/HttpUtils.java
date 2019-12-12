package com.guosen.zebra.core.grpc.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference; 
   
/**
 * http工具类
 * 
 */ 
public class HttpUtils { 
	private static final Logger log = LogManager.getLogger(HttpUtils.class);
	
    private static String defaultContentEncoding = Charset.defaultCharset().name();    

    /**
     * 发送GET请求
     * 
     * @param urlString  URL地址
     * @return 响应对象
     * @throws IOException
     */ 
    public static InputStream sendGet(String urlString) throws IOException { 
        return send(urlString, "GET", null, null); 
    } 
   
    /**
     * 发送GET请求
     * 
     * @param urlString  URL地址
     * @param params 参数集合
     * @return 响应对象
     * @throws IOException
     */ 
    public static InputStream sendGet(String urlString, Map<String, String> params) throws IOException { 
        return send(urlString, "GET", params, null); 
    } 
    
    /**
     * 发送GET请求
     * 
     * @param urlString  URL地址
     * @param params 参数集合
     * @return 响应对象
     * @throws IOException
     */ 
    public static InputStream sendGet(String urlString, JSONObject json) throws IOException { 
        return send(urlString, "GET", json.toJavaObject(new TypeReference<Map<String,String>>() {}), null); 
    } 
   
    /**
     * 发送GET请求
     * 
     * @param urlString URL地址
     * @param params 参数集合
     * @param propertys 请求属性
     * @return 响应对象
     * @throws IOException
     */ 
    public static InputStream sendGet(String urlString, Map<String, String> params, Map<String, String> propertys) 
            throws IOException { 
        return send(urlString, "GET", params, propertys); 
    } 
   
    /**
     * 发送POST请求
     * 
     * @param urlString URL地址
     * @return 响应对象
     * @throws IOException
     */ 
    public static InputStream sendPost(String urlString) throws IOException { 
        return send(urlString, "POST", null, null); 
    } 
   
    /**
     * 发送POST请求
     * 
     * @param urlString URL地址
     * @param params 参数集合
     * @return 响应对象
     * @throws IOException
     */ 
    public static InputStream sendPost(String urlString, Map<String, String> params) throws IOException { 
        return send(urlString, "POST", params, null); 
    } 
   
    /**
     * 发送POST请求
     * 
     * @param urlString URL地址
     * @param params 参数集合
     * @param propertys 请求属性
     * @return 响应对象
     * @throws IOException
     */ 
    public static InputStream sendPost(String urlString, Map<String, String> params, Map<String, String> propertys) 
            throws IOException { 
        return send(urlString, "POST", params, propertys); 
    } 
   
    /**
     * 发送HTTP请求
     * 
     * @param urlString 地址
     * @param method  get/post
     * @param parameters  添加由键值对指定的请求参数
     * @param propertys  添加由键值对指定的一般请求属性
     * @return 响映对象
     * @throws IOException
     */ 
    private static InputStream send(String urlString, String method, Map<String, String> parameters, 
            Map<String, String> propertys) throws IOException { 
       
    	HttpURLConnection urlConnection = null; 
   
        if (method.equalsIgnoreCase("GET") && parameters != null) { 
            StringBuffer param = new StringBuffer(); 
            int i = 0; 
            for (String key : parameters.keySet()) { 
                param.append(key).append("=").append(URLEncoder.encode(parameters.get(key), "utf-8")); 
                if(i<parameters.keySet().size()-1){
                	param.append("&"); 
                }
                i++; 
            } 
            String p = "?"+param.toString();
            urlString += p; 
        } 
        log.debug("http get url:={}",urlString);
        URL url = new URL(urlString); 
        urlConnection = (HttpURLConnection) url.openConnection(); 
        urlConnection.setRequestMethod(method); 
        urlConnection.setDoOutput(true); 
        urlConnection.setDoInput(true); 
        urlConnection.setUseCaches(false); 
   
        if (propertys != null) 
            for (String key : propertys.keySet()) { 
                urlConnection.addRequestProperty(key, propertys.get(key)); 
            } 
   
        if (method.equalsIgnoreCase("POST") && parameters != null) { 
            StringBuffer param = new StringBuffer(); 
            for (String key : parameters.keySet()) { 
                param.append("&"); 
                param.append(key).append("=").append(parameters.get(key)); 
            } 
            urlConnection.getOutputStream().write(param.toString().getBytes()); 
            urlConnection.getOutputStream().flush(); 
            urlConnection.getOutputStream().close(); 
        } 
        return urlConnection.getInputStream(); 
    } 
   
    /**
     * 默认的响应字符集
     */ 
    public static String getDefaultContentEncoding() { 
        return defaultContentEncoding; 
    } 
   
      
    /**
     * 发送GET请求
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws Exception
     */ 
    public static URLConnection sendGetRequest(String url, 
            Map<String, String> params, Map<String, String> headers) 
            throws Exception { 
        StringBuilder buf = new StringBuilder(url); 
        Set<Entry<String, String>> entrys = null; 
        // 如果是GET请求，则请求参数在URL中 
        if (params != null && !params.isEmpty()) { 
            buf.append("?"); 
            entrys = params.entrySet(); 
            for (Map.Entry<String, String> entry : entrys) { 
                buf.append(entry.getKey()).append("=") 
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8")) 
                        .append("&"); 
            } 
            buf.deleteCharAt(buf.length() - 1); 
        } 
        URL url1 = new URL(buf.toString()); 
        HttpURLConnection conn = (HttpURLConnection) url1.openConnection(); 
        conn.setRequestMethod("GET"); 
        // 设置请求头 
        if (headers != null && !headers.isEmpty()) { 
            entrys = headers.entrySet(); 
            for (Map.Entry<String, String> entry : entrys) { 
                conn.setRequestProperty(entry.getKey(), entry.getValue()); 
            } 
        } 
        conn.getResponseCode(); 
        return conn; 
    } 
    /**
     * 发送POST请求
     * @param url   
     * @param params
     * @param headers
     * @return 
     * @throws Exception
     */ 
    public static URLConnection sendPostRequest(String url, 
            Map<String, String> params, Map<String, String> headers) 
            throws Exception { 
        StringBuilder buf = new StringBuilder(); 
        Set<Entry<String, String>> entrys = null; 
        // 如果存在参数，则放在HTTP请求体，形如name=aaa&age=10 
        if (params != null && !params.isEmpty()) { 
            entrys = params.entrySet(); 
            for (Map.Entry<String, String> entry : entrys) { 
                buf.append(entry.getKey()).append("=") 
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8")) 
                        .append("&"); 
            } 
            buf.deleteCharAt(buf.length() - 1); 
        } 
        URL url1 = new URL(url); 
        HttpURLConnection conn = (HttpURLConnection) url1.openConnection(); 
        conn.setRequestMethod("POST"); 
        conn.setDoOutput(true); 
        OutputStream out = conn.getOutputStream(); 
        out.write(buf.toString().getBytes("UTF-8")); 
        if (headers != null && !headers.isEmpty()) { 
            entrys = headers.entrySet(); 
            for (Map.Entry<String, String> entry : entrys) { 
                conn.setRequestProperty(entry.getKey(), entry.getValue()); 
            } 
        } 
        conn.getResponseCode(); // 为了发送成功 
        return conn; 
    } 
    
    /**
     * 将输入流转为字节数组
     * @param inStream
     * @return
     * @throws Exception
     */ 
    public static byte[] read2Byte(InputStream inStream)throws Exception{ 
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream(); 
        byte[] buffer = new byte[1024]; 
        int len = 0; 
        while( (len = inStream.read(buffer)) !=-1 ){ 
            outSteam.write(buffer, 0, len); 
        } 
        outSteam.close(); 
        inStream.close(); 
        return outSteam.toByteArray(); 
    } 
    /**
     * 将输入流转为字符串
     * @param inStream
     * @return
     * @throws Exception
     */ 
    public static String read2String(InputStream inStream)throws Exception{ 
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream(); 
        byte[] buffer = new byte[1024]; 
        int len = 0; 
        while( (len = inStream.read(buffer)) !=-1 ){ 
            outSteam.write(buffer, 0, len); 
        } 
        outSteam.close(); 
        inStream.close(); 
        return new String(outSteam.toByteArray(),"UTF-8"); 
    } 
    /**
     * 发送xml数据
     * @param path 请求地址
     * @param xml xml数据
     * @param encoding 编码
     * @return
     * @throws Exception
     */ 
    public static byte[] postXml(String path, String xml, String encoding) throws Exception{ 
        byte[] data = xml.getBytes(encoding); 
        URL url = new URL(path); 
        HttpURLConnection conn = (HttpURLConnection)url.openConnection(); 
        conn.setRequestMethod("POST"); 
        conn.setDoOutput(true); 
        conn.setRequestProperty("Content-Type", "text/xml; charset="+ encoding); 
        conn.setRequestProperty("Content-Length", String.valueOf(data.length)); 
        conn.setConnectTimeout(5 * 1000); 
        OutputStream outStream = conn.getOutputStream(); 
        outStream.write(data); 
        outStream.flush(); 
        outStream.close(); 
        if(conn.getResponseCode()==200){ 
            return read2Byte(conn.getInputStream()); 
        } 
        return null; 
    } 
       
    /**
     * 设置默认的响应字符集
     */ 
    public static void setDefaultContentEncoding(String contentEncoding) { 
        defaultContentEncoding = contentEncoding; 
    } 
    
    
    
    public static String getUrl(String urlString, Map<String, String> parameters) throws IOException {
		StringBuffer param = new StringBuffer();
		int i = 0;
		for (String key : parameters.keySet()) {
			if(StringUtils.isEmpty((parameters.get(key)))){
				param.append(key).append("=").append("");
			}else{
				param.append(key).append("=").append(URLEncoder.encode(parameters.get(key), "utf-8"));
			}
			if (i < parameters.keySet().size() - 1) {
				param.append("&");
			}
			i++;
		}
		String p = "?" + param.toString();
		urlString += p;
		return urlString;
	} 
    
    public static String getUrl(String urlString, JSONObject json) throws IOException {
    	Map<String,String> parameters = json.toJavaObject(new TypeReference<Map<String,String>>() {});
		StringBuffer param = new StringBuffer();
		int i = 0;
		for (String key : parameters.keySet()) {
			param.append(key).append("=").append(URLEncoder.encode(parameters.get(key), "utf-8"));
			if (i < parameters.keySet().size() - 1) {
				param.append("&");
			}
			i++;
		}
		String p = "?" + param.toString();
		urlString += p;
		return urlString;
	} 
}