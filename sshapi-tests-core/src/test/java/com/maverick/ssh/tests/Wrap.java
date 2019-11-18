package com.maverick.ssh.tests;

public class Wrap<T> {

	private T o;
	
	public Wrap() {
	}

	public Wrap(T o) {
		set(o);
	}
	
	public void set(T o) {
		this.o = o;
	}
	
	public T get() {
		return o;
	}
}
