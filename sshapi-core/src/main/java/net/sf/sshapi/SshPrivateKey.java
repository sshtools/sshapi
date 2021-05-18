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
package net.sf.sshapi;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a private key.
 */
public interface SshPrivateKey {
	
	/**
	 * Algorithm.
	 */
	public enum Algorithm {
		
		/** rsa1. */
		RSA1,
		
		/** ssh dss. */
		SSH_DSS,
		
		/** ssh rsa. */
		SSH_RSA,
		
		/** ecdsa. */
		ECDSA,
		
		/** ed25519. */
		ED25519,
		
		/** x509v3 sign rsa sha1. */
		X509V3_SIGN_RSA_SHA1,
		
		/** error. */
		ERROR,
		
		/** unknown. */
		UNKNOWN;
		
		/**
		 * From algo name.
		 *
		 * @param algoName the algo name
		 * @return the algorithm
		 */
		public static Algorithm fromAlgoName(String algoName) {
			return Algorithm.valueOf(algoName.toUpperCase().replace("-", "_"));
		}
		
		/**
		 * To algo name.
		 *
		 * @return the string
		 */
		public String toAlgoName() {
			if(this == ERROR || this == Algorithm.UNKNOWN)
				throw new IllegalArgumentException("Not actually a valid algorithm");
			return name().toLowerCase().replace("_", "-");
		}
		
		/**
		 * To key type.
		 *
		 * @param bits the bits
		 * @return the string
		 */
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

		/**
		 * Algos.
		 *
		 * @return the algorithm[]
		 */
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
	 * @param data the data
	 * @return signed data
	 * @throws SshException the ssh exception
	 */
	byte[] sign(byte[] data) throws SshException;
}
