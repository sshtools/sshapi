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
package net.sf.sshapi;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Utilities for basic access to SSH operations. For simple use, this will
 * usually be the entry point.
 */
public class Ssh {
	/**
	 * Callable appropriate for use with an SSH component.
	 *
	 * @param <T> type of component
	 */
	public interface SshCallable<T> {
		/**
		 * Call.
		 * 
		 * @param component component
		 * @throws IOException on error
		 */
		void call(T component) throws IOException;
	}
	
	/**
	 * Utility method to wait for a future, then chain to another task.
	 * 
	 * @param future future to wait for
	 * @param callable task to run
	 * @return the object from the future
	 * @throws IOException on any error
	 */
	public static <T> T then(Future<T> future, SshCallable<T> callable) throws IOException {
		try {
			T t = future.get();
			callable.call(t);
			return t;
		} catch (InterruptedException e) {
			throw new SshException(SshException.INTERRUPTED, e);
		} catch (ExecutionException e) {
			if(e.getCause() instanceof SshException)
				throw (SshException)e.getCause();
			else
				throw new SshException(SshException.GENERAL, "Failed to run task.", e);
		}
	}
	
	/**
	 * Connect to an SSH server as a specified user and password.
	 * 
	 * @param username username
	 * @param password password
	 * @param hostname hostname
	 * @param port port
	 * @return connect client
	 * @throws SshException
	 */
	public static SshClient open(String username, char[] password, String hostname, int port) throws SshException {
		SshConfiguration configuration = new SshConfiguration();
		return configuration.open(username, hostname, port, new SimplePasswordAuthenticator(password));
	}

	/**
	 * Connect to an SSH server as a specified user,using the provided
	 * authenticators.
	 * 
	 * @param username username
	 * @param hostname hostname
	 * @param port port
	 * @param authenticators authenticators
	 * @return connect client
	 * @throws SshException
	 */
	public static SshClient open(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws SshException {
		return new SshConfiguration().open(username, hostname, port, authenticators);
	}

	/**
	 * Connect to an SSH server using a connection string in the format
	 * user[:password]@host[:port].
	 * 
	 * @param spec spec
	 * @param authenticators authenticators
	 * @return connect client
	 * @throws SshException
	 */
	public static SshClient open(String spec, SshAuthenticator... authenticators) throws SshException {
		return new SshConfiguration().open(spec, authenticators);
	}

	/**
	 * Connect to an SSH server as a specified user and password, but do not
	 * block, instead return a future to monitor state of connection operation.
	 * 
	 * @param username username
	 * @param password password
	 * @param hostname hostname
	 * @param port port
	 * @return connect client
	 * @throws SshException
	 */
	public static SshClient openLater(String username, char[] password, String hostname, int port) throws SshException {
		SshConfiguration configuration = new SshConfiguration();
		return configuration.open(username, hostname, port, new SimplePasswordAuthenticator(password));
	}

	/**
	 * Connect to an SSH server as a specified user,using the provided
	 * authenticators, but do not block, instead return a future to monitor
	 * state of connection operation.
	 * 
	 * @param username username
	 * @param hostname hostname
	 * @param port port
	 * @param authenticators authenticators
	 * @return future
	 */
	public static Future<SshClient> openLater(String username, String hostname, int port, SshAuthenticator... authenticators) {
		return new SshConfiguration().openLater(username, hostname, port, authenticators);
	}

	/**
	 * Connect to an SSH server using a connection string in the format
	 * user[:password]@host[:port], but do not block, instead return a future to
	 * monitor state of connection operation.
	 * 
	 * @param spec spec
	 * @param authenticators authenticators
	 * @return future
	 */
	public static Future<SshClient> openLater(String spec, SshAuthenticator... authenticators) {
		return new SshConfiguration().openLater(spec, authenticators);
	}

	/**
	 * Get the version of SSHAPI.
	 * 
	 * @return SSHAPI version
	 */
	public static String version() {
		return Util.getArtifactVersion("com.sshtools", "sshapi-core");
	}
}
