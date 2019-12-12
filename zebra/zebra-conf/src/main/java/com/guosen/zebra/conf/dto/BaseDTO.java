/**   
* @Title: BaseDTO.java 
* @Package com.guosen.zebra.conf.dto 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年8月27日 下午3:27:58 
* @version V1.0   
*/
package com.guosen.zebra.conf.dto;

/** 
* @ClassName: BaseDTO 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2018年8月27日 下午3:27:58 
*  
*/
public class BaseDTO {
	private String msg;
	private int code;
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
}
