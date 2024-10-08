package net.sf.sshapi.examples;
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
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.Util;

class ExampleUtilities {
	/**
	 * Default buffer size for stream utility methods
	 */
	public static int BUFFER_SIZE = 8192;

	static void dumpClientInfo(SshClient client) {
		System.out.println("Provider");
		System.out.println("    Name: " + client.getProvider().getName());
		System.out.println("    Version: " + client.getProvider().getVersion());
		System.out.println("    Vendor: " + client.getProvider().getVendor());
		System.out.println("    Class: " + client.getProvider().getClass().getName());
		System.out.println("Client: " + client.getClass().getName());
		System.out.println("Capabilities: " + client.getProvider().getCapabilities());
		int protocolVersion = client.getConfiguration().getProtocolVersion();
		System.out.println("Ciphers: " + client.getProvider().getSupportedCiphers(protocolVersion));
		if (protocolVersion != SshConfiguration.SSH1_ONLY) {
			System.out.println("MAC: " + client.getProvider().getSupportedMAC());
			System.out.println("Compression: " + client.getProvider().getSupportedCompression());
			System.out.println("Key Exchange: " + client.getProvider().getSupportedKeyExchange());
			System.out.println("Public Key: " + client.getProvider().getSupportedPublicKey());
		}
	}

	static void joinShellToConsole(final SshShell channel) throws IOException, SshException {
		AtomicBoolean fin = new AtomicBoolean();
		AtomicBoolean closed = new AtomicBoolean();
		Thread mainThread = Thread.currentThread();
		Thread readErrThread = new Thread() {
			public void run() {
				try {
					Util.joinStreams(channel.getExtendedInputStream(), channel.getOutputStream());
				} catch (Exception e) {
				}
			}
		};
		readErrThread.start();
		Thread readInThread = new Thread() {
			public void run() {
				/*
				 * Wrapping in a channel allows this thread to be interrupted
				 * (on Linux at least, other OS's .. YMMV
				 */
				try (InputStream in = Channels.newInputStream((new FileInputStream(FileDescriptor.in)).getChannel())) {
					Util.joinStreams(in, channel.getOutputStream());
					channel.getInputStream().close();
				} catch (Exception e) {
				}
				if (!closed.get()) {
					closed.set(true);
					try {
						channel.close();
					} catch (IOException e) {
					}
					if (!fin.get())
						mainThread.interrupt();
				}
			}
		};
		readInThread.start();
		Util.joinStreams(channel.getInputStream(), System.out);
		fin.set(true);
		System.out.println("Finished reading");
		readInThread.interrupt();
		readErrThread.interrupt();
		if (!closed.get()) {
			closed.set(true);
			try {
				channel.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Copy from an input stream to an output stream. It is up to the caller to
	 * close the streams.
	 * 
	 * @param in input stream
	 * @param out output stream
	 * @throws IOException on any error
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException {
		copy(in, out, -1);
	}

	/**
	 * Copy the specified number of bytes from an input stream to an output
	 * stream. It is up to the caller to close the streams.
	 * 
	 * @param in input stream
	 * @param out output stream
	 * @param count number of bytes to copy
	 * @throws IOException on any error
	 */
	public static void copy(InputStream in, OutputStream out, long count) throws IOException {
		copy(in, out, count, BUFFER_SIZE);
	}

	/**
	 * Copy the specified number of bytes from an input stream to an output
	 * stream. It is up to the caller to close the streams.
	 * 
	 * @param in input stream
	 * @param out output stream
	 * @param count number of bytes to copy
	 * @param bufferSize buffer size
	 * @throws IOException on any error
	 */
	public static void copy(InputStream in, OutputStream out, long count, int bufferSize) throws IOException {
		byte buffer[] = new byte[bufferSize];
		int i = bufferSize;
		if (count >= 0) {
			while (count > 0) {
				if (count < bufferSize)
					i = in.read(buffer, 0, (int) count);
				else
					i = in.read(buffer, 0, bufferSize);
				if (i == -1)
					break;
				count -= i;
				out.write(buffer, 0, i);
			}
		} else {
			while (true) {
				i = in.read(buffer, 0, bufferSize);
				if (i < 0)
					break;
				out.write(buffer, 0, i);
			}
		}
	}
}
