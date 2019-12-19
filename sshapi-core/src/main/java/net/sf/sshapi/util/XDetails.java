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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import net.sf.sshapi.SshConfiguration;

/**
 * Utility class that tries to determine the default values for X Display Host,
 * Screen and Magic Cookie.
 */
public class XDetails {

	private int x11Screen;
	private String x11Host;
	private byte[] x11Cookie;
	private File x11UnixSocketFile;

	/**
	 * Constructor. Uses default X authority file and DISPLAY
	 * 
	 * @throws IOException
	 */
	public XDetails() throws IOException {
		this(getDefaultXAuthorityFile(), System.getenv("DISPLAY"));
	}

	/**
	 * Constructor.
	 * 
	 * @param xAuthorityFile X authority file
	 * @param display DISPLAY environment variable value
	 * @throws IOException
	 */
	public XDetails(File xAuthorityFile, String display) throws IOException {

		String[] displayElements = display.split(":");
		String[] displayNumberElements = displayElements[1].split("\\.");
		int displayNumber = Integer.parseInt(displayNumberElements[0]);

		x11Host = displayElements[0];
		if (x11Host.equals("")) {
			/*
			 * Try and get the hostname from the HOSTNAME variable, as this
			 * should match what's in the XAUTHORITY file
			 */
			x11Host = System.getenv("HOSTNAME");
			if (x11Host == null || x11Host.equals("")) {
				x11Host = InetAddress.getLocalHost().getHostName();
			}

		}
		x11Screen = displayNumber;
		x11Cookie = null;
		
		//
		File socketDir = new File(System.getProperty("sshapi.x11SocketDirectory", System.getProperty("java.io.tmpdir") + File.separator + ".X11-unix"));
		File socketFile = new File(socketDir, String.format("X%d", x11Screen));
		if(socketFile.exists()) {
			x11UnixSocketFile = socketFile;
		}

		// Try and get the magic cookie
		if (xAuthorityFile.exists()) {
			XAuthorityFile af = new XAuthorityFile(xAuthorityFile, x11Host, displayNumber);
			x11Cookie = af.getData();
		}
	}

	private static File getDefaultXAuthorityFile() {
		String xauthorityFile = System.getenv("XAUTHORITY");
		File xAuthorityFile = new File(xauthorityFile);
		return xAuthorityFile;
	}

	/**
	 * Apply the discovered details to the configuration.
	 * 
	 * @param configuration configuration
	 */
	public void configure(SshConfiguration configuration) {
		SshConfiguration.getLogger().debug("Configuring for X11 tunnel. Host {0}, Screen {1}, Cookie {2}, File {3}", x11Host, x11Screen, Util.formatAsHexString(x11Cookie), x11UnixSocketFile);
		configuration.setX11Host(x11Host);
		configuration.setX11Screen(x11Screen);
		configuration.setX11Cookie(x11Cookie);
		configuration.setX11UnixSocketFile(x11UnixSocketFile);
	}

	/**
	 * Get the screen number.
	 * 
	 * @return screen number
	 */
	public int getX11Screen() {
		return x11Screen;
	}

	/**
	 * Get the host.
	 * 
	 * @return host
	 */
	public String getX11Host() {
		return x11Host;
	}

	/**
	 * Get the unix socket file.
	 * 
	 * @return unix socket file
	 */
	public File getX11UnixSocketFile() {
		return x11UnixSocketFile;
	}

	/**
	 * Get the cookie.
	 * 
	 * @return cookie
	 */
	public byte[] getX11Cookie() {
		return x11Cookie;
	}

	/**
	 * Set the cookie.
	 * 
	 * @param x11Cookie cookie
	 * @return this for chaining
	 */
	public XDetails setX11Cookie(byte[] x11Cookie) {
		this.x11Cookie = x11Cookie;
		return this;
	}
}
