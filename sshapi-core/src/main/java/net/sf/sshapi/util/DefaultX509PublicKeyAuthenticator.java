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
import java.io.InputStream;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshPasswordPrompt;
import net.sf.sshapi.auth.SshX509PublicKeyAuthenticator;

/**
 * A default implementation for {@link SshX509PublicKeyAuthenticator} that reads
 * the keystore from a file or from a byte array. The keystore passphrase for
 * the key is retrieved using the provided {@link SshPasswordPrompt} as is the
 * key password if it is different.
 */
public class DefaultX509PublicKeyAuthenticator extends DefaultPublicKeyAuthenticator implements SshX509PublicKeyAuthenticator {
	private String alias;
	private SshPasswordPrompt keyPassphrasePrompt;

	public DefaultX509PublicKeyAuthenticator(String alias, byte[] privateKeyData) {
		super(privateKeyData);
		init(alias, null);
	}

	public DefaultX509PublicKeyAuthenticator(String alias, InputStream privateKeyData) throws IOException {
		super(privateKeyData);
		init(alias, null);
	}

	public DefaultX509PublicKeyAuthenticator(String alias, File privateKeyFile) throws IOException {
		super(privateKeyFile);
		init(alias, null);
	}

	public DefaultX509PublicKeyAuthenticator(String alias, SshPasswordPrompt passphrasePrompt, byte[] privateKeyData) {
		super(passphrasePrompt, privateKeyData);
		init(alias, null);
	}

	public DefaultX509PublicKeyAuthenticator(String alias, SshPasswordPrompt passphrasePrompt, InputStream privateKeyData) throws IOException {
		super(passphrasePrompt, privateKeyData);
		init(alias, null);
	}

	public DefaultX509PublicKeyAuthenticator(String alias, SshPasswordPrompt passphrasePrompt, File privateKeyFile)
			throws IOException {
		super(passphrasePrompt, privateKeyFile);
		init(alias, null);
	}

	public DefaultX509PublicKeyAuthenticator(String alias, SshPasswordPrompt passphrasePrompt,
			SshPasswordPrompt keyPassphrasePrompt, byte[] privateKeyData) {
		super(passphrasePrompt, privateKeyData);
		init(alias, keyPassphrasePrompt);
	}

	public DefaultX509PublicKeyAuthenticator(String alias, SshPasswordPrompt passphrasePrompt,
			SshPasswordPrompt keyPassphrasePrompt, InputStream privateKeyData) throws IOException {
		super(passphrasePrompt, privateKeyData);
		init(alias, keyPassphrasePrompt);
	}

	public DefaultX509PublicKeyAuthenticator(String alias, SshPasswordPrompt passphrasePrompt,
			SshPasswordPrompt keyPassphrasePrompt, File privateKeyFile) throws IOException {
		super(passphrasePrompt, privateKeyFile);
		init(alias, keyPassphrasePrompt);
	}

	@Override
	public String getAlias() {
		return alias;
	}

	private void init(String alias, SshPasswordPrompt keyPassphrasePrompt) {
		this.alias = alias;
		this.keyPassphrasePrompt = keyPassphrasePrompt;
	}

	@Override
	public char[] promptForKeyPassphrase(SshClient session, String message) {
		if (keyPassphrasePrompt == null)
			return SshX509PublicKeyAuthenticator.super.promptForKeyPassphrase(session, message);
		else
			return keyPassphrasePrompt.promptForPassword(session, message);
	}
}
