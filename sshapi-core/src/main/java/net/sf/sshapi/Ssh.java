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

package net.sf.sshapi;

import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

/**
 * Utilities for basic access to SSH operations. For simple use, this will
 * usually be the entry point.
 */
public class Ssh {

	/**
	 * Connect to an SSH server as a specified user and password.
	 * 
	 * @param username
	 *            username
	 * @param hostname
	 *            hostname
	 * @param port
	 *            port
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
	 * @param username
	 *            username
	 * @param hostname
	 *            hostname
	 * @param port
	 *            port
	 * @param authenticators authenticators
	 * @return connect client
	 * @throws SshException
	 */
	public static SshClient open(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws SshException {
		return new SshConfiguration().open(username, hostname, port, authenticators);
	}
	
	/**
	 * Connect to an SSH server using a connection string in the format user[:password]@host[:port].
	 * 
	 * @param spec spec
	 * @param authenticators authenticators
	 * @return connect client
	 * @throws SshException
	 */
	public static SshClient open(String spec, SshAuthenticator... authenticators)
			throws SshException {
		return new SshConfiguration().open(spec, authenticators);
	}
}
