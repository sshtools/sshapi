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