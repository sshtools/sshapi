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
package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import javax.net.SocketFactory;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import net.sf.sshapi.AbstractClient;
import net.sf.sshapi.Logger;
import net.sf.sshapi.SshBannerHandler;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshSCPClient;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.auth.SshKeyboardInteractiveAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;
import net.sf.sshapi.auth.SshPublicKeyAuthenticator;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.sftp.SftpClient;
import ssh.SshLibrary;
import ssh.SshLibrary.ssh_key;
import ssh.SshLibrary.ssh_session;

public class LibsshClient extends AbstractClient {
	public final static int S_IRWXU = 448;
	public final static int S_IRUSR = 256;
	public final static int S_IWUSR = 128;
	public final static int S_IXUSR = 64;
	public final static int O_RDONLY = 0;
	public final static int O_WRONLY = 1;
	public final static int O_RDWR = 2;
	public final static int O_CREAT = 512;
	public final static int O_TRUNC = 1024;
	// TODO make configurable
	public final static int SCP_BUFFER_SIZE = 32768;
	public final static int SFTP_BUFFER_SIZE = 32768;
	public static final int FORWARDING_BUFFER_SIZE = 32768;
	// Private instance variables
	private String username;
	private boolean connected;
	ssh_session libSshSession;
	private SshLibrary library;
	private Pointer hostname;
	private boolean authenticated;
	final static Logger LOG = SshConfiguration.getLogger();

	public LibsshClient(SshConfiguration configuration) {
		super(configuration);
		library = SshLibrary.INSTANCE;
	}

	@Override
	protected void doConnect(String username, final String hostname, int port, SshAuthenticator... authenticators)
			throws SshException {
		SshConfiguration configuration = getConfiguration();
		libSshSession = library.ssh_new();
		if (libSshSession == null) {
			throw new SshException(SshException.GENERAL, "Failed to create connection.");
		}
		this.hostname = stringPointer(hostname);
		try {
			library.ssh_options_set(libSshSession, SshLibrary.ssh_options_e.SSH_OPTIONS_HOST, this.hostname);
			library.ssh_options_set(libSshSession, SshLibrary.ssh_options_e.SSH_OPTIONS_PORT,
					new IntByReference(port).getPointer());
			if (configuration.getPreferredClientToServerCompression() != null) {
				library.ssh_options_set(libSshSession, SshLibrary.ssh_options_e.SSH_OPTIONS_COMPRESSION_C_S,
						stringPointer(configuration.getPreferredClientToServerCompression()));
			}
			if (configuration.getPreferredServerToClientCompression() != null) {
				library.ssh_options_set(libSshSession, SshLibrary.ssh_options_e.SSH_OPTIONS_COMPRESSION_S_C,
						stringPointer(configuration.getPreferredServerToClientCompression()));
			}
			library.ssh_options_set(libSshSession, SshLibrary.ssh_options_e.SSH_OPTIONS_SSH1,
					new IntByReference(configuration.getProtocolVersion() == SshConfiguration.SSH2_ONLY ? 0 : 1).getPointer());
			library.ssh_options_set(libSshSession, SshLibrary.ssh_options_e.SSH_OPTIONS_SSH2,
					new IntByReference(configuration.getProtocolVersion() == SshConfiguration.SSH1_ONLY ? 0 : 1).getPointer());
			// TODO other options -
			// http://api.libssh.org/stable/group__libssh__session.html#ga7a801b85800baa3f4e16f5b47db0a73d
			// library.ssh_options_set(libSshSession,
			// SshLibrary.ssh_options_e.SSH_OPTIONS_LOG_VERBOSITY, new
			// IntByReference(
			// SshLibrary.SSH_LOG_PROTOCOL).getPointer());
			this.username = username;
			int rc = library.ssh_connect(libSshSession);
			if (rc != SshLibrary.SSH_OK) {
				throw new SshException(SshException.GENERAL,
						"Error  " + library.ssh_get_error(libSshSession.getPointer()) + " connecting to " + hostname + ":" + port);
			}
			connected = true;
			SshHostKeyValidator hostKeyValidator = configuration.getHostKeyValidator();
			if (hostKeyValidator != null) {
				PointerByReference ref = new PointerByReference(new Memory(16));
				// Fingerprint
				int bytes = library.ssh_get_pubkey_hash(libSshSession, ref);
				Pointer mem = ref.getValue();
				final byte[] hash = mem.getByteArray(0, bytes);
				Pointer f = library.ssh_get_hexa(hash, new NativeSize(bytes));
				final String fingerPrint = f.getString(0);
				// Type
				// TODO - looks like it is always RSA - can't find way to get at
				// key bytes
				// final String keyType = Util.guessKeyType(hash);
				final String keyType = SshConfiguration.PUBLIC_KEY_SSHRSA;
				// The key
				SshHostKey hostKey = new SshHostKey() {
					@Override
					public String getType() {
						return keyType;
					}

					@Override
					public byte[] getKey() {
						return null;
					}

					@Override
					public String getHost() {
						return hostname;
					}

					@Override
					public String getFingerprint() {
						return fingerPrint;
					}
				};
				if (hostKeyValidator.verifyHost(hostKey) != SshHostKeyValidator.STATUS_HOST_KEY_VALID) {
					throw new SshException(SshException.HOST_KEY_REJECTED);
				}
			}
		} catch (SshException sshe) {
			library.ssh_free(libSshSession);
			throw sshe;
		}
	}

	protected final static Pointer stringPointer(String str) {
		Memory mem = new Memory(str.length() + 1);
		mem.setString(0, str);
		return mem;
	}

	protected final static Pointer bytePointer(byte[] bytes) {
		Memory mem = new Memory(bytes.length + 1);
		for (int i = 0; i < bytes.length; i++)
			mem.setByte(i, bytes[i]);
		return mem;
	}

	@Override
	public SocketFactory createTunneledSocketFactory() throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SshSCPClient createSCP() throws SshException {
		return new LibsshSCPClient(library, libSshSession);
	}

	@Override
	public boolean authenticate(SshAuthenticator... authenticators) throws SshException {
		if(authenticated)
			throw new IllegalStateException("Already authenticated.");
		try {
			if (!doAuthentication(authenticators)) {
				return false;
			}
			SshBannerHandler bannerHandler = getConfiguration().getBannerHandler();
			if (bannerHandler != null) {
				Pointer ret = library.ssh_get_issue_banner(libSshSession);
				if (ret != null) {
					bannerHandler.banner(ret.getString(0));
				}
			}
			authenticated = true;
			return true;
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
	}

	private boolean doAuthentication(SshAuthenticator[] authenticators) throws IOException, SshException {
		Map<String, SshAuthenticator> authenticatorMap = createAuthenticatorMap(authenticators);
		// SshAgentAuthenticator agent = (SshAgentAuthenticator)
		// authenticatorMap.get("agent");
		SshPasswordAuthenticator paw = (SshPasswordAuthenticator) authenticatorMap.get("password");
		SshPublicKeyAuthenticator pk = (SshPublicKeyAuthenticator) authenticatorMap.get("publickey");
		SshKeyboardInteractiveAuthenticator ki = (SshKeyboardInteractiveAuthenticator) authenticatorMap.get("keyboard-interactive");
		int ret = 0;
		ret = library.ssh_userauth_none(libSshSession, username);
		if (ret == SshLibrary.ssh_auth_e.SSH_AUTH_SUCCESS) {
			return true;
		}
		int remaining = library.ssh_userauth_list(libSshSession, username);
		ret = SshLibrary.ssh_auth_e.SSH_AUTH_DENIED;
		if ((remaining & SshLibrary.SSH_AUTH_METHOD_PUBLICKEY) != 0 && pk != null) {
			PointerByReference keyRef = new PointerByReference();
			if (library.ssh_pki_import_privkey_base64(new String(pk.getPrivateKey(), "US-ASCII"), null, null, null,
					keyRef) == SshLibrary.SSH_OK) {
				ssh_key key = new ssh_key(keyRef.getValue());
				PointerByReference pubkeyRef = new PointerByReference();
				if (library.ssh_pki_export_privkey_to_pubkey(key, pubkeyRef) == SshLibrary.SSH_OK) {
					ssh_key pubkey = new ssh_key(pubkeyRef.getValue());
					ret = library.ssh_userauth_try_publickey(libSshSession, getUsername(), pubkey);
					if (ret == SshLibrary.ssh_auth_e.SSH_AUTH_SUCCESS) {
						ret = library.ssh_userauth_publickey(libSshSession, getUsername(), key);
						if (ret == SshLibrary.ssh_auth_e.SSH_AUTH_SUCCESS)
							return true;
					}
				}
			}
			return false;
		}
		if ((remaining & SshLibrary.SSH_AUTH_METHOD_PASSWORD) != 0 && paw != null) {
			char[] pw = paw.promptForPassword(this, "Password");
			if (pw == null) {
				throw new SshException("Authentication cancelled.");
			}
			ret = library.ssh_userauth_password(libSshSession, username, new String(pw));
			if (ret == SshLibrary.ssh_auth_e.SSH_AUTH_SUCCESS) {
				return true;
			}
		}
		if ((remaining & SshLibrary.SSH_AUTH_METHOD_INTERACTIVE) != 0 && ki != null) {
			int rc = library.ssh_userauth_kbdint(libSshSession, username, null);
			while (rc == SshLibrary.ssh_auth_e.SSH_AUTH_INFO) {
				String name = library.ssh_userauth_kbdint_getname(libSshSession);
				String instruction = library.ssh_userauth_kbdint_getinstruction(libSshSession);
				int nprompts = library.ssh_userauth_kbdint_getnprompts(libSshSession);
				String[] prompts = new String[nprompts];
				boolean[] echo = new boolean[nprompts];
				for (int i = 0; i < nprompts; i++) {
					ByteBuffer b = ByteBuffer.allocate(1);
					prompts[i] = library.ssh_userauth_kbdint_getprompt(libSshSession, i, b);
					echo[i] = b.get() > 0;
				}
				String[] answers = ki.challenge(name, instruction, prompts, echo);
				if (answers != null) {
					for (int i = 0; i < answers.length; i++) {
						library.ssh_userauth_kbdint_setanswer(libSshSession, i, answers[i]);
					}
				}
				rc = library.ssh_userauth_kbdint(libSshSession, username, null);
			}
			if (ret == SshLibrary.ssh_auth_e.SSH_AUTH_SUCCESS) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getRemoteIdentification() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		return "Unknown";
	}

	@Override
	public int getRemoteProtocolVersion() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		// TODO get the actual remote version
		return getConfiguration().getProtocolVersion() == SshConfiguration.SSH1_OR_SSH2 ? 2
				: getConfiguration().getProtocolVersion();
	}

	@Override
	public SshPortForward createLocalForward(final String localAddress, final int localPort, final String remoteHost,
			final int remotePort) throws SshException {
		return new LibsshLocalForward(libSshSession, library, localAddress, localPort, remoteHost, remotePort);
	}

	@Override
	public SshPortForward createRemoteForward(final String remoteHost, final int remotePort, final String localAddress,
			final int localPort) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SshShell createShell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException {
		return new LibsshShell(libSshSession, library, termType, cols, rows, false);
	}

	@Override
	public SshCommand createCommand(final String command) throws SshException {
		return new LibsshSshCommand(libSshSession, library, command);
	}

	@Override
	public void disconnect() throws SshException {
		if (!isConnected()) {
			throw new SshException(SshException.NOT_OPEN, "Not connected.");
		}
		library.ssh_disconnect(libSshSession);
		connected = false;
		authenticated = false;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean isAuthenticated() {
		return isConnected() && authenticated;
	}

	@Override
	public SftpClient createSftp() throws SshException {
		return new LibsshSFTPClient(library, libSshSession);
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public int getChannelCount() {
		// TODO Can't see anything in libssh, might have to count our own
		// channels
		return 0;
	}
}
