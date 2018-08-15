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
package net.sf.sshapi.impl.trilead;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.trilead.ssh2.KnownHosts;
import com.trilead.ssh2.signature.DSAPublicKey;
import com.trilead.ssh2.signature.DSASHA1Verify;
import com.trilead.ssh2.signature.RSAPublicKey;
import com.trilead.ssh2.signature.RSASHA1Verify;

import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.hostkeys.AbstractHostKey;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.util.Util;

/**
 * Trilead host key management supports the OpenSSH known_hosts format. This
 * class adapts that to the SSHAPI {@link SshHostKeyManager} interface.
 */
class TriliadHostKeyManager implements SshHostKeyManager {

	private KnownHosts knownHosts;
	private File knownHostsFile;
	private SshConfiguration configuration;

	public TriliadHostKeyManager(SshConfiguration configuration) throws SshException {
		this.configuration = configuration;
		reload();
	}

	public void add(SshHostKey hostKey, boolean persist) throws SshException {
		try {
			KnownHosts.addHostkeyToFile(knownHostsFile, new String[] { hostKey.getHost() }, hostKey.getType(), hostKey.getKey());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
		reload();
	}

	public SshHostKey[] getKeys() {
		List keys = new ArrayList();
		// Ewwwww :(
		try {
			Field field = knownHosts.getClass().getDeclaredField("publicKeys");
			field.setAccessible(true);
			LinkedList publickeys = (LinkedList) field.get(knownHosts);
			addKeys(keys, publickeys);
		} catch (Exception e) {
			SshConfiguration.getLogger().log(Level.ERROR, "Failed to get host keys.", e);
		}

		return (SshHostKey[]) keys.toArray(new SshHostKey[0]);
	}

	private void addKeys(List keys, Collection publickeys) throws NoSuchFieldException, IllegalAccessException, IOException {
		Field field;
		for (Iterator e = publickeys.iterator(); e.hasNext();) {
			Object knownHostEntry = e.next();
			field = knownHostEntry.getClass().getDeclaredField("key");
			field.setAccessible(true);
			Object key = field.get(knownHostEntry);
			field = knownHostEntry.getClass().getDeclaredField("patterns");
			field.setAccessible(true);
			String[] patterns = (String[]) field.get(knownHostEntry);
			String type = null;
			byte[] keyBytes = null;
			if (key instanceof RSAPublicKey) {
				type = SshConfiguration.PUBLIC_KEY_SSHRSA;
				keyBytes = RSASHA1Verify.encodeSSHRSAPublicKey((RSAPublicKey) key);
			} else if (key instanceof DSAPublicKey) {
				type = SshConfiguration.PUBLIC_KEY_SSHDSA;
				keyBytes = DSASHA1Verify.encodeSSHDSAPublicKey((DSAPublicKey) key);
			} else {
				SshConfiguration.getLogger().log(Level.WARN, "Unsupported key format " + key);
				continue;
			}
			keys.add(new GanymedHostKey(type, keyBytes, patterns));
		}
	}

	public SshHostKey[] getKeysForHost(String host, String type) {
		SshHostKey[] keys = getKeys();
		List hostKeys = new ArrayList();
		try {
			Method m = knownHosts.getClass().getDeclaredMethod("hostnameMatches", new Class[] { String[].class, String.class });
			m.setAccessible(true);
			for (int i = 0; i < keys.length; i++) {
				if (type.equals(keys[i].getType())
					&& ((Boolean) m.invoke(knownHosts, new Object[] { ((GanymedHostKey) keys[i]).hosts, host })).booleanValue()) {
					hostKeys.add(keys[i]);
				}
			}
		} catch (Exception e) {
			SshConfiguration.getLogger().log(Level.ERROR, "Error locating host keys.", e);
		}
		return (SshHostKey[]) hostKeys.toArray(new SshHostKey[0]);
	}

	public boolean isWriteable() {
		return knownHostsFile.canWrite();
	}

	public void remove(SshHostKey hostKey) {
		throw new UnsupportedOperationException();
	}

	private void reload() throws SshException {
		try {
			knownHostsFile = Util.getKnownHostsFile(configuration);
			knownHosts = new KnownHosts(knownHostsFile);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	private final class GanymedHostKey extends AbstractHostKey {
		private final String type;
		private byte[] key;
		private String[] hosts;

		private GanymedHostKey(String fType, byte[] key, String[] hosts) {
			this.type = fType;
			this.key = key;
			this.hosts = hosts;
		}

		public String getType() {
			return type;
		}

		public byte[] getKey() {
			return key;
		}

		public String getHost() {
			return hosts[0];
		}

		public String getFingerprint() {
			return KnownHosts.createHexFingerprint(getType(), getKey());
		}
	}

}
