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
package net.sf.sshapi.vfs;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractRandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.sftp.SftpHandle;
import net.sf.sshapi.sftp.SftpHandleInputStream;

class SftpRawContent extends AbstractRandomAccessContent {
	private final SftpHandle handle;
	private final SftpClient client;
	private final SftpFileSystem fs;

	public SftpRawContent(final SftpHandle handle, RandomAccessMode mode, SftpClient client, SftpFileSystem fs) {
		super(mode);
		this.handle = handle;
		this.client = client;
		this.fs = fs;
	}

	public long getFilePointer() throws IOException {
		return handle.position();
	}

	public void seek(long pos) throws IOException {
		if (pos == handle.position()) {
			return;
		}

		if (pos < 0) {
			throw new FileSystemException("vfs.provider/random-access-invalid-position.error",
					new Object[] { Long.valueOf(pos) });
		}
		handle.position(pos);
	}

	public void close() throws IOException {
		try {
			handle.close();
		}
		finally {
			fs.putClient(client);
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
//		if(inputStream == null) {
//			inputStream = new SftpHandleInputStream(null, handle);
//		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long length() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setLength(long newLength) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int skipBytes(int n) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean readBoolean() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte readByte() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readUnsignedByte() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short readShort() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readUnsignedShort() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public char readChar() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readInt() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readLong() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float readFloat() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double readDouble() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String readUTF() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
