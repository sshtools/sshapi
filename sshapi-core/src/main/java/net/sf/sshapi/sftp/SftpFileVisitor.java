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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * {@link FileVisitor} for use with {@link SftpFile}.
 */
public interface SftpFileVisitor extends FileVisitor<SftpFile> {
	@Override
	default FileVisitResult preVisitDirectory(SftpFile dir, BasicFileAttributes attrs) throws IOException {
		Objects.requireNonNull(dir);
		Objects.requireNonNull(attrs);
		return FileVisitResult.CONTINUE;
	}

	@Override
	default FileVisitResult visitFileFailed(SftpFile file, IOException exc) throws IOException {
		Objects.requireNonNull(file);
		throw exc;
	}

	@Override
	default FileVisitResult postVisitDirectory(SftpFile dir, IOException exc) throws IOException {
		Objects.requireNonNull(dir);
		if (exc != null)
			throw exc;
		return FileVisitResult.CONTINUE;
	}
}
