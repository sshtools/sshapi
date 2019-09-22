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

import java.io.IOException;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPublicKey;

/**
 * Represent a persisted {@link SshPublicKey}. Provider implementations are
 * capable of reading some known public key format, and presenting as an
 * instance of this interface.
 */
public interface SshPublicKeyFile {
	/**
	 * OpenSSH
	 */
	public static final int OPENSSH_FORMAT = 0;

	/**
	 * SECSH
	 */
	public static final int SECSH_FORMAT = 1;

	/**
	 * SSH1
	 */
	public static final int SSH1_FORMAT = 2;

	/**
	 * SSHTools
	 */
	public static final int SSHTOOLS_FORMAT = 3;

	/**
	 * Get the format of this key. Will be one of the <strong>_FORMAT</strong>
	 * constants that are part of this interface.
	 * 
	 * @return format
	 */
	int getFormat();

	/**
	 * Get the actual public key.
	 * 
	 * @return public key
	 * @throws SshException
	 */
	SshPublicKey getPublicKey() throws SshException;

	/**
	 * Get any comment stored with this key (if supported by provider / format).
	 * 
	 * @return comment
	 */
	String getComment();

	/**
	 * Format the key for storage.
	 * 
	 * @return formatted key
	 * @throws IOException
	 */
	byte[] getFormattedKey() throws IOException;

	/**
	 * Get any additional options stored with this key (if supported by provider
	 * / format)
	 * 
	 * @return additional options
	 */
	String getOptions();

}
