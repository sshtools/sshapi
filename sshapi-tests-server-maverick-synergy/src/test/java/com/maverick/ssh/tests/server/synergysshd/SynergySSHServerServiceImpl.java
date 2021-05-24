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
package com.maverick.ssh.tests.server.synergysshd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.maverick.ssh.tests.AbstractServer;
import com.maverick.ssh.tests.ServerCapability;
import com.maverick.ssh.tests.SshTestConfiguration;
import com.sshtools.common.auth.AuthorizedKeysPublicKeyAuthenticationProvider;
import com.sshtools.common.auth.DefaultAuthenticationMechanismFactory;
import com.sshtools.common.files.direct.DirectFileFactory;
import com.sshtools.common.files.vfs.VirtualFileFactory;
import com.sshtools.common.files.vfs.VirtualMountTemplate;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.scp.ScpCommand;
import com.sshtools.server.DefaultServerChannelFactory;
import com.sshtools.server.InMemoryPasswordAuthenticator;
import com.sshtools.server.SshServer;
import com.sshtools.server.SshServerContext;
import com.sshtools.synergy.nio.SshEngineContext;

public class SynergySSHServerServiceImpl extends AbstractServer {
	private SshTestConfiguration configuration;
	@SuppressWarnings("unused")
	private Properties properties;
	private File homeRoot;
	private SshServer sshd;
	private int eventErrors = 0;
	private StringBuffer eventErrorMessages = new StringBuffer();

	public List<ServerCapability> init(SshTestConfiguration configuration, Properties properties) throws Exception {
		this.configuration = configuration;
		this.properties = properties;
		if (!configuration.getName().endsWith("synergy-server")) {
			throw new Exception("This server is not intended for use with this configuration.");
		}
		return Arrays.asList(ServerCapability.CAN_DO_MULTIFACTOR_AUTH, ServerCapability.SUPPORTS_PERMISSIONS, ServerCapability.SYMLINKS);
	}

	protected void doStart() throws Exception {
		eventErrors = 0;
		eventErrorMessages.setLength(0);
		sshd = new SshServer(configuration.getPort()) {

			@Override
			public SshServerContext createContext(SshEngineContext daemonContext, SocketChannel sc)
					throws IOException, com.sshtools.common.ssh.SshException {
				SshServerContext sshContext = super.createContext(daemonContext, sc);
				// sshContext.enableSFTPVersion4();
				// Use one of the following to set a preferred key exchange
				
//				sshContext.setPreferredKeyExchange(SshContext.KEX_DIFFIE_HELLMAN_GROUP1_SHA1);
//				sshContext.setPreferredCipherCS(SshContext.CIPHER_AES128_CBC);
//				sshContext.setPreferredCipherSC(SshContext.CIPHER_AES128_CBC);
//				sshContext.setPreferredMacCS(SshContext.HMAC_MD5);
//				sshContext.setPreferredMacSC(SshContext.HMAC_MD5);
				
				sshContext.setChannelLimit(1000);
				sshContext.setRemoteForwardingCancelKillsTunnels(true);
				sshContext.setSoftwareVersionComments("MaverickTests");
				sshContext.setSocketOptionKeepAlive(true);
				sshContext.setSocketOptionTcpNoDelay(true);
				sshContext.setSocketOptionReuseAddress(true);
				// TODO no idea
//				sshContext.setAllowDeniedKEX(true); 
				sshContext.setIdleConnectionTimeoutSeconds(60);
				
				DefaultServerChannelFactory channelFactory = new DefaultServerChannelFactory() {
					{
						/* These are not using the shell */
						commands.add("scp", ScpCommand.class);
						commands.add("commandWithOutput", CommandWithOutput.class);
						commands.add("commandWithInput", CommandWithInput.class);
						commands.add("basicCommand", BasicCommand.class);
					}
				};
				
				sshContext.setChannelFactory(channelFactory);

				DefaultAuthenticationMechanismFactory<SshServerContext> authFactory = new DefaultAuthenticationMechanismFactory<>();
				authFactory.addProvider(new InMemoryPasswordAuthenticator().addUser("root", configuration.getPassword())
						.addUser("testuser", configuration.getPassword()).addUser("testuser2", configuration.getPassword()));
				authFactory.addProvider(new SynergyKeyboardInteractiveProvider().addUser("root", configuration.getPassword(), false)
						.addUser("testuser", configuration.getPassword(), false).addUser("testuser2", configuration.getPassword(), false));
				authFactory.addProvider(new AuthorizedKeysPublicKeyAuthenticationProvider());
				sshContext.setAuthenicationMechanismFactory(authFactory);
								
				for (AuthenticationMethod a : methods) {
					authFactory.addRequiredAuthentication(a.toString());
				}


				
//				//
//				// Set the finger print in the configuration
//				//
//				for (SshKeyPair p : sshContext.getHostKeys()) {
//					try {
//						configuration.setFingerprint(p.getPublicKey().getFingerprint());
//					} catch (SshException sshe) {
//						throw new SshIOException(sshe);
//					}
//				}
//				//
//				// Authentication
//				//
//				sshContext.setBannerMessage("Maverick Integration Test Server.");
//				sshContext.setSessionProvider(MaverickSSHSession.class);

				return sshContext;
			}
			
		};
		homeRoot = new File(new File(System.getProperty("java.io.tmpdir"), "synergy-sshd-homes"), "home");
		sshd.setFileFactory((con) -> {
			try {
				File home = new File(homeRoot, con.getUsername());
				return new VirtualFileFactory(new VirtualMountTemplate("/", home.getAbsolutePath(), new DirectFileFactory(home), true));
			} catch (IOException | PermissionDeniedException e) {
				if(e instanceof IOException)
					throw (IOException)e;
				else
					throw new IOException("Failed to create virtual file factory.", e);
			}
		});
		sshd.addHostKey(SshKeyUtils.getPrivateKey(new File("ssh_host_rsa_key"), null));
		sshd.addHostKey(SshKeyUtils.getPrivateKey(new File("ssh_host_dsa_key"), null));
		// sshd.useThisAuthorizedKeysFile("authorized_keys_folder/authorized_keys");
		// Copy some keys for authentication
		try {
			File root = new File(System.getProperty("java.io.tmpdir"), "synergy-sshd-homes");
			FileUtils.deleteDirectory(root);
			homeRoot = new File(root, "home");
			homeRoot.mkdirs();
			copyKeystore("dsa-valid", "root", "id_dsa.pub");
			copyKeystore("dsa-valid", "testuser", "id_dsa.pub");
			copyKeystore("dsa-with-passphrase", "testuser", "id_dsa.pub");
			copyKeystore("rsa-valid", "testuser", "id_rsa.pub");
			copyKeystore("rsa-with-passphrase", "testuser", "id_rsa.pub");
			copyKeystore("x509-valid", "testuser", "authorized_keys");
			copyKeystore("ed25519-valid", "testuser", "id_ed25519.pub");
			copyKeystore("ed25519-with-passphrase", "testuser", "id_ed25519.pub");
			copyKeystore("ecdsa384-valid", "testuser", "id_ecdsa.pub");
			copyKeystore("ecdsa384-with-passphrase", "testuser", "id_ecdsa.pub");
			copyKeystore("ecdsa521-valid", "testuser", "id_ecdsa.pub");
			copyKeystore("ecdsa521-with-passphrase", "testuser", "id_ecdsa.pub");
			copyKeystore("ecdsa256-with-passphrase", "testuser", "id_ecdsa.pub");
			
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		// Start
		sshd.start();
		// If port 0 was specified, get the actual port
		if (configuration.getPort() == 0) {
			configuration.setPort(sshd.getPort());
		}
	}

	private void copyKeystore(String key, String user, String name) throws IOException {
		String keytext = IOUtils.toString(getClass().getResource("/" + key + "/" + name), "UTF-8").trim();
		if (!keytext.endsWith("\n")) {
			keytext += "\n";
		}
		File file = new File(new File(homeRoot, user), ".ssh");
		file.mkdirs();
		File authKeyFile = new File(file, "authorized_keys");
		System.out.println("Writing new authorized key '" + keytext + "' to " + authKeyFile);
		OutputStream out = new FileOutputStream(authKeyFile, true);
		try {
			out.write(keytext.getBytes());
		} finally {
			out.close();
		}
	}

	protected void doStop() throws Exception {
		sshd.close();
		if (eventErrors > 0) {
			System.out.println(eventErrorMessages.toString());
			throw new Exception("There were " + eventErrors + " event related errors");
		}
	}
}
