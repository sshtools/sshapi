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
 * Interface to be implemented by components that can produce file transfer
 * events, such as SFTP and SCP.
 * @param <L> listener type
 * @param <C> component type
 * 
 */
public interface SshFileTransferClient<L extends SshLifecycleListener<C>, C extends SshLifecycleComponent<L>> extends SshLifecycleComponent<L> {
	/**
	 * Add a listener to those informed when file transfer events occur.
	 * 
	 * @param listener listener to add
	 */
	void addFileTransferListener(SshFileTransferListener listener);

	/**
	 * Remove a listener from those informed when file transfer events occur.
	 * 
	 * @param listener listener to remove
	 */
	void removeFileTransferListener(SshFileTransferListener listener);
}
