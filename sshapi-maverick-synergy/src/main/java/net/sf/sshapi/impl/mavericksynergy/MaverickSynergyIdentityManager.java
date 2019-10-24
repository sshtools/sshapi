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
package net.sf.sshapi.impl.mavericksynergy;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.List;

import com.sshtools.common.publickey.InvalidPassphraseException;
import com.sshtools.common.publickey.SshKeyPairGenerator;
import com.sshtools.common.publickey.SshPrivateKeyFileFactory;
import com.sshtools.common.publickey.SshPublicKeyFileFactory;
import com.sshtools.common.ssh.components.SshKeyPair;
import com.sshtools.common.ssh.components.SshX509RsaSha1PublicKey;
import com.sshtools.common.ssh.components.jce.Ssh2RsaPrivateKey;

import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey.Algorithm;
import net.sf.sshapi.SshPublicKey;
import net.sf.sshapi.identity.SshIdentityManager;
import net.sf.sshapi.identity.SshPrivateKeyFile;
import net.sf.sshapi.identity.SshPublicKeyFile;

/**
 * Maverick implementation of an {@link SshIdentityManager}, used for managing
 * private keys used for authentication.
 */
public class MaverickSynergyIdentityManager implements SshIdentityManager {
 
	public SshPrivateKeyFile createPrivateKeyFromStream(InputStream in) throws SshException {
		try {
			return new MaverickPrivateKeyFile(SshPrivateKeyFileFactory.parse(in));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	public SshPublicKeyFile createPublicKeyFromStream(InputStream in) throws SshException {
		try {
			return new MaverickPublicKeyFile(SshPublicKeyFileFactory.parse(in));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	public SshPrivateKeyFile create(net.sf.sshapi.identity.SshKeyPair pair, int format, char[] passphrase,
			String comment) throws SshException {
		if (format != SshPrivateKeyFile.VENDOR_OPENSSH)
			throw new SshException(SshException.UNSUPPORTED_FEATURE,
					"Private key file format " + format + " not supported.");
		try {
			com.sshtools.common.publickey.SshPrivateKeyFile pk = SshPrivateKeyFileFactory.create(convertPair(pair),
					passphrase == null ? null : new String(passphrase));
			return new MaverickPrivateKeyFile(pk);
		} catch (IOException e) {
			throw new SshException(e);
		}
	}

	private SshKeyPair convertPair(net.sf.sshapi.identity.SshKeyPair pair) {
		final MaverickSynergyPublicKey publicKey = (MaverickSynergyPublicKey) pair.getPublicKey();
		final MaverickSynergyPrivateKey privateKey = (MaverickSynergyPrivateKey) pair.getPrivateKey();
		return SshKeyPair.getKeyPair(privateKey.privateKey, publicKey.getPublicKey());
	}

	public net.sf.sshapi.identity.SshKeyPair generateKeyPair(Algorithm keyType, int keyBits) throws SshException {
		try {
			SshKeyPair pair = SshKeyPairGenerator.generateKeyPair(keyType.toAlgoName(), keyBits);
			return new net.sf.sshapi.identity.SshKeyPair(new MaverickSynergyPublicKey(pair.getPublicKey()),
					new MaverickSynergyPrivateKey(pair.getPrivateKey()));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		} catch (com.sshtools.common.ssh.SshException e) {
			throw new SshException(SshException.GENERAL, e);
		}
	}

	public List<Integer> getSupportedKeyLengths() {
		return Arrays
				.asList(new Integer[] { new Integer(2048), new Integer(1024), new Integer(768), new Integer(512) });
	}

	public List<Integer> getSupportedPublicKeyFileFormats() {
		return Arrays.asList(new Integer[] { new Integer(SshPublicKeyFile.OPENSSH_FORMAT) });
	}

	public List<Integer> getSupportedPrivateKeyFileFormats() {
		return Arrays.asList(new Integer[] { new Integer(SshPrivateKeyFile.VENDOR_OPENSSH) });
	}

	public List<String> getSupportedKeyTypes() {
		return Arrays.asList(new String[] { "ssh-dss", "ssh-rsa", "rsa1" });
	}

	public SshPublicKeyFile create(SshPublicKey key, String options, String comment, int format) throws SshException {
		MaverickSynergyPublicKey pk = (MaverickSynergyPublicKey) key;
		int type;
		switch (format) {
		case SshPublicKeyFile.OPENSSH_FORMAT:
			type = SshPublicKeyFileFactory.OPENSSH_FORMAT;
			break;
		case SshPublicKeyFile.SECSH_FORMAT:
			type = SshPublicKeyFileFactory.SECSH_FORMAT;
			break;
		default:
			throw new SshException(SshException.UNSUPPORTED_FEATURE, "Unsupport public key file format.");
		}
		com.sshtools.common.publickey.SshPublicKeyFile publicKeyFile;
		try {
			publicKeyFile = SshPublicKeyFileFactory.create(pk.getPublicKey(), options, comment, type);
		} catch (IOException e) {
			throw new SshException(e);
		}
		return new MaverickPublicKeyFile(publicKeyFile);
	}

	class MaverickPrivateKeyFile implements SshPrivateKeyFile {

		private com.sshtools.common.publickey.SshPrivateKeyFile privateKeyFile;
		private SshKeyPair pair;

		public MaverickPrivateKeyFile(com.sshtools.common.publickey.SshPrivateKeyFile privateKeyFile) {
			this.privateKeyFile = privateKeyFile;
			try {
				if (!isEncrypted()) {
					pair = privateKeyFile.toKeyPair(null);
				}
			} catch (SshException e) {
				SshConfiguration.getLogger().log(Level.WARN, "Failed to test if key is encrypted.");
			} catch (IOException e) {
				SshConfiguration.getLogger().log(Level.ERROR, "Failed to load key.");
			} catch (InvalidPassphraseException e) {
				SshConfiguration.getLogger().log(Level.WARN, "Failed to decode supposedly unencrypted key.");
			}

		}

		public void changePassphrase(char[] newPassphrase) throws SshException {
			if (isEncrypted()) {
				throw new SshException(SshException.PASSPHRASE_REQUIRED,
						"Key is encrypted, you must decrypt it before changing the passphrase.");
			}
			try {
				privateKeyFile.changePassphrase("", newPassphrase == null ? null : new String(newPassphrase));
			} catch (IOException e) {
				throw new SshException(SshException.IO_ERROR, e);
			} catch (InvalidPassphraseException e) {
				throw new SshException(SshException.INCORRECT_PASSPHRASE,
						"Could not decrypte key, invalid passphrase.");
			}
		}

		public byte[] getFormattedKey() throws SshException {
			try {
				return privateKeyFile.getFormattedKey();
			} catch (IOException e) {
				throw new SshException(SshException.IO_ERROR, e);
			}
		}

		public void decrypt(char[] passphrase) throws SshException {
			if (!isEncrypted()) {
				throw new SshException(SshException.NOT_ENCRYPTED, "Key not encrypted.");
			}
			try {
				pair = privateKeyFile.toKeyPair(new String(passphrase));
				privateKeyFile = SshPrivateKeyFileFactory.create(pair, null);
			} catch (IOException e) {
				throw new SshException(SshException.IO_ERROR, e);
			} catch (InvalidPassphraseException e) {
				throw new SshException(SshException.INCORRECT_PASSPHRASE,
						"Could not decrypte key, invalid passphrase.");
			}
		}

		public boolean isEncrypted() throws SshException {
			return privateKeyFile.isPassphraseProtected();
		}

		public boolean supportsPassphraseChange() {
			return privateKeyFile.supportsPassphraseChange();
		}

		public int getFormat() {
			String type = privateKeyFile.getType();
			if (type.equals("OpenSSH")) {
				return VENDOR_OPENSSH;
			} else if (type.equals("SSHTools")) {
				return VENDOR_SSHTOOLS;
			} else if (type.equals("SSH1")) {
				return VENDOR_SSH1;
			} else if (type.equals("Putty")) {
				return VENDOR_PUTTY;
			} else if (type.equals("SSH Communications Security")) {
				return VENDOR_SSHCOM;
			}
			return VENDOR_UNKNOWN;
		}

		public net.sf.sshapi.identity.SshKeyPair toKeyPair() throws SshException {
			if (isEncrypted()) {
				throw new SshException(SshException.PASSPHRASE_REQUIRED,
						"Key is encrypted, you must decrypt it before extracing the keys.");
			}
			try {
				return new net.sf.sshapi.identity.SshKeyPair(new MaverickSynergyPublicKey(pair.getPublicKey()),
						new MaverickSynergyPrivateKey(pair.getPrivateKey()));
			} catch (com.sshtools.common.ssh.SshException e) {
				throw new SshException(SshException.GENERAL, e);
			}
		}
	}

	@Override
	public net.sf.sshapi.identity.SshKeyPair importX509(InputStream pkcs12Keystore, char[] keystorePassphrase, String key,
			char[] keyPassphrase) throws SshException {
		try {
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			try {
				keystore.load(pkcs12Keystore, keystorePassphrase);
			} finally {
				pkcs12Keystore.close();
			}
			RSAPrivateKey prv = (RSAPrivateKey) keystore.getKey(key, keyPassphrase);
			X509Certificate x509 = (X509Certificate) keystore.getCertificate("mykey");
			SshX509RsaSha1PublicKey pubkey = new SshX509RsaSha1PublicKey(x509);
			Ssh2RsaPrivateKey privkey = new Ssh2RsaPrivateKey(prv);
			return new net.sf.sshapi.identity.SshKeyPair(new MaverickSynergyPublicKey(pubkey), new MaverickSynergyPrivateKey(privkey));
		} catch (IOException ioe) {
			throw new SshException("Failed to import X509 key.", ioe);
		} catch (KeyStoreException e) {
			throw new SshException("Failed to import X509 key.", e);
		} catch (CertificateException e) {
			throw new SshException("Failed to import X509 key.", e);
		} catch (UnrecoverableKeyException e) {
			throw new SshException("Failed to import X509 key.", e);
		} catch (NoSuchAlgorithmException e) {
			throw new SshException("Failed to import X509 key.", e);
		} catch (com.sshtools.common.ssh.SshException e) {
			throw new SshException("Failed to import X509 key.", e);
		} 
	}

	class MaverickPublicKeyFile implements SshPublicKeyFile {

		private byte[] formattedKey;
		private String options;
		private String comment;
		private MaverickSynergyPublicKey publicKey;
		private final int format;

		public MaverickPublicKeyFile(com.sshtools.common.publickey.SshPublicKeyFile keyFile) throws SshException {
			comment = keyFile.getComment();
			options = keyFile.getOptions();
			try {
				formattedKey = keyFile.getFormattedKey();
				publicKey = new MaverickSynergyPublicKey(keyFile.toPublicKey());
			} catch (Exception e) {
				throw new SshException(SshException.GENERAL, e);
			}
			format = SshPublicKeyFile.OPENSSH_FORMAT;

		}

		public SshPublicKey getPublicKey() throws SshException {
			return publicKey;
		}

		public String getComment() {
			return comment;
		}

		public byte[] getFormattedKey() {
			return formattedKey;
		}

		public String getOptions() {
			return options;
		}

		public int getFormat() {
			return format;
		}
	}

}
