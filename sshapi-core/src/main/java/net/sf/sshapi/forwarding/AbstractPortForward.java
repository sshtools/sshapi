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

import net.sf.sshapi.AbstractLifecycleComponent;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;

/**
 * Abstract implementation of {@link SshPortForward} that maintains the
 * {@link AbstractPortForward#isOpen()} state for any sub-classes.
 */
public abstract class AbstractPortForward extends AbstractLifecycleComponent<SshPortForwardListener, SshPortForward> implements SshPortForward {

	private boolean open;
	private int timeout;
	
	protected AbstractPortForward(SshProvider provider) {
		super(provider);
	}

	public void close() throws SshException {
		if (!open) {
			throw new SshException(SshException.NOT_OPEN, "The port forward is not open");
		}
		fireClosing();
		onClose();
		open = false;
		fireClosed();

	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public int getTimeout() {
		return timeout;
	}

	public boolean isOpen() {
		return open;
	}

	@Override
	public int getBoundPort() {
		return 0;
	}

	public final void open() throws SshException {
		if (open) {
			throw new SshException(SshException.ALREADY_OPEN, "The port forward is already open");
		}
		onOpen();
		open = true;
		fireOpened();
	}

	protected abstract void onOpen() throws SshException;

	protected abstract void onClose() throws SshException;
}
