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
package net.sf.sshapi.forwarding;

import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshDataProducingComponent;
import net.sf.sshapi.SshPortForwardTunnelListener;

/**
 * Represents a single tunnelled connection that is spawned as the result of a
 * client making a connection to the listening socket of a configured port
 * forward.
 * <p>
 * For example, if a single port forward to a web server address has been
 * configured, upon using entering the tunnelled URL the browser will make
 * multiple concurrent connections. For each connection that browser makes, all
 * registered {@link SshPortForwardListener} will be notified via
 * {@link SshPortForwardListener#channelOpened(int, SshPortForwardTunnel)}.
 * <p>
 * The browser will eventually time-out, and start closing all the connections
 * it made. When this happens,
 * {@link SshPortForwardListener#channelClosed(int, SshPortForwardTunnel)} will
 * be fired.
 */

public interface SshPortForwardTunnel extends SshDataProducingComponent<SshPortForwardTunnelListener, SshDataListener<SshPortForwardTunnel>> {

	/**
	 * Get the address the tunnel connection originated from
	 * 
	 * @return target address
	 * @see #getOriginatingPort()
	 */
	String getOriginatingAddress();

	/**
	 * Get the port the tunnel connection originated from
	 * 
	 * @return port
	 * @see #getOriginatingAddress()
	 */
	int getOriginatingPort();

	/**
	 * Get the address that the tunnel will listen on. Depending on the type of
	 * tunnel, this will either be an interface address on the the local machine, or
	 * an interface address on the remote SSH server.
	 * 
	 * @return address to listen on
	 * @see #getBindPort()
	 */
	String getBindAddress();

	/**
	 * Get the port that the tunnel will listen on. Depending on the type of tunnel,
	 * this will either be on an interface address on the the local machine, or on
	 * an interface address on the remote SSH server.
	 * 
	 * @return port to listen on
	 * @see #getBindAddress()
	 */
	int getBindPort();
}
