package net.sf.sshapi.sftp;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * This class provides a list of operations that have been/or will be completed
 * by the SftpClient's copyRemoteDirectory/copyLocalDirectory methods.
 * </p>
 * <p>
 * The objects returned could either be
 * {@link com.sshtools.client.sftp.maverick.sftp.SftpFile} or
 * <em>java.io.File</em> depending upon the commit state and whether
 * syncronization is required. Any code using the values returned should be able
 * to handle both types of file object.
 * </p>
 * 
 * 
 */
public interface SftpOperation {
	List<String> all();

	List<String> deleted();

	List<String> unchanged();

	List<String> updated();

	List<String> created();

	Map<String, Exception> errors();

	long size();

	long files();
}
