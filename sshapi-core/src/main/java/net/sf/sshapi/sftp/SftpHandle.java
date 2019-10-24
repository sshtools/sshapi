package net.sf.sshapi.sftp;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 */
public interface SftpHandle extends Closeable {
	
	/**
	 * Get the current position of the handle.
	 * 
	 * @return position
	 */
	long position();
	
	/**
	 * Set the current position of the handle.
	 * 
	 * @param position position
	 */
	SftpHandle position(long position);
	
	/**
	 * Write the provided buffer to the current position.
	 * @throws IOException 
	 */
	SftpHandle write(ByteBuffer buffer) throws SftpException;
	
	/**
	 * Write the provided buffer to the current position.
	 * @throws SftpException 
	 */
	int read(ByteBuffer buffer) throws SftpException;
}
