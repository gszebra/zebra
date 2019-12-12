package com.guosen.zebra.core.boot.autoconfigure;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.guosen.zebra.core.registry.etcd.EtcdRegistry;

/**
 * @ClassName: MonitorAutoconfiguration
 * @Description: 加载测试用的controller
 * @author 邓启翔
 * @date 2017年10月31日 上午9:26:57
 * 
 */
@Configuration
@Order(1)
public class MonitorAutoconfiguration {
	private static final Logger log = LogManager.getLogger(MonitorAutoconfiguration.class);
	@Bean
	public BeanFactoryPostProcessor beanFactoryPostProcessor(ApplicationContext applicationContext) {
		return new BeanFactoryPostProcessor() {
			@Override
			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
				printZebra();
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
						log.info("####################内存信息####################");
						log.info("Heap Memory: " + memorymbean.getHeapMemoryUsage());
						log.info("Non Heap Memory: " + memorymbean.getNonHeapMemoryUsage());

						List<GarbageCollectorMXBean> list = ManagementFactory.getGarbageCollectorMXBeans();
						if (list != null && list.size() > 0) {
							log.info("####################Gc信息####################");
							for (GarbageCollectorMXBean gcBean : list) {
								String s = "gc name=" + gcBean.getName() + ",gc count=" + gcBean.getCollectionCount()
										+ ",gc time=" + gcBean.getCollectionTime();
								log.info(s);
							}
						}

						ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
						long[] ids = threadBean.getAllThreadIds();
						log.info("####################线程信息####################");
						for (long id : ids) {
							ThreadInfo threadInfo = threadBean.getThreadInfo(id, Integer.MAX_VALUE);
							if (threadInfo != null) {
								String s = "blockcount=" + threadInfo.getBlockedCount() + ",blocktime="
										+ threadInfo.getBlockedTime();
								s = s + ",waitedcount=" + threadInfo.getWaitedCount() + ",waitedtime="
										+ threadInfo.getWaitedTime();
								log.info(s);
								log.info(getThreadInfo(threadInfo));
							}
						}

						long[] deadlock_ids = threadBean.findDeadlockedThreads();
						if (deadlock_ids != null) {
							log.info("####################死锁信息####################");
							for (long id : deadlock_ids) {
								log.info("死锁的线程号：" + id);
							}
						}
						EtcdRegistry.unRegister();
					}
				});
			}

		};
	}

	private void printZebra() {
		System.err.println("…………………………………………  ZEBRA   …………………………………………           ");
		System.err.println("                      ~~%%%%%%%%_,_,       ");
		System.err.println("                   ~~%%%%%%%%%-'/./        ");
		System.err.println("                 ~~%%%%%%%-’   /  `.       ");
		System.err.println("              ~~%%%%%%%%’  .     ,__;      ");
		System.err.println("            ~~%%%%%%%%’   :       |O)      ");
		System.err.println("          ~~%%%%%%%%’    :          `.     ");
		System.err.println("       ~~%%%%%%%%’       `. _,        ’    ");
		System.err.println("    ~~%%%%%%%%’          .’`-._        `.  ");
		System.err.println("~~%%%%%%%%%’           :     `-.     (,;   ");
		System.err.println("~~%%%%%%%%’             :         `._&_.’  ");
		System.err.println("~~%%%%%%%’              ;                  ");
		System.err.println("…………………………………………  ZEBRA   …………………………………………           ");
	}

	public static String getThreadInfo(ThreadInfo t) {

		try {
			StringBuilder sb = new StringBuilder(
					"\"" + t.getThreadName() + "\"" + " Id=" + t.getThreadId() + " " + t.getThreadState());
			if (t.getLockName() != null) {
				sb.append(" on " + t.getLockName());
			}
			if (t.getLockOwnerName() != null) {
				sb.append(" owned by \"" + t.getLockOwnerName() + "\" Id=" + t.getLockOwnerId());
			}
			if (t.isSuspended()) {
				sb.append(" (suspended)");
			}
			if (t.isInNative()) {
				sb.append(" (in native)");
			}
			sb.append('\n');
			int i = 0;
			for (StackTraceElement ste : t.getStackTrace()) {
				sb.append("\tat " + ste.toString());
				sb.append('\n');
				if (i == 0 && t.getLockInfo() != null) {
					Thread.State ts = t.getThreadState();
					switch (ts) {
					case BLOCKED:
						sb.append("\t-  blocked on " + t.getLockInfo());
						sb.append('\n');
						break;
					case WAITING:
						sb.append("\t-  waiting on " + t.getLockInfo());
						sb.append('\n');
						break;
					case TIMED_WAITING:
						sb.append("\t-  waiting on " + t.getLockInfo());
						sb.append('\n');
						break;
					default:
					}
				}

				for (MonitorInfo mi : t.getLockedMonitors()) {
					if (mi.getLockedStackDepth() == i) {
						sb.append("\t-  locked " + mi);
						sb.append('\n');
					}
				}
			}
			if (i < t.getStackTrace().length) {
				sb.append("\t...");
				sb.append('\n');
			}

			LockInfo[] locks = t.getLockedSynchronizers();
			if (locks.length > 0) {
				sb.append("\n\tNumber of locked synchronizers = " + locks.length);
				sb.append('\n');
				for (LockInfo li : locks) {
					sb.append("\t- " + li);
					sb.append('\n');
				}
			}
			sb.append('\n');
			return sb.toString();
		} catch (Exception e) {
			
		}
		return "";
	}
}
