package com.guosen.zebra.console.dto;

public class SentinelDTO {
	private int id;
	private String type;
	private String serverName;
	private String ip;
	private String data;

	public String toString() {
		return "SentinelDTO [id=" + this.id + ", type=" + this.type + ", serverName=" + this.serverName + ", ip="
				+ this.ip + ", data=" + this.data + "]";
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getServerName() {
		return this.serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getIp() {
		return this.ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getData() {
		return this.data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
