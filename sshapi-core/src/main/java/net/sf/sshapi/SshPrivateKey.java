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
package net.sf.sshapi;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a private key.
 */
public interface SshPrivateKey {
	
	/**
	 * Algorithm
	 */
	public enum Algorithm {
		RSA1,
		SSH_DSS,
		SSH_RSA,
		ECDSA,
		ED25519,
		X509V3_SIGN_RSA_SHA1,
		ERROR,
		UNKNOWN;
		
		public static Algorithm fromAlgoName(String algoName) {
			return Algorithm.valueOf(algoName.toUpperCase().replace("-", "_"));
		}
		
		public String toAlgoName() {
			if(this == ERROR || this == Algorithm.UNKNOWN)
				throw new IllegalArgumentException("Not actually a valid algorithm");
			return name().toLowerCase().replace("_", "-");
		}
		
		public String toKeyType(int bits) {
			switch(this) {
			case RSA1:
				return SshConfiguration.PUBLIC_KEY_SSHRSA1;
			case SSH_DSS:
				return SshConfiguration.PUBLIC_KEY_SSHDSA;
			case SSH_RSA:
				return SshConfiguration.PUBLIC_KEY_SSHRSA;
			case X509V3_SIGN_RSA_SHA1:
				return SshConfiguration.PUBLIC_KEY_X509V3_RSA_SHA1;
			case ECDSA:
				switch(bits) {
				case 256:
					return SshConfiguration.PUBLIC_KEY_ECDSA_256;	
				case 384:
					return SshConfiguration.PUBLIC_KEY_ECDSA_384;	
				case 521:
					return SshConfiguration.PUBLIC_KEY_ECDSA_521;
				default:
					throw new UnsupportedOperationException(String.format("%d is an unsupported bit length.", bits));
				}
			case ED25519:
				return SshConfiguration.PUBLIC_KEY_ED25519;
			default:
				throw new UnsupportedOperationException(String.format("%s is an unsupported key type.", this));
				
			}
		}

		public static Algorithm[] algos() {
			List<Algorithm> l = new ArrayList<>();
			for(Algorithm a : values()) {
				if(a != Algorithm.ERROR && a != UNKNOWN)
					l.add(a);
			}
			return l.toArray(new Algorithm[0]);
		}
	}

	/**
	 * Get the private key algorithm used. 
	 * 
	 * @return algorithm
	 */
	Algorithm getAlgorithm();

	/**
	 * Sign the data using this private key.
	 * 
	 * @param data
	 * @return signed data
	 * @throws SshException
	 */
	byte[] sign(byte[] data) throws SshException;
}
