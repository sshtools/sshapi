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

import java.io.Closeable;
import java.util.concurrent.Future;

/**
 * Many components of SSHAPI implement this interface, as they all follow the open() / close() pattern.
 * This applies to sessions, SFTP clients, port forwards and more.
 * <p>
 * All of the components may fire events when the lifecycle events occur.
 * @param <L> listener type
 *
 */
public interface SshLifecycleComponent<L extends SshLifecycleListener<? extends SshLifecycleComponent<L>>> extends Closeable, AutoCloseable {
	/**
	 * Add a listener to those informed when the current phase in this components lifecycle changes.
	 * 
	 * @param listener listener
	 */
	void addListener(L listener);

	/**
	 * Remove a listener from those informed when the current phase in this components lifecycle changes.
	 * 
	 * @param listener listener
	 */
	void removeListener(L listener);

	/**
	 * Open the component. Remember to {@link #close()} the component when
	 * you are finished with it.
	 * 
	 * @throws SshException
	 * @see {@link #close()}
	 * @see {@link #closeLater()}
	 */
	void open() throws SshException;

	/**
	 * Open the component without blocking. Remember to {@link #close()} or {@link #closeLater()} the component when
	 * you are finished with it.
	 * 
	 * @return future
	 * @throws SshException
	 * @see {@link #close()}
	 * @see {@link #closeLater()}
	 */
	Future<Void> openLater();

	/**
	 * Get if this component is currently open.
	 * 
	 * @return open
	 * @see #open()
	 * @see #close()
	 */
	boolean isOpen();

	/**
	 * Close this component sinking exceptions.
	 */
	void closeQuietly();
	
	/**
	 * Close this component, but do not block, instead return a future to monitor the progress
	 * of the close operation.
	 * 
	 * @return future
	 */
	Future<Void> closeLater();
}
