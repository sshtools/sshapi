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
package net.sf.sshapi.impl.maverick;

import java.io.IOException;

import com.sshtools.publickey.PublicKeySubsystem;
import com.sshtools.publickey.PublicKeySubsystemException;
import com.sshtools.ssh2.Ssh2Session;

import net.sf.sshapi.AbstractLifecycleComponentWithEvents;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshLifecycleListener;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshPublicKey;
import net.sf.sshapi.identity.SshPublicKeySubsystem;

class MaverickPublicKeySubsystem extends
		AbstractLifecycleComponentWithEvents<SshLifecycleListener<SshPublicKeySubsystem>, SshPublicKeySubsystem>
		implements SshPublicKeySubsystem {

	private PublicKeySubsystem subsystem;
	private Ssh2Session ssh2Session;

	/**
	 * Constructor.
	 * 
	 * @param ssh2Session
	 *            session
	 */
	MaverickPublicKeySubsystem(SshProvider provider, Ssh2Session ssh2Session) {
		super(provider);
		this.ssh2Session = ssh2Session;
	}

	@Override
	public void add(final SshPublicKey key, String comment) throws SshException {
		try {
			subsystem.add(new com.sshtools.ssh.components.SshPublicKey() {

				@Override
				public boolean verifySignature(byte[] signature, byte[] data) throws com.sshtools.ssh.SshException {
					return false;
				}

				@Override
				public void init(byte[] blob, int start, int len) throws com.sshtools.ssh.SshException {
				}

				@Override
				public String getFingerprint() throws com.sshtools.ssh.SshException {
					try {
						return key.getFingerprint();
					} catch (SshException e) {
						throw new com.sshtools.ssh.SshException("Failed to get fingerprint", e);
					}
				}

				@Override
				public byte[] getEncoded() throws com.sshtools.ssh.SshException {
					try {
						return key.getEncodedKey();
					} catch (SshException e) {
						throw new com.sshtools.ssh.SshException("Failed to get fingerprint", e);
					}
				}

				@Override
				public int getBitLength() {
					return key.getBitLength();
				}

				@Override
				public String getAlgorithm() {
					return key.getAlgorithm().toAlgoName();
				}
			}, comment);
		} catch (com.sshtools.ssh.SshException e) {
			throw new SshException(SshException.GENERAL, e);
		} catch (PublicKeySubsystemException e) {
			throw new SshException(SshException.GENERAL, e);
		}
	}

	@Override
	public SshPublicKey[] list() throws SshException {
		// TODO complete
		return null;
	}

	@Override
	public void remove(SshPublicKey key) throws SshException {
		// TODO complete
	}

	@Override
	protected void onClose() throws SshException {
		try {
			subsystem.close();
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	protected void onOpen() throws SshException {
		try {
			subsystem = new PublicKeySubsystem(ssh2Session);
		} catch (com.sshtools.ssh.SshException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}
}
