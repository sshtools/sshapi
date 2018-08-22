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
package net.sf.sshapi.impl.maverickng;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sshtools.common.nio.SshEngine;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.ComponentManager;
import com.sshtools.common.ssh.components.jce.JCEComponentManager;
import com.sshtools.common.ssh.components.jce.JCEProvider;

import net.sf.sshapi.AbstractProvider;
import net.sf.sshapi.Capability;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.identity.SshIdentityManager;

/**
 * Provider implementation for Maverick SSH.
 */
public class MaverickNGSshProvider extends AbstractProvider {

	private final static Capability[] DEFAULT_CAPS = new Capability[] { Capability.PER_CONNECTION_CONFIGURATION,
			Capability.SSH2, Capability.PASSWORD_AUTHENTICATION, Capability.PUBLIC_KEY_AUTHENTICATION,
			Capability.KEYBOARD_INTERACTIVE_AUTHENTICATION, Capability.SFTP, Capability.PUBLIC_KEY_SUBSYSTEM,
			Capability.WINDOW_CHANGE, Capability.FILE_TRANSFER_EVENTS, Capability.DATA_TIMEOUTS,
			Capability.HOST_KEY_VERIFICATION };

	private SshEngine engine;

	private JCEComponentManager componentManager;

	static {
		// Warning for slow startup on Linux / Solaris
		if ((System.getProperty("os.name").toLowerCase().indexOf("linux") != -1
				|| System.getProperty("os.name").toLowerCase().indexOf("solaris") != -1)
				&& System.getProperty("java.security.egd") == null) {
			SshConfiguration.getLogger().log(Level.WARN,
					"If you experience slow startup of the Maverick API on Linux or Solaris, try setting the system property java.security.egd=file:/dev/urandom");
		}

	}

	private synchronized void checkEngine() {
		if (engine == null) {
			engine = new SshEngine();
			try {
				engine.startup();
			} catch (IOException e) {
				throw new IllegalStateException("I/O error starting Maverick NG SSH Engine");
			}
		}
	}

	/**
	 * Constructor
	 */
	public MaverickNGSshProvider() {
		super("Maverick NG");
		componentManager = ComponentManager.getDefaultInstance();
	}

	public SshEngine getEngine() {
		return engine;
	}

	public SshIdentityManager createIdentityManager(SshConfiguration configurshation) {
		return new MaverickNGIdentityManager();
	}

	public SshClient doCreateClient(SshConfiguration configuration) {
		checkEngine();
		try {
			return new MaverickNGSshClient(engine, configuration);
		} catch (SshException e) {
			throw new IllegalArgumentException("Could not create client for configuration.", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not create client for configuration.", e);
		}
	}

	public void doSupportsConfiguration(SshConfiguration configuration) {
		try {
			Class.forName("com.sshtools.client.SshClient", false, getClass().getClassLoader());
		} catch (ClassNotFoundException cnfe) {
			throw new UnsupportedOperationException("Maverick is not on the CLASSPATH");
		}
	}

	public List<Capability> getCapabilities() {
		return Arrays.asList(DEFAULT_CAPS);
	}

	public List<String> getSupportedCiphers(int protocolVersion) {
		checkEngine();
		return Arrays.asList(componentManager.supportedSsh2CiphersCS().list("").split(","));
	}

	public List<String> getSupportedCompression() {
		// throw new UnsupportedOperationException();
		return Collections.emptyList();
	}

	public List<String> getSupportedMAC() {
		checkEngine();
		return Arrays.asList(componentManager.supportedHMacsCS().list("").split(","));
	}

	public List<String> getSupportedKeyExchange() {
		return Collections.emptyList();
	}

	public List<String> getSupportedPublicKey() {
		checkEngine();
		List<String> pks = new ArrayList<>();
		pks.addAll(Arrays.asList(componentManager.supportedPublicKeys().list("").split(",")));
		return pks;
	}

	public void seed(long seed) {
		SecureRandom rnd;
		try {
			rnd = JCEProvider.getSecureRandom();
			rnd.setSeed(seed);
		} catch (NoSuchAlgorithmException e) {
			SshConfiguration.getLogger().log(Level.ERROR, "Failed to set seed.", e);
		}
	}

}
