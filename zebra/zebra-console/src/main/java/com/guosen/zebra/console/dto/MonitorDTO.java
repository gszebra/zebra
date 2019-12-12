package com.guosen.zebra.console.dto;

import java.util.Date;
import javax.annotation.Generated;

public class MonitorDTO {
	private String service;
	private String method;
	private String addr;
	private long delay;
	private boolean isSuccess;
	private String message;
	private String desc;
	private Date monitorTime;
	private boolean isExpect;

	@Generated("SparkTools")
	private MonitorDTO(Builder builder) {
		this.service = builder.service;
		this.method = builder.method;
		this.addr = builder.addr;
		this.delay = builder.delay;
		this.isSuccess = builder.isSuccess;
		this.message = builder.message;
		this.desc = builder.desc;
		this.monitorTime = builder.monitorTime;
		this.isExpect = builder.isExpect;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Date getMonitorTime() {
		return monitorTime;
	}

	public void setMonitorTime(Date monitorTime) {
		this.monitorTime = monitorTime;
	}

	public boolean isExpect() {
		return isExpect;
	}

	public void setExpect(boolean isExpect) {
		this.isExpect = isExpect;
	}

	@Override
	public String toString() {
		return "MonitorDTO [service=" + service + ", method=" + method + ", addr=" + addr + ", delay=" + delay
				+ ", isSuccess=" + isSuccess + ", message=" + message + ", desc=" + desc + ", monitorTime="
				+ monitorTime + ", isExpect=" + isExpect + "]";
	}

	/**
	 * Creates builder to build {@link MonitorDTO}.
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link MonitorDTO}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private String service;
		private String method;
		private String addr;
		private long delay;
		private boolean isSuccess;
		private String message;
		private String desc;
		private Date monitorTime;
		private boolean isExpect;

		private Builder() {
		}

		public Builder withService(String service) {
			this.service = service;
			return this;
		}

		public Builder withMethod(String method) {
			this.method = method;
			return this;
		}

		public Builder withAddr(String addr) {
			this.addr = addr;
			return this;
		}

		public Builder withDelay(long delay) {
			this.delay = delay;
			return this;
		}

		public Builder withIsSuccess(boolean isSuccess) {
			this.isSuccess = isSuccess;
			return this;
		}

		public Builder withMessage(String message) {
			this.message = message;
			return this;
		}

		public Builder withDesc(String desc) {
			this.desc = desc;
			return this;
		}

		public Builder withMonitorTime(Date monitorTime) {
			this.monitorTime = monitorTime;
			return this;
		}

		public Builder withIsExpect(boolean isExpect) {
			this.isExpect = isExpect;
			return this;
		}

		public MonitorDTO build() {
			return new MonitorDTO(this);
		}
	}

}
