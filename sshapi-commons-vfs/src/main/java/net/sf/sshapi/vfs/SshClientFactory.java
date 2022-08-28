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
package net.sf.sshapi.vfs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.net.SocketFactory;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;
import net.sf.sshapi.util.DefaultPublicKeyAuthenticator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

/**
 * Helper to create {@link SshClient} objects.
 */
public class SshClientFactory {

	static SocketFactory socketFactory = null;

	/**
	 * Set the socket factory to use for making connections to remote SSH servers.
	 * 
	 * @param socketFactory socket factory
	 */
	public static void setSocketFactory(SocketFactory socketFactory) {
		SshClientFactory.socketFactory = socketFactory;
	}

	/**
	 * Create a new SSH client.
	 * 
	 * @param hostname          host name
	 * @param port              port
	 * @param username          username
	 * @param password          password
	 * @param fileSystemOptions options
	 * @return client
	 * @throws FileSystemException
	 */
	public static SshClient createConnection(String hostname, int port, String username, String password,
			FileSystemOptions fileSystemOptions) throws FileSystemException {

		// The file system options may already have a client
		SftpFileSystemConfigBuilder cbuilder = SftpFileSystemConfigBuilder.getInstance();

		// Get the SSH provider instance
		SshConfiguration config = new SshConfiguration();
		if (socketFactory != null) {
			config.addRequiredCapability(Capability.SOCKET_FACTORY);
			config.setSocketFactory(socketFactory);
		}
		config.addRequiredCapability(Capability.SFTP);
		config.setHostKeyValidator(cbuilder.getHostKeyVerification(fileSystemOptions));
		config.setPreferredClientToServerCompression(cbuilder.getCompression(fileSystemOptions));
		config.setPreferredServerToClientCompression(cbuilder.getCompression(fileSystemOptions));
		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);

		/**
		 * TODO: use the FileSystemOptions variable to retrieve some SSH context
		 * settings
		 * 
		 * TODO: No analogue for Charset configuration
		 */
		try {
			SshClient con =  provider.createClient(config);
			List<SshAuthenticator> auths = new ArrayList<>();
			if (username == null) {
				UserAuthenticator ua = DefaultFileSystemConfigBuilder.getInstance()
						.getUserAuthenticator(fileSystemOptions);
				UserAuthenticationData data = ua.requestAuthentication(password == null
						? new UserAuthenticationData.Type[] { UserAuthenticationData.USERNAME,
								UserAuthenticationData.PASSWORD }
						: new UserAuthenticationData.Type[] { UserAuthenticationData.USERNAME });
				if (data == null) {
					throw new Exception("vfs.provider.sftp/authentication-cancelled.error");
				}
				username = new String(data.getData(UserAuthenticationData.USERNAME));
				if (password == null)
					password = new String(data.getData(UserAuthenticationData.PASSWORD));

				if (password != null)
					auths.add(new SimplePasswordAuthenticator(password.toCharArray()));
			} else if (password != null) {
				auths.add(new SimplePasswordAuthenticator(password.toCharArray()));
			}
			if (cbuilder.getIdentities(fileSystemOptions) != null) {
				for (File pk : cbuilder.getIdentities(fileSystemOptions)) {
					auths.add(new DefaultPublicKeyAuthenticator(pk));
				}
			} else if (cbuilder.isUseUserAuthenticator(fileSystemOptions)) {
				auths.add(new SshPasswordAuthenticator() {
					@Override
					public char[] promptForPassword(SshClient session, String message) {
						UserAuthenticator ua = DefaultFileSystemConfigBuilder.getInstance()
								.getUserAuthenticator(fileSystemOptions);
						UserAuthenticationData data = ua.requestAuthentication(
								new UserAuthenticationData.Type[] { UserAuthenticationData.PASSWORD });
						return data == null ? null : data.getData(UserAuthenticationData.PASSWORD);
					}
				});
			}
			con.connect(username, hostname, port);
			if (!con.authenticate(auths))
				throw new FileSystemException("vfs.provider.sftp/authentication-failed.error",
						new Object[] { username });

			return con;
		} catch (FileSystemException fse) {
			throw fse;
		} catch (final Exception ex) {
			throw new FileSystemException("vfs.provider.sftp/connect.error", ex, new Object[] { hostname });
		}

	}
}
