package com.guosen.zebra.conf.dto;

import javax.annotation.Generated;

public class Result {
	private int code;
	private String msg;
	private Object data;
	@Generated("SparkTools")
	private Result(Builder builder) {
		this.code = builder.code;
		this.msg = builder.msg;
		this.data = builder.data;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "Result [code=" + code + ", msg=" + msg + ", data=" + data + "]";
	}
	/**
	 * Creates builder to build {@link Result}.
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}
	/**
	 * Builder to build {@link Result}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private int code;
		private String msg;
		private Object data;

		private Builder() {
		}

		public Builder withCode(int code) {
			this.code = code;
			return this;
		}

		public Builder withMsg(String msg) {
			this.msg = msg;
			return this;
		}

		public Builder withData(Object data) {
			this.data = data;
			return this;
		}

		public Result build() {
			return new Result(this);
		}
	}

}