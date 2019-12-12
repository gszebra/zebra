/**   
* @Title: FrequencyLimit.java 
* @Package com.guosen.zebra.frequency 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年12月12日 上午9:26:19 
* @version V1.0   
*/
package com.guosen.zebra.frequency;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @ClassName: FrequencyLimit
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2018年12月12日 上午9:26:19
 * 
 */
public class FrequencyLimit {
	private final String ZEBRA_FREQUENCY_LIMT_PREFIX = "fLimit";
	private JedisPool jedisPool;
	private String host;
	private int port;
	private String password;
	private String client;
	private boolean isOpen = true;

	public static FrequencyLimit newBuilder() {
		return new FrequencyLimit();
	}

	public FrequencyLimit host(String host) {
		this.host = host;
		return this;
	}

	public FrequencyLimit port(int port) {
		this.port = port;
		return this;
	}

	public FrequencyLimit isOpen(boolean isOpen) {
		this.isOpen = isOpen;
		return this;
	}

	public FrequencyLimit password(String password) {
		this.password = password;
		return this;
	}

	public FrequencyLimit client(String client) {
		this.client = client;
		return this;
	}

	public FrequencyLimit build() {
		JedisPoolConfig conf = new JedisPoolConfig();
		conf.setMaxIdle(10);
		conf.setMaxTotal(100);
		conf.setTestOnBorrow(true);
		conf.setMaxWaitMillis(1000);
		jedisPool = new JedisPool(conf, host, port, 1000, password, 0, client);
		return this;
	}

	/**
	 * @Title: passFrequencyLimit @Description: 对请求频率进行监控 @param @param
	 *         methodName 监控的方法名称 @param @param limitTime 限制时间 @param @param
	 *         freq 请求频率的上限 @param @param values 限制的字段如IP，HWID等等 @param @return
	 *         设定文件 @return boolean 返回类型 @throws
	 */
	public boolean passFrequencyLimit(String methodName, int freqTime, int limitTime, int freq, String... values) {
		if (!isOpen)
			return true;
		boolean isPass = true;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if (methodName == null) {
				methodName = "";
			}
			for (String vaule : values) {
				String key = ZEBRA_FREQUENCY_LIMT_PREFIX + client + methodName + vaule;
				String result = jedis.set(key, "1", "NX", "EX", freqTime);
				if ("OK".equals(result)) {
					isPass = true;
					return isPass;
				}
				String freqStr = jedis.get(key);
				int frequency = Integer.valueOf(freqStr);
				if (frequency > freq) {
					isPass = false;
					if (frequency - freq == 1) {
						jedis.del(key);
						frequency++;
						jedis.set(key, frequency + "", "NX", "EX", limitTime);
					}
					return isPass;
				}
				frequency++;
				jedis.setex(key, jedis.ttl(key).intValue(), frequency + "");
			}

		} catch (Exception e) {
			e.printStackTrace();
			isPass = true;
		} finally {
			try {
				if (jedis != null)
					jedis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return isPass;
	}

	@Override
	public String toString() {
		return "FrequencyLimit [ZEBRA_FREQUENCY_LIMT_PREFIX=" + ZEBRA_FREQUENCY_LIMT_PREFIX + ", jedisPool=" + jedisPool
				+ ", host=" + host + ", port=" + port + ", password=" + password + ", client=" + client + "]";
	}
}
