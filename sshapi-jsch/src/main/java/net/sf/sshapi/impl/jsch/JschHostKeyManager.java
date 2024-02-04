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
package net.sf.sshapi.impl.jsch;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.hostkeys.AbstractHostKey;
import net.sf.sshapi.hostkeys.AbstractHostKeyManager;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.hostkeys.SshManagedHostKey;
import net.sf.sshapi.util.Util;

/**
 * JSch host key management supports the OpenSSH known_hosts format. This class
 * adapts that to the SSHAPI {@link SshHostKeyManager} interface.
 */
class JschHostKeyManager extends AbstractHostKeyManager {

	private HostKeyRepository hkr;
	private JSch jsch;
	private List<SshManagedHostKey> temporaryKeys = new ArrayList<>();
	private File file;

	public JschHostKeyManager(SshConfiguration configuration) throws SshException {
		super(configuration);

		jsch = new JSch();

		// 
		file = Util.getKnownHostsFile(configuration);
		if (file.exists()) {
			try {
				jsch.setKnownHosts(file.getAbsolutePath());
			} catch (JSchException e) {
				throw new SshException(SshException.IO_ERROR, e);
			}
		}

		hkr = jsch.getHostKeyRepository();
	}

	@Override
	public void add(SshManagedHostKey hostKey, boolean persist) throws SshException {
		if (!persist) {
			temporaryKeys.add(hostKey);
		} else {
			try {
				hkr.add(new HostKey(hostKey.getHost(),
					hostKey.getType().equals(SshConfiguration.PUBLIC_KEY_SSHDSA) ? HostKey.SSHDSS : HostKey.SSHRSA, hostKey
						.getKey(), hostKey.getComments()), new UserInfo() {

					@Override
					public void showMessage(String message) {
					}

					@Override
					public boolean promptYesNo(String message) {
						return true;
					}

					@Override
					public boolean promptPassword(String message) {
						return false;
					}

					@Override
					public boolean promptPassphrase(String message) {
						return false;
					}

					@Override
					public String getPassword() {
						return null;
					}

					@Override
					public String getPassphrase() {
						return null;
					}
				});
			} catch (JSchException e) {
				throw new SshException(e);
			}
		}
	}

	@Override
	public boolean isWriteable() {
		return file.canWrite();
	}

	@Override
	public void remove(SshManagedHostKey hostKey) {
		hkr.remove(hostKey.getHost(), hostKey.getType());
	}

	@Override
	public SshManagedHostKey[] getKeys() {
		List<SshManagedHostKey> hostKeys = new ArrayList<>();
		HostKey[] keys = hkr.getHostKey();
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				hostKeys.add(new JschHostKey(keys[i]));
			}
		}
		return hostKeys.toArray(new SshManagedHostKey[0]);
	}

	@Override
	protected SshManagedHostKey[] doGetKeysForHost(String host, String type) {
		List<SshHostKey> keys = new ArrayList<>();

		// Stored
		HostKey[] hk = hkr.getHostKey(host, type);
		if (hk != null) {
			for (int i = 0; i < hk.length; i++) {
				keys.add(new JschHostKey(hk[i]));
			}
		}

		// Try temporary keys
		for (SshHostKey k : temporaryKeys) {
			if (k.getHost().equals(host) && k.getType().equals(type)) {
				keys.add(k);
			}
		}

		return keys.size() == 0 ? null : keys.toArray(new SshManagedHostKey[0]);
	}

	class JschHostKey extends AbstractHostKey implements SshManagedHostKey {
		private HostKey key;

		public JschHostKey(HostKey key) {
			this.key = key;
		}

		@Override
		public String getHost() {
			return key.getHost();
		}

		@Override
		public String getType() {
			return key.getType();
		}

		@Override
		public String getFingerprint() {
			return key.getFingerPrint(jsch);
		}

		@Override
		public byte[] getKey() {
			try {
				Method m = Class.forName("com.jcraft.jsch.Util").getDeclaredMethod("fromBase64",
					new Class[] { byte[].class, int.class, int.class });
				m.setAccessible(true);
				return (byte[]) m.invoke(null, new Object[] { key.getKey().getBytes(), Integer.valueOf(0),
						Integer.valueOf(key.getKey().length()) });
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getComments() {
			return key.getComment();
		}
	}
}
