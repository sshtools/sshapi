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
package net.sf.sshapi;

import java.net.Socket;

import javax.net.SocketFactory;

import net.sf.sshapi.auth.SshGSSAPIAuthenticator;
import net.sf.sshapi.auth.SshKeyboardInteractiveAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;
import net.sf.sshapi.auth.SshPublicKeyAuthenticator;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.sftp.SftpHandle;
import net.sf.sshapi.util.RemoteSocketFactory;

/**
 * Represents a feature that a provider implementation is capable of. Clients
 * may use this to make more informed choices about which provider to choose.
 * For example, an application may absolutely require SSH1 support. It may
 * examine the capabilities of each provider and stop execution if no provider
 * that supports SSH1 is available.
 * 
 * @see SshProvider#getCapabilities()
 * 
 */
public class Capability {

	/**
	 * The provider fully supports per-connection configuration.
	 * 
	 */
	public final static Capability PER_CONNECTION_CONFIGURATION = new Capability("perConnectionConfig");

	/**
	 * The provider fully supports SSH1.
	 */
	public final static Capability SSH1 = new Capability("ssh1");

	/**
	 * The provider fully supports SSH2.
	 */
	public final static Capability SSH2 = new Capability("ssh2");

	/**
	 * The provider supports HTTP proxy connections.
	 */
	public final static Capability HTTP_PROXY = new Capability("httpProxy");

	/**
	 * The provider supports SOCKS4 proxy connections.
	 */
	public final static Capability SOCKS4_PROXY = new Capability("socks4");

	/**
	 * The provider supports SOCKS5 proxy connections.
	 */
	public final static Capability SOCKS5_PROXY = new Capability("socks5");

	/**
	 * The provider supports password authentication. See
	 * {@link SshPasswordAuthenticator}.
	 */
	public final static Capability PASSWORD_AUTHENTICATION = new Capability("password");

	/**
	 * The provider supports public key authentication. See
	 * {@link SshPublicKeyAuthenticator}.
	 */
	public final static Capability PUBLIC_KEY_AUTHENTICATION = new Capability("publicKey");
	/**
	 * The provider supports keyboard interactive authentication. See
	 * {@link SshKeyboardInteractiveAuthenticator}.
	 */
	public final static Capability KEYBOARD_INTERACTIVE_AUTHENTICATION = new Capability("kbi");
	/**
	 * The provider supports GSSAPI authentication. See
	 * {@link SshGSSAPIAuthenticator}.
	 */
	public final static Capability GSSAPI_AUTHENTICATION = new Capability("gssapi");
	/**
	 * The provider supports identity management. See
	 * {@link SshProvider#createIdentityManager(SshConfiguration)}.
	 */
	public final static Capability IDENTITY_MANAGEMENT = new Capability("identityManagment");
	/**
	 * The provider supports host key management. See
	 * {@link SshProvider#createHostKeyManager(SshConfiguration)}.
	 */
	public final static Capability HOST_KEY_MANAGEMENT = new Capability("hostKeyManagment");
	/**
	 * The provider supports host key verification. See
	 * {@link SshProvider#createHostKeyManager(SshConfiguration)}.
	 */
	public final static Capability HOST_KEY_VERIFICATION = new Capability("hostKeyVerification");
	/**
	 * The provider supports port forwarding events. See
	 * {@link SshClient#addPortForwardListener(net.sf.sshapi.forwarding.SshPortForwardListener)}
	 */
	public final static Capability PORT_FORWARD_EVENTS = new Capability("portForwardEvents");
	/**
	 * The provider reports on channel data events. See
	 * {@link SshDataProducingComponent#addDataListener(SshDataListener)}.
	 */
	public final static Capability CHANNEL_DATA_EVENTS = new Capability("channelDataEvents");

	/**
	 * The provider will fire events when file transfers start, progress, and
	 * finished.
	 */
	public final static Capability FILE_TRANSFER_EVENTS = new Capability("fileTransferEvents");
	/**
	 * The provider supports local forwards.
	 */
	public final static Capability LOCAL_PORT_FORWARD = new Capability("localPortForward");
	/**
	 * The provider supports remote forwards.
	 */
	public final static Capability REMOTE_PORT_FORWARD = new Capability("localPortForward");
	/**
	 * The provider supports SCP. See {@link SshClient#createSCPClient()}.
	 */
	public final static Capability SCP = new Capability("scp");
	/**
	 * The provider supports SFTP. See {@link SshClient#createSftpClient()}
	 */
	public final static Capability SFTP = new Capability("sftp");
	/**
	 * The provider supports the public key subsystem. See
	 * {@link SshClient#createPublicKeySubsystem()}.
	 */
	public final static Capability PUBLIC_KEY_SUBSYSTEM = new Capability("publicKeySubsystem");
	/**
	 * The provider supports use of a {@link SocketFactory} to create
	 * connections. See {@link SshClient#setSocketFactory(SocketFactory)}.
	 */
	public final static Capability SOCKET_FACTORY = new Capability("socketFactory");
	/**
	 * The provider supports changing of terminal window size
	 * {@link SshShell#requestPseudoTerminalChange(int, int, int, int)}.
	 */
	public final static Capability WINDOW_CHANGE = new Capability("windowChange");
	/**
	 * The provider can create a socket factory that is tunneled to a remote
	 * host over SSH. See {@link SshClient#createTunneledSocketFactory()}.
	 */
	public final static Capability TUNNELED_SOCKET_FACTORY = new Capability("tunnelledSocketFactory");

	/**
	 * The provider can fall-back to using SCP if SFTP is not available. This
	 * allows you to write you application using the SFTP API, with SCP being
	 * used if it is not available.
	 */
	public final static Capability SFTP_OVER_SCP = new Capability("sftpOverScp");

	/**
	 * Allows setting of I/O timeouts (in millisecond) on the connection. These are set by {@link SshClient#setTimeout(int)}
	 * and usually apply to the socket, i.e. are delegated to {@link Socket#setSoTimeout(int)}. 
	 */
	public final static Capability IO_TIMEOUTS = new Capability("ioTimeouts");

	/**
	 * The provider supports channel handlers. See {@link SshChannelHandler}.
	 */
	public final static Capability CHANNEL_HANDLERS = new Capability("channelHandlers");
	/**
	 * The provider supports X11 forwarding. This capability will be accompanied by either
	 * a {@link #X11_FORWARDING_UNIX_SOCKET} or {@link #X11_FORWARDING_TCP}, or both.
	 */
	public final static Capability X11_FORWARDING = new Capability("x11Forwarding");
	/**
	 * The provider supports X11 forwarding (TCP)
	 */
	public final static Capability X11_FORWARDING_TCP = new Capability("x11ForwardingTcp");
	/**
	 * The provider supports X11 forwarding (Unix Socket)
	 */
	public final static Capability X11_FORWARDING_UNIX_SOCKET = new Capability("x11ForwardingUnixSocket");
	/**
	 * The provider supports sending signals on session channels
	 */
	public final static Capability SIGNALS = new Capability("signals");
	/**
	 * The provider supports a shell.
	 */
	public final static Capability SHELL = new Capability("shell");
	/**
	 * The provider forcing a key exchange.
	 */
	public final static Capability FORCE_KEY_EXCHANGE = new Capability("forceKex");
	
	/**
	 * The provider supports raw SFTP usage (i.e. using a {@link SftpHandle} to read or write).
	 */
	public final static Capability RAW_SFTP = new Capability("rawSftp");
	/**
	 * The provider supports loading of public / private keys from an X509 keystore.
	 */
	public final static Capability X509_PUBLIC_KEY = new Capability("x509PublicKey");
	/**
	 * The provider supports text / binary mode for SFTP.
	 */
	public final static Capability SFTP_TRANSFER_MODE = new Capability("sftpTransferMode");
	/**
	 * The provider supports setting of last modified time.
	 */
	public final static Capability SET_LAST_MODIFIED = new Capability("setLastModified");
	/**
	 * The provider supports recursive getting of files over SCP.
	 */
	public final static Capability RECURSIVE_SCP_GET = new Capability("recursiveScpGet");
	/**
	 * The provider supports reading a symbolic links target ({@link SftpClient#readLink(String)}).
	 */
	public final static Capability SFTP_READ_LINK = new Capability("sftpReadLink");
	/**
	 * The provider supports hard links (hardlink@openssh.com) target ({@link SftpClient#link(String, String)}).
	 * The server must support this extended message.
	 */
	public final static Capability SFTP_HARD_LINK = new Capability("sftpHardLink");
	/**
	 * The provider supports {@link SftpClient#lstat(String)} for reading file attributes without
	 * following links
	 */
	public final static Capability SFTP_LSTAT = new Capability("sftpLstat");
	/**
	 * The provider supports an offset {@link SftpClient#get(String, java.io.OutputStream, long)},
	 * {@link SftpClient#put(String, java.io.InputStream, int, long)} and related methods, allowing
	 * specifying an offset using these methods. If this capability is present, that would usually
	 * imply {@link Capability#SFTP_RESUME} too, as resume may be implemented using offsets.
	 */
	public final static Capability SFTP_OFFSET= new Capability("sftpOffset");
	/**
	 * The provider supports {@link SftpClient#resumeGet(String, java.io.File)}
	 * {@link SftpClient#resumePut(java.io.File, String)}, allowing
	 * resuming of uploads and downloads.
	 */
	public final static Capability SFTP_RESUME = new Capability("sftpResume");
	/**
	 * The provider supports an optimised version of {@link SftpClient#directory(String, java.nio.file.DirectoryStream.Filter)}
	 * and / or {@link SftpClient#visit(String, java.nio.file.FileVisitor)}. This is preferred when handling
	 * large directories or directory trees.
	 */
	public final static Capability SFTP_ITERABLE = new Capability("sftpIterable");

	/**
	 * The provider supports a agent for key authentication. One of the other
	 * capabilities {@link Capability#RFC_AGENT} or {@link Capability#OPENSSH_AGENT} will
	 * also be present.
	 */
	public final static Capability AGENT = new Capability("agent");
	
	/**
	 * The provider supports an RFC agent for key authentication.
	 */
	public final static Capability RFC_AGENT = new Capability("rfc-agent");
	/**
	 * The provider supports an SSH agent for key authentication.
	 */
	public final static Capability OPENSSH_AGENT = new Capability("openssh-agent");
	/**
	 * The provider supports forwarding channels. This capability implies {@link Capability#TUNNELED_SOCKET_FACTORY}
	 * as {@link RemoteSocketFactory} may be used with any provided that supports forwarding channels.
	 */
	public final static Capability FORWARDING_CHANNELS = new Capability("forwarding-channels");
	
	/** The provider filters out '.' and '..' entries returned by SFTP list operations */
	public final static Capability FILTERS_SFTP_DOT_DIRECTORIES = new Capability("filters-sftp-dot-directories");
	
	/** The provider provides option to configure if last access / last modification times
	 * are preserved during SCP transfers. Note, if this capability is missing, it doesn't mean
	 * the provide WONT preserve access times, it just means it is not configurable with
	 * {@link ScpClient#setTimesPreserved(boolean)}.
	 */
	public final static Capability SCP_CAN_PRESERVE_ATTRIBUTES = new Capability("scp-can-preserve-times");

	private String name;

	private Capability(String name) {
		this.name = name;
	}

	/**
	 * Get the capability name
	 * 
	 * @return capability name
	 */
	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
}
