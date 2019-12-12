/**   
* @Title: AESUtil.java 
* @Package com.guosen.zebra.core.serializer.utils 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年11月14日 上午9:04:01 
* @version V1.0   
*/
package com.guosen.zebra.core.serializer.utils;

import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

public class AESUtil {

	private static final String KEY_ALGORITHM = "AES";
	private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";// 默认的加密算法
	private static final String KEY = "0755821308331234";

	/**
	 * AES 加密操作
	 *
	 * @param content
	 *            待加密内容
	 * @param password
	 *            加密密码
	 * @return 返回Base64转码后的加密数据
	 */
	public static String encrypt(String content, String password) {
		try {
			if (StringUtils.isEmpty(password)) {
				password = KEY;
			}
			Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);// 创建密码器

			byte[] byteContent = content.getBytes("utf-8");

			SecretKeySpec skeySpec = new SecretKeySpec(password.getBytes(), KEY_ALGORITHM);

			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);// 初始化为加密模式的密码器

			byte[] result = cipher.doFinal(byteContent);// 加密
			System.err.println(Arrays.toString(result));

			return Base64.getEncoder().encodeToString(result);// 通过Base64转码返回
		} catch (Exception ex) {
			Logger.getLogger(AESUtil.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	/**
	 * AES 解密操作
	 *
	 * @param content
	 * @param password
	 * @return
	 */
	public static String decrypt(String content, String password) {
		try {
			if (StringUtils.isEmpty(password)) {
				password = KEY;
			}
			byte[] raw = password.getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, KEY_ALGORITHM);
			// 实例化
			Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			// 使用密钥初始化，设置为解密模式
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);

			// 执行操作
			byte[] result = cipher.doFinal(Base64.getDecoder().decode(content));

			return new String(result, "utf-8");
		} catch (Exception ex) {
			Logger.getLogger(AESUtil.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	public static void main(String[] args) {
		String s = "147258";
		System.out.println("s:" + s);
		String s1 = AESUtil.encrypt(s, "");
		System.out.println("s1:" + s1);
		System.out.println("s2:" + AESUtil.decrypt(s1, ""));
	}
}