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
package net.sf.sshapi.cli;

import java.util.concurrent.TimeUnit;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshFileTransferListener;
import net.sf.sshapi.util.Util;

public abstract class AbstractSshFilesCommand extends AbstractSshCommand  implements SshFileTransferListener {

	private static final long MS_IN_HOUR = TimeUnit.HOURS.toMillis(1);
	private static final long MS_IN_MINUTE = TimeUnit.MINUTES.toMillis(1);
	
	private long transferLastUpdate;
	private long transferLength;
	private String transferPath;
	private long transferProgressed;
	private int transferSpeed;
	private int transferBlock;
	
	public AbstractSshFilesCommand() throws SshException {
	}

	@Override
	public void finishedTransfer(String path, String targetPath) {
		updateBlock(true);
	}

	@Override
	public void startedTransfer(String path, String targetPath, long length) {
		this.transferLength = length;
		this.transferLastUpdate = System.currentTimeMillis();
		this.transferProgressed = 0;
		this.transferSpeed = 0;
		this.transferBlock = 0;
		this.transferPath = Util.basename(path);
		updateProgress(false);
	}

	@Override
	public void transferProgress(String path, String targetPath, long progress) {
		transferBlock += progress;
		if ((System.currentTimeMillis() - this.transferLastUpdate) > 1000) {
			updateBlock(false);
		}
	}

	private String formatSize(long bytes) {
		var sizeSoFar = String.valueOf(bytes);
		var size = bytes;
		if (size > 9999) {
			size = size / 1024;
			sizeSoFar = size + "KB";
			if (size > 9999) {
				size = size / 1024;
				sizeSoFar = size + "MB";
				if (size > 9999) {
					size = size / 1024;
					sizeSoFar = size + "GB";
				}
			}
		}
		return sizeSoFar;
	}

	private String formatSpeed(long bytesPerSecond) {
		var speedText = String.valueOf(bytesPerSecond) + "B/s";
		if (bytesPerSecond > 9999) {
			bytesPerSecond = bytesPerSecond / 1024;
			speedText = bytesPerSecond + "KB/s";
			if (bytesPerSecond > 9999) {
				bytesPerSecond = bytesPerSecond / 1024;
				speedText = bytesPerSecond + "MB/s";
				if (bytesPerSecond > 9999) {
					bytesPerSecond = bytesPerSecond / 1024;
					speedText = bytesPerSecond + "GB/s";
				}
			}
		}
		return speedText;
	}

	private String formatTime(long ms) {
		long hrs = ms / MS_IN_HOUR;
		long mins = (ms % MS_IN_HOUR) / MS_IN_MINUTE;
		long secs = (ms - (hrs * MS_IN_HOUR + (mins * MS_IN_MINUTE))) / 1000;

		if(hrs > 99999) {
			return String.format("%08d ETA", hrs);
		}
		else if(hrs > 99) {
			return String.format("%4d:%02dm ETA", hrs, mins);
		}
		else if(hrs > 0) {
			return String.format("%02d:%02d:%02d ETA", hrs, mins, secs);
		}
		else {
			return String.format("   %02d:%02d ETA", mins, secs);
		}
	}

	private void updateBlock(boolean newline) {
		var now = System.currentTimeMillis();
		var taken = now - this.transferLastUpdate;
		this.transferLastUpdate = now;
		this.transferSpeed = (int) ((taken / 1000.0) * transferBlock);
		transferProgressed += transferBlock;
		transferBlock = 0;
		updateProgress(newline);
	}

	private void updateProgress(boolean newline) {
		var pc = (int) (((double) transferProgressed / (double) transferLength) * 100.0);
		var sizeSoFar = formatSize(transferProgressed);
		// width - ( 5+ 10 + 8 + 3 + 1 + 1 + 1 + 1 )
		var w = reader == null ? 80 : terminal.getWidth();
		var filenameWidth = Math.max(10, w - 38);
		var msRemain = ( transferLength - transferProgressed ) / Math.max(1, transferSpeed / 1000);
		var result = String.format("%-" + filenameWidth + "s %3d%% %-8s %10s %5s",
				new Object[] { transferPath, Integer.valueOf(pc), sizeSoFar, formatSpeed(transferSpeed), formatTime(msRemain) });
		if (terminal == null) {
			System.out.print(result + "\r");
			if (newline) {
				System.out.println();
			}
		} else {
			if(newline)
				terminal.writer().println(result);
			else {
				terminal.writer().print(result + "\r");
			}
			terminal.writer().flush();
		}

	}
}
