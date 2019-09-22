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

import net.sf.sshapi.SshLifecycleListener;

/**
 * Interfaces to be implemented by classes that wish to be notified when port
 * forwarding channels are opened and closed.
 */
public interface SshPortForwardListener extends SshLifecycleListener<SshPortForward> {
	/**
	 * A port forwarding channel has been opened.
	 * 
	 * @param type
	 *            type (see {@link SshPortForward} for types)
	 * @param channel
	 *            the channel that was opened
	 */
	default void channelOpened(int type, SshPortForwardTunnel channel) {
	}

	/**
	 * A port forwarding channel has been closed.
	 * 
	 * @param type
	 *            type (see {@link SshPortForward} for types)
	 * @param channel
	 *            the channel that was closed
	 */
	default void channelClosed(int type, SshPortForwardTunnel channel) {
	}
}
