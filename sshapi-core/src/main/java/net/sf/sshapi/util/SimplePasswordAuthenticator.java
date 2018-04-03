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

import net.sf.sshapi.SshClient;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;

/**
 * Simple {@link SshAuthenticator} implementation that has fixed passwords and
 * passphrases.
 */
public class SimplePasswordAuthenticator implements SshPasswordAuthenticator {
	private char[] password;

	/**
	 * Constructor for when only password is going to be prompted for.
	 * 
	 * @param password
	 *            password
	 */
	public SimplePasswordAuthenticator(char[] password) {
		this.password = password;
	}

	/**
	 * Set the password that will be returned when
	 * {@link #promptForPassword(SshSession, String)} is called.
	 * 
	 * @param password
	 *            password
	 */
	public void setPassword(char[] password) {
		this.password = password;
	}

	public char[] promptForPassword(SshClient session, String message) {
		return password;
	}

	public String getTypeName() {
		return "password";
	}
}
