package net.sf.sshapi;

/**
 * Interface to be implemented by components that can produce file transfer
 * events, such as SFTP and SCP.
 * 
 */
public interface SshFileTransferClient<L extends SshLifecycleListener<C>, C extends SshLifecycleComponent<L, C>> extends SshLifecycleComponent<L, C> {
	/**
	 * Add a listener to those informed when file transfer events occur.
	 * 
	 * @param listener listener to add
	 */
	void addFileTransferListener(SshFileTransferListener listener);

	/**
	 * Remove a listener from those informed when file transfer events occur.
	 * 
	 * @param listener listener to remove
	 */
	void removeFileTransferListener(SshFileTransferListener listener);
}
