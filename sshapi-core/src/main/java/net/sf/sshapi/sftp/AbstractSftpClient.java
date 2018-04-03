/* 
 * Copyright (c) 2010 The JavaSSH Project
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.sf.sshapi.sftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.StringTokenizer;

import net.sf.sshapi.AbstractLifecycleComponentWithEvents;
import net.sf.sshapi.SshException;

/**
 * Abstract implementation of an {@link SftpClient}, providing some common
 * methods. All provider implentations will probably want to exted this.
 */
public abstract class AbstractSftpClient extends AbstractLifecycleComponentWithEvents implements SftpClient {

	/**
	 * Default implementation of {@link #mkdirs(String, int)} that uses
	 * {@link #mkdir(String, int)}.
	 * 
	 * @param path path
	 * @param permissions permissions
	 * @throws SshException on any error
	 */
	public void mkdirs(String dir, int permissions) throws SshException {
		StringTokenizer tokens = new StringTokenizer(dir, "/");
		String path = dir.startsWith("/") ? "/" : "";

		while (tokens.hasMoreElements()) {
			path += (String) tokens.nextElement();

			try {
				stat(path);
			} catch (SftpException ex) {
				if (ex.getCode() == SftpException.SSH_FX_NO_SUCH_FILE) {
					mkdir(path, permissions);
				} else {
					throw ex;
				}
			}

			path += "/";
		}
	}
	


	public void get(String path, OutputStream out, long filePointer) throws SshException {
		if(filePointer > 0) {
			throw new UnsupportedOperationException("This provider does not support setting of file pointer for downloads.");
		}
		get(path, out);
	}

	public InputStream get(String path, long filePointer) throws SshException {
		if(filePointer > 0) {
			throw new UnsupportedOperationException("This provider does not support setting of file pointer for downloads.");
		}
		return get(path);
	}

	public InputStream get(final String path) throws SshException {
		try {
			final PipedOutputStream pout = new PipedOutputStream();
			PipedInputStream pin = new PipedInputStream(pout);
			new Thread() {
				public void run() {
					try {
						get(path, pout);
					} catch (SshException sshe) {
					}
				}
			}.start();
			return pin;
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	public OutputStream put(final String path, final int permissions) throws SshException {
		final PipedInputStream pin = new PipedInputStream();
		try {
			PipedOutputStream pout = new PipedOutputStream(pin);
			new Thread() {
				public void run() {
					try {
						put(path, pin, permissions);
					} catch (SshException sshe) {
					}
				}
			}.start();
			return pout;
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}
	
	public OutputStream put(final String path, final int permissions, long offset) throws SshException {
		if(offset > 0) {
			throw new UnsupportedOperationException();
		}
		return put(path, permissions);
	}
}
