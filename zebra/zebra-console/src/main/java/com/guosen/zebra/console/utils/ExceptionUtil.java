/**   
* @Title: ExceptionUtil.java 
* @Package com.guosen.etrade.biz.exception 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2015年10月16日 上午8:52:33 
* @version V1.0   
*/
package com.guosen.zebra.console.utils;


import java.io.PrintWriter;
import java.io.StringWriter;
/** 
* @ClassName: ExceptionUtil 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2015年10月16日 上午8:52:33 
*  
*/
public class ExceptionUtil {
	public static String printStack(Exception e){
		e.printStackTrace();
		StringWriter sw = new StringWriter(); 
        e.printStackTrace(new PrintWriter(sw, true)); 
        String str = sw.toString(); 
        return str;
	}
}
