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

import net.sf.sshapi.SshPrivateKey;
import net.sf.sshapi.SshPublicKey;

/**
 * Composite of an {@link SshPrivateKey} and an {@link SshPublicKey}. Key pairs
 * are used by {link SshIdentityManager}.
 */
public class SshKeyPair {

	/**
	 * SSH1 RSA
	 */
	public static final String SSH1_RSA = "rsa1";
	/**
	 * SSH2 RSA
	 */
	public static final String SSH2_RSA = "ssh-rsa";
	/**
	 * SSH2 DSA
	 */
	public static final String SSH2_DSA = "ssh-dss";

	private SshPublicKey publicKey;
	private SshPrivateKey privateKey;

	/**
	 * Constructor.
	 * 
	 * @param publicKey public key
	 * @param privateKey private key
	 */
	public SshKeyPair(SshPublicKey publicKey, SshPrivateKey privateKey) {
		super();
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	/**
	 * Get the public key portion of this key pair.
	 * 
	 * @return public key
	 */
	public SshPublicKey getPublicKey() {
		return publicKey;
	}

	/**
	 * Set the public key portion of this key pair.
	 * 
	 * @param publicKey public key
	 */
	public void setPublicKey(SshPublicKey publicKey) {
		this.publicKey = publicKey;
	}

	/**
	 * Get the private key portion of this key pair.
	 * 
	 * @return private key
	 */
	public SshPrivateKey getPrivateKey() {
		return privateKey;
	}

	/**
	 * Set the private key portion of this key pair.
	 * 
	 * @param privateKey private key
	 */
	public void setPrivateKey(SshPrivateKey privateKey) {
		this.privateKey = privateKey;
	}
}
