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
package net.sf.sshapi.forwarding;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshLifecycleComponent;

/**
 * Maintains state of either a local or remote port forward. The
 * {@link SshSession#createLocalForward(String, int, String, int)} and
 * {@link SshSession#createRemoteForward(String, int, String, int)} methods both
 * create instances of of this class. That object may then be used to start and
 * stop the actual tunnel.
 * 
 */
public interface SshPortForward extends SshLifecycleComponent<SshPortForwardListener, SshPortForward> {

	/**
	 * Constant used to specify whether the event relates to a local forwarding
	 **/
	public static final int LOCAL_FORWARDING = 1;

	/**
	 * Constant used to specify whether the event relates to a remote forwarding
	 **/
	public static final int REMOTE_FORWARDING = 2;

	/**
	 * Constant used to specify whether the event relates to an X11 forwarding
	 */
	public static final int X11_FORWARDING = 3;
	
	/**
	 * Set a timeout in milliseconds for opening the forward
	 * 
	 *  @param timeout timeout
	 */
	void setTimeout(int timeout);
	
	/**
	 * Get a timeout in milliseconds for opening the forward
	 * 
	 * @return timeout
	 */
	int getTimeout();

	/**
	 * Open the tunnel.
	 * 
	 * @throws SshException on any error
	 */
	void open() throws SshException;

	/**
	 * Close the tunnel.
	 * 
	 * @throws SshException on any error
	 */
	void close() throws SshException;

	/**
	 * Get if the tunnel is open or closed
	 */
	boolean isOpen();

	/**
	 * If a local port forward was started with a zero port (meaning next available port is chosen),
	 * this will return the actual port bound. Note, not all providers will support this,
	 * zero will be returned if they do not. Remote port forwards will always return zero 
	 * 
	 * @return bound port of zero if unsupported or a remote forward
	 */
	int getBoundPort();
}
