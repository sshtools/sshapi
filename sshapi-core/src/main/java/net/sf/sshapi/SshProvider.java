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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import net.sf.sshapi.agent.SshAgent;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.identity.SshIdentityManager;

/**
 * Every SSH API implementation must provide one implementation of this
 * interface. It is responsible for providing information about capabilities and
 * creating {@link SshClient} instances.
 */
public interface SshProvider {

	/**
	 * Get the name of the library that is used for this provider.
	 * 
	 * @return provider name
	 */
	String getName();

	/**
	 * Get the version of the library that is used for this provider.
	 * 
	 * @return provider version
	 */
	String getVersion();

	/**
	 * Get the vendor of the library that is used for this provider.
	 * 
	 * @return provider vendor
	 */
	String getVendor();

	/**
	 * Create a {@link SshHostKeyManager} that may be used to list, add and remove
	 * valid host keys. An implementation is not required to provide this
	 * functionality, but vendors do, so this interface provides a common way to
	 * access them.
	 * 
	 * @param configuration
	 *            configuration
	 * @return host key manager
	 * @throws SshException
	 * @throws UnsupportedOperationException
	 *             if not supported
	 */
	SshHostKeyManager createHostKeyManager(SshConfiguration configuration) throws SshException;

	/**
	 * Create an @{@link SshIdentityManager} (if supported)
	 * 
	 * @param configuration
	 *            configuration
	 * @return identity manager
	 * @throws UnsupportedOperationException
	 *             if not supported
	 */
	SshIdentityManager createIdentityManager(SshConfiguration configuration);

	/**
	 * Get a list of supported ciphers. Each element will be a {@link String} of the
	 * cipher name. If {@link SshConfiguration#SSH1_OR_SSH2} is specified, the list
	 * will contain cipher names for both version (in practice, SSH1 only supported
	 * one cipher).
	 * <p>
	 * If the provider does not support SSH1, it should throw an
	 * {@link UnsupportedOperationException} if {@link SshConfiguration#SSH1_ONLY}
	 * is requested. The same applies for SSH2
	 * 
	 * @param protocolVersion
	 *            version of protocol
	 * 
	 * @return supported ciphers
	 * @throws UnsupportedOperationException
	 *             if protocol version not supported
	 */
	List<String> getSupportedCiphers(int protocolVersion);

	/**
	 * Get a list of supported MAC types. Each element will be a {@link String} of
	 * the MAC type name.
	 * <p>
	 * NOTE: Only applicable to SSH2
	 * 
	 * @return supported MAC types
	 * @throws UnsupportedOperationException
	 *             if SSH2 not supported
	 */
	List<String> getSupportedMAC();

	/**
	 * Get a list of supported compression types. Each element will be a
	 * {@link String} of the compression type name.
	 * <p>
	 * NOTE: Only applicable to SSH2
	 * 
	 * @return supported compression type names
	 * @throws UnsupportedOperationException
	 *             if SSH2 not supported
	 */
	List<String> getSupportedCompression();

	/**
	 * Get a list of supported fingerprint algorithms. Each element will be a
	 * {@link String} of the algorithm name. This affects what is returned by 
	 * {@link SshPublicKey#getFingerprint()} and {@link SshHostKey#getFingerprint()} .
	 * <p>
	 * NOTE: Only applicable to SSH2
	 * 
	 * @return supported fingerprint hashing algorithms
	 * @throws UnsupportedOperationException
	 *             if SSH2 not supported
	 */
	List<String> getFingerprintHashingAlgorithms();

	/**
	 * Get a list of supported key exchange algorithms. Each element will be a
	 * {@link String} of the key exchange name.
	 * <p>
	 * NOTE: Only applicable to SSH2
	 * 
	 * @return supported key exchange names
	 * @throws UnsupportedOperationException
	 *             if SSH2 not supported
	 */
	List<String> getSupportedKeyExchange();

	/**
	 * Get a list of supported public key algorithms. Each element will be a
	 * {@link String} of the public key name.
	 * <p>
	 * NOTE: Only applicable to SSH2
	 * 
	 * @return supported public key names
	 * @throws UnsupportedOperationException
	 *             if SSH2 not supported
	 */
	List<String> getSupportedPublicKey();

	/**
	 * Get a list of the capabilities of this implementation ({@link Capability}
	 * objects)
	 * 
	 * @return list of capabilities
	 * @see Capability
	 */
	List<Capability> getCapabilities();

	/**
	 * Examine the configuration to see if this provider supports it.
	 * 
	 * @param configuration
	 *            configuration
	 * @return configuration is supported
	 */
	boolean supportsConfiguration(SshConfiguration configuration);

	/**
	 * Create a new client instance with the specified configuration.
	 * <p>
	 * IMPLEMENTATION NOTE: The provider implementation is expected to invoke
	 * {@link SshClient#init(SshProvider)} after construction.
	 * 
	 * @param configuration
	 *            configuration
	 * @return client
	 * @throws UnsupportedOperationException
	 *             if the provider configuration is not valid
	 */
	SshClient createClient(SshConfiguration configuration);

	/**
	 * Create a new client instance with the specified configuration and connect and
	 * authenticate it.
	 * <p>
	 * IMPLEMENTATION NOTE: The provider implementation is expected to invoke
	 * {@link SshClient#init(SshProvider)} after construction.
	 * 
	 * @param configuration
	 *            configuration
	 * @param username
	 *            user name
	 * @param hostname
	 *            hostname
	 * @param port
	 *            port
	 * @param authenticators
	 *            authenticators
	 * @return client
	 * @throws SshException
	 *             on error
	 * @throws UnsupportedOperationException
	 *             if the provider configuration is not valid
	 */
	SshClient open(SshConfiguration configuration, String username, String hostname, int port,
			SshAuthenticator... authenticators) throws SshException;

	/**
	 * Create a new client instance with the specified configuration and connect and
	 * authenticate it.
	 * <p>
	 * IMPLEMENTATION NOTE: The provider implementation is expected to invoke
	 * {@link SshClient#init(SshProvider)} after construction.
	 * 
	 * @param configuration
	 *            configuration
	 * @param spec
	 *            connection spec in the format username[:password]@host[:port]
	 * @param authenticators
	 *            authenticators
	 * @return client
	 * @throws SshException
	 *             on error
	 * @throws UnsupportedOperationException
	 *             if the provider configuration is not valid
	 */
	SshClient open(SshConfiguration configuration, String spec,
			SshAuthenticator... authenticators) throws SshException;

	/**
	 * Create a new client instance with the specified configuration and connect and
	 * authenticate it, but do not block. Instead, a {@link SshFuture} will be returned
	 * allowing monitoring of the state.
	 * <p>
	 * IMPLEMENTATION NOTE: The provider implementation is expected to invoke
	 * {@link SshClient#init(SshProvider)} after construction.
	 * 
	 * @param configuration
	 *            configuration
	 * @param username
	 *            user name
	 * @param hostname
	 *            hostname
	 * @param port
	 *            port
	 * @param authenticators
	 *            authenticators
	 * @return future
	 * @throws UnsupportedOperationException
	 *             if the provider configuration is not valid
	 */
	Future<SshClient> openLater(SshConfiguration configuration, String username, String hostname, int port,
			SshAuthenticator... authenticators);

	/**
	 * Create a new client instance with the specified configuration and connect and
	 * authenticate it, but do not block. Instead, a {@link SshFuture} will be returned
	 * allowing monitoring of the state.
	 * <p>
	 * IMPLEMENTATION NOTE: The provider implementation is expected to invoke
	 * {@link SshClient#init(SshProvider)} after construction.
	 * 
	 * @param configuration
	 *            configuration
	 * @param spec
	 *            connection spec in the format username[:password]@host[:port]
	 * @param authenticators
	 *            authenticators
	 * @return future
	 * @throws UnsupportedOperationException
	 *             if the provider configuration is not valid
	 */
	Future<SshClient> openLater(SshConfiguration configuration, String spec,
			SshAuthenticator... authenticators);

	/**
	 * Create a connection to the local agent using default or auto-detected
	 * settings.
	 * <p>
	 * 
	 * @param application
	 *            the application connecting
	 * @return connected agent client
	 * @throws SshException
	 *             if the provider configuration is not valid
	 */
	SshAgent connectToLocalAgent(String application) throws SshException;

	/**
	 * Create a connection to the local agent using default or auto-detected
	 * settings.
	 * <p>
	 * 
	 * @param application
	 *            the application connecting
	 * @param protocol
	 *            protocol. One of {@link SshAgent#AUTO_PROTOCOL},
	 *            {@link SshAgent#OPENSSH_PROTOCOL} or
	 *            {@link SshAgent#RFC_PROTOCOL}.
	 * @return connected agent client
	 * @throws SshException
	 *             if the provider configuration is not valid
	 */
	SshAgent connectToLocalAgent(String application, int protocol) throws SshException;

	/**
	 * Create a connection to the local agent.
	 * <p>
	 * 
	 * @param application
	 *            the application connecting
	 * @param location
	 *            the location of the agent, in the form "localhost:port"
	 * @param socketType
	 *            the type of socket. One of
	 *            {@link SshAgent#TCPIP_AGENT_SOCKET_TYPE} or
	 *            {@link SshAgent#UNIX_DOMAIN_AGENT_SOCKET_TYPE}.
	 * @param protocol
	 *            protocol. One of {@link SshAgent#AUTO_PROTOCOL},
	 *            {@link SshAgent#OPENSSH_PROTOCOL} or
	 *            {@link SshAgent#RFC_PROTOCOL}.
	 * @return connected agent client
	 * @throws SshException
	 *             if the provider configuration is not valid
	 */
	SshAgent connectToLocalAgent(String application, String location, int socketType, int protocol) throws SshException;

	/**
	 * Seed the random number generator.
	 * 
	 * @param seed
	 *            seed
	 */
	void seed(long seed);
	
	/**
	 * Get an {@link ExecutorService} that may be used to place background tasks onto. The
	 * provider is responsible for configuring this with enough threads for it's operation.
	 * 
	 * @return executor service
	 */
	ExecutorService getExecutor();
}
