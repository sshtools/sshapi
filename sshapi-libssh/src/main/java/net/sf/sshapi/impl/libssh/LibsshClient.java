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
package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import net.sf.sshapi.AbstractClient;
import net.sf.sshapi.Logger;
import net.sf.sshapi.SshBannerHandler;
import net.sf.sshapi.SshChannel;
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
import net.sf.sshapi.util.Util;
import ssh.SshLibrary;
import ssh.SshLibrary.SizeT;
import ssh.SshLibrary.ssh_auth_callback;
import ssh.SshLibrary.ssh_channel;
import ssh.SshLibrary.ssh_key;
import ssh.SshLibrary.ssh_session;

/**
 * libssh client implementation.
 *
 */
public class LibsshClient extends AbstractClient {
	/**
	 * Forwarding buffer size
	 */
	public static final int FORWARDING_BUFFER_SIZE = 32768;
	
	final static int S_IRUSR = 256;
	final static int S_IRWXU = 448;
	final static int S_IWUSR = 128;
	final static int S_IXUSR = 64;
	// TODO make configurable
	final static int SCP_BUFFER_SIZE = 32768;
	final static int SFTP_BUFFER_SIZE = 32768;
	final static Logger LOG = SshConfiguration.getLogger();

	protected final static Pointer bytePointer(byte[] bytes) {
		Memory mem = new Memory(bytes.length + 1);
		for (int i = 0; i < bytes.length; i++)
			mem.setByte(i, bytes[i]);
		return mem;
	}

	protected final static Pointer stringPointer(String str) {
		Memory mem = new Memory(str.length() + 1);
		mem.setString(0, str);
		return mem;
	}

	ssh_session libSshSession;
	private boolean authenticated;
	private boolean connected;
	private Pointer hostname;
	private SshLibrary library;
	// Private instance variables
	private String username;

	/**
	 * Constructor.
	 * 
	 * @param configuration configuration
	 */
	public LibsshClient(SshConfiguration configuration) {
		super(configuration);
		library = SshLibrary.INSTANCE;
	}

	@Override
	public boolean authenticate(SshAuthenticator... authenticators) throws SshException {
		if (authenticated)
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
			
			if(getConfiguration().getX11Screen() != -1) {
				ssh_channel x11chan = library.ssh_channel_new(libSshSession);
				if(x11chan == null) {
					throw new SshException(SshException.GENERAL,
							"Error  " + library.ssh_get_error(libSshSession.getPointer()) + " retrieving public key");
				}
				library.ssh_channel_open_session(x11chan);
				library.ssh_channel_request_x11(x11chan, 0, null, Util.formatAsHexString(getConfiguration().getX11Cookie()), getConfiguration().getX11Screen());
			}
			
			authenticated = true;
			return true;
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
	}

	@Override
	public int getChannelCount() {
		// TODO Can't see anything in libssh, might have to count our own
		// channels
		return 0;
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
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAuthenticated() {
		return isConnected() && authenticated;
	}

	@Override
	public boolean isConnected() {
		return connected;
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
				// Type
				// TODO - looks like it is always RSA - can't find way to get at
				// key bytes
				// final String keyType = Util.guessKeyType(hash);
				final String keyType = SshConfiguration.PUBLIC_KEY_SSHRSA;
				ssh_key key = new ssh_key();
				PointerByReference pbr = new PointerByReference(key.getPointer());
				rc = library.ssh_get_server_publickey(libSshSession, pbr);
				if (rc != SshLibrary.SSH_OK) {
					throw new SshException(SshException.GENERAL,
							"Error  " + library.ssh_get_error(libSshSession.getPointer()) + " retrieving public key");
				}
				// The key
				final byte[] keybytes = pbr.getValue().getByteArray(0, 128);
				

				PointerByReference ref = new PointerByReference(new Memory(32));
				// Fingerprint
				int bytes = library.ssh_get_pubkey_hash(libSshSession, ref);
				Pointer mem = ref.getValue();
				final byte[] hash = mem.getByteArray(0, bytes);
				Pointer f = library.ssh_get_hexa(hash, new SizeT(bytes));
				final String fingerPrint = f.getString(0);
				
				SshHostKey hostKey = new SshHostKey() {
					@Override
					public String getFingerprint() {
						return fingerPrint;
					}

					@Override
					public String getHost() {
						return hostname;
					}

					@Override
					public byte[] getKey() {
						return keybytes;
					}

					@Override
					public String getType() {
						return keyType;
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

	@Override
	protected SshChannel doCreateForwardingChannel(String hostname, int port) throws SshException {
		return new LibsshForwardingChannel(library, this, getProvider(), getConfiguration(), hostname, port);
	}

	@Override
	protected SshCommand doCreateCommand(final String command, String termType, int cols, int rows, int pixWidth, int pixHeight,
			byte[] terminalModes) throws SshException {
		return new LibsshSshCommand(getProvider(), getConfiguration(), libSshSession, library, command, termType, cols, rows);
	}

	@Override
	protected SshPortForward doCreateLocalForward(final String localAddress, final int localPort, final String remoteHost,
			final int remotePort) throws SshException {
		return new LibsshLocalForward(getProvider(), libSshSession, library, localAddress, localPort, remoteHost, remotePort);
	}

	@Override
	protected SshSCPClient doCreateSCP() throws SshException {
		return new LibsshSCPClient(getProvider(), library, libSshSession);
	}

	@Override
	protected SftpClient doCreateSftp() throws SshException {
		return new LibsshSFTPClient(getProvider(), getConfiguration(), library, libSshSession);
	}

	@Override
	protected SshShell doCreateShell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException {
		return new LibsshShell(getProvider(), getConfiguration(), libSshSession, library, termType, cols, rows, true);
	}

	@Override
	protected void onClose() throws SshException {
		if (!isConnected()) {
			throw new SshException(SshException.NOT_OPEN, "Not connected.");
		}
		LOG.debug("Disconnecting libssh session");
		library.ssh_disconnect(libSshSession);
		LOG.debug("Disconnected libssh session");
		connected = false;
		authenticated = false;
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
			AtomicBoolean askedFor = new AtomicBoolean();
			ssh_auth_callback cb = new ssh_auth_callback() {
				@Override
				public int apply(Pointer prompt, Pointer buf, SizeT len, int echo, int verify, Pointer userdata) {
					askedFor.set(true);
					char[] pw = pk.promptForPassphrase(LibsshClient.this, prompt.getString(0));
					if(pw == null)
						return -1;
					else {
						buf.setString(0, new String(pw));
					}
					return 0;
				}
			};
			
			int result = library.ssh_pki_import_privkey_base64(new String(pk.getPrivateKey(), "US-ASCII"), null, cb, null,
					keyRef);
			if (result == SshLibrary.SSH_OK) {
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
			if(askedFor.get())
				return false;
			else
				throw new SshException(SshException.PRIVATE_KEY_FORMAT_NOT_SUPPORTED);
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
			ret = library.ssh_userauth_kbdint(libSshSession, username, null);
			while (ret == SshLibrary.ssh_auth_e.SSH_AUTH_INFO) {
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
				if (answers == null) {
					throw new SshException(SshException.AUTHENTICATION_CANCELLED);
				}
				else {
					for (int i = 0; i < answers.length; i++) {
						library.ssh_userauth_kbdint_setanswer(libSshSession, i, answers[i]);
					}
				}
				ret = library.ssh_userauth_kbdint(libSshSession, username, null);
			}
			if (ret == SshLibrary.ssh_auth_e.SSH_AUTH_SUCCESS) {
				return true;
			}
		}
		return false;
	}
}
