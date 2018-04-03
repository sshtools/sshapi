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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshPasswordPrompt;
import net.sf.sshapi.auth.SshPublicKeyAuthenticator;

/**
 * A public key authenticator that reads the key from a file. The passphrase for
 * the key is retrieved using the provided {@link SshPasswordPrompt}.
 */
public class PEMFilePublicKeyAuthenticator implements SshPublicKeyAuthenticator {

	private SshPasswordPrompt passphrasePrompt;
	private byte[] privateKey;

	/**
	 * Constructor
	 * 
	 * @param passphrasePrompt invoked when passphrase is required
	 * @param pemFile A file containing a DSA or RSA private key of the user in
	 *            OpenSSH key format.
	 * @throws IOException on any IO error
	 */
	public PEMFilePublicKeyAuthenticator(SshPasswordPrompt passphrasePrompt, File pemFile) throws IOException {
		this.passphrasePrompt = passphrasePrompt;

		byte[] buff = new byte[256];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		FileInputStream fin = new FileInputStream(pemFile);
		try {
			while (true) {
				int len = fin.read(buff);
				if (len < 0)
					break;
				baos.write(buff, 0, len);
			}
			privateKey = baos.toByteArray();
		} finally {
			fin.close();
		}
	}

	public byte[] getPrivateKey() {
		return privateKey;
	}

	public char[] promptForPassphrase(SshClient session, String message) {
		return passphrasePrompt.promptForPassword(session, message);
	}

	public String getTypeName() {
		return "publickey";
	}

}
