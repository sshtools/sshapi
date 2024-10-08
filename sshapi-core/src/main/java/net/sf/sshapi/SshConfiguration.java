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

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import net.sf.sshapi.agent.SshAgent;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.util.ConsoleLogger;
import net.sf.sshapi.util.DumbWithWarningHostKeyValidator;
import net.sf.sshapi.util.Util;
import net.sf.sshapi.util.XDetails;

/**
 * Represents configuration of an {@link SshClient} and some attributes /
 * callbacks that are common to all providers. Also contains a property sheet
 * that may be used to pass configuration to the provider.
 * <p>
 * <h2>Default Configuration</h2> The default constructor will create a
 * configuration that allows SSH1 or SSH2, has no custom host key validation,
 * and has no properties. This is what will be passed to providers that support
 * a default configuration.
 * 
 */
public class SshConfiguration {
	/**
	 * Maximum shell packet size
	 */
	public static final int SHELL_MAXIMUM_PACKET_SIZE = 32 * 1024;
	/**
	 * Maximum shell window size
	 */
	public static final int SHELL_WINDOW_SIZE_MAX = (64 * SHELL_MAXIMUM_PACKET_SIZE);
	/**
	 * Default SFTP block size for providers to use
	 */
	public static final int SFTP_BLOCK_SIZE = 32 * 1024;
	/**
	 * Maximum SFTP packet size
	 */
	public static final int SFTP_MAXIMUM_PACKET_SIZE = 32 * 1024;
	/**
	 * Maximum SFTP window size
	 */
	public static final int SFTP_WINDOW_SIZE_MAX = (64 * SFTP_MAXIMUM_PACKET_SIZE);
	/**
	 * Maximum tunnel packet size
	 */
	public static final int TUNNEL_MAXIMUM_PACKET_SIZE = 32 * 1024;
	/**
	 * Maximum tunnel window size
	 */
	public static final int TUNNEL_WINDOW_SIZE_MAX = (64 * SFTP_MAXIMUM_PACKET_SIZE);

	/** The 3DES CBC cipher **/
	public static final String CIPHER_TRIPLEDES_CBC = "3des-cbc";
	/** The Blowfish CBC cipher */
	public static final String CIPHER_BLOWFISH_CBC = "blowfish-cbc";
	/** The AES CBC Cipher */
	public static final String CIPHER_AES128_CBC = "aes128-cbc";
	/** SHA1 message authentication **/
	public static final String HMAC_SHA1 = "hmac-sha1";
	/** SHA1 96 bit message authentication **/
	public static final String HMAC_SHA1_96 = "hmac-sha1-96";
	/** MD5 message authentication **/
	public static final String HMAC_MD5 = "hmac-md5";
	/** MD5 96 bit message authentication **/
	public static final String HMAC_MD5_96 = "hmac-md5-96";
	/** Compression off **/
	public static final String COMPRESSION_NONE = "none";
	/** Optional zlib compression */
	public static final String COMPRESSION_ZLIB = "zlib";
	/** DH group 1 exchange method **/
	public static final String KEX_DIFFIE_HELLMAN_GROUP1_SHA1 = "diffie-hellman-group1-sha1";
	/** DH group 14 exchange method **/
	public static final String KEX_DIFFIE_HELLMAN_GROUP14_SHA1 = "diffie-hellman-group14-sha1";
	/**
	 * Optional key exchange mechanism in which the server maintains a list of
	 * acceptable generators and primes
	 **/
	public static final String KEX_DIFFIE_HELLMAN_GROUP_EXCHANGE_SHA1 = "diffie-hellman-group-exchange-sha1";
	/** SSH2 DSA Public Key **/
	public static final String PUBLIC_KEY_SSHDSA = "ssh-dss";
	/** SSH2 RSA Public Key **/
	public static final String PUBLIC_KEY_SSHRSA = "ssh-rsa";
	/** SSH2 ECDSA **/
	public static final String PUBLIC_KEY_ECDSA_256 = "ecdsa-sha2-nistp256";
	/** SSH2 ECDSA **/
	public static final String PUBLIC_KEY_ECDSA_384 = "ecdsa-sha2-nistp384";
	/** SSH2 ECDSA **/
	public static final String PUBLIC_KEY_ECDSA_521 = "ecdsa-sha2-nistp521";
	/** SSH2 ED448 **/
	public static final String PUBLIC_KEY_ED448 = "draft-ietf-curdle-ssh-ed25519-ed448-10";
	/** SSH2 ED25519 Key **/
	public static final String PUBLIC_KEY_ED25519 = "ssh-ed25519";
	/** SSH1 RSA Public Key **/
	public static final String PUBLIC_KEY_SSHRSA1 = "rsa1";
	/** X509 RSA Public Key **/
	public static final String PUBLIC_KEY_X509V3_RSA_SHA1 = "x509v3-sign-rsa-sha1";

	/** SSH1 Cipher **/
	public static final String CIPHER_DES = "des";
	/** SSH1 Cipher **/
	public static final String CIPHER_3DES = "3des";
	/**
	 * Request only use of the SSH1 protocol.
	 * 
	 * @see #getProtocolVersion()
	 */
	public final static int SSH1_ONLY = 1;
	/**
	 * Request only use of the SSH2 protocol.
	 * 
	 * @see #getProtocolVersion()
	 */
	public final static int SSH2_ONLY = 2;
	/**
	 * Request use of either SSH1 or SSH2 protocols.
	 * 
	 * @see #getProtocolVersion()
	 */
	public final static int SSH1_OR_SSH2 = 3;

	/***
	 * MD5 fingerprint hashes
	 */
	public final static String FINGERPRINT_MD5 = "md5";
	/***
	 * SHA256 fingerprint hashes
	 */
	public final static String FINGERPRINT_SHA256 = "sha256";
	/***
	 * SHA1 fingerprint hashes
	 */
	public final static String FINGERPRINT_SHA1 = "sha1";
	/***
	 * Automatically detect if OpenSsh is in use and flip source
	 * and target paths for symbolic links.
	 */
	public final static int AUTO_SFTP_SYMLINKS = 0;
	/***
	 * Flip source and target paths for symbolic links for OpenSSH 
	 * compatibility with semantics of source and target
	 * paths in SFTP symlinks.
	 */
	public final static int OPENSSH_SFTP_SYMLINKS = 1;
	/***
	 * Use the SSH specification semantics for source and target
	 * paths in SFTP symlinks.
	 */
	public final static int STANDARD_SFTP_SYMLINKS = 2;

	// Private statics
	private static Logger logger = new ConsoleLogger();
	// Private instance variables
	private int protocolVersion = SSH1_OR_SSH2;
	private final Properties properties;
	private SshHostKeyValidator hostKeyValidator;
	private String x11Host;
	private int x11Screen;
	private byte[] x11Cookie;
	private File x11UnixSocketFile;
	private boolean x11SingleConnection;
	private SshBannerHandler bannerHandler;
	private String preferredServerToClientCipher;
	private String preferredClientToServerCipher;
	private String preferredServerToClientMAC;
	private String preferredClientToServerMAC;
	private String preferredServerToClientCompression;
	private String preferredClientToServerCompression;
	private String preferredKeyExchange;
	private String preferredPublicKey;
	private SshProxyServerDetails proxyServer;
	private String preferredSSH1Cipher;
	private String sftpSSH1Path;
	private String fingerprintHashingAlgorithm;
	private SshAgent agent;
	private List<Capability> requiredCapabilities = new ArrayList<>();
	private SocketFactory socketFactory;
	private int maxAuthAttempts = 3;
	private long sftpWindowSizeMax = SFTP_WINDOW_SIZE_MAX;
	private long sftpWindowSize = SFTP_MAXIMUM_PACKET_SIZE * 2;
	private long sftpPacketSize = SFTP_MAXIMUM_PACKET_SIZE;
	private long sftpBlockSize = SFTP_BLOCK_SIZE;
	private long shellWindowSizeMax = SHELL_WINDOW_SIZE_MAX;
	private long shellWindowSize = SHELL_MAXIMUM_PACKET_SIZE * 2;
	private long shellPacketSize = SHELL_MAXIMUM_PACKET_SIZE;
	private long tunnelWindowSizeMax = TUNNEL_WINDOW_SIZE_MAX;
	private long tunnelWindowSize = TUNNEL_MAXIMUM_PACKET_SIZE * 2;
	private long tunnelPacketSize = TUNNEL_MAXIMUM_PACKET_SIZE;
	private int streamBufferSize = SFTP_MAXIMUM_PACKET_SIZE;
	private static SshHostKeyValidator defaultHostKeyValidator = new DumbWithWarningHostKeyValidator();
	private int ioTimeout = (int)TimeUnit.SECONDS.toMillis(Integer.parseInt(System.getProperty("sshapi.defaultIoTimeout", "60")));
	private int sftpSymlinks = AUTO_SFTP_SYMLINKS;

	/**
	 * Do reverse DNS lookups for hosts in the known_hosts (
	 * {@link JschHostKeyManager}).
	 */
	public final static String CFG_KNOWN_HOSTS_REVERSE_DNS = "sshapi.knownHosts.reverseDNS";
	/**
	 * Location of the known_hosts file. Several implementations support this
	 * format, so this configuration property is common.
	 */
	public final static String CFG_KNOWN_HOSTS_PATH = "sshapi.knownHosts.path";

	/**
	 * Set the logger to use
	 * 
	 * @param logger logger
	 */
	public static void setLogger(Logger logger) {
		SshConfiguration.logger = logger;
	}

	/**
	 * Get the logger
	 * 
	 * @return logger
	 */
	public static Logger getLogger() {
		return logger;
	}

	/**
	 * Get the default host key valid to use when constructing a new
	 * {@link SshConfiguration}.
	 * 
	 * @return default host key validator
	 */
	public static SshHostKeyValidator getDefaultHostKeyValidator() {
		return defaultHostKeyValidator;
	}

	/**
	 * Set the default host key valid to use when constructing a new
	 * {@link SshConfiguration}.
	 * 
	 * @param defaultHostKeyValidator default host key validator
	 */
	public static void setDefaultHostKeyValidator(SshHostKeyValidator defaultHostKeyValidator) {
		SshConfiguration.defaultHostKeyValidator = defaultHostKeyValidator;
	}

	/**
	 * Constructor
	 */
	public SshConfiguration() {
		this(new Properties());
	}

	/**
	 * Constructor
	 * 
	 * @param hostKeyValidator host key validator
	 */
	public SshConfiguration(SshHostKeyValidator hostKeyValidator) {
		this(new Properties(), hostKeyValidator);
	}

	/**
	 * Constructor
	 * 
	 * @param properties properties
	 */
	public SshConfiguration(Properties properties) {
		this(properties, defaultHostKeyValidator);
	}

	/**
	 * Constructor
	 * 
	 * @param properties       properties
	 * @param hostKeyValidator host key validator
	 */
	public SshConfiguration(Properties properties, SshHostKeyValidator hostKeyValidator) {
		this.properties = properties;
		this.hostKeyValidator = hostKeyValidator;
	}

	/**
	 * Get the behaviour of the source and target parameters when creating
	 * symbolic links. This is to work-around the incorrect (according to the 
	 * specification) semantics used by OpenSSH SFTP. Can be one off
	 *  {@link #AUTO_SFTP_SYMLINKS} (the default), {@link #OPENSSH_SFTP_SYMLINKS} or
	 *  {@link #STANDARD_SFTP_SYMLINKS}.  
	 *  
	 * @return sftp symlinks mode
	 */
	public int getSftpSymlinks() {
		return sftpSymlinks;
	}

	/**
	 * Set the behaviour of the source and target parameters when creating
	 * symbolic links. This is to work-around the incorrect (according to the 
	 * specification) semantics used by OpenSSH SFTP. Can be one off
	 *  {@link #AUTO_SFTP_SYMLINKS} (the default), {@link #OPENSSH_SFTP_SYMLINKS} or
	 *  {@link #STANDARD_SFTP_SYMLINKS}.  
	 *  
	 * @param sftpSymlinks sftp symlinks mode
	 */
	public SshConfiguration setSftpSymlinks(int sftpSymlinks) {
		this.sftpSymlinks = sftpSymlinks;
		return this;
	}

	/**
	 * Get the default I/O timeout in milliseconds. This is generally passed to
	 * {@link Socket#setSoTimeout(int)}. A value of zero means never timeout. The provider must support
	 * {@link Capability#IO_TIMEOUTS}. This may be override per connection with
	 * {@link SshClient#setTimeout(int)}.
	 * 
	 * @return IO timeout in milliseconds
	 */
	public int getIoTimeout() {
		return ioTimeout;
	}

	/**
	 * Set the default idle timeout in milliseconds. This is generally passed to
	 * {@link Socket#setSoTimeout(int)}. A value of zero means never timeout. The provider must support
	 * {@link Capability#IO_TIMEOUTS}. This may be override per connection with
	 * {@link SshClient#setTimeout(int)}.
	 * 
	 * @param ioTimeout IO timeout in milliseconds
	 */
	public void setIoTimeout(int ioTimeout) {
		this.ioTimeout = ioTimeout;
	}

	/**
	 * Get the {@link SshAgent} in use. This will be added to the client as a
	 * channel handler if set (you may do this yourself if you wish).
	 * 
	 * @return agent
	 */
	public SshAgent getAgent() {
		return agent;
	}

	/**
	 * Set the {@link SshAgent} in use. This will be added to the client as a
	 * channel handler if set (you may do this yourself if you wish).
	 * 
	 * @param agent agent
	 * @return this for chaining
	 */
	public SshConfiguration setAgent(SshAgent agent) {
		this.agent = agent;
		return this;
	}

	/**
	 * Get the max window size <strong>hint</strong> for tunnels. Use zero for no
	 * hint.
	 * 
	 * @return tunnel max window size
	 */
	public long getTunnelWindowSizeMax() {
		return tunnelWindowSizeMax;
	}

	/**
	 * Set the tunnel size max <strong>hint</strong> for tunnels. Use zero for no
	 * hint.
	 * 
	 * @param tunnelWindowSizeMax Tunnel window size max
	 * @return this for chaining
	 */
	public SshConfiguration setTunnelWindowSizeMax(long tunnelWindowSizeMax) {
		this.tunnelWindowSizeMax = tunnelWindowSizeMax;
		return this;
	}

	/**
	 * Get the window size <strong>hint</strong> for tunnels. Use zero for no hint.
	 * 
	 * @return tunnel window size
	 */
	public long getTunnelWindowSize() {
		return tunnelWindowSize;
	}

	/**
	 * Set the window size <strong>hint</strong> for SFTP. Use zero for no hint.
	 * 
	 * @param tunnelWindowSize SFTP window size
	 * @return this for chaining
	 */
	public SshConfiguration setTunnelWindowSize(long tunnelWindowSize) {
		this.tunnelWindowSize = tunnelWindowSize;
		return this;
	}

	/**
	 * Get the packet size <strong>hint</strong> for tunnels. Use zero for no hint.
	 * 
	 * @return tunnel packet size
	 */
	public long getTunnelPacketSize() {
		return tunnelPacketSize;
	}

	/**
	 * Set the packet size <strong>hint</strong> for tunnels. Use zero for no hint.
	 * 
	 * @param tunnelPacketSize tunnel packet size
	 * @return this for chaining
	 */
	public SshConfiguration setTunnelPacketSize(long tunnelPacketSize) {
		this.tunnelPacketSize = tunnelPacketSize;
		return this;
	}

	/**
	 * Get the max window size <strong>hint</strong> for SFTP. Use zero for no hint.
	 * 
	 * @return SFTP max window size
	 */
	public long getSftpWindowSizeMax() {
		return sftpWindowSizeMax;
	}

	/**
	 * Set the window size max <strong>hint</strong> for SFTP. Use zero for no hint.
	 * 
	 * @param sftpWindowSizeMax SFTP window size max
	 * @return this for chaining
	 */
	public SshConfiguration setSftpWindowSizeMax(long sftpWindowSizeMax) {
		this.sftpWindowSizeMax = sftpWindowSizeMax;
		return this;
	}

	/**
	 * Get the window size <strong>hint</strong> for SFTP. Use zero for no hint.
	 * 
	 * @return SFTP window size
	 */
	public long getSftpWindowSize() {
		return sftpWindowSize;
	}

	/**
	 * Set the window size <strong>hint</strong> for SFTP. Use zero for no hint.
	 * 
	 * @param sftpWindowSize SFTP window size
	 * @return this for chaining
	 */
	public SshConfiguration setSftpWindowSize(long sftpWindowSize) {
		this.sftpWindowSize = sftpWindowSize;
		return this;
	}

	/**
	 * Get the block size <strong>hint</strong> for SFTP. Use zero for no hint.
	 * 
	 * @return SFTP block size
	 */
	public long getSftpBlockSize() {
		return sftpBlockSize;
	}

	/**
	 * Set the window size <strong>hint</strong> for SFTP. Use zero for no hint.
	 * 
	 * @param sftpWindowSize SFTP window size
	 * @return this for chaining
	 */
	public SshConfiguration setSftpBlockSize(long sftpBlockSize) {
		this.sftpBlockSize = sftpBlockSize;
		return this;
	}

	/**
	 * Get the packet size <strong>hint</strong> for SFTP. Use zero for no hint.
	 * 
	 * @return SFTP packet size
	 */
	public long getSftpPacketSize() {
		return sftpPacketSize;
	}

	/**
	 * Set the packet size <strong>hint</strong> for SFTP. Use zero for no hint.
	 * 
	 * @param sftpPacketSize SFTP packet size
	 * @return this for chaining
	 */
	public SshConfiguration setSftpPacketSize(long sftpPacketSize) {
		this.sftpPacketSize = sftpPacketSize;
		return this;
	}

	/**
	 * Get the max window size <strong>hint</strong> for shell. Use zero for no
	 * hint.
	 * 
	 * @return shell max window size
	 */
	public long getShellWindowSizeMax() {
		return shellWindowSizeMax;
	}

	/**
	 * Set the window size max <strong>hint</strong> for shell. Use zero for no
	 * hint.
	 * 
	 * @param shellWindowSizeMax shell window size max
	 * @return this for chaining
	 */
	public SshConfiguration setShellWindowSizeMax(long shellWindowSizeMax) {
		this.sftpWindowSizeMax = shellWindowSizeMax;
		return this;
	}

	/**
	 * Get the window size <strong>hint</strong> for shell. Use zero for no hint.
	 * 
	 * @return shell window size
	 */
	public long getShellWindowSize() {
		return shellWindowSize;
	}

	/**
	 * Set the window size <strong>hint</strong> for SFTP. Use zero for no hint.
	 * 
	 * @param shellWindowSize shell window size
	 * @return this for chaining
	 */
	public SshConfiguration setShellWindowSize(long shellWindowSize) {
		this.shellWindowSize = shellWindowSize;
		return this;
	}

	/**
	 * Get the packet size <strong>hint</strong> for shell. Use zero for no hint.
	 * 
	 * @return Shell packet size
	 */
	public long getShellPacketSize() {
		return shellPacketSize;
	}

	/**
	 * Set the packet size <strong>hint</strong> for shells. Use zero for no hint.
	 * 
	 * @param shellPacketSize shell packet size
	 * @return this for chaining
	 */
	public SshConfiguration setShellPacketSize(long shellPacketSize) {
		this.shellPacketSize = shellPacketSize;
		return this;
	}

	/**
	 * Get the maximum number of authentication attempts when using the
	 * {@link SshClient#connect(String, String, int, SshAuthenticator...)} method
	 * with one or more authenticators. This does not impact
	 * {@link SshClient#authenticate(SshAuthenticator...)} which is there is you
	 * wish to perform authentication separately.
	 * 
	 * @return max auth attempts
	 */
	public int getMaxAuthAttempts() {
		return maxAuthAttempts;
	}

	/**
	 * Set the maximum number of authentication attempts when using the
	 * {@link SshClient#connect(String, String, int, SshAuthenticator...)} method
	 * with one or more authenticators. This does not impact
	 * {@link SshClient#authenticate(SshAuthenticator...)} which is there is you
	 * wish to perform authentication separately.
	 * 
	 * @param maxAuthAttempts max auth attempts
	 * @return this for chaining
	 */
	public SshConfiguration setMaxAuthAttempts(int maxAuthAttempts) {
		this.maxAuthAttempts = maxAuthAttempts;
		return this;
	}

	/**
	 * Add a capability we REQUIRE for this configuration
	 * 
	 * @param capability
	 * @return this for chaining
	 */
	public SshConfiguration addRequiredCapability(Capability capability) {
		requiredCapabilities.add(capability);
		return this;
	}

	/**
	 * Get the object that is used to validate host keys. When not set, all host
	 * keys will be allowed.
	 * 
	 * @return host key validator
	 */
	public SshHostKeyValidator getHostKeyValidator() {
		return hostKeyValidator;
	}

	/**
	 * Set the object that is used to validate host keys. When not set, all host
	 * keys will be allowed.
	 * 
	 * @param hostKeyValidator host key validator
	 * @return this for chaining
	 */
	public SshConfiguration setHostKeyValidator(SshHostKeyValidator hostKeyValidator) {
		this.hostKeyValidator = hostKeyValidator;
		return this;
	}

	/**
	 * Get the protocol version to accept. May be one {@link #SSH1_ONLY},
	 * {@link #SSH2_ONLY} or {link {@link #SSH1_OR_SSH2}.
	 * 
	 * @return protocol version.
	 */
	public int getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * Get the properties for this configuration. The property names supported will
	 * depend on the provider implementation they are passed to. See the
	 * documentation for the provider for details on what properties are supported.
	 * 
	 * @return properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Get the unix socket file to use for X11 forwarding. This file is a local Unix
	 * Socket, that provides access to the local X11 server. The provider must
	 * support {@link Capability#X11_FORWARDING_UNIX_SOCKET}.
	 * 
	 * @return X11 unix socket file.
	 */
	public File getX11UnixSocketFile() {
		return x11UnixSocketFile;
	}

	/**
	 * Set the host to use for X11 forwarding. This file is a local Unix Socket,
	 * that provides access to the local X11 server. The provider must support
	 * {@link Capability#X11_FORWARDING_UNIX_SOCKET}.
	 * 
	 * @param x11UnixSocketFile X11 host
	 * @return this for chaining
	 */
	public SshConfiguration setX11UnixSocketFile(File x11UnixSocketFile) {
		this.x11UnixSocketFile = x11UnixSocketFile;
		return this;
	}

	/**
	 * Get the host to use for X11 forwarding. This would usually be 'localhost' or
	 * the local hostname, and provides network access to the local X11 server. The
	 * provider must support {@link Capability#X11_FORWARDING_TCP}.
	 * 
	 * @return X11 host
	 */
	public String getX11Host() {
		return x11Host;
	}

	/**
	 * Set the host to use for X11 forwarding. This would usually be 'localhost' or
	 * the local hostname, and provides network access to the local X11 server. The
	 * provider must support {@link Capability#X11_FORWARDING} and
	 * {@link Capability#X11_FORWARDING_TCP}.
	 * 
	 * @param x11Host X11 host
	 * @return this for chaining
	 */
	public SshConfiguration setX11Host(String x11Host) {
		this.x11Host = x11Host;
		return this;
	}

	/**
	 * Get the screen number to use for X11 forwarding. This will be used to
	 * determining the port the local X11 server is listening on if TCP/IP X11
	 * forwarding is in use, or the name of the unix socket file if unix socket X11
	 * is in use. The provider must support {@link Capability#X11_FORWARDING} and
	 * either {@link Capability#X11_FORWARDING_TCP} or
	 * {@link Capability#X11_FORWARDING_UNIX_SOCKET}.
	 * 
	 * @return screen to use for X11 forwarding
	 */
	public int getX11Screen() {
		return x11Screen;
	}

	/**
	 * Set the screen number to use for X11 forwarding. This will be used to
	 * determining the port the local X11 server is listening on if TCP/IP X11
	 * forwarding is in use, or the name of the unix socket file if unix socket X11
	 * is in use. The provider must support {@link Capability#X11_FORWARDING} and
	 * either {@link Capability#X11_FORWARDING_TCP} or
	 * {@link Capability#X11_FORWARDING_UNIX_SOCKET}.
	 * 
	 * @param x11Screen screen to use for X11 forwarding
	 * @return this for chaining
	 */
	public SshConfiguration setX11Screen(int x11Screen) {
		this.x11Screen = x11Screen;
		return this;
	}

	/**
	 * Get whether or not X11 forwarding should only allow a single application to
	 * be forwarded. The provider must support {@link Capability#X11_FORWARDING} and
	 * either {@link Capability#X11_FORWARDING_TCP} or
	 * {@link Capability#X11_FORWARDING_UNIX_SOCKET}.
	 * 
	 * @return single X11 connection
	 */
	public boolean isX11SingleConnection() {
		return x11SingleConnection;
	}

	/**
	 * Set whether or not X11 forwarding should only allow a single application to
	 * be forwarded. The provider must support {@link Capability#X11_FORWARDING} and
	 * either {@link Capability#X11_FORWARDING_TCP} or
	 * {@link Capability#X11_FORWARDING_UNIX_SOCKET}.
	 * 
	 * @param x11SingleConnection single X11 connection
	 * @return this for chaining
	 */
	public SshConfiguration setX11SingleConnection(boolean x11SingleConnection) {
		this.x11SingleConnection = x11SingleConnection;
		return this;
	}

	/**
	 * Get the cookie to use for X11 forwarding. You may want to consider using
	 * the @{link {@link XDetails} helper instead of setting this directly. The
	 * provider must support {@link Capability#X11_FORWARDING} and either
	 * {@link Capability#X11_FORWARDING_TCP} or
	 * {@link Capability#X11_FORWARDING_UNIX_SOCKET}.
	 * 
	 * @return X11 cookie
	 */
	public byte[] getX11Cookie() {
		return x11Cookie;
	}

	/**
	 * Set the cookie to use for X11 forwarding. You may want to consider using
	 * the @{link {@link XDetails} helper instead of setting this directly. The
	 * provider must support {@link Capability#X11_FORWARDING} and either
	 * {@link Capability#X11_FORWARDING_TCP} or
	 * {@link Capability#X11_FORWARDING_UNIX_SOCKET}.
	 * 
	 * @param x11Cookie X11 cookie
	 * @return this for chaining
	 */
	public SshConfiguration setX11Cookie(byte[] x11Cookie) {
		this.x11Cookie = x11Cookie;
		return this;
	}

	/**
	 * Get the banner handler (called to display the login banner).
	 * 
	 * @return banner handler
	 */
	public SshBannerHandler getBannerHandler() {
		return bannerHandler;
	}

	/**
	 * Set the banner handler (called to display the login banner).
	 * 
	 * @param bannerHandler banner handler
	 * @return this for chaining
	 */
	public SshConfiguration setBannerHandler(SshBannerHandler bannerHandler) {
		this.bannerHandler = bannerHandler;
		return this;
	}

	/**
	 * Get the preferred server to client cipher.
	 * 
	 * @return preferred server to client cipher.
	 * @see SshProvider#getSupportedCiphers(int)
	 */
	public String getPreferredServerToClientCipher() {
		return preferredServerToClientCipher;
	}

	/**
	 * Set the preferred server to client cipher.
	 * 
	 * @param preferredServerToClientCipher preferred server to client cipher.
	 * @return this for chaining
	 * @see SshProvider#getSupportedCiphers(int)
	 */
	public SshConfiguration setPreferredServerToClientCipher(String preferredServerToClientCipher) {
		this.preferredServerToClientCipher = preferredServerToClientCipher;
		return this;
	}

	/**
	 * Get the preferred client to server cipher.
	 * 
	 * @return preferred client to server cipher.
	 * @see SshProvider#getSupportedCiphers(int)
	 */
	public String getPreferredClientToServerCipher() {
		return preferredClientToServerCipher;
	}

	/**
	 * Set the preferred client to server cipher.
	 * 
	 * @param preferredClientToServerCipher preferred client to server cipher.
	 * @return this for chaining
	 * @see SshProvider#getSupportedCiphers(int)
	 */
	public SshConfiguration setPreferredClientToServerCipher(String preferredClientToServerCipher) {
		this.preferredClientToServerCipher = preferredClientToServerCipher;
		return this;
	}

	/**
	 * Get the preferred server to client MAC.
	 * 
	 * @return preferred server to client MAC.
	 * @see SshProvider#getSupportedMAC()
	 */
	public String getPreferredServerToClientMAC() {
		return preferredServerToClientMAC;
	}

	/**
	 * Set the preferred server to client MAC.
	 * 
	 * @param preferredServerToClientMAC preferred server to client MAC.
	 * @return this for chaining
	 * @see SshProvider#getSupportedMAC()
	 */
	public SshConfiguration setPreferredServerToClientMAC(String preferredServerToClientMAC) {
		this.preferredServerToClientMAC = preferredServerToClientMAC;
		return this;
	}

	/**
	 * Get the preferred client to server MAC.
	 * 
	 * @return preferred client to server MAC.
	 * @see SshProvider#getSupportedMAC()
	 */
	public String getPreferredClientToServerMAC() {
		return preferredClientToServerMAC;
	}

	/**
	 * Set the preferred client to server MAC.
	 * 
	 * @param preferredClientToServerMAC preferred client to server MAC.
	 * @return this for chaining
	 * @see SshProvider#getSupportedMAC()
	 */
	public SshConfiguration setPreferredClientToServerMAC(String preferredClientToServerMAC) {
		this.preferredClientToServerMAC = preferredClientToServerMAC;
		return this;
	}

	/**
	 * Get the preferred server to client compression.
	 * 
	 * @return preferred server to client compression.
	 * @see SshProvider#getSupportedCompression()
	 */
	public String getPreferredServerToClientCompression() {
		return preferredServerToClientCompression;
	}

	/**
	 * Set the preferred server to client compression.
	 * 
	 * @param preferredServerToClientCompression preferred server to client
	 *                                           compression.
	 * @return this for chaining
	 * @see SshProvider#getSupportedCompression()
	 */
	public SshConfiguration setPreferredServerToClientCompression(String preferredServerToClientCompression) {
		this.preferredServerToClientCompression = preferredServerToClientCompression;
		return this;
	}

	/**
	 * Get the preferred client to server compression.
	 * 
	 * @return preferred client to server compression.
	 * @see SshProvider#getSupportedCompression()
	 */
	public String getPreferredClientToServerCompression() {
		return preferredClientToServerCompression;
	}

	/**
	 * Set the preferred client to server compression.
	 * 
	 * @param preferredClientToServerCompression preferred client to server
	 *                                           compression.
	 * @return this for chaining
	 * @see SshProvider#getSupportedCompression()
	 */
	public SshConfiguration setPreferredClientToServerCompression(String preferredClientToServerCompression) {
		this.preferredClientToServerCompression = preferredClientToServerCompression;
		return this;
	}

	/**
	 * Set the preferred protocol version. Use one of
	 * {@link SshConfiguration#SSH1_ONLY}, {@link SshConfiguration#SSH1_OR_SSH2} or
	 * {@link SshConfiguration#SSH2_ONLY}.
	 * 
	 * @param protocolVersion protocol version
	 * @return this for chaining
	 */
	public SshConfiguration setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
		return this;
	}

	/**
	 * Get the preferred server to client key exchange.
	 * 
	 * @return preferred server to client key exchange.
	 * @see SshProvider#getSupportedKeyExchange()
	 */
	public String getPreferredKeyExchange() {
		return preferredKeyExchange;
	}

	/**
	 * Set the preferred key exchange.
	 * 
	 * @param preferredKeyExchange preferred key exchange.
	 * @return this for chaining
	 * @see SshProvider#getSupportedKeyExchange()
	 */
	public SshConfiguration setPreferredKeyExchange(String preferredKeyExchange) {
		this.preferredKeyExchange = preferredKeyExchange;
		return this;
	}

	/**
	 * Get the preferred public key algorithm.
	 * 
	 * @return public key algorithm.
	 * @see SshProvider#getSupportedPublicKey()
	 */
	public String getPreferredPublicKey() {
		return preferredPublicKey;
	}

	/**
	 * Set the preferred public key algorithm.
	 * 
	 * @param preferredPublicKey public key algorithm.
	 * @return this for chaining
	 * @see SshProvider#getSupportedPublicKey()
	 */
	public SshConfiguration setPreferredPublicKey(String preferredPublicKey) {
		this.preferredPublicKey = preferredPublicKey;
		return this;
	}

	/**
	 * Get the fingerprint hashing algorithm to use.
	 * 
	 * @return fingerprint hashing algorithm
	 */
	public String getFingerprintHashingAlgorithm() {
		return fingerprintHashingAlgorithm;
	}

	/**
	 * Set the fingerprint hashing algorithm to use.
	 * 
	 * @param fingerprintHashingAlgorithm fingerprint hashing algorithm
	 * @return this for chaining
	 */
	public SshConfiguration setFingerprintHashingAlgorithm(String fingerprintHashingAlgorithm) {
		this.fingerprintHashingAlgorithm = fingerprintHashingAlgorithm;
		return this;
	}

	/**
	 * Get the proxy server details.
	 * 
	 * @return proxy server details
	 */
	public SshProxyServerDetails getProxyServer() {
		return proxyServer;
	}

	/**
	 * Set the proxy server details.
	 * 
	 * @param proxyServer proxy server details
	 * @return this for chaining
	 */
	public SshConfiguration setProxyServer(SshProxyServerDetails proxyServer) {
		this.proxyServer = proxyServer;
		return this;
	}

	/**
	 * Set the preferred SSH1 cipher. Will be only of
	 * {@link SshConfiguration.CIPHER_3DES} or {@link SshConfiguration#CIPHER_DES}
	 * as these are the only two supported by SSH1.
	 * 
	 * @param preferredSSH1Cipher preferred SSH1 cipher
	 * @return this for chaining
	 */
	public SshConfiguration setPreferredSSH1CipherType(String preferredSSH1Cipher) {
		this.preferredSSH1Cipher = preferredSSH1Cipher;
		return this;
	}

	/**
	 * Get the preferred SSH1 cipher. Will be only of
	 * {@link SshConfiguration.CIPHER_3DES} or {@link SshConfiguration#CIPHER_DES}
	 * as these are the only two supported by SSH1.
	 * 
	 * @return preferred SSH1 cipger
	 */
	public String getPreferredSSH1CipherType() {
		return preferredSSH1Cipher;
	}

	/**
	 * Set the path of the SFTP server executable (SSH1 only).
	 * 
	 * @param sftpSSH1Path SSH1 SFTP server executable path
	 * @return this for chaining
	 */
	public SshConfiguration setSftpSSH1Path(String sftpSSH1Path) {
		this.sftpSSH1Path = sftpSSH1Path;
		return this;
	}

	/**
	 * Get the path of the SFTP server executable (SSH1 only).
	 * 
	 * @return SSH1 SFTP server executable path
	 */
	public String getSftpSSH1Path() {
		return sftpSSH1Path;
	}

	/**
	 * Test if a provider has all the capbilities required by this configuration.
	 * 
	 * @param provider provider
	 * @throws UnsupportedOperationException if a provider does not have all the
	 *                                       required capabilities
	 */
	public void providerHasCapabilities(SshProvider provider) throws UnsupportedOperationException {
		for (Iterator<Capability> i = requiredCapabilities.iterator(); i.hasNext();) {
			Capability c = (Capability) i.next();
			if (!provider.getCapabilities().contains(c)) {
				throw new UnsupportedOperationException(
						"Capability " + c + " is required, but not supported by the provider " + provider.getName());
			}
		}
	}

	/**
	 * Get the socket factory to use to make outgoing connections to SSH servers.
	 * Will be set to <code>null</code> when the default Java socket factory is to
	 * be used.
	 * 
	 * @return socket factory
	 */
	public SocketFactory getSocketFactory() {
		return socketFactory;
	}

	/**
	 * Set the socket factory to use to make outgoing connections to SSH servers.
	 * Set to <code>null</code> to use the default Java socket factory.
	 * 
	 * @param socketFactory socket factory
	 * @return this for chaining
	 */
	public SshConfiguration setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
		return this;
	}

	/**
	 * Get the size of the buffer to use internally when transferring files or
	 * joining streams.
	 * 
	 * @return file transfer buffer size
	 */
	public int getStreamBufferSize() {
		return streamBufferSize;
	}

	/**
	 * Get the size of the buffer to use internally when transferring files or
	 * joining streams.
	 * 
	 * @param streamBufferSize file transfer buffer size
	 * @return this for chaining
	 */
	public SshConfiguration setStreamBufferSize(int streamBufferSize) {
		this.streamBufferSize = streamBufferSize;
		return this;
	}

	/**
	 * Utility to create a client that may be used with this configuration. It uses
	 * the DefaultProviderFactory, so if you want to custom how providers are
	 * selected, do not use this method.
	 * 
	 * @return client
	 */
	public SshClient createClient() {
		// Create the client using that configuration
		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(this);
		return provider.createClient(this);
	}

	/**
	 * Utility to create a client that may be used with this configuration and
	 * connect to a host as a particular user, optionally authenticating. It uses
	 * the DefaultProviderFactory, so if you want to custom how providers are
	 * selected, do not use this method.
	 * 
	 * @param spec           connection spec in the format
	 *                       username[:password]@hostname[:port]
	 * @param authenticators authenticators
	 * @return client client
	 * @throws SshException on error
	 */
	public SshClient open(String spec, SshAuthenticator... authenticators) throws SshException {
		return DefaultProviderFactory.getInstance().getProvider(this).open(this, Util.extractUsername(spec),
				Util.extractHostname(spec), Util.extractPort(spec), authenticators);
	}

	/**
	 * Utility to create a client that may be used with this configuration and
	 * connect to a host as a particular user, optionally authenticating, but do not
	 * block. Instead a future is returned allowing monitoring of state. . It uses
	 * the DefaultProviderFactory, so if you want to custom how providers are
	 * selected, do not use this method.
	 * 
	 * @param spec           connection spec in the format
	 *                       username[:password]@hostname[:port]
	 * @param authenticators authenticators
	 * @return future
	 */
	public Future<SshClient> openLater(String spec, SshAuthenticator... authenticators) {
		return openLater(Util.extractUsername(spec), Util.extractHostname(spec), Util.extractPort(spec),
				authenticators);
	}

	/**
	 * Utility to create a client that may be used with this configuration and
	 * connect to a host as a particular user, optionally authenticating. It uses
	 * the DefaultProviderFactory, so if you want to custom how providers are
	 * selected, do not use this method.
	 * 
	 * @param username       user name
	 * @param hostname       hostname
	 * @param port           port
	 * @param authenticators authenticators
	 * @return client client
	 * @throws SshException on error
	 */
	public SshClient open(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws SshException {
		return DefaultProviderFactory.getInstance().getProvider(this).open(this, username, hostname, port,
				authenticators);
	}

	/**
	 * Utility to create a client that may be used with this configuration and
	 * connect to a host as a particular user, optionally authenticating, but do not
	 * block. Instead a future is returned allowing monitoring of state. It uses the
	 * DefaultProviderFactory, so if you want to custom how providers are selected,
	 * do not use this method.
	 * 
	 * @param username       user name
	 * @param hostname       hostname
	 * @param port           port
	 * @param authenticators authenticators
	 * @return future
	 */
	public Future<SshClient> openLater(String username, String hostname, int port, SshAuthenticator... authenticators) {
		return DefaultProviderFactory.getInstance().getProvider(this).openLater(this, username, hostname, port,
				authenticators);
	}
}
