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
package net.sf.sshapi.impl.maverick16;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshHmac;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.util.Base64;
import com.sshtools.publickey.AbstractKnownHostsKeyVerification;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.hostkeys.AbstractHostKey;
import net.sf.sshapi.hostkeys.AbstractHostKeyManager;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.util.Util;

/**
 * Maverick host key management supports the OpenSSH known_hosts format. This
 * class adapts that to the SSHAPI {@link SshHostKeyManager} interface.
 */
public class MaverickHostKeyManager extends AbstractHostKeyManager {

	private AbstractKnownHostsKeyVerification knownHosts;

	/**
	 * Constructor.
	 * 
	 * @param configuration configuration
	 * @throws SshException
	 */
	public MaverickHostKeyManager(SshConfiguration configuration) throws SshException {
		super(configuration);
		Util.checkKnownHostsFile(configuration);
		load(configuration);
	}

	private void load(SshConfiguration configuration) throws SshException {
		try {
			knownHosts = new AbstractKnownHostsKeyVerification(Util.getKnownHostsFile(configuration).getAbsolutePath()) {

				@Override
				public void onUnknownHost(String host, SshPublicKey key) throws com.maverick.ssh.SshException {
				}

				@Override
				public void onHostKeyMismatch(String host, SshPublicKey allowedHostKey, SshPublicKey actualHostKey)
						throws com.maverick.ssh.SshException {
				}
			};
		} catch (com.maverick.ssh.SshException e) {
			throw new SshException(SshException.GENERAL, e);
		}
	}

	@Override
	public void add(final SshHostKey hostKey, boolean persist) throws SshException {
		try {
			knownHosts.allowHost(hostKey.getHost(), new SshPublicKey() {

				@Override
				public boolean verifySignature(byte[] signature, byte[] data) throws com.maverick.ssh.SshException {
					return false;
				}

				@Override
				public void init(byte[] blob, int start, int len) throws com.maverick.ssh.SshException {
				}

				@Override
				public String getFingerprint() throws com.maverick.ssh.SshException {
					return hostKey.getFingerprint();
				}

				@Override
				public byte[] getEncoded() throws com.maverick.ssh.SshException {
					return hostKey.getKey();
				}

				@Override
				public int getBitLength() {
					return 0;
				}

				@Override
				public String getAlgorithm() {
					return hostKey.getType();
				}

				@Override
				public String getSigningAlgorithm() {
					return hostKey.getType();
				}

				@Override
				public String test() {
					throw new UnsupportedOperationException();
				}

				@Override
				public String getEncodingAlgorithm() {
					throw new UnsupportedOperationException();
				}
			}, persist);
		} catch (com.maverick.ssh.SshException e) {
			throw new SshException(SshException.GENERAL, e);
		}
	}

	@Override
	public SshHostKey[] getKeys() {
		List<SshHostKey> hostKeys = new ArrayList<>();
		Hashtable<String, Hashtable<String, SshPublicKey>> hosts = knownHosts.allowedHosts();
		for (Map.Entry<String, Hashtable<String, SshPublicKey>> en : hosts.entrySet()) {
			for (Map.Entry<String, SshPublicKey> en2 : en.getValue().entrySet()) {
				hostKeys.add(new AbstractHostKey() {
					@Override
					public String getType() {
						return en2.getValue().getAlgorithm();
					}

					@Override
					public byte[] getKey() {
						try {
							return en2.getValue().getEncoded();
						} catch (com.maverick.ssh.SshException e) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public String getHost() {
						return en.getKey();
					}

					@Override
					public String getFingerprint() {
						try {
							return en2.getValue().getFingerprint();
						} catch (com.maverick.ssh.SshException e) {
							throw new RuntimeException(e);
						}
					}
				});
			}
		}
		return hostKeys.toArray(new SshHostKey[0]);
	}

	@Override
	public boolean isWriteable() {
		return knownHosts.isHostFileWriteable();
	}

	@Override
	public void remove(SshHostKey hostKey) throws SshException {
		knownHosts.removeAllowedHost(hostKey.getHost());
		try {
			knownHosts.saveHostFile();
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	protected boolean checkHost(String storedHostName, String hostToCheck) {
		if (storedHostName.startsWith(HASH_MAGIC)) {
			try {
				SshHmac sha1 = (SshHmac) ComponentManager.getInstance().supportedHMacsCS().getInstance("hmac-sha1");
				String hashData = storedHostName.substring(HASH_MAGIC.length());
				String hashSalt = hashData.substring(0, hashData.indexOf(HASH_DELIM));
				String hashStr = hashData.substring(hashData.indexOf(HASH_DELIM) + 1);
				sha1.init(Base64.decode(hashSalt));
				sha1.update(hostToCheck.getBytes());
				byte[] ourHash = sha1.doFinal();
				byte[] storedHash = Base64.decode(hashStr);
				return Arrays.equals(storedHash, ourHash);
			} catch (com.maverick.ssh.SshException e) {
				throw new RuntimeException(e);
			}
		} else {
			return super.checkHost(storedHostName, hostToCheck);
		}
	}
}
