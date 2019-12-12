/**
 * 
 */
package com.guosen.zebra.core.grpc.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public final class Sequences {

	private static UUIDKeyGenerator uuid = new UUIDKeyGenerator();
	private static AtomicInteger num = new AtomicInteger(0);

	private Sequences() {
	}

	/** 
	* @Title: getPK 
	* @Description: 获取16位Id
	* @param @return    设定文件 
	* @return String    返回类型 
	* @throws 
	*/
	public synchronized static String getPK() {
		StringBuffer buffer = new StringBuffer(16);
		buffer.append(uuid.getCurrentTime(8));
		num.compareAndSet(99999, 0);
		String str = uuid.intToHexString(num.getAndIncrement(), 5);
		buffer.append(str);
		buffer.append(uuid.getMachineID(3));
		return buffer.toString();
	}
	public static  String getAuthCode() { 
		Random random = new Random();
		String result="";
		for(int i=0;i<6;i++){
		result+=random.nextInt(10);
		}
		return result;
	}
	
	public static void main(String args []){
		System.err.println(getPK().toLowerCase());
		System.err.println(Integer.toHexString(6000));
	}
}