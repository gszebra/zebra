/**   
* @Title: RegExp.java 
* @Package com.guosen.zebra.core.grpc.util 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年7月17日 下午2:51:07 
* @version V1.0   
*/
package com.guosen.zebra.core.grpc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
* @ClassName: RegExp 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2018年7月17日 下午2:51:07 
*  
*/
public class RegExp {
	public boolean match(String reg, String str) {
        return Pattern.matches(reg, str);
    }
 
    public List<String> find(String reg, String str) {
        Matcher matcher = Pattern.compile(reg).matcher(str);
        List<String> list = new ArrayList<String>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }
     
    public List<String> find(String reg, String str, int index) {
        Matcher matcher = Pattern.compile(reg).matcher(str);
        List<String> list = new ArrayList<String>();
        while (matcher.find()) {
            list.add(matcher.group(index));
        }
        return list;
    }
     
    public String findString(String reg, String str, int index) {
        String returnStr = null;
        List<String> list = this.find(reg, str, index);
        if (list.size() != 0)
            returnStr = list.get(0);
        return returnStr;      
    }
 
    public String findString(String reg, String str) {
        String returnStr = null;
        List<String> list = this.find(reg, str);
        if (list.size() != 0)
            returnStr = list.get(0);
        return returnStr;
    }
 
}
