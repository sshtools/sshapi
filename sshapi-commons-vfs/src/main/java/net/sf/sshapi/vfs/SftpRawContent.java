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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

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
	private SftpHandleInputStream inputStream;
	private final String path;

	public SftpRawContent(final String path, final SftpHandle handle, RandomAccessMode mode, SftpClient client, SftpFileSystem fs) {
		super(mode);
		this.path = path;
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
		inputStream = null;
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
		if(inputStream == null) {
			inputStream = new SftpHandleInputStream(ByteBuffer.allocate(8192), handle);
		}
		return inputStream;
	}

	@Override
	public long length() throws IOException {
		return client.stat(path).getSize();
	}

	@Override
	public void setLength(long newLength) throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		ByteBuffer buf = ByteBuffer.wrap(b);
		long r = 0;
		while(r<=b.length) {
			int rr = handle.read(buf);
			if(rr == -1) {
				if(r != b.length)
					throw new EOFException("Unexpected EOF.");
			}
			r += rr; 
		}
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(len);
		long r = 0;
		while(r<=b.length) {
			int rr = handle.read(buf);
			if(rr == -1) {
				if(r != b.length)
					throw new EOFException("Unexpected EOF.");
			}
			r += rr; 
		}
		buf.flip();
		buf.get(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		long was = handle.position();
		seek(was + n);
		return (int)(handle.position() - was);
	}

	@Override
	public boolean readBoolean() throws IOException {
		var bb = ByteBuffer.allocate(1);
		handle.read(bb);
		bb.flip();
		return bb.get() > 0;
	}

	@Override
	public byte readByte() throws IOException {
		var bb = ByteBuffer.allocate(1);
		handle.read(bb);
		bb.flip();
		return bb.get();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return Byte.toUnsignedInt(readByte());
	}

	@Override
	public short readShort() throws IOException {
		var bb = ByteBuffer.allocate(2);
		handle.read(bb);
		bb.flip();
		return bb.getShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return Short.toUnsignedInt(readShort());
	}

	@Override
	public char readChar() throws IOException {
		return (char)readShort();
	}

	@Override
	public int readInt() throws IOException {
		var bb = ByteBuffer.allocate(4);
		handle.read(bb);
		bb.flip();
		return bb.getInt();
	}

	@Override
	public long readLong() throws IOException {
		var bb = ByteBuffer.allocate(8);
		handle.read(bb);
		bb.flip();
		return bb.getLong();
	}

	@Override
	public float readFloat() throws IOException {
		var bb = ByteBuffer.allocate(4);
		handle.read(bb);
		bb.flip();
		return bb.getFloat();
	}

	@Override
	public double readDouble() throws IOException {
		var bb = ByteBuffer.allocate(8);
		handle.read(bb);
		bb.flip();
		return bb.getDouble();
	}

	@Override
	public String readUTF() throws IOException {
		var len = readShort();
		var bb = ByteBuffer.allocate(len);
		handle.read(bb);
		bb.flip();
		return bb.asCharBuffer().toString();
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.put(bytes);
		bb.flip();
		handle.write(bb);
	}

	@Override
	public void write(byte[] bytes, int off, int len) throws IOException {
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.put(bytes, off, len);
		bb.flip();
		handle.write(bb);
	}

	@Override
	public void write(int b) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(1);
		bb.put((byte)b);
		bb.flip();
		handle.write(bb);
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(1);
		bb.put(v ? (byte)1 : (byte)0);
		bb.flip();
		handle.write(bb);
	}

	@Override
	public void writeByte(int v) throws IOException {
		write(v);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(s.length());
		for(var c : s.toCharArray())
			bb.put((byte)c);
		bb.flip();
		handle.write(bb);
	}

	@Override
	public void writeChar(int v) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putChar((char)v);
		bb.flip();
		handle.write(bb);
	}

	@Override
	public void writeChars(String s) throws IOException {
		byte[] bytes = s.getBytes();
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.put(bytes); // TODO encoding from configuration (when it's added)
		bb.flip();
		handle.write(bb);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putDouble(v);
		bb.flip();
		handle.write(bb);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putFloat(v);
		bb.flip();
		handle.write(bb);
	}

	@Override
	public void writeInt(int v) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(v);
		bb.flip();
		handle.write(bb);
	}

	@Override
	public void writeLong(long v) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putLong(v);
		bb.flip();
		handle.write(bb);
	}

	@Override
	public void writeShort(int v) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putShort((short)v);
		bb.flip();
		handle.write(bb);
	}

	@Override
	public void writeUTF(String str) throws IOException {
		writeShort(str.length());
		write(str.getBytes("UTF-8"));
	}
}
