package net.sf.sshapi;

/**
 * Listener that may be implemented to received events about file transfer
 * progress, for both SCP and SFTP.
 */
public interface SshFileTransferListener {
	/**
	 * A new file transfer has started.
	 * 
	 * @param sourcePath path of file (may be <code>null</code> if the source is
	 *            a stream)
	 * @param targetPath path of file (may be <code>null</code> if the target is
	 *            a stream)
	 * @param length total length of file (or -1 if not known)
	 */
	void startedTransfer(String sourcePath, String targetPath, long length);

	/**
	 * A file transfer has progressed. The amount of bytes that may have been
	 * transmitted since the last progress will depend on the buffer size. .
	 * 
	 * @param sourcePath path of file (may be <code>null</code> if the source is
	 *            a stream)
	 * @param targetPath path of file (may be <code>null</code> if the target is
	 *            a stream)
	 * @param progress number of bytes sent since the last progress
	 */
	void transferProgress(String sourcePath, String targetPath, long progress);

	/**
	 * A file transfer has finished.
	 * 
	 * @param sourcePath path of file (may be <code>null</code> if the source is
	 *            a stream)
	 * @param targetPath path of file (may be <code>null</code> if the target is
	 *            a stream)
	 */
	void finishedTransfer(String sourcePath, String targetPath);
}
