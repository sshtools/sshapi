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
package com.maverick.ssh.tests.client;

import net.sf.sshapi.SshFileTransferListener;

public final class CountingFileTransferProgress implements
		SshFileTransferListener {
	
	private long progressed;
	private long bytesTotal;
	
	@Override
	public void startedTransfer(String sourcePath, String targetPath, long bytesTotal) {
		this.bytesTotal = bytesTotal;
	}

	@Override
	public void transferProgress(String sourcePath, String targetPath, long progress) {
		progressed = progress;
	}

	@Override
	public void finishedTransfer(String sourcePath, String targetPath) {
	}

	public long getTransferred() {
		return progressed;
	}
	
	public long getProgressed() {
		return progressed;
	}
	
	public long getBytesTotal() {
		return bytesTotal;
	}

	public boolean isCancelled() {
		return false;
	}
}