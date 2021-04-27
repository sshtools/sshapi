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
package com.maverick.ssh.tests.server.mavericksshd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.maverick.events.Event;
import com.maverick.events.EventListener;
import com.maverick.events.EventServiceImplementation;
import com.maverick.nio.Daemon;
import com.maverick.nio.DaemonContext;
import com.maverick.nio.ListeningInterface;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.tests.AbstractServer;
import com.maverick.ssh.tests.ServerCapability;
import com.maverick.ssh.tests.SshTestConfiguration;
import com.maverick.sshd.AuthorizedKeysStoreImpl;
import com.maverick.sshd.Connection;
import com.maverick.sshd.SshContext;
import com.maverick.sshd.events.SSHDEventCodes;
import com.maverick.sshd.scp.ScpCommand;
import com.maverick.sshd.vfs.VirtualFileSystemFactory;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.SshKeyPairGenerator;

public class MaverickSSHServerServiceImpl extends AbstractServer implements
		EventListener {

	private SshTestConfiguration configuration;
	@SuppressWarnings("unused")
	private Properties properties;
	private File homeRoot;
	private TestSSHD sshd;
	private int eventErrors = 0;
	private StringBuffer eventErrorMessages = new StringBuffer();
	
	public List<ServerCapability> init(SshTestConfiguration configuration,
			Properties properties) throws Exception {
		this.configuration = configuration;
		this.properties = properties;
		if (!configuration.getName().endsWith("maverick-server")) {
			throw new Exception(
					"This server is not intended for use with this configuration.");
		}
		EventServiceImplementation.getInstance().addListener(this);
		return Arrays.asList(ServerCapability.CAN_DO_MULTIFACTOR_AUTH);
	}

	protected void doStart() throws Exception {

		eventErrors = 0;
		eventErrorMessages.setLength(0);
		
		sshd = new TestSSHD(configuration.getPort(), Integer.parseInt(System.getProperty("sftp.version", "3")));
		
		// sshd.useThisAuthorizedKeysFile("authorized_keys_folder/authorized_keys");

		// Copy some keys for authentication
		try {
			File root = new File(System.getProperty("java.io.tmpdir"),
					"maverick-sshd-homes");
			FileUtils.deleteDirectory(root);
			homeRoot = new File(root, "home");
			homeRoot.mkdirs();
			// copyKeystore("x509-valid", "testuser", "authorized_keys");
			copyKeystore("dsa-valid", "root", "id_dsa.pub");
			copyKeystore("dsa-valid", "testuser", "id_dsa.pub");
			copyKeystore("dsa-with-passphrase", "testuser", "id_dsa.pub");
			copyKeystore("rsa-valid", "testuser", "id_rsa.pub");
			copyKeystore("rsa-with-passphrase", "testuser", "id_rsa.pub");
			copyKeystore("x509-valid", "testuser", "authorized_keys");
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

		// Start
		sshd.startup();

		// If port 0 was specified, get the actual port
		if (configuration.getPort() == 0) {
			configuration.setPort(sshd.testListeningInterface.getActualPort());
		}
	}

	private void copyKeystore(String key, String user, String name)
			throws IOException {
		String keytext = IOUtils.toString(
				getClass().getResource("/" + key + "/" + name)).trim();
		if (!keytext.endsWith("\n")) {
			keytext += "\n";
		}
		File file = new File(new File(homeRoot, user), ".ssh");
		file.mkdirs();
		File authKeyFile = new File(file, "authorized_keys");
		System.out.println("Writing new authorized key to " + authKeyFile);
		OutputStream out = new FileOutputStream(authKeyFile, true);
		try {
			out.write(keytext.getBytes());
		} finally {
			out.close();
		}
	}

	protected void doStop() throws Exception {
		sshd.shutdownNow(false, 0);
		
		if(eventErrors > 0) {
			System.out.println(eventErrorMessages.toString());
			throw new Exception("There were " + eventErrors + " event related errors");
		}
	}

	class TestSSHD extends Daemon {

		int port = -1;
		int sftpVersion;
		String authorizedKeysFile = null;
		String preferredKeyExchange = null;
		ListeningInterface testListeningInterface;

		public TestSSHD() {
		}

		public TestSSHD(int port, int sftpVersion) {
			this.port = port;
			this.sftpVersion = sftpVersion;
		}

		public void useThisAuthorizedKeysFile(String filepath) {
			this.authorizedKeysFile = filepath;
		}

		public void setPreferredKeyExchange(String preferredKeyExchange) {
			this.preferredKeyExchange = preferredKeyExchange;
		}


		/**
		 * This is where the server is configured; add the servers host keys and
		 * configure the authentication, file system and session providers. You
		 * can optionally define an access manager that will be consulted before
		 * giving a user access to individual SSH features.
		 * 
		 * @param context
		 * @throws IOException
		 */
		protected void configure(DaemonContext context) throws IOException {

			SshContext sshContext = new SshContext(this);
			//sshContext.enableSFTPVersion4();
			// Use one of the following to set a preferred key exchange
			try {
				sshContext
						.setPreferredKeyExchange(SshContext.KEX_DIFFIE_HELLMAN_GROUP1_SHA1);
			} catch (SshException e1) {
				throw new IOException("Could not set preferred key exchange");
			}

			try {
				sshContext.setPreferredCipherCS(SshContext.CIPHER_AES128_CBC);
				sshContext.setPreferredCipherSC(SshContext.CIPHER_AES128_CBC);
			} catch (SshException e1) {
				throw new IOException("Could not set preferred cipher");
			}

			try {
				sshContext.setPreferredMacCS(SshContext.HMAC_MD5);
				sshContext.setPreferredMacSC(SshContext.HMAC_MD5);
			} catch (SshException e1) {
				throw new IOException("Could not set preferred hmac");
			}

			if (authorizedKeysFile != null) {
				sshContext.setPublicKeyStore(new AuthorizedKeysStoreImpl(
						authorizedKeysFile));
				sshContext
						.addRequiredAuthentication(SshContext.PUBLICKEY_AUTHENTICATION);
			}

			sshContext.setChannelLimit(1000);

			// Add an RSA key and preferably a DSA key as well to avoid
			// connection
			// errors with clients that only support one key type (F-Secure 4.3
			// for example)
			try {
				try {

					sshContext.loadOrGenerateHostKey(new File(
							"ssh_host_rsa_key"), SshKeyPairGenerator.SSH2_RSA,
							1024);

					sshContext.loadOrGenerateHostKey(new File(
							"ssh_host_dsa_key"), SshKeyPairGenerator.SSH2_DSA,
							1024);

				} catch (SshException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (InvalidPassphraseException ex) {
			}

			//
			// Set the finger print in the configuration
			//
			List<String> fps = new ArrayList<String>();
			for (SshKeyPair p : sshContext.getHostKeys()) {
				try {
					fps.add(p.getPublicKey()
							.getFingerprint());
				} catch (SshException sshe) {
					throw new SshIOException(sshe);
				}
			}
			configuration.setFingerprints(fps.toArray(new String[0]));

			//
			// Authentication
			//
			sshContext.setAuthenticationProvider(new MaverickSSHAuthenticationProvider(configuration));
			testListeningInterface = context.addListeningInterface(
					InetAddress.getByName("127.0.0.1"), port, sshContext);
			sshContext.setBannerMessage("Maverick Integration Test Server.");

			sshContext.setSessionProvider(MaverickSSHSession.class);
			sshContext.setFileSystemProvider(new VirtualFileSystemFactory());
			sshContext.setRemoteForwardingCancelKillsTunnels(true);
			sshContext.setKeyboardInteractiveProvider(MaverickSSHKeyboardInteractiveProvider.class);
			System.setProperty("com.maverick.sshd.vfs.VFSRoot",
					homeRoot.getAbsolutePath());
			sshContext.setAccessManager(new MaverickSSHAccessManager());
			sshContext.addCommand("scp", ScpCommand.class);
			sshContext.addCommand("commandWithOutput", CommandWithOutput.class);
			sshContext.addCommand("commandWithInput", CommandWithInput.class);
			sshContext.addCommand("basicCommand", BasicCommand.class);
			context.setPermanentTransferThreads(1);
			sshContext.setSoftwareVersionComments("MaverickTests");
			sshContext.setSocketOptionKeepAlive(true);
			sshContext.setSocketOptionTcpNoDelay(true);
			sshContext.setSocketOptionReuseAddress(true);
			sshContext.setAllowDeniedKEX(true);
			sshContext.setIdleConnectionTimeoutSeconds(60);

			if(sftpVersion > 3) {
				sshContext.enableSFTPVersion4();
			}

			for (AuthenticationMethod a : methods) {
				sshContext.addRequiredAuthentication(a.toString());
			}
		}
	}

	public void processEvent(Event evt) {

		if((evt.getId() & SSHDEventCodes.SSHD_EVENT_CLASS) == SSHDEventCodes.SSHD_EVENT_CLASS) {
			if(evt.getAttribute(SSHDEventCodes.ATTRIBUTE_CONNECTION)!=null) {
				if(!(evt.getAttribute(SSHDEventCodes.ATTRIBUTE_CONNECTION) instanceof Connection)) {
					eventErrorMessages.append("Connection object is not correct type in event id " + Integer.toHexString(evt.getId()) + "\r\n");
					eventErrors++;
				}
				Connection con = (Connection) evt.getAttribute(SSHDEventCodes.ATTRIBUTE_CONNECTION);
				checkNotNull("localAddress", con.getLocalAddress(), evt);
				checkNotNull("localPort", con.getLocalPort(), evt);
				checkNotNull("remoteAddress", con.getRemoteAddress(), evt);
				checkNotNull("remotePort", con.getRemotePort(), evt);
				checkNotNull("sessionId", con.getSessionId(), evt);
				checkNotNull("totalBytesIn", con.getTotalBytesIn(), evt);
				checkNotNull("totalBytesOut", con.getTotalBytesOut(), evt);
				if(evt.getId() >= SSHDEventCodes.EVENT_USERAUTH_SUCCESS && evt.getId()!= SSHDEventCodes.EVENT_DISCONNECTED) {
					checkNotNull("username", con.getUsername(), evt);
				}
				checkNotNull("localSocketAddress", con.getLocalSocketAddress().toString(), evt);
				checkNotNull("remoteSocketAddress", con.getRemoteSocketAddress().toString(), evt);
				checkNotNull("startTime", con.getStartTime(), evt);
			}
		}
		
	}
	
	void checkNotNull(String name, Object obj, Event evt) {
		if(obj==null) {
			eventErrorMessages.append("We got a null " + name + " attribute on event " + Integer.toHexString(evt.getId()) + "\r\n");
			eventErrors++;
		}
	}

}
