package net.sf.sshapi.sftp;

/**
 * Unchecked exception.
 */
@SuppressWarnings("serial")
public class SftpError extends RuntimeException {
	/**
	 * Constructor.
	 * 
	 * @param message message
	 */
	public SftpError(String message) {
		this("Unchecked exception.", new SftpException(SftpException.GENERAL, message));
	}

	/**
	 * Constructor.
	 * 
	 * @param message message
	 * @param cause   cause
	 */
	public SftpError(SftpException cause) {
		this("Unchecked exception.", cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message message
	 * @param cause   cause
	 */
	public SftpError(String message, Exception cause) {
		super(message, cause instanceof SftpException ? cause : new SftpException(SftpException.GENERAL, cause));
	}

	/**
	 * Get the underlying {@link SftpException}.
	 * 
	 * @return causing SFTP exception
	 */
	public SftpException getSftpCause() {
		return (SftpException) getCause();
	}

}
