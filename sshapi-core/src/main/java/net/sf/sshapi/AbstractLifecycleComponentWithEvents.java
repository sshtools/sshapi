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
 * Abstract implementation of an {@link AbstractLifecycleComponent} that fires
 * events when the lifecycle methods are called. Most SSHAPI lifecycle
 * components will extend this.
 * 
 */
public abstract class AbstractLifecycleComponentWithEvents<L extends SshLifecycleListener<C>, C extends SshLifecycleComponent<L, C>>
		extends AbstractLifecycleComponent<L, C> {

	private boolean open;

	public final boolean isOpen() {
		return open;
	}

	public final void open() throws SshException {
		if (isOpen()) {
			throw new SshException(SshException.ALREADY_OPEN, "Channel already open.");
		}
		onOpen();
		open = true;
		fireOpened();
	}

	protected final void checkOpen() {
		if (!open) {
			throw new IllegalStateException("Not open");
		}
	}

	public final void close() throws SshException {
		if (!isOpen()) {
			throw new SshException(SshException.NOT_OPEN, "Channel not open.");
		}
		fireClosing();
		onClose();
		open = false;
		fireClosed();
	}

	protected abstract void onOpen() throws SshException;

	protected abstract void onClose() throws SshException;
}
