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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Abstract implementation of an {@link SshLifecycleComponent}, providing some
 * command methods.
 * @param <L> the listener type
 */
public abstract class AbstractLifecycleComponent<L extends SshLifecycleListener<SshLifecycleComponent<L>>> implements SshLifecycleComponent<L> {

	protected List<L> listeners;

	protected final SshProvider provider;

	protected boolean closeFired;

	private boolean closingFired;
	
	protected AbstractLifecycleComponent(SshProvider provider) {
		this.provider = provider;
	}

	@Override
	public Future<Void> openLater() {
		AbstractFuture<Void> openFuture = new AbstractFuture<Void>() {
			@Override
			Void doFuture() throws Exception {
				open();
				return null;
			}
		};
		provider.getExecutor().submit(openFuture.createRunnable());
		return openFuture;
	}

	public final synchronized void addListener(L listener) {
		if (listeners == null) {
			listeners = new ArrayList<>();
		}
		listeners.add(listener);
	}

	public final synchronized void removeListener(L listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	@Override
	public final void closeQuietly() {
		try {
			close();
		} catch (Exception e) {
		}
	}

	@Override
	public Future<Void> closeLater() {
		return new AbstractFuture<Void>() {
			@Override
			Void doFuture() throws Exception {
				close();
				return null;
			}
		};
	}

	/**
	 * Inform all listeners the component has opened.
	 */
	protected void fireOpened() {
		if (listeners != null) {
			for (int i = listeners.size() - 1; i >= 0; i--)
				listeners.get(i).opened(this);
		}
	}

	/**
	 * Inform all listeners the component has closed.
	 */
	protected void fireClosed() {
		if (listeners != null && !closeFired) {
			closeFired = true;
			for (int i = listeners.size() - 1; i >= 0; i--)
				listeners.get(i).closed(this);
		}
	}

	/**
	 * Inform all listeners the component is closing.
	 */
	protected void fireClosing() {
		if (listeners != null && !closingFired) {
			closingFired = true;
			for (int i = listeners.size() - 1; i >= 0; i--)
				listeners.get(i).closing(this);
		}
	}

	protected List<L> getListeners() {
		return listeners;
	}
}
