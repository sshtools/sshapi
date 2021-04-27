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
package net.sf.sshapi.util;

import java.io.File;
import java.io.IOException;

import net.sf.sshapi.SshPasswordPrompt;

/**
 * A public key authenticator that reads the key from a file or from a byte
 * array. The passphrase for the key is retrieved using the provided
 * {@link SshPasswordPrompt}.
 * <p>
 * Deprecated. See {@link DefaultPublicKeyAuthenticator}.
 */
@Deprecated
public class PEMFilePublicKeyAuthenticator extends DefaultPublicKeyAuthenticator {
	public PEMFilePublicKeyAuthenticator(SshPasswordPrompt passphrasePrompt, byte[] privateKeyData) throws IOException {
		super(passphrasePrompt, privateKeyData);
	}

	public PEMFilePublicKeyAuthenticator(SshPasswordPrompt passphrasePrompt, File privateKeyFile) throws IOException {
		super(passphrasePrompt, privateKeyFile);
	}
}
