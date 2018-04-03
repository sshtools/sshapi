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
package net.sf.sshapi.identity;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey;

/**
 * Represent a persisted {@link SshPrivateKey}. Provider implementations are capable of reading some known
 * private key format, and presenting as an instance of this interface.
 * <p>
 * The formatted key may be encrypted if the format/provider supports.
 */
public interface SshPrivateKeyFile {

	/**
	 * RSA
	 */
	public final static int TYPE_RSA = 0;

	/**
	 * DSA
	 */
	public final static int TYPE_DSA = 1;

	/**
	 * Unknown
	 */
	public final static int TYPE_UNKNOWN = -1;

	/**
	 * OpenSSH
	 */
	public final static int VENDOR_OPENSSH = 0;

	/**
	 * FSecure
	 */
	public final static int VENDOR_FSECURE = 1;
	/**
	 * Unknown
	 */
	public final static int VENDOR_UNKNOWN = 2;

	/**
	 * SSHTools
	 */
	public final static int VENDOR_SSHTOOLS = 3;

	/**
	 * SSH1. Not truely a vendor, but has its own key format
	 */
	public final static int VENDOR_SSH1 = 4;

	/**
	 * Putty.
	 */
	public final static int VENDOR_PUTTY = 5;
	/**
	 * SSH Communications Security
	 */
	public final static int VENDOR_SSHCOM = 6;

	/**
	 * Format the key so it can be written.
	 * 
	 * @return formatted key
	 * @throws SshException
	 */
	byte[] getFormattedKey() throws SshException;

	/**
	 * Decrypt a formatted key.
	 * 
	 * @param passphrase
	 * @throws SshException
	 * @see {@link #isEncrypted()}
	 */
	void decrypt(char[] passphrase) throws SshException;

	/**
	 * Change the passphrase of this key. The key must be decrypted using {@link #decrypt(char[])}
	 * first if it is encrypted.
	 * 
	 * @param newPassphrase new passphrase
	 * @throws SshException
	 */
	void changePassphrase(char[] newPassphrase) throws SshException;

	/**
	 * Get if this key is currently encrypted.
	 * 
	 * @return encrypted
	 * @throws SshException
	 */
	boolean isEncrypted() throws SshException;

	/**
	 * Get whether changing of the key passphrase is supported.
	 * 
	 * @return supports passphrase change
	 */
	boolean supportsPassphraseChange();

	/**
	 * Get the format of the key. This will be one of the <strong>VENDOR_</strong>
	 * constants from this interface.
	 * 
	 * @return format
	 */
	int getFormat();

	/**
	 * Get the actual key pair. The key pair may have to be decrypted before it
	 * can be used.
	 * 
	 * @return key pair
	 * @throws SshException
	 */
	SshKeyPair toKeyPair() throws SshException;

}
