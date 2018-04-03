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

import java.util.List;

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
	 * Create a {@link SshHostKeyManager} that may be used to list, add and
	 * remove valid host keys. An implementation is not required to provide this
	 * functionality, but vendors do, so this interface provides a common way to
	 * access them.
	 * 
	 * @param configuration configuration
	 * @return host key manager
	 * @throws SshException
	 * @throws UnsupportedOperationException if not supported
	 */
	SshHostKeyManager createHostKeyManager(SshConfiguration configuration) throws SshException;

	/**
	 * Create an @{@link SshIdentityManager} (if supported)
	 * 
	 * @param configuration configuration
	 * @return identity manager
	 * @throws UnsupportedOperationException if not supported
	 */
	SshIdentityManager createIdentityManager(SshConfiguration configuration);

	/**
	 * Get a list of supported ciphers. Each element will be a {@link String} of
	 * the cipher name. If {@link SshConfiguration#SSH1_OR_SSH2} is specified,
	 * the list will contain cipher names for both version (in practice, SSH1
	 * only supported one cipher).
	 * <p>
	 * If the provider does not support SSH1, it should throw an
	 * {@link UnsupportedOperationException} if
	 * {@link SshConfiguration#SSH1_ONLY} is requested. The same applies for
	 * SSH2
	 * 
	 * @param protocolVersion version of protocol
	 * 
	 * @return supported ciphers
	 * @throws UnsupportedOperationException if protocol version not supported
	 */
	List getSupportedCiphers(int protocolVersion);

	/**
	 * Get a list of supported MAC types. Each element will be a {@link String}
	 * of the MAC type name.
	 * <p>
	 * NOTE: Only applicable to SSH2
	 * 
	 * @return supported MAC types
	 * @throws UnsupportedOperationException if SSH2 not supported
	 */
	List getSupportedMAC();

	/**
	 * Get a list of supported compression types. Each element will be a
	 * {@link String} of the compression type name.
	 * <p>
	 * NOTE: Only applicable to SSH2
	 * 
	 * @return supported compression type names
	 * @throws UnsupportedOperationException if SSH2 not supported
	 */
	List getSupportedCompression();

	/**
	 * Get a list of supported key exchange algorithms. Each element will be a
	 * {@link String} of the key exchange name.
	 * <p>
	 * NOTE: Only applicable to SSH2
	 * 
	 * @return supported key exchange names
	 * @throws UnsupportedOperationException if SSH2 not supported
	 */
	List getSupportedKeyExchange();

	/**
	 * Get a list of supported public key algorithms. Each element will be a
	 * {@link String} of the public key name.
	 * <p>
	 * NOTE: Only applicable to SSH2
	 * 
	 * @return supported public key names
	 * @throws UnsupportedOperationException if SSH2 not supported
	 */
	List getSupportedPublicKey();

	/**
	 * Get a list of the capabilities of this implementation ({@link Capability}
	 * objects)
	 * 
	 * @return list of capabilities
	 * @see Capability
	 */
	List getCapabilities();

	/**
	 * Examine the configuration to see if this provider supports it.
	 * 
	 * @param configuration configuration
	 * @return configuration is supported
	 */
	boolean supportsConfiguration(SshConfiguration configuration);

	/**
	 * Create a new client instance with the specified configuration.
	 * <p>
	 * IMPLEMENTATION NOTE: The provider implementation is expected to invoke
	 * {@link SshClient#init(SshProvider)} after construction.
	 * 
	 * @param configuration configuration
	 * @return client
	 * @throws UnsupportedOperationException if the provider configuration is
	 *             not valid
	 */
	SshClient createClient(SshConfiguration configuration);

	/**
	 * Seed the random number generator.
	 * 
	 * @param seed seed
	 */
	void seed(long seed);
}
