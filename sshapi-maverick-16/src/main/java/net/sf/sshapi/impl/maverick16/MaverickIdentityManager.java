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
package net.sf.sshapi.impl.maverick16;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPublicKey;
import net.sf.sshapi.identity.SshIdentityManager;
import net.sf.sshapi.identity.SshPrivateKeyFile;
import net.sf.sshapi.identity.SshPublicKeyFile;

import com.maverick.ssh.components.SshKeyPair;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.OpenSSHPublicKeyFile;
import com.sshtools.publickey.SECSHPublicKeyFile;
import com.sshtools.publickey.SshKeyPairGenerator;
import com.sshtools.publickey.SshPrivateKeyFileFactory;
import com.sshtools.publickey.SshPublicKeyFileFactory;

/**
 * Maverick implementation of an {@link SshIdentityManager}, used for managing
 * private keys used for authentication.
 */
public class MaverickIdentityManager implements SshIdentityManager {

	@Override
	public SshPrivateKeyFile createPrivateKeyFromStream(InputStream in) throws SshException {
		try {
			return new MaverickPrivateKeyFile(SshPrivateKeyFileFactory.parse(in));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public SshPublicKeyFile createPublicKeyFromStream(InputStream in) throws SshException {
		try {
			return new MaverickPublicKeyFile(SshPublicKeyFileFactory.parse(in));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public SshPrivateKeyFile create(net.sf.sshapi.identity.SshKeyPair pair, int format, char[] passphrase,
			String comment) throws SshException {
		int typeNo = SshPrivateKeyFileFactory.OPENSSH_FORMAT;
		if (format == SshPrivateKeyFile.VENDOR_OPENSSH) {
			typeNo = SshPrivateKeyFileFactory.OPENSSH_FORMAT;
		} else if (format == SshPrivateKeyFile.VENDOR_SSH1) {
			typeNo = SshPrivateKeyFileFactory.SSH1_FORMAT;
		} else if (format == SshPrivateKeyFile.VENDOR_SSHTOOLS) {
			typeNo = SshPrivateKeyFileFactory.SSHTOOLS_FORMAT;
		} else {
			throw new SshException(SshException.UNSUPPORTED_FEATURE,
					"Private key file format " + format + " not supported.");
		}
		try {
			MaverickPrivateKeyFile pk = new MaverickPrivateKeyFile(SshPrivateKeyFileFactory.create(convertPair(pair),
					passphrase == null ? null : new String(passphrase), comment, typeNo));
			return pk;
		} catch (IOException e) {
			throw new SshException(e);
		}
	}

	private SshKeyPair convertPair(net.sf.sshapi.identity.SshKeyPair pair) {
		final MaverickPublicKey publicKey = (MaverickPublicKey) pair.getPublicKey();
		final MaverickPrivateKey privateKey = (MaverickPrivateKey) pair.getPrivateKey();
		return SshKeyPair.getKeyPair(privateKey.privateKey, publicKey.getPublicKey());
	}

	@Override
	public net.sf.sshapi.identity.SshKeyPair generateKeyPair(String keyType, int keyBits) throws SshException {
		try {
			SshKeyPair pair = SshKeyPairGenerator.generateKeyPair(translateKeyType(keyType), keyBits);
			return new net.sf.sshapi.identity.SshKeyPair(new MaverickPublicKey(pair.getPublicKey()),
					new MaverickPrivateKey(pair.getPrivateKey()));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		} catch (com.maverick.ssh.SshException e) {
			throw new SshException(SshException.GENERAL, e);
		}
	}

	private static String translateKeyType(String type) {
		if (type.equals(SshConfiguration.PUBLIC_KEY_SSHRSA)) {
			return SshKeyPairGenerator.SSH2_RSA;
		} else if (type.equals(SshConfiguration.PUBLIC_KEY_SSHRSA1)) {
			return SshKeyPairGenerator.SSH1_RSA;
		} else {
			return SshKeyPairGenerator.SSH2_DSA;
		}
	}

	@Override
	public List<Integer> getSupportedKeyLengths() {
		return Arrays
				.asList(new Integer[] { new Integer(2048), new Integer(1024), new Integer(768), new Integer(512) });
	}

	@Override
	public List<Integer> getSupportedPublicKeyFileFormats() {
		return Arrays.asList(new Integer[] { new Integer(SshPublicKeyFile.OPENSSH_FORMAT),
				new Integer(SshPublicKeyFile.SECSH_FORMAT), new Integer(SshPublicKeyFile.SSH1_FORMAT) });
	}

	@Override
	public List<Integer> getSupportedPrivateKeyFileFormats() {
		return Arrays.asList(new Integer[] { new Integer(SshPrivateKeyFile.VENDOR_OPENSSH),
				new Integer(SshPrivateKeyFile.VENDOR_SSH1), new Integer(SshPrivateKeyFile.VENDOR_SSHTOOLS) });
	}

	@Override
	public List<String> getSupportedKeyTypes() {
		return Arrays.asList(new String[] { "ssh-dss", "ssh-rsa", "rsa1" });
	}

	@Override
	public SshPublicKeyFile create(SshPublicKey key, String options, String comment, int format) throws SshException {
		MaverickPublicKey pk = (MaverickPublicKey) key;
		int type;
		switch (format) {
		case SshPublicKeyFile.OPENSSH_FORMAT:
			type = SshPublicKeyFileFactory.OPENSSH_FORMAT;
			break;
		case SshPublicKeyFile.SECSH_FORMAT:
			type = SshPublicKeyFileFactory.SECSH_FORMAT;
			break;
		case SshPublicKeyFile.SSH1_FORMAT:
			type = SshPublicKeyFileFactory.SSH1_FORMAT;
			break;
		default:
			throw new SshException(SshException.UNSUPPORTED_FEATURE, "Unsupport public key file format.");
		}
		com.sshtools.publickey.SshPublicKeyFile publicKeyFile;
		try {
			publicKeyFile = SshPublicKeyFileFactory.create(pk.getPublicKey(), options, comment, type);
		} catch (IOException e) {
			throw new SshException(e);
		}
		return new MaverickPublicKeyFile(publicKeyFile);
	}

	class MaverickPrivateKeyFile implements SshPrivateKeyFile {

		private com.sshtools.publickey.SshPrivateKeyFile privateKeyFile;
		private SshKeyPair pair;

		public MaverickPrivateKeyFile(com.sshtools.publickey.SshPrivateKeyFile privateKeyFile) {
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

		@Override
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

		@Override
		public byte[] getFormattedKey() throws SshException {
			try {
				return privateKeyFile.getFormattedKey();
			} catch (IOException e) {
				throw new SshException(SshException.IO_ERROR, e);
			}
		}

		@Override
		public void decrypt(char[] passphrase) throws SshException {
			if (!isEncrypted()) {
				throw new SshException(SshException.NOT_ENCRYPTED, "Key not encrypted.");
			}
			try {
				pair = privateKeyFile.toKeyPair(new String(passphrase));
				privateKeyFile = SshPrivateKeyFileFactory.create(pair, null, "Create by SSHAPI Identity Management",
						convertType(privateKeyFile.getType()));
			} catch (IOException e) {
				throw new SshException(SshException.IO_ERROR, e);
			} catch (InvalidPassphraseException e) {
				throw new SshException(SshException.INCORRECT_PASSPHRASE,
						"Could not decrypte key, invalid passphrase.");
			}
		}

		@Override
		public boolean isEncrypted() throws SshException {
			return privateKeyFile.isPassphraseProtected();
		}

		private int convertType(String typeName) throws SshException {
			if (typeName.equals("OpenSSH")) {
				return SshPrivateKeyFileFactory.OPENSSH_FORMAT;
			} else if (typeName.equals("SSHTools")) {
				return SshPrivateKeyFileFactory.SSHTOOLS_FORMAT;
			} else if (typeName.equals("SSH1")) {
				return SshPrivateKeyFileFactory.SSH1_FORMAT;
			}
			throw new SshException(SshException.PRIVATE_KEY_FORMAT_NOT_SUPPORTED,
					"Cannot write keys in " + typeName + " format");
		}

		@Override
		public boolean supportsPassphraseChange() {
			return privateKeyFile.supportsPassphraseChange();
		}

		@Override
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

		@Override
		public net.sf.sshapi.identity.SshKeyPair toKeyPair() throws SshException {
			if (isEncrypted()) {
				throw new SshException(SshException.PASSPHRASE_REQUIRED,
						"Key is encrypted, you must decrypt it before extracing the keys.");
			}
			try {
				return new net.sf.sshapi.identity.SshKeyPair(new MaverickPublicKey(pair.getPublicKey()),
						new MaverickPrivateKey(pair.getPrivateKey()));
			} catch (com.maverick.ssh.SshException e) {
				throw new SshException(SshException.GENERAL, e);
			}
		}
	}

	class MaverickPublicKeyFile implements SshPublicKeyFile {

		private byte[] formattedKey;
		private String options;
		private String comment;
		private MaverickPublicKey publicKey;
		private final int format;

		public MaverickPublicKeyFile(com.sshtools.publickey.SshPublicKeyFile keyFile) throws SshException {
			comment = keyFile.getComment();
			options = keyFile.getOptions();
			try {
				formattedKey = keyFile.getFormattedKey();
				publicKey = new MaverickPublicKey(keyFile.toPublicKey());
			} catch (Exception e) {
				throw new SshException(SshException.GENERAL, e);
			}
			if (keyFile instanceof OpenSSHPublicKeyFile) {
				format = SshPublicKeyFile.OPENSSH_FORMAT;
			} else if (keyFile instanceof SECSHPublicKeyFile) {
				format = SshPublicKeyFile.SECSH_FORMAT;
			} else if (keyFile.getClass().getName().endsWith(".Ssh1RsaPublicKeyFile")) {
				// ^^ Wasn't public
				format = SshPublicKeyFile.SSH1_FORMAT;
			} else {
				throw new SshException(SshException.UNSUPPORTED_FEATURE,
						"Unsupported public key file of type " + keyFile.getClass());
			}

		}

		@Override
		public SshPublicKey getPublicKey() throws SshException {
			return publicKey;
		}

		@Override
		public String getComment() {
			return comment;
		}

		@Override
		public byte[] getFormattedKey() {
			return formattedKey;
		}

		@Override
		public String getOptions() {
			return options;
		}

		@Override
		public int getFormat() {
			return format;
		}
	}

}
