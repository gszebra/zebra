package com.guosen.zebra.console.dto;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;

public class ServiceInfo {
	private String name;
	private int x;
	private int y;
	private List<Line> lines = Lists.newArrayList();
	private String value;
	private Set<String> addrs = Sets.newHashSet();
	private int status = 1;
	private String type;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getX() {
		return this.x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return this.y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Set<String> getAddrs() {
		return this.addrs;
	}

	public void setAddrs(Set<String> addrs) {
		this.addrs = addrs;
	}

	public List<Line> getLines() {
		return this.lines;
	}

	public void setLines(List<Line> lines) {
		this.lines = lines;
	}

	public String toString() {
		return "ServiceInfo [name=" + this.name + ", x=" + this.x + ", y=" + this.y + ", lines=" + this.lines
				+ ", value=" + this.value + ", addrs=" + this.addrs + ", status=" + this.status + ", type=" + this.type
				+ "]";
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
