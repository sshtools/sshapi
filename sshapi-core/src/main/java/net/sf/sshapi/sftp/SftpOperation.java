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
