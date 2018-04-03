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
package net.sf.sshapi.hostkeys;

import net.sf.sshapi.SshConfiguration;

/**
 * Represent an authorized host key.
 * 
 * @see SshHostKeyValidator
 */
public interface SshHostKey {

	/**
	 * Get the hostnames of the key. Note, each key may be hashed.
	 * 
	 * @return hosts
	 */
	String getHost();

	/**
	 * Get the key type. Will be one of
	 * {@link SshConfiguration#PUBLIC_KEY_SSHDSS} or
	 * {@link SshConfiguration#PUBLIC_KEY_SSHRSA}.
	 * 
	 * @return type
	 */
	String getType();

	/**
	 * Get the fingerprint as a hex string.
	 * 
	 * @return fingerprint
	 */
	String getFingerprint();

	/**
	 * Get the key bytes
	 * 
	 * @return key bytes
	 */
	byte[] getKey();
}
