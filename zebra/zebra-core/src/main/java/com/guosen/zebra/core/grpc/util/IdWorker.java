package com.guosen.zebra.core.grpc.util;

import java.net.UnknownHostException;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IdWorker {
	private static final Logger log = LogManager.getLogger(IdWorker.class);
	/**
	 * 起始的时间戳
	 */
	private final static long START_STMP = 1480166465631L;

	/**
	 * 每一部分占用的位数
	 */
	private static long SEQUENCE_BIT = 16; // 序列号占用的位数
	private static long MACHINE_BIT = 8; // 机器标识占用的位数
	private static long DATACENTER_BIT = 8;// 数据中心占用的位数

	/**
	 * 每一部分的最大值
	 */
	private static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
	private static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
	private static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

	/**
	 * 每一部分向左的位移
	 */
	private static long MACHINE_LEFT = SEQUENCE_BIT;
	private static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
	private static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

	private long datacenterId; // 数据中心
	private long machineId; // 机器标识
	private long sequence = 0L; // 序列号
	private long lastStmp = -1L;// 上一次时间戳

	private static IdWorker singleton = null;

	public synchronized static IdWorker getSingleton() {
		if (singleton == null) {
			synchronized (IdWorker.class) {
				if (singleton == null) {
					try {
						String addr = NetUtils.getLocalHost();
						String ip[] = addr.split("\\.");
						long datacenterId = Long.valueOf(ip[0]);
						long machineId = Long.valueOf(ip[3]);
						log.info("IdWorker init datacenterId {},machineId {}", datacenterId, machineId);
						singleton = new IdWorker(datacenterId, machineId);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						Random rand = new Random();
						long machineId = rand.nextInt(100) % 32;
						long datacenterId = rand.nextInt(100) % 32;
						singleton = new IdWorker(datacenterId, machineId);
					}

				}
			}
		}
		return singleton;
	}

	public IdWorker(long datacenterId, long machineId) {
		if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
			throw new IllegalArgumentException(
					"datacenterId can't be greater than MAX_DATACENTER_NUM " + MAX_DATACENTER_NUM + "or less than 0");
		}
		if (machineId > MAX_MACHINE_NUM || machineId < 0) {
			throw new IllegalArgumentException(
					"machineId can't be greater than MAX_MACHINE_NUM " + MAX_MACHINE_NUM + "or less than 0");
		}
		this.datacenterId = datacenterId;
		this.machineId = machineId;
	}

	/**
	 * 产生下一个ID
	 *
	 * @return
	 */
	public synchronized String nextId(boolean isHex) {
		long currStmp = getNewstmp();
		if (currStmp < lastStmp) {
			throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
		}

		if (currStmp == lastStmp) {
			// 相同毫秒内，序列号自增
			sequence = (sequence + 1) & MAX_SEQUENCE;
			// 同一毫秒的序列数已经达到最大
			if (sequence == 0L) {
				currStmp = getNextMill();
			}
		} else {
			// 不同毫秒内，序列号置为0
			sequence = 0L;
		}

		lastStmp = currStmp;
		long id= ((currStmp - START_STMP) << TIMESTMP_LEFT // 时间戳部分
				| datacenterId << DATACENTER_LEFT // 数据中心部分
				| machineId << MACHINE_LEFT // 机器标识部分
				| sequence); // 序列号部分
		return Long.toHexString(id);
	}
	
	/** 
	* @Title: nextId 
	* @Description: 可能返回负数
	* @param @return    设定文件 
	* @return Long    返回类型 
	* @throws 
	*/
	public synchronized Long nextId() {
		long currStmp = getNewstmp();
		if (currStmp < lastStmp) {
			throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
		}

		if (currStmp == lastStmp) {
			// 相同毫秒内，序列号自增
			sequence = (sequence + 1) & MAX_SEQUENCE;
			// 同一毫秒的序列数已经达到最大
			if (sequence == 0L) {
				currStmp = getNextMill();
			}
		} else {
			// 不同毫秒内，序列号置为0
			sequence = 0L;
		}

		lastStmp = currStmp;
		return ((currStmp - START_STMP) << TIMESTMP_LEFT // 时间戳部分
				| datacenterId << DATACENTER_LEFT // 数据中心部分
				| machineId << MACHINE_LEFT // 机器标识部分
				| sequence); // 序列号部分
	}

	private long getNextMill() {
		long mill = getNewstmp();
		while (mill <= lastStmp) {
			mill = getNewstmp();
		}
		return mill;
	}

	private long getNewstmp() {
		return System.currentTimeMillis();
	}
	
	public static void main(String[] args) throws UnknownHostException, InterruptedException {
		IdWorker idWork = IdWorker.getSingleton();
		for (int i = 0; i < 100; i++) {
			String a = idWork.nextId(true);
//			System.err.println(Long.toBinaryString(a));-9032733528206147573
			System.err.println((a+""));
//			Long b = Long.valueOf("-9032733528206147573");
//			Span.newBuilder().traceId(Long.toHexString(b));
		}
	}
}