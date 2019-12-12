package com.guosen.zebra.console.dto;

import com.google.common.collect.Sets;
import java.util.Set;

public class Counter {
	private Set<String> total = Sets.newHashSet();
	private Set<String> down = Sets.newHashSet();
	private int status = 1;

	public Set<String> getTotal() {
		return this.total;
	}

	public void setTotal(Set<String> total) {
		this.total = total;
	}

	public Set<String> getDown() {
		return this.down;
	}

	public void setDown(Set<String> down) {
		this.down = down;
	}

	public String toString() {
		return "Counter [total=" + this.total + ", down=" + this.down + "]";
	}

	public int getStatus() {
		if ((this.down.size() > 0) && (this.down.size() < this.total.size())) {
			this.status = 2;
		}
		if ((this.total.size() > 0) && (this.down.size() == this.total.size())) {
			this.status = 3;
		}
		return this.status;
	}
}
