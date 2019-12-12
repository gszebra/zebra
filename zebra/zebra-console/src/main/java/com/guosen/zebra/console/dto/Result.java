package com.guosen.zebra.console.dto;

public class Result {
	private int code;
	private String msg;
	private Object data;
	private int count;

	private Result(Builder builder) {
		this.code = builder.code;
		this.msg = builder.msg;
		this.data = builder.data;
		this.count = builder.count;
	}

	public int getCode() {
		return this.code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return this.msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return this.data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String toString() {
		return "Result [code=" + this.code + ", msg=" + this.msg + ", data=" + this.data + ", count=" + this.count
				+ "]";
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private int code;
		private String msg;
		private Object data;
		private int count;

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

		public Builder withCount(int count) {
			this.count = count;
			return this;
		}

		public Result build() {
			return new Result(this);
		}
	}
}
