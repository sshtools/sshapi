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
package net.sf.sshapi.impl.j2ssh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.hostkeys.AbstractHostKey;
import net.sf.sshapi.hostkeys.AbstractHostKeyManager;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.util.Util;

import com.sshtools.j2ssh.transport.AbstractKnownHostsKeyVerification;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

/**
 * JSch host key management supports the OpenSSH known_hosts format. This class
 * adapts that to the SSHAPI {@link SshHostKeyManager} interface.
 */
class J2SshHostKeyManager extends AbstractHostKeyManager {

	private AbstractKnownHostsKeyVerification knownHosts;

	public J2SshHostKeyManager(SshConfiguration configuration) throws SshException {
		super(configuration);
		try {
			knownHosts = new AbstractKnownHostsKeyVerification(Util.getKnownHostsFile(configuration).getAbsolutePath()) {
				public void onHostKeyMismatch(String arg0, SshPublicKey arg1, SshPublicKey arg2) throws TransportProtocolException {
				}

				public void onUnknownHost(String arg0, SshPublicKey arg1) throws TransportProtocolException {
				}
			};
		} catch (InvalidHostFileException e) {
			throw new SshException(SshException.GENERAL, e);
		}
	}

	public void add(final SshHostKey hostKey, boolean persist) throws SshException {
		try {
			knownHosts.allowHost(hostKey.getHost(), new SshPublicKey() {

				public boolean verifySignature(byte[] signature, byte[] data) {
					return false;
				}

				public String getFingerprint() {
					return hostKey.getFingerprint();
				}

				public byte[] getEncoded() {
					return hostKey.getKey();
				}

				public int getBitLength() {
					return 0;
				}

				public String getAlgorithmName() {
					return hostKey.getType();
				}
			}, persist);
		} catch (InvalidHostFileException e) {
			throw new SshException(SshException.GENERAL, e);
		}
	}

	public SshHostKey[] getKeys() {
		List<SshHostKey> hostKeys = new ArrayList<>();
		Map<?,?> hosts = knownHosts.allowedHosts();
		for (Iterator<?> e = hosts.keySet().iterator(); e.hasNext();) {
			final String host = (String) e.next();
			HashMap<?,?> allowed = (HashMap<?,?>) hosts.get(host);
			for (Iterator<?> e2 = allowed.keySet().iterator(); e2.hasNext();) {
				final String algo = (String) e2.next();
				final SshPublicKey key = (SshPublicKey) allowed.get(algo);
				hostKeys.add(new AbstractHostKey() {
					
					public String getType() {
						return key.getAlgorithmName();
					}

					public byte[] getKey() {
						return key.getEncoded();
					}

					public String getHost() {
						return host;
					}

					public String getFingerprint() {
						return key.getFingerprint();
					}
				});
			}
		}
		return (SshHostKey[]) hostKeys.toArray(new SshHostKey[0]);
	}

	public boolean isWriteable() {
		return knownHosts.isHostFileWriteable();
	}

	public void remove(SshHostKey hostKey) {
		// TODO maverick doesn't allow more specific removal of host keys
		knownHosts.removeAllowedHost(hostKey.getHost());
	}
}
