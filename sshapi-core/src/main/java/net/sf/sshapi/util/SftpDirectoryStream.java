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
package net.sf.sshapi.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.sftp.SftpError;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpHandle;

/**
 * An abstract {@link DirectoryStream} implementation that may be useful to
 * providers implementing
 * {@link SftpClient#directory(String, java.nio.file.DirectoryStream.Filter)}.
 * 
 * @param <H> handle type
 * @param <N> native file type
 */
public abstract class SftpDirectoryStream<H extends SftpHandle, N> implements DirectoryStream<SftpFile> {

	private List<H> handles = Collections.synchronizedList(new ArrayList<>());
	private boolean closed;
	private String path;
	private Filter<SftpFile> filter;

	public SftpDirectoryStream(String path, DirectoryStream.Filter<SftpFile> filter) {
		this.path = path;
		this.filter = filter;
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			while (!handles.isEmpty()) {
				try {
					handles.remove(0).close();
				} catch (Exception e) {
				}
			}
			closed = true;
		}
	}

	public abstract List<N> readDirectory(H handle);

	public abstract H createDirectoryHandle(String path);
	
	public abstract SftpFile nativeToFile(String path, N nativeFile);

	@Override
	public Iterator<SftpFile> iterator() {
		if (closed)
			throw new SftpError("Closed.");
		H thisHandle = createDirectoryHandle(path);
		handles.add(thisHandle);
		return new Iterator<SftpFile>() {
			SftpFile file;
			Iterator<N> nativeIterator;

			void checkNext() {
				if (closed)
					throw new IllegalStateException("Closed.");
				while (true) {
					if (nativeIterator == null) {
						List<N> nativeList = readDirectory(thisHandle);
						if (nativeList == null || nativeList.isEmpty())
							/* No more */
							return;
						nativeIterator = nativeList.iterator();
					}
					if (file == null) {
						if (nativeIterator.hasNext()) {
							/* Got a new file, exit for now */
							N nativeFile = nativeIterator.next();
							file = nativeToFile(path, nativeFile);
							try {
								if (filter != null && !filter.accept(file)) {
									/* Rejected by filter, go around again */
									file = null;
								} else
									break;
							} catch (IOException e) {
								throw new SftpError("Failed to filter.", e);
							}
						} else {
							/* No more on this iterator */
							nativeIterator = null;
						}
					} else
						/* We have a file already */
						break;
				}
			}

			@Override
			public boolean hasNext() {
				checkNext();
				return file != null;
			}

			@Override
			public SftpFile next() {
				checkNext();
				if (file == null)
					throw new SftpError("End of iteration.");
				try {
					return file;
				} finally {
					file = null;
				}
			}
		};
	}
}
