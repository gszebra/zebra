package com.guosen.zebra.console.dto;

import com.alibaba.fastjson.JSONObject;

public class ServiceDetail {
	private String serverName;
	private String method;
	private String desc;
	private String pb;
	private JSONObject req;
	private JSONObject rsp;

	private ServiceDetail(Builder builder) {
		this.serverName = builder.serverName;
		this.method = builder.method;
		this.desc = builder.desc;
		this.pb = builder.pb;
		this.req = builder.req;
		this.rsp = builder.rsp;
	}

	public String toString() {
		return "ServiceDetail [serverName=" + this.serverName + ", method=" + this.method + ", desc=" + this.desc
				+ ", pb=" + this.pb + ", req=" + this.req + ", rsp=" + this.rsp + "]";
	}

	public String getServerName() {
		return this.serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getMethod() {
		return this.method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getDesc() {
		return this.desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getPb() {
		return this.pb;
	}

	public void setPb(String pb) {
		this.pb = pb;
	}

	public JSONObject getReq() {
		return this.req;
	}

	public void setReq(JSONObject req) {
		this.req = req;
	}

	public JSONObject getRsp() {
		return this.rsp;
	}

	public void setRsp(JSONObject rsp) {
		this.rsp = rsp;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String serverName;
		private String method;
		private String desc;
		private String pb;
		private JSONObject req;
		private JSONObject rsp;

		public Builder withServerName(String serverName) {
			this.serverName = serverName;
			return this;
		}

		public Builder withMethod(String method) {
			this.method = method;
			return this;
		}

		public Builder withDesc(String desc) {
			this.desc = desc;
			return this;
		}

		public Builder withPb(String pb) {
			this.pb = pb;
			return this;
		}

		public Builder withReq(JSONObject req) {
			this.req = req;
			return this;
		}

		public Builder withRsp(JSONObject rsp) {
			this.rsp = rsp;
			return this;
		}

		public ServiceDetail build() {
			return new ServiceDetail(this);
		}
	}
}
