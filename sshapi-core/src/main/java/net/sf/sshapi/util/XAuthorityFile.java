/* 
 * Copyright (c) 2010 The JavaSSH Project
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.sf.sshapi.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Parses a standard X Authority File
 * 
 */
public class XAuthorityFile {

	private static final String COOKIE_TYPE_NAME = "MIT-MAGIC-COOKIE-1";
	private byte[] data;
	private final int screen;
	private final String hostname;
	private final File file;

	/**
	 * Constructor.
	 * 
	 * @param file file to read
	 * @param hostname hostnam
	 * @param screen screen
	 * @throws IOException
	 */
	public XAuthorityFile(File file, String hostname, int screen) throws IOException {
		this.hostname = hostname;
		this.file = file;
		this.screen = screen;

		// Find the users real cookie
		if (file.exists()) {
			FileInputStream in = new FileInputStream(file);
			DataInputStream din = new DataInputStream(in);
			try {
				while (din.available() > 0) {
					short family = din.readShort();
					short len = din.readShort();
					byte[] address = new byte[len];
					din.readFully(address);
					len = din.readShort();
					byte[] number = new byte[len];
					din.readFully(number);
					len = din.readShort();
					byte[] name = new byte[len];
					din.readFully(name);
					len = din.readShort();
					byte[] data = new byte[len];
					din.readFully(data);

					String n = new String(number);
					int d = Integer.parseInt(n);

					String protocol = new String(name);
					if (protocol.equals(COOKIE_TYPE_NAME)) {
						if (family == 0) {
							// We cannot use InetAddress.getByAddress since it
							// was only introduced in 1.4 :(
							// So we're going to do this really crude formating
							// of the IP Address and get by name
							// which works just as well!
							String ip = (address[0] & 0xFF) + "." + (address[1] & 0xFF) + "." + (address[2] & 0xFF) + "."
								+ (address[3] & 0xFF);
							InetAddress addr = java.net.InetAddress.getByName(ip);
							if (addr.getHostAddress().equals(hostname) || addr.getHostName().equals(hostname)) {
								if (screen == d) {
									this.data = data;
									break;
								}
							}
						} else if (family == 256) {
							String h = new String(address);
							if (h.equals(hostname)) {
								if (screen == d) {
									this.data = data;
									break;
								}
							}
						}
					}
				}
			} finally {
				din.close();
			}
		}
	}

	/**
	 * Get the XAuthorty data.
	 * 
	 * @return data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Set the XAuthority data.
	 * 
	 * @param data
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * Write out the attributes of this object in the .XAuthority file format.
	 * 
	 * @param append append
	 * @throws IOException
	 */
	public void write(boolean append) throws IOException {
		FileOutputStream fout = new FileOutputStream(file, append);
		try {
			DataOutputStream dout = new DataOutputStream(fout);
			dout.writeShort(256);
			dout.writeShort(hostname.length());
			dout.writeBytes(hostname);
			String screenNo = String.valueOf(screen);
			dout.writeShort(screenNo.length());
			dout.writeBytes(screenNo);
			dout.writeShort(COOKIE_TYPE_NAME.length());
			dout.writeBytes(COOKIE_TYPE_NAME);
			dout.writeShort(data.length);
			dout.write(data);
		} finally {
			fout.close();
		}

	}
}
