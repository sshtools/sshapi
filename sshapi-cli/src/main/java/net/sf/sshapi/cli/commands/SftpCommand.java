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
package net.sf.sshapi.cli.commands;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import net.sf.sshapi.SshException;
import net.sf.sshapi.cli.SftpContainer;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpFileVisitor;
import net.sf.sshapi.util.Util;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Abstract sftp command.
 */
public abstract class SftpCommand implements IVersionProvider {
	
	public interface FileOp {
		void op(String path) throws Exception;
	}
	
	@Spec
	private CommandSpec spec;

	@SuppressWarnings("unchecked")
	<C extends SftpContainer> C getContainer() {
		return (C) spec.parent().userObject();
	}
	
	protected void expand(String path, FileOp op, boolean recurse) throws SshException {
		var matcher =
			    FileSystems.getDefault().getPathMatcher("glob:" + path);
		
		getContainer().getClient().visit(path, new SftpFileVisitor() {
			@Override
			public FileVisitResult postVisitDirectory(SftpFile dir, IOException exc) throws IOException {
				if(recurse)
					return FileVisitResult.TERMINATE;
				else
					return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(SftpFile file, BasicFileAttributes attrs) throws IOException {
				var filePath = file.getPath();
				if(matcher.matches(Paths.get(filePath))) {
					try {
						op.op(filePath);
					} catch(IOException | RuntimeException re) {
						throw re;
					} catch (Exception e) {
						throw new IOException("Failed to match pattern.", e);
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	static String translatePath(String cwd, String newCwd) {
		if (!newCwd.startsWith("/")) {
			if (newCwd.equals("..")) {
				int idx = cwd.lastIndexOf('/');
				if (idx > 0) {
					newCwd = cwd.substring(0, idx);
					if (newCwd.equals("")) {
						newCwd = "/";
					}
				}
			} else {
				newCwd = Util.concatenatePaths(cwd, newCwd);
			}
		}
		return newCwd;
	}

	@Override
	public String[] getVersion() throws Exception {
		return Util.getArtifactVersion("com.sshtools", "sshapi-cli").split("\\.");
	}
}