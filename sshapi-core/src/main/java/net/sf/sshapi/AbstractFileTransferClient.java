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

/**
 * Abstract implementation of a file transfer client, providing some useful
 * default methods (currently transfer events).
 */
public abstract class AbstractFileTransferClient<L extends SshLifecycleListener<C>, C extends SshLifecycleComponent<L, C>>
		extends AbstractLifecycleComponentWithEvents<L, C> {

	private List<SshFileTransferListener> fileTransferListeners;
	
	protected AbstractFileTransferClient(SshProvider provider) {
		super(provider);
	}

	/**
	 * Add a new listener to be informed when file transfers start, progress or
	 * stop.
	 * 
	 * @param listener
	 *            listener to add
	 */
	public final synchronized void addFileTransferListener(SshFileTransferListener listener) {
		if (fileTransferListeners == null) {
			fileTransferListeners = new ArrayList<>();
		}
		fileTransferListeners.add(listener);
	}

	/**
	 * Remove a new listener from those informed when file transfers start, progress
	 * or stop.
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public final synchronized void removeFileTransferListener(SshFileTransferListener listener) {
		if (fileTransferListeners != null) {
			fileTransferListeners.remove(listener);
		}
	}

	/**
	 * Inform all listeners a file transfer has started
	 * 
	 * @param path
	 *            path of file
	 * @param targetPath
	 *            target path
	 * @param length
	 *            length of file
	 */
	public void fireFileTransferStarted(String path, String targetPath, long length) {
		if (fileTransferListeners != null) {
			for (int i = fileTransferListeners.size() - 1; i >= 0; i--)
				fileTransferListeners.get(i).startedTransfer(path, targetPath, length);
		}
	}

	/**
	 * Inform all listeners a file transfer has progressed.
	 * 
	 * @param path
	 *            path of file
	 * @param targetPath
	 *            target path
	 * @param progress
	 *            number of bytes progressed
	 */
	public void fireFileTransferProgressed(String path, String targetPath, long progress) {
		if (fileTransferListeners != null) {
			for (int i = fileTransferListeners.size() - 1; i >= 0; i--)
				fileTransferListeners.get(i).transferProgress(path, targetPath, progress);
		}
	}

	/**
	 * Inform all listeners a file transfer has finished.
	 * 
	 * @param path
	 *            path of file
	 * @param targetPath
	 *            target path
	 */
	public void fireFileTransferFinished(String path, String targetPath) {
		if (fileTransferListeners != null) {
			for (int i = fileTransferListeners.size() - 1; i >= 0; i--)
				fileTransferListeners.get(i).finishedTransfer(path, targetPath);
		}
	}

}
