package com.maverick.ssh.tests;

import java.io.IOException;
import java.io.InputStream;

public class RandomizedInputStream extends InputStream {
	
	private long length;
	private long progress;
	
	public RandomizedInputStream(long length) {
		this.length = length;
	}

	@Override
	public int read() throws IOException {
		if(progress == length) {
			return -1;
		}
		progress++;
		return (int)(Math.random() * 256);
	}

}
