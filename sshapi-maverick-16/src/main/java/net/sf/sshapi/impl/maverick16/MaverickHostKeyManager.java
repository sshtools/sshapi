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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshHmac;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.util.Base64;
import com.sshtools.publickey.KnownHostsKeyVerification;
import com.sshtools.publickey.KnownHostsKeyVerification.KeyEntry;

import net.sf.sshapi.Logger.Level;
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

	private KnownHostsKeyVerification knownHosts;
	private File file;
	private long lastModified;

	/**
	 * Constructor.
	 * 
	 * @param configuration configuration
	 * @throws SshException
	 */
	public MaverickHostKeyManager(SshConfiguration configuration) throws SshException {
		super(configuration);
		Util.checkKnownHostsFile(configuration);
		if (Util.getKnownHostsFile(configuration).exists())
			load(configuration);
		else
			knownHosts = new KnownHostsKeyVerification();
	}

	private void load(SshConfiguration configuration) throws SshException {
		try {
			file = Util.getKnownHostsFile(configuration);
			lastModified = file.lastModified();
			FileInputStream fin = new FileInputStream(file);
			try {
				knownHosts = new KnownHostsKeyVerification(fin);
			} finally {
				fin.close();
			}
		} catch (com.maverick.ssh.SshException e) {
			throw new SshException(SshException.GENERAL, e);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public void add(final SshHostKey hostKey, boolean persist) throws SshException {
		try {
			checkForChanges();
			knownHosts.addEntry(new SshPublicKey() {

				@Override
				public boolean verifySignature(byte[] signature, byte[] data)
						throws com.maverick.ssh.SshException {
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
			}, hostKey.getComments(), hostKey.getHost());
			if (persist) {
				try {
					saveHostFile();
				} catch (IOException ioe) {
					throw new SshException(SshException.IO_ERROR,
							String.format("Failed to save known hosts file %s", ioe));
				}
			}
		} catch (com.maverick.ssh.SshException e) {
			throw new SshException(SshException.GENERAL, e);
		}
	}

	@Override
	public SshHostKey[] getKeys() {
		checkForChanges();
		List<SshHostKey> hostKeys = new ArrayList<>();
		Set<KeyEntry> hosts = knownHosts.getKeyEntries();
		for (KeyEntry ke : hosts) {
			hostKeys.add(new AbstractHostKey() {
				@Override
				public String getType() {
					return ke.getKey().getAlgorithm();
				}

				@Override
				public byte[] getKey() {
					try {
						return ke.getKey().getEncoded();
					} catch (com.maverick.ssh.SshException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public String getHost() {
					return ke.getNames();
				}

				@Override
				public String getFingerprint() {
					try {
						return ke.getKey().getFingerprint();
					} catch (com.maverick.ssh.SshException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public String getComments() {
					return ke.getComment();
				}
			});
		}
		return hostKeys.toArray(new SshHostKey[0]);
	}

	@Override
	public boolean isWriteable() {
		return file == null || !file.exists() || file.canWrite();
	}

	@Override
	public void remove(SshHostKey hostKey) throws SshException {
		try {
			checkForChanges();
			knownHosts.removeEntries(hostKey.getHost());
			saveHostFile();
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		} catch (com.maverick.ssh.SshException e) {
			throw new SshException(SshException.GENERAL, e);
		}
	}

	@Override
	protected boolean checkHost(String storedHostName, String hostToCheck) {
		checkForChanges();
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

	protected void checkForChanges() {
		try {
			file = Util.getKnownHostsFile(getConfiguration());
			if (file.lastModified() != lastModified) {
				load(getConfiguration());
			}
		} catch (Exception e) {
			SshConfiguration.getLogger().log(Level.ERROR,
					"Failed to reload trusted host key store after an external modification.");
		}
	}

	protected void saveHostFile() throws IOException {
		file = Util.getKnownHostsFile(getConfiguration());
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(knownHosts.toString().getBytes());

		} finally {
			lastModified = file.lastModified();
		}
	}
}
