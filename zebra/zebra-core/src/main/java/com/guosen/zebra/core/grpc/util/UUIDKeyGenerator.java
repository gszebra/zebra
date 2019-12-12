package com.guosen.zebra.core.grpc.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>Title:  </p>
 * <p>Description: UUID生成器. </p>
 * <p>Company: </p>
 * @version 1.0
 * @Create date:
 * @Update date:
 * @todo: Nothing
 */

public class UUIDKeyGenerator {
	
	private static final Logger logger = LogManager.getLogger(UUIDKeyGenerator.class);

    SecureRandom secureRandom = new SecureRandom();
    private String midValue;
	private static UUIDKeyGenerator singleton = null;
	
	public synchronized static UUIDKeyGenerator getSingleton() {
		if (singleton == null) {
			synchronized (UUIDKeyGenerator.class) {
				if (singleton == null) {
					singleton = new UUIDKeyGenerator();
				}
			}
		}
		return singleton;
	}

    /**
     * 取当前时间（精确道毫秒）的指定位数的16进制字符串
     * @throws Exception
     * @return String: 当前时间的指定位数的16进制字符串
     */
    public String generateUUIDKey() {
        //8位 + 16位 + 8位
        //BE2A3329 C0A801B1007541F8 697C7A86
        StringBuffer buffer = new StringBuffer(32);
        StringBuffer bf = new StringBuffer(16);
        byte addr[] = null;
        try {
            addr = InetAddress.getLocalHost().getAddress();
        } catch (UnknownHostException e) {
        	logger.error(e);
        }
        bf.append(intToHexString(byteToInt(addr), 8));
        bf.append(intToHexString(System.identityHashCode(this), 8));
        midValue = bf.toString();
        buffer.append(intToHexString((int)(System.currentTimeMillis() & -1L), 8));
        buffer.append(midValue);
        buffer.append(intToHexString(secureRandom.nextInt(), 8));
        return buffer.toString();
    }

    /**
     * 取当前时间（精确道毫秒）的指定位数的16进制字符串
     * @param stringLength 指定位数
     * @throws Exception
     * @return String: 当前时间的指定位数的16进制字符串
     */
    public String getCurrentTime(int stringLength) {
        StringBuffer currentTime = new StringBuffer(stringLength);
        //currentTime.append(intToHexString((int)(System.currentTimeMillis() & -1L), stringLength));
        long time = System.currentTimeMillis()/1000;
        currentTime.append(intToHexString((int)(time), stringLength));
        return currentTime.toString();
    }

    /**
     * 取一个随机数的指定位数的16进制字符串
     * @param stringLength 指定位数
     * @throws Exception
     * @return String: 随机数的指定位数的16进制字符串
     */
    public String getRandomNum(int stringLength) {
        StringBuffer randomNum = new StringBuffer(stringLength);
        randomNum.append(intToHexString(secureRandom.nextInt(), stringLength));
        return randomNum.toString();
    }

    /**
     * 取机器码的指定位数的16进制字符串
     * @param stringLength 指定位数
     * @throws Exception
     * @return String: 机器码的指定位数的16进制字符串
     */
    public String getMachineID(int stringLength) {
        StringBuffer machineID = new StringBuffer(stringLength);
        String addr = NetUtils.getLocalHost();
        String dataCenter = addr.split("\\.")[0];
        if("10".equals(dataCenter)){
        	dataCenter = "255";
        }else if("192".equals(dataCenter)){
        	dataCenter = "510";
        }else{
        	dataCenter ="0";
        }
        int mId= Integer.parseInt(addr.split("\\.")[3])+Integer.parseInt(dataCenter);
        machineID.append(intToHexString(mId, stringLength));
        return machineID.toString();
    }

    /**
     * int转换成指定位数16进制字符串
     * @param value int值
     * @param stringLength 指定位数
     * @throws Exception
     * @return String: 16进制字符串
     */
    public String intToHexString(int value, int stringLength) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                            'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuffer buffer = new StringBuffer(stringLength);
        int shift = stringLength - 1 << 2;
        for (int i = -1; ++i < stringLength;){
            buffer.append(hexDigits[value >> shift & 0XF]);
            value <<= 4;
        }
        return buffer.toString();
    }

    /**
     * byte转换成int
     * @param bytes byte
     * @throws Exception
     * @return int: int
     */
    public static int byteToInt(byte bytes[]) {
        int value = 0;
        for (int i = -1; ++i < bytes.length;) {
            value <<= 8;
            int b = bytes[i] & 0XFF;
            value |= b;
        }
        return value;
    }

    public String getMidValue() {
	return midValue;
    }

    public void setMidValue(String midValue) {
	this.midValue = midValue;
    }

    public static void main(String args []){
         System.err.println(UUIDKeyGenerator.getSingleton().getCurrentTime(8));
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
         try {
			Long a = sdf.parse("21000101").getTime()/1000;
			System.err.println(Long.toHexString(a));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//         System.err.println(byteToInt(addr));
//         System.err.println(Integer.toHexString(byteToInt(addr)));
//         System.err.println(UUIDKeyGenerator.getSingleton().intToHexString(byteToInt(addr), 4));
    }
}