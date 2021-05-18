/**
 * Copyright (c) 2020 The JavaSSH Project
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package net.sf.sshapi.impl.jsch;

import java.security.SecureRandom;

import com.jcraft.jsch.Random;

/**
 * Jsch implementation of random number generator.
 */
public class JschRandom implements Random {
	
	private byte[] tmp = new byte[16];
	private SecureRandom random = JschSshProvider.RANDOM;

	/**
	 * Fill.
	 *
	 * @param foo the foo
	 * @param start the start
	 * @param len the len
	 */
	@Override
	public void fill(byte[] foo, int start, int len) {
		/*
		 * // This case will not become true in our usage. if(start==0 &&
		 * foo.length==len){ random.nextBytes(foo); return; }
		 */
		if (len > tmp.length) {
			tmp = new byte[len];
		}
		random.nextBytes(tmp);
		System.arraycopy(tmp, 0, foo, start, len);
	}
}
