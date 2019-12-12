package com.guosen.zebra.core.grpc;

import java.io.Serializable;

/**
 * @ClassName: RpcBaseConfig
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2017年10月31日 上午10:40:26
 * 
 */
public class RpcServiceBaseInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String group;
	private String service;
	/**
	 * @Fields type :gateway、monitor、server、client、pub、sub
	 */
	private String type;
	private Object target;
	private String set;
	private String version;
	private String ip;
	private int port;
	private String nodeId;
	
	private Class<?> []clzs;
	
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Object getTarget() {
		return target;
	}
	public void setTarget(Object target) {
		this.target = target;
	}
	
	public String getSet() {
		return set;
	}
	public void setSet(String set) {
		this.set = set;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "RpcSeriviceBaseInfo [group=" + group + ", service=" + service + ", type=" + type + ", target=" + target
				+ ", set=" + set + ", version=" + version + ", ip=" + ip + ", port=" + port + ", nodeId="+ nodeId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		result = prime * result + ((set == null) ? 0 : set.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RpcServiceBaseInfo other = (RpcServiceBaseInfo) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		if (set == null) {
			if (other.set != null)
				return false;
		} else if (!set.equals(other.set))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public Class<?> [] getClzs() {
		return clzs;
	}
	public void setClzs(Class<?> [] clzs) {
		this.clzs = clzs;
	}
}
