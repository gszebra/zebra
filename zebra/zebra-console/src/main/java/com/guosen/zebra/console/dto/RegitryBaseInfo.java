package com.guosen.zebra.console.dto;

import java.io.Serializable;

public class RegitryBaseInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String group;
	private String service;
	private String type;
	private String set;
	private String version;
	private String ip;
	private int port;
	private String nodeId;
	private String key;
	private String lease;

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLease() {
		return this.lease;
	}

	public void setLease(String lease) {
		this.lease = lease;
	}

	public static long getSerialversionuid() {
		return 1L;
	}

	public String getGroup() {
		return this.group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getService() {
		return this.service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSet() {
		return this.set;
	}

	public void setSet(String set) {
		this.set = set;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String toString() {
		return "RegitryBaseInfo [group=" + this.group + ", service=" + this.service + ", type=" + this.type + ", set="
				+ this.set + ", version=" + this.version + ", ip=" + this.ip + ", port=" + this.port + ", nodeId="
				+ this.nodeId + ", key=" + this.key + ", lease=" + this.lease + "]";
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RegitryBaseInfo other = (RegitryBaseInfo) obj;
		if (this.group == null) {
			if (other.group != null) {
				return false;
			}
		} else if (!this.group.equals(other.group)) {
			return false;
		}
		if (this.service == null) {
			if (other.service != null) {
				return false;
			}
		} else if (!this.service.equals(other.service)) {
			return false;
		}
		if (this.set == null) {
			if (other.set != null) {
				return false;
			}
		} else if (!this.set.equals(other.set)) {
			return false;
		}
		if (this.version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!this.version.equals(other.version)) {
			return false;
		}
		return true;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIp() {
		return this.ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getNodeId() {
		return this.nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
}
