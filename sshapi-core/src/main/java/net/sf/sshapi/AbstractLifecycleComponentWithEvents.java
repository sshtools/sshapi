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

/**
 * Abstract implementation of an {@link AbstractLifecycleComponent} that fires
 * events when the lifecycle methods are called. Most SSHAPI lifecycle
 * components will extend this.
 * @param <L> 
 * @param <C> 
 * 
 */
public abstract class AbstractLifecycleComponentWithEvents<L extends SshLifecycleListener<C>, C extends SshLifecycleComponent<L, C>>
		extends AbstractLifecycleComponent<L, C> {
	protected static final Logger LOG = SshConfiguration.getLogger();
	private boolean open;
	private boolean firesOwnCloseEvents;
	private boolean closing;
	protected Object closeLock = new Object();
	private boolean closed;

	protected AbstractLifecycleComponentWithEvents(SshProvider provider) {
		super(provider);
	}

	/**
	 * Get whether the component fires it's own close events.
	 * 
	 * @return fires own close events.
	 */
	public final boolean isFiresOwnCloseEvents() {
		return firesOwnCloseEvents;
	}


	/**
	 * Get whether the component fires it's own close events.
	 * 
	 * @param firesOwnCloseEvents fires own close events.
	 */
	public final void setFiresOwnCloseEvents(boolean firesOwnCloseEvents) {
		this.firesOwnCloseEvents = firesOwnCloseEvents;
	}
	
	/**
	 * Get if this component is closed.
	 * 
	 * @return closed
	 */
	public final boolean isClosed() {
		return closed;
	}
	
	/**
	 * Get if this component is closing.
	 * 
	 * @return closing
	 */
	public final boolean isClosing() {
		return closing;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public final void open() throws SshException {
		if (isOpen()) {
			throw new SshException(SshException.ALREADY_OPEN, "Channel already open.");
		}
		onOpen();
		open = true;
		fireOpened();
	}

	protected final void checkOpen() {
		if (!isOpen()) {
			throw new IllegalStateException("Not open");
		}
	}

	@Override
	public final void close() throws SshException {
		if (!closing) {
			if(LOG.isDebug())
				LOG.debug("Closing {0}", hashCode());
			/*
			 * Do NOT use isOpen() here because that may be override, but we still want
			 * events
			 */
			synchronized (closeLock) {
				if (open && !closing) {
					closing = true;
					try {
						try {
							beforeClose();
							if (!firesOwnCloseEvents)
								fireClosing();
							onClose();
						} finally {
							open = false;
							closed = true;
						}
						if (!closeFired)
							fireClosed();

						if(LOG.isDebug())
							LOG.debug("Closed {0}", hashCode());
						return;
					} finally {
						closing = false;
					}
				}
			}
		}

		LOG.debug("Request to close {0}, but it wasn't open", toString());
	}

	protected abstract void onOpen() throws SshException;

	protected abstract void onClose() throws SshException;

	protected void beforeClose() throws SshException {
	}
}
