package net.sf.sshapi.agent;

import net.sf.sshapi.SshChannelHandler;

/* 
 * Copyright (c) 2018 The JavaSSH Project
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

/**
 * Client implementations are responsible for maintaining the connection with
 * the server, authentication, and access to the sub-systems
 */
public interface SshAgent extends SshChannelHandler {
	/**
	 * OpenSSH and the de-facto standard
	 */
	public final static int OPENSSH_PROTOCOL = 0;
	/**
	 * RFC agent protocol.
	 */
	public final static int RFC_PROTOCOL = 1;
	/**
	 * Attempt to determine protocol automatically, falling back to
	 * {@link #OPENSSH_PROTOCOL} if not possible. NOTE: No providers can currently
	 * implement auto-detection due to a code clash used by the two different
	 * protocols. Both use code 9, with the RFC version using it as the FIRST
	 * request to determine protocol version. However, OpenSSH uses the code for
	 * removing all keys! This makes detection of protocol in use hard.
	 */
	public final static int AUTO_PROTOCOL = 2;

	/**
	 * Try to automatically determine the socket type.
	 */
	public final static int AUTO_AGENT_SOCKET_TYPE = 0;
	/**
	 * Use a TCP/IP socket for communication with agent.
	 */
	public final static int TCPIP_AGENT_SOCKET_TYPE = 1;
	/**
	 * Use a domain socket for communication with agent.
	 */
	public final static int UNIX_DOMAIN_AGENT_SOCKET_TYPE = 2;
	/**
	 * Use a windows named pipe for communication with agent.
	 */
	public final static int NAMED_PIPED_AGENT_SOCKET_TYPE = 3;

}
