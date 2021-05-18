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
package net.sf.sshapi.sftp;

import java.io.Closeable;
import java.nio.ByteBuffer;

/**
 *
 */
public interface SftpHandle extends Closeable {
	
	/**
	 * Get the current position of the handle.
	 * 
	 * @return position
	 */
	long position();
	
	/**
	 * Set the current position of the handle.
	 * 
	 * @param position position
	 * @return this for chaining
	 */
	SftpHandle position(long position);
	
	/**
	 * Write the provided buffer to the current position.
	 * 
	 * @param buffer
	 * @return this for chaining
	 * @throws SftpException 
	 */
	SftpHandle write(ByteBuffer buffer) throws SftpException;
	
	/**
	 * Write the provided buffer to the current position.
	 * @param buffer 
	 * @return bytes read
	 * @throws SftpException 
	 */
	int read(ByteBuffer buffer) throws SftpException;
}
