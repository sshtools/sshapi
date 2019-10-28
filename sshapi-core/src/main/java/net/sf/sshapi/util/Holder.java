package net.sf.sshapi.util;

public class Holder<T> {
	private T value;

	public Holder() {
	}
	
	public Holder(T value) {
		set(value);
	}

	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = value;
	}
}