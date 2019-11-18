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
package net.sf.sshapi.identity;

import java.io.InputStream;
import java.util.List;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey;
import net.sf.sshapi.SshPrivateKey.Algorithm;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshPublicKey;

/**
 * Identity manager implementations are responsible for generating and parsing
 * various different formats of Public and Private keys.
 * <p>
 * To be able to store a {@link SshPublicKey} or {@link SshPrivateKey}, they
 * must first be turned into {@link SshPublicKeyFile} or
 * {@link SshPrivateKeyFile} objects respectively.
 * <p>
 * From these, a byte array that is formatted in the appropriate way can then be
 * obtained and persisted however you wish.
 */
public interface SshIdentityManager {

	/**
	 * Parse a stream that provides a public key in some supported format. The
	 * implementation will detect the format and construct the key object
	 * appropriately. The returned object is a {@link SshPublicKeyFile} so may
	 * then be persisted elsewhere if you wish, or you can just obtain the
	 * {@link SshPublicKey}.
	 * 
	 * @param in stream to read key from
	 * @return public key file
	 * @throws SshException if the raw data cannot be parsed into a public key
	 */
	SshPublicKeyFile createPublicKeyFromStream(InputStream in) throws SshException;

	/**
	 * Parse a stream that provides a private key in some supported format. The
	 * implementation will detect the format and construct the key object
	 * appropriately. The returned object is a {@link SshPrivateKeyFile} so may
	 * then be persisted elsewhere if you wish, or you can just obtain the
	 * {@link SshPrivateKeyFile}. If the stream is a PKCS12 keystore, then it must
	 * not be encrypted with a passphrase, and if the keys themselves are encrypted, the {@link SshPrivateKeyFile}
	 * returned will still be encrypted (see {@link SshPrivateKeyFile#isEncrypted()} and {@link SshPrivateKeyFile#decrypt(char[])}.
	 * 
	 * @param in stream to read key from
	 * @return private key file
	 * @throws SshException if the raw data cannot be parsed into a private key
	 * @see #createPrivateKeyFromStream(InputStream, char[])
	 */
	SshPrivateKeyFile createPrivateKeyFromStream(InputStream in) throws SshException;

	/**
	 * Parse a stream that provides a private key in some supported format. The
	 * implementation will detect the format and construct the key object
	 * appropriately. The returned object is a {@link SshPrivateKeyFile} so may
	 * then be persisted elsewhere if you wish, or you can just obtain the
	 * {@link SshPrivateKeyFile}. If the stream is a keystore, then the passphrase will
	 * be that for the keystore, and if the keys themselves are encrypted, the {@link SshPrivateKeyFile}
	 * returned will still be encrypted (see {@link SshPrivateKeyFile#isEncrypted()} and {@link SshPrivateKeyFile#decrypt(char[])}.
	 * If passphrase is provided for a standard private key, then it will be used to decrypt that key. 
	 * 
	 * @param in stream to read key from
	 * @param passphrase passphrase of either private key, or the stream itself in the case of a PKCS12 keytore. 
	 * @return private key file
	 * @throws SshException if the raw data cannot be parsed into a private key
	 * @see #createPrivateKeyFromStream(InputStream)
	 */
	SshPrivateKeyFile createPrivateKeyFromStream(InputStream in, char[] passphrase) throws SshException;

	/**
	 * Generate a new key pair. Type must be one of the supported types, and key
	 * bits must be a supported key length. It is recommended you seed the
	 * random number generate before calling this method using
	 * {@link SshProvider#seed(long)}.
	 * 
	 * @param keyType key type.
	 * @param keyBits key length
	 * @return key pair
	 * @throws SshException if pair cannot be generated.
	 */
	SshKeyPair generateKeyPair(Algorithm keyType, int keyBits) throws SshException;

	/**
	 * Create a {@link SshPrivateKeyFile} that will allow you obtain a formatted
	 * version of a {@link SshPrivateKey} for storage. The public key is
	 * required for this, so {@link SshKeyPair} containing the private key is
	 * required. See {@link SshPrivateKeyFile} for possible format codes.
	 * <p>
	 * Note, not all key formats support the optional comment field.
	 * 
	 * @param pair key pair
	 * @param format format
	 * @param passphrase passphrase if you want the key encrypted, <code>null</code> if not
	 * @param comment comment if supported by format
	 * @return private key file
	 * @throws SshException
	 */
	SshPrivateKeyFile create(SshKeyPair pair, int format, char[] passphrase, String comment) throws SshException;

	/**
	 * Get a list of all the supported key types. Each element is a
	 * {@link String} which will be one of
	 * {@link SshConfiguration#PUBLIC_KEY_SSHRSA},
	 * {@link SshConfiguration#PUBLIC_KEY_ECDSA},
	 * {@link SshConfiguration#PUBLIC_KEY_ED25519} or
	 * {@link SshConfiguration#PUBLIC_KEY_SSHDSA}.
	 * 
	 * @return supported key types
	 */
	List<String> getSupportedKeyTypes();

	/**
	 * Get a list of all the supported key lengths. Each element is an
	 * {@link Integer} whose value is the key size in bits.
	 * 
	 * @return listed of supprted key lengths
	 */
	List<Integer> getSupportedKeyLengths();

	/**
	 * Get a list of all the supported public key file formats that may be
	 * created. Each element in the list is an {@link Integer} whose value
	 * corresponds to one of the format codes in {@link SshPublicKeyFile}
	 * 
	 * @return supported public key file formats
	 * @see SshPublicKeyFile
	 */
	List<Integer> getSupportedPublicKeyFileFormats();

	/**
	 * Get a list of all the supported private key file formats that may be
	 * created. Each element in the list is an {@link Integer} whose value
	 * corresponds to one of the format codes in {@link SshPrivateKeyFile}
	 * 
	 * @return supported public key file formats
	 * @see SshPublicKeyFile
	 */
	List<Integer> getSupportedPrivateKeyFileFormats();

	/**
	 * Create a Public Key File that may be used to write authorized_keys files
	 * or other public key storage systems.
	 * 
	 * @param key public key to use
	 * @param options additional options when supported by the format
	 * @param comment comment when supported by the format
	 * @param format key format. See {@link SshPublicKeyFile} for format types.
	 * @return public key file object
	 * @throws SshException if public key file may not be created
	 */
	SshPublicKeyFile create(SshPublicKey key, String options, String comment, int format) throws SshException;
}
