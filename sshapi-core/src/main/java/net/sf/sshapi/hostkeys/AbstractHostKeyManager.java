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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import net.sf.sshapi.SshConfiguration;

/**
 * Abstract implementation of an {@link SshHostKeyManager} that provides some
 * default common methods.
 */
public abstract class AbstractHostKeyManager implements SshHostKeyManager {

	protected static final String HASH_MAGIC = "|1|";
	protected static final String HASH_DELIM = "|";
	
	private final SshConfiguration configuration;

	/**
	 * Constructor.
	 * 
	 * @param configuration configuration
	 */
	public AbstractHostKeyManager(SshConfiguration configuration) {
		this.configuration = configuration;
	}

	protected SshConfiguration getConfiguration() {
		return configuration;
	}

	public final SshHostKey[] getKeysForHost(String host, String type) {
		// Look up first using the plain text hostname
		SshHostKey[] hk = doGetKeysForHost(host, type);
		if (hk == null) {
			// Now try a reverse look up
			if ("true".equals(configuration.getProperties().getProperty(SshConfiguration.CFG_KNOWN_HOSTS_REVERSE_DNS, "true"))) {
				try {
					InetAddress addr = InetAddress.getByName(host);
					hk = doGetKeysForHost(addr.getHostName(), type);
					if (hk == null) {
						hk = doGetKeysForHost(addr.getCanonicalHostName(), type);
					}
					if (hk == null) {
						hk = doGetKeysForHost(addr.getHostAddress(), type);
					}
				} catch (UnknownHostException uhe) {
				}
			}
		}
		return hk == null ? new SshHostKey[0] : hk;
	}

	/**
	 * Default simple implementation. Just calls {@link #getKeys()} and matches
	 * the type and hostname. Providers may override if they have a more
	 * efficient method.
	 * 
	 * @param host hostname
	 * @param type type
	 * @return host keys
	 */
	protected SshHostKey[] doGetKeysForHost(String host, String type) {
		SshHostKey[] keys = getKeys();
		List<SshHostKey> hostKeys = new ArrayList<>();
		for (SshHostKey k : keys) {
			if (checkHost(k.getHost(), host) && ( type == null || type.equals(k.getType()))) {
				hostKeys.add(k);
			}
		}
		return hostKeys.toArray(new SshHostKey[0]);
	}

	protected boolean checkHost(String storedHostName, String hostToCheck) {
		boolean match = storedHostName.equals(hostToCheck);
		if(!match) {
			System.out.println("STORED: " + storedHostName + " CHECK: " +hostToCheck);
		}
		return match;
	}
}
