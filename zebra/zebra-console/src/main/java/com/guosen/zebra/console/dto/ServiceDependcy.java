package com.guosen.zebra.console.dto;

import com.google.common.collect.Sets;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class ServiceDependcy {
	private String serviceName;
	private Set<ZebraServiceDependcy> dependcyApps;

	public Set<ZebraServiceDependcy> getDependcyApps() {
		return this.dependcyApps;
	}

	public void setDependcyApps(Set<ZebraServiceDependcy> dependcyApps) {
		this.dependcyApps = dependcyApps;
	}

	public void addDependcyApps(ZebraServiceDependcy dependcyApp) {
		if (this.dependcyApps == null) {
			this.dependcyApps = Sets.newHashSet();
		}
		this.dependcyApps.add(dependcyApp);
	}

	public void addDependcyService(String serviceName, Pair<String, Integer> serviceCallCount) {
		if (this.dependcyApps == null) {
			this.dependcyApps = Sets.newHashSet();
		}
		ZebraServiceDependcy item2Add = null;
		for (ZebraServiceDependcy depend : this.dependcyApps) {
			if (serviceName.equals(depend.getServiceName())) {
				item2Add = depend;
			}
		}
		if (item2Add == null) {
			item2Add = new ZebraServiceDependcy(serviceName);
		}
		this.dependcyApps.add(item2Add);
	}

	public String getServiceName() {
		return this.serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public static class ZebraServiceDependcy {
		private String serviceName;

		public ZebraServiceDependcy() {
		}

		public ZebraServiceDependcy(String serviceName) {
			this.serviceName = serviceName;
		}

		public String getServiceName() {
			return this.serviceName;
		}

		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}
	}
}
