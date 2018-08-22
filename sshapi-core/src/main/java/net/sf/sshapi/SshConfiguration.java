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
package net.sf.sshapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.net.SocketFactory;

import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.util.ConsoleLogger;
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

	/** SSH1 RSA Public Key **/
	public static final String PUBLIC_KEY_SSHRSA1 = "rsa1";

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

	// Private statics
	private static Logger logger = new ConsoleLogger();

	// Private instance variables
	private int protocolVersion = SSH1_OR_SSH2;
	private final Properties properties;
	private SshHostKeyValidator hostKeyValidator;
	private String x11Host;
	private int x11Port;
	private byte[] x11Cookie;
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
	private List requiredCapabilities = new ArrayList();
	private SocketFactory socketFactory;
	private int maxAuthAttempts = 3;

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
	 * Constructor
	 */
	public SshConfiguration() {
		this(new Properties());
	}

	/**
	 * Set the logger to use
	 * 
	 * @param logger
	 *            logger
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
	 * Constructor
	 * 
	 * @param hostKeyValidator
	 *            host key validator
	 */
	public SshConfiguration(SshHostKeyValidator hostKeyValidator) {
		this(new Properties(), hostKeyValidator);
	}

	/**
	 * Constructor
	 * 
	 * @param properties
	 *            properties
	 */
	public SshConfiguration(Properties properties) {
		this(properties, null);
	}

	/**
	 * Constructor
	 * 
	 * @param properties
	 *            properties
	 * @param hostKeyValidator
	 *            host key validator
	 */
	public SshConfiguration(Properties properties, SshHostKeyValidator hostKeyValidator) {
		this.properties = properties;
		this.hostKeyValidator = hostKeyValidator;
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
	 * @param maxAuthAttempts
	 *            max auth attempts
	 */
	public void setMaxAuthAttempts(int maxAuthAttempts) {
		this.maxAuthAttempts = maxAuthAttempts;
	}

	/**
	 * Add a capability we REQUIRE for this configuration
	 * 
	 * @param capability
	 */
	public void addRequiredCapability(Capability capability) {
		requiredCapabilities.add(capability);
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
	 * @param hostKeyValidator
	 *            host key validator
	 */
	public void setHostKeyValidator(SshHostKeyValidator hostKeyValidator) {
		this.hostKeyValidator = hostKeyValidator;
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
	 * Get the host to use for X11 forwarding.
	 * 
	 * @return X11 host
	 */
	public String getX11Host() {
		return x11Host;
	}

	/**
	 * Set the host to use for X11 forwarding.
	 * 
	 * @param x11Host
	 *            X11 host
	 */
	public void setX11Host(String x11Host) {
		this.x11Host = x11Host;
	}

	/**
	 * Get the port to use for X11 forwarding.
	 * 
	 * @return port to use for X11 forwarding
	 */
	public int getX11Port() {
		return x11Port;
	}

	/**
	 * Set the port to use for X11 forwarding.
	 * 
	 * @param x11Port
	 *            port to use for X11 forwarding
	 */
	public void setX11Port(int x11Port) {
		this.x11Port = x11Port;
	}

	/**
	 * Get the cookie to use for X11 forwarding. You may want to consider using
	 * the @{link {@link XDetails} helper instead of setting this directly.
	 * 
	 * @return X11 cookie
	 */
	public byte[] getX11Cookie() {
		return x11Cookie;
	}

	/**
	 * Set the cookie to use for X11 forwarding. You may want to consider using
	 * the @{link {@link XDetails} helper instead of setting this directly.
	 * 
	 * @param x11Cookie
	 *            X11 cookie
	 */
	public void setX11Cookie(byte[] x11Cookie) {
		this.x11Cookie = x11Cookie;
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
	 * @param bannerHandler
	 *            banner handler
	 */
	public void setBannerHandler(SshBannerHandler bannerHandler) {
		this.bannerHandler = bannerHandler;
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
	 * @param preferredServerToClientCipher
	 *            preferred server to client cipher.
	 * @see SshProvider#getSupportedCiphers(int)
	 */
	public void setPreferredServerToClientCipher(String preferredServerToClientCipher) {
		this.preferredServerToClientCipher = preferredServerToClientCipher;
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
	 * @param preferredClientToServerCipher
	 *            preferred client to server cipher.
	 * @see SshProvider#getSupportedCiphers(int)
	 */
	public void setPreferredClientToServerCipher(String preferredClientToServerCipher) {
		this.preferredClientToServerCipher = preferredClientToServerCipher;
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
	 * @param preferredServerToClientMAC
	 *            preferred server to client MAC.
	 * @see SshProvider#getSupportedMAC()
	 */
	public void setPreferredServerToClientMAC(String preferredServerToClientMAC) {
		this.preferredServerToClientMAC = preferredServerToClientMAC;
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
	 * @param preferredClientToServerMAC
	 *            preferred client to server MAC.
	 * @see SshProvider#getSupportedMAC()
	 */
	public void setPreferredClientToServerMAC(String preferredClientToServerMAC) {
		this.preferredClientToServerMAC = preferredClientToServerMAC;
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
	 * @param preferredServerToClientCompression
	 *            preferred server to client compression.
	 * @see SshProvider#getSupportedCompression()
	 */
	public void setPreferredServerToClientCompression(String preferredServerToClientCompression) {
		this.preferredServerToClientCompression = preferredServerToClientCompression;
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
	 * @param preferredClientToServerCompression
	 *            preferred client to server compression.
	 * @see SshProvider#getSupportedCompression()
	 */
	public void setPreferredClientToServerCompression(String preferredClientToServerCompression) {
		this.preferredClientToServerCompression = preferredClientToServerCompression;
	}

	/**
	 * Set the preferred protocol version. Use one of
	 * {@link SshConfiguration#SSH1_ONLY}, {@link SshConfiguration#SSH1_OR_SSH2} or
	 * {@link SshConfiguration#SSH2_ONLY}.
	 * 
	 * @param protocolVersion
	 *            protocol version
	 */
	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
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
	 * @param preferredKeyExchange
	 *            preferred key exchange.
	 * @see SshProvider#getSupportedKeyExchange()
	 */
	public void setPreferredKeyExchange(String preferredKeyExchange) {
		this.preferredKeyExchange = preferredKeyExchange;
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
	 * @param preferredPublicKey
	 *            public key algorithm.
	 * @see SshProvider#getSupportedPublicKey()
	 */
	public void setPreferredPublicKey(String preferredPublicKey) {
		this.preferredPublicKey = preferredPublicKey;
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
	 * @param proxyServer
	 *            proxy server details
	 */
	public void setProxyServer(SshProxyServerDetails proxyServer) {
		this.proxyServer = proxyServer;
	}

	/**
	 * Set the preferred SSH1 cipher. Will be only of
	 * {@link SshConfiguration.CIPHER_3DES} or {@link SshConfiguration#CIPHER_DES}
	 * as these are the only two supported by SSH1.
	 * 
	 * @param preferredSSH1Cipher
	 *            preferred SSH1 cipger
	 */
	public void setPreferredSSH1CipherType(String preferredSSH1Cipher) {
		this.preferredSSH1Cipher = preferredSSH1Cipher;
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
	 * @param sftpSSH1Path
	 *            SSH1 SFTP server executable path
	 */
	public void setSftpSSH1Path(String sftpSSH1Path) {
		this.sftpSSH1Path = sftpSSH1Path;
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
	 * @param provider
	 *            provider
	 * @throws UnsupportedOperationException
	 *             if a provider does not have all the required capabilities
	 */
	public void providerHasCapabilities(SshProvider provider) throws UnsupportedOperationException {
		for (Iterator i = requiredCapabilities.iterator(); i.hasNext();) {
			Capability c = (Capability) i.next();
			if (!provider.getCapabilities().contains(c)) {
				throw new UnsupportedOperationException(
						"Capability " + c + " is required, but not supported by this provider.");
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
	 * @param socketFactory
	 *            socket factory
	 */
	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
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
	 * @param username
	 *            user name
	 * @param hostname
	 *            hostname
	 * @param port
	 *            port
	 * @param authenticators
	 *            authenticators
	 * @return client client
	 * @throws SshException
	 *             on error
	 */
	public SshClient open(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws SshException {
		// Create the client using that configuration
		if (authenticators.length < 1)
			throw new IllegalArgumentException("At least one authenticator must be provided.");
		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(this);
		SshClient client = provider.createClient(this);
		client.connect(username, hostname, port, authenticators);
		return client;

	}

}
