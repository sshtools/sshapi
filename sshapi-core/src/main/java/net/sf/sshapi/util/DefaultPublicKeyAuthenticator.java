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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshPasswordPrompt;
import net.sf.sshapi.auth.SshPublicKeyAuthenticator;

/**
 * A public key authenticator that reads the key from a file or from a byte
 * array. The passphrase for the key is retrieved using the provided
 * {@link SshPasswordPrompt}.
 */
public class DefaultPublicKeyAuthenticator implements SshPublicKeyAuthenticator {
	private SshPasswordPrompt passphrasePrompt;
	private byte[] privateKeyData;
	private File privateKeyFile;


	/**
	 * Constructor
	 * 
	 * @param privateKeyData A file containing a DSA or RSA private key of the
	 *            user in OpenSSH key format.
	 */
	public DefaultPublicKeyAuthenticator(byte[] privateKeyData) {
		this(null, privateKeyData);
	}

	/**
	 * Constructor
	 * 
	 * @param passphrasePrompt invoked when passphrase is required
	 * @param privateKeyData A file containing a DSA or RSA private key of the
	 *            user in OpenSSH key format.
	 */
	public DefaultPublicKeyAuthenticator(SshPasswordPrompt passphrasePrompt, byte[] privateKeyData) {
		this.passphrasePrompt = passphrasePrompt;
		this.privateKeyData = privateKeyData;
	}

	/**
	 * Constructor
	 * 
	 * @param privateKeyFile A file containing a DSA or RSA private key of the
	 *            user in OpenSSH key format.
	 * @throws IOException on any IO error
	 */
	public DefaultPublicKeyAuthenticator(File privateKeyFile) throws IOException {
		this(null, privateKeyFile);
	}

	/**
	 * Constructor
	 * 
	 * @param passphrasePrompt invoked when passphrase is required
	 * @param privateKeyFile A file containing a DSA or RSA private key of the
	 *            user in OpenSSH key format.
	 * @throws IOException on any IO error
	 */
	public DefaultPublicKeyAuthenticator(SshPasswordPrompt passphrasePrompt, File privateKeyFile) throws IOException {
		this.passphrasePrompt = passphrasePrompt;
		this.privateKeyFile = privateKeyFile;
	}

	public byte[] getPrivateKey() {
		if (privateKeyData == null) {
			if (privateKeyFile != null) {
				privateKeyData = new byte[(int) privateKeyFile.length()];
				try (FileInputStream fin = new FileInputStream(privateKeyFile)) {
					int r;
					int o = 0;
					while (o < privateKeyData.length && (r = fin.read(privateKeyData, o, privateKeyData.length - o)) != -1)
						o += r;
				} catch (IOException ioe) {
					throw new IllegalStateException("Failed to read private key data.", ioe);
				}
			}
		}
		return privateKeyData;
	}

	@Override
	public File getPrivateKeyFile() {
		if (privateKeyFile == null) {
			if (privateKeyData != null) {
				try {
					privateKeyFile = File.createTempFile("pkey", ".tmp");
					privateKeyFile.deleteOnExit();
					try (FileOutputStream out = new FileOutputStream(privateKeyFile)) {
						out.write(privateKeyData);
					}
					try {
						Files.setPosixFilePermissions(privateKeyFile.toPath(),
								new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)));
					} catch (Exception e) {
					}
				} catch (IOException ioe) {
					throw new IllegalStateException("Failed to write private key data to temporary file.", ioe);
				}
			}
		}
		return privateKeyFile;
	}

	public char[] promptForPassphrase(SshClient session, String message) {
		return passphrasePrompt == null ? null : passphrasePrompt.promptForPassword(session, message);
	}
}
