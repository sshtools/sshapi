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

import java.io.IOException;

import net.sf.sshapi.forwarding.SshPortForward;

/** 
 * Exception thrown during various SSH operations.
 */
public class SshException extends IOException {
	/**
	 * General error. The provider did not or could not provide additional
	 * information with an error code. The actual exception will most likely be
	 * available through {@link #getCause()}.
	 */
	public final static Code GENERAL = new Code("general");
	/**
	 * The implementation does not support the protocol version request by
	 * {@link SshConfiguration#getProtocolVersion()}.
	 */
	public static final Code UNSUPPORTED_PROTOCOL_VERSION = new Code("unsupportedProtocolVersion");
	/**
	 * An attempt was made to 'open' a resource, an {@link SshPortForward} for
	 * example, but the resource was already open.
	 */
	public final static Code ALREADY_OPEN = new Code("open");
	/**
	 * An attempt was made to use a resource that must be open, a {@link SshPortForward} for
	 * example, but the resource was already closed. 
	 */
	public final static Code NOT_OPEN = new Code("notOpen");
	/**
	 * An attempt was made to use a resource that must be required authentication, but
	 * authentication is not yet complete.
	 */
	public final static Code NOT_AUTHENTICATED = new Code("notAuthenticated");
	/**
	 * The {@link SshConfiguration} requested use of a feature not supported by
	 * the provider in use.
	 */
	public static final Code UNSUPPORTED_FEATURE = new Code("unsupportedFeature");
	/**
	 * General I/O error. Actual exception will be available through
	 * {@link #getCause()}.
	 */
	public final static Code IO_ERROR = new Code("io");
	/**
	 * The provided passphrase is incorrect.
	 */
	public final static Code INCORRECT_PASSPHRASE = new Code("incorrectPassphrase");
	/**
	 * An attempt was made to decrypt a key that was not encrypted.
	 */
	public final static Code NOT_ENCRYPTED = new Code("notEncrypted");
	/**
	 * An attempt was made to write to a private key format that is not
	 * supported.
	 */
	public final static Code PRIVATE_KEY_FORMAT_NOT_SUPPORTED = new Code("privateKeyFormatNotSupported");
	/**
	 * A passphrase is required to perform an operation, but was not supplied.
	 * For example, changing the password on an encrypted private key.
	 */
	public final static Code PASSPHRASE_REQUIRED = new Code("passphraseRequired");
	/**
	 * Authentication failed. The provided credentials were not acceptable.
	 */
	public final static Code AUTHENTICATION_FAILED = new Code("authenticationFailed");
	/**
	 * Authentication was cancelled by user when being prompted for something
	 * (password, passphrase etc)
	 */
	public final static Code AUTHENTICATION_CANCELLED = new Code("authenticationCancelled");
	/**
	 * Connection was cancelled by user while connecting (non-blocking mode)
	 */
	public final static Code CONNECTION_CANCELLED = new Code("connectionCancelled");
	/**
	 * Too many authentication attempts
	 */
	public final static Code AUTHENTICATION_ATTEMPTS_EXCEEDED = new Code("authenticationAttemptsExceeded");
	/**
	 * Host key was rejected. Connection will be closed.
	 */
	public final static Code HOST_KEY_REJECTED = new Code("hostKeyRejected");
	/**
	 * Attempt to perform an operation that required a decrypted key failed
	 * because the key was still encrypted.
	 */
	public static final Code ENCRYPTED = new Code("encrypted");
	/**
	 * Failed to open shell.
	 */
	public final static Code FAILED_TO_OPEN_SHELL = new Code("failedToOpenShell");
	/**
	 * No agent could be found.
	 */
	public final static Code NO_AGENT = new Code("noAgent");
	/**
	 * Failed to start tunnel, unauthorized.
	 */
	public final static Code UNAUTHORIZED = new Code("unauthorized");
	/**
	 * There is an agent, but failed to connect to it.
	 */
	public final static Code FAILED_TO_CONNECT_TO_AGENT = new Code("failedToConnectToAgent");
	/**
	 * A task was interrupted.
	 */
	public final static Code INTERRUPTED = new Code("interrupted");
	/**
	 * Permission was denied to a resource, such as a file system
	 */
	public final static Code PERMISSION_DENIED = new Code("permissionDenied");
	
	private static final long serialVersionUID = 1L;
	// Private instance variables
	private final Code code;

	/**
	 * Constructor.
	 */
	public SshException() {
		this(GENERAL);
	}

	/**
	 * Constructor.
	 * 
	 * @param message message
	 * @param cause cause
	 */
	public SshException(String message, Throwable cause) {
		this(GENERAL, message, cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message message
	 */
	public SshException(String message) {
		this(GENERAL, message);
	}

	/**
	 * Constructor.
	 * 
	 * @param cause cause
	 */
	public SshException(Throwable cause) {
		this(GENERAL, cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param code code
	 */
	public SshException(Code code) {
		super(String.format("SSHERR %s", code.id));
		this.code = code;
	}

	/**
	 * Constructor.
	 * 
	 * @param code code
	 * @param message message
	 * @param cause cause
	 */
	public SshException(Code code, String message, Throwable cause) {
		super(String.format("SSHERR %s. %s", code.id, message), cause);
		this.code = code;
	}

	/**
	 * Constructor.
	 * 
	 * @param code code
	 * @param message message
	 */
	public SshException(Code code, String message) {
		super(String.format("SSHERR %s. %s", code.id, message));
		this.code = code;
	}

	/**
	 * Constructor.
	 * 
	 * @param code code
	 * @param cause cause
	 */
	public SshException(Code code, Throwable cause) {
		super(String.format("SSHERR %s", code.id),  cause);
		this.code = code;
	}

	/**
	 * Get the error code. For some operations, the SSH provider implementation
	 * will be expected to throw exceptions with appropriate code. For all other
	 * operations, general codes such as {@link #IO_ERROR} may be thrown, or the
	 * provide may add its own codes.
	 * 
	 * @return code
	 * 
	 */
	public Code getCode() {
		return code;
	}

	/**
	 * Error code.
	 */
	public static class Code {
		private String id;

		/**
		 * Constructor.
		 * 
		 * @param id error id
		 */
		public Code(String id) {
			this.id = id;
		}

		public String toString() {
			return id;
		}
	}
}
