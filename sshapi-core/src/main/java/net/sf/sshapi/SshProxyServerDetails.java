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

/**
 * Holds details of the proxy server to connect to the remote SSH through. This
 * supports SOCKS4, SOCKS5 and HTTP (actual support will depend on provider).
 * 
 */
public class SshProxyServerDetails {

	/**
	 * Proxy sever type.
	 */
	public static class Type {
		/**
		 * SOCK4
		 */
		public final static Type SOCKS4 = new Type("socks4");
		/**
		 * SOCKS5
		 */
		public final static Type SOCKS5 = new Type("socks5");
		/**
		 * HTTP
		 */
		public final static Type HTTP = new Type("http");

		private String name;

		/**
		 * Constructor
		 * 
		 * @param name name
		 */
		public Type(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}

	private Type type;
	private String hostname;
	private int port;
	private String username;
	private char[] password;

	/**
	 * Constructor.
	 * 
	 * @param type type
	 * @param hostname host name of proxy server
	 * @param port port on which proxy server is listening
	 * @param username any username required (null for none)
	 * @param password any password required (null for none)
	 */
	public SshProxyServerDetails(Type type, String hostname, int port, String username, char[] password) {
		this.type = type;
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	/**
	 * Get the proxy server type.
	 * 
	 * @return type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Set the proxy server type.
	 * 
	 * @param type type
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Get the proxy server hostname
	 * 
	 * @return proxy server hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * Set the proxy server hostname
	 * 
	 * @param hostname proxy server hostname
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * Get the proxy server port.
	 * 
	 * @return port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Set the proxy server port.
	 * 
	 * @param port port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Get the username required to authenticate with the proxy server. If no
	 * authentication is required, this should be <code>null</code>.
	 * 
	 * @return proxy server user name
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the username required to authenticate with the proxy server. If no
	 * authentication is required, this should be <code>null</code>.
	 * 
	 * @param username proxy server user name
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Get the password required to authenticate with the proxy server. If no
	 * authentication is required, this should be <code>null</code>.
	 * 
	 * @return proxy server user password
	 */
	public char[] getPassword() {
		return password;
	}

	/**
	 * Set the password required to authenticate with the proxy server. If no
	 * authentication is required, this should be <code>null</code>.
	 * 
	 * @param password proxy server password
	 */
	public void setPassword(char[] password) {
		this.password = password;
	}

}
