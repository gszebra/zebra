/**   
* @Title: UpdateMessage.java 
* @Description: TODO(用一句话描述该文件做什么)
* @author 邓启翔   
* @date 2018年9月6日 下午5:02:41 
* @version V1.0   
*/
package com.guosen.zebra.cache.change;

/** 
* @ClassName: UpdateMessage 
* @author 邓启翔
* @date 2018年9月6日 下午5:02:41 
*/
public class UpdateMessage {
	private Object key;
	private Object value;
	private Type type;
	private String cacheName;

	public enum Type{
		PUT,
		CLEAN,
		PUTIFABSENT,
		REMOVE;
	}

	/**
	 * 默认构造方法，用于fastjson的反序列化，一定要添加此默认构造方法
	 */
	public UpdateMessage(){}

	public UpdateMessage(Object key,Object value,Type type){
		this.key= key;
		this.value = value;
		this.type = type;
	}
	
	public UpdateMessage(Object key,Type type){
		this.key= key;
		this.type = type;
	}
	
	public UpdateMessage(Type type){
		this.type = type;
	}
	
	public Object getKey() {
		return key;
	}
	public void setKey(Object key) {
		this.key = key;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	@Override
	public String toString() {
		// 不要返回body，可能有敏感数据
		return "{cacheName : " + cacheName + ","
				+ "key : " + key + ","
				+ "type : " + type + "}";
	}
}
