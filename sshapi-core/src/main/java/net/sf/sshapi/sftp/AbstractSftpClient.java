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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import net.sf.sshapi.AbstractFileTransferClient;
import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshLifecycleListener;
import net.sf.sshapi.util.Util;

/**
 * Abstract implementation of an {@link SftpClient}, providing some common
 * methods. All provider implementations will probably want to extend this.
 * <p>
 * Many default implementations are provided (especially for file transfer),
 * often filling gaps where concrete providers. For example, all that really
 * needs to be implemented to support all transfer methods is
 * {@link #file(String, net.sf.sshapi.sftp.SftpClient.OpenMode...)}, as this may
 * be used for all high level get/put methods.
 * <p>
 * However, if a provider <strong>can</strong> implement it itself, it
 * <strong>should</strong>.
 */
public abstract class AbstractSftpClient<C extends SshClient> extends AbstractFileTransferClient<SshLifecycleListener<SftpClient>, SftpClient>
		implements SftpClient {
	protected SshConfiguration configuration;
	protected C client;
	protected EOLPolicy[] eolPolicy;
	protected TransferMode transferMode = TransferMode.BINARY;

	protected AbstractSftpClient(C client) {
		super(client.getProvider());
		this.client = client;
		this.configuration = client.getConfiguration();
	}

	@Override
	public final SshClient getSshClient() {
		return client;
	}

	@Override
	public SftpOperation download(String remotedir, File localdir, boolean recurse, boolean sync, boolean commit)
			throws SshException {
		// Create an operation object to hold the information
		DefaultSftpOperation op = new DefaultSftpOperation();
		// Record the previous working directoies
		// Setup the local cwd
		String base = remotedir;
		if (base.endsWith("/"))
			base = base.substring(0, base.length() - 1);
		int idx = base.lastIndexOf('/');
		if (idx != -1) {
			base = base.substring(idx + 1);
		}
		if (!localdir.isAbsolute()) {
			localdir = localdir.getAbsoluteFile();
		}
		if (!localdir.exists() && commit) {
			localdir.mkdir();
		}
		SftpFile[] files = ls(remotedir);
		SftpFile file;
		File f;
		for (int i = 0; i < files.length; i++) {
			file = files[i];
			if (file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..")) {
				if (recurse) {
					f = new File(localdir, file.getName());
					op.add(download(remotedir + "/" + file.getName(), new File(localdir, file.getName()), recurse, sync, commit));
				}
			} else if (file.isFile()) {
				f = new File(localdir, file.getName());
				if (f.exists() && (f.length() == file.getSize()) && ((f.lastModified() / 1000) == file.getLastModified())) {
					if (commit) {
						op.unchanged().add(f.toString());
					} else {
						op.unchanged().add(file.toString());
					}
					continue;
				}
				try {
					if (f.exists()) {
						if (commit) {
							op.updated().add(f.toString());
						} else {
							op.updated().add(file.toString());
						}
					} else {
						if (commit) {
							op.created().add(f.toString());
						} else {
							op.created().add(file.toString());
						}
					}
					if (commit) {
						// Get the file
						get(file.getPath(), f);
					}
				} catch (SftpException ex) {
					op.errors().put(f.toString(), ex);
				}
			}
		}
		if (sync) {
			// List the contents of the new local directory and remove any
			// files/directories that were not updated
			String[] contents = localdir.list();
			File f2;
			if (contents != null) {
				for (int i = 0; i < contents.length; i++) {
					f2 = new File(localdir, contents[i]);
					if (!op.contains(f2.toString())) {
						op.deleted().add(f2.toString());
						if (f2.isDirectory() && !f2.getName().equals(".") && !f2.getName().equals("..")) {
							recurseMarkForDeletion(f2, op);
							if (commit) {
								Util.delTree(f2);
							}
						} else if (commit) {
							f2.delete();
						}
					}
				}
			}
		}
		return op;
	}

	@Override
	public EOLPolicy[] eol() {
		return eolPolicy;
	}

	@Override
	public SftpClient eol(EOLPolicy... eolPolicy) {
		if (!provider.getCapabilities().contains(Capability.SFTP_TRANSFER_MODE))
			throw new UnsupportedOperationException("This provider does not support file transfer modes.");
		if (!Objects.equals(eolPolicy, this.eolPolicy)) {
			this.eolPolicy = eolPolicy;
			onEOLPolicyChange(eolPolicy);
		}
		return this;
	}

	@Override
	public final boolean exists(String path) throws SshException {
		try {
			stat(path);
			return true;
		} catch (SftpException se) {
			if (se.getCode() == SftpException.SSH_FX_NO_SUCH_FILE)
				return false;
			else
				throw se;
		}
	}

	@Override
	public SftpHandle file(String path, OpenMode... modes) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public final InputStream get(final String path) throws SshException {
		return get(path, 0);
	}

	//
	// Local Files
	//
	@Override
	public void get(String path, File destination) throws SshException {
		if (destination.isDirectory())
			destination = new File(destination, Util.basename(path));
		String name = destination.toString();
		try (OutputStream out = new FileOutputStream(destination) {
			@Override
			public String toString() {
				return name;
			}
		}) {
			get(path, out);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public final InputStream get(String path, long filePointer) throws SshException {
		if (isUseRawSFTP(filePointer)) {
			ByteBuffer buf = ByteBuffer.allocate(configuration.getStreamBufferSize());
			SftpHandle h = file(path, OpenMode.SFTP_READ);
			SftpHandleInputStream in = new SftpHandleInputStream(buf, h);
			return new SftpInputStream(in, this, path, in.toString());
		} else
			return doGet(path, filePointer);
	}

	//
	// Raw Streams
	//
	@Override
	public final void get(String path, OutputStream out) throws SshException {
		get(path, out, 0);
	}

	@Override
	public final void get(String path, OutputStream out, long filePointer) throws SshException {
		if (isUseRawSFTP(filePointer)) {
			ByteBuffer buf = ByteBuffer.allocate(configuration.getStreamBufferSize());
			out = new SftpOutputStream(out, this, path, out.toString());
			try (SftpHandle h = file(path, OpenMode.SFTP_READ)) {
				int r;
				h.position(filePointer);
				while ((r = h.read(buf)) != -1) {
					out.write(buf.array(), 0, r);
					buf.rewind();
				}
			} catch (IOException e) {
				if (e instanceof SshException)
					throw (SshException) e;
				throw new SshException(SshException.IO_ERROR, e);
			}
		} else
			doGet(path, out, filePointer);
	}

	@Override
	public EOLPolicy getRemoteEOL() {
		throw new UnsupportedOperationException("This provider does not support file transfer modes.");
	}

	@Override
	public int getSftpVersion() {
		return 0;
	}

	@Override
	public Iterable<SftpFile> list(String path) throws SshException {
		return Arrays.asList(ls(path));
	}

	@Override
	public final void mkdir(String dir) throws SshException {
		mkdir(dir, -1);
	}

	@Override
	public final void mkdirs(String dir) throws SshException {
		mkdirs(dir, -1);
	}

	@Override
	public void mkdirs(String dir, int permissions) throws SshException {
		StringTokenizer tokens = new StringTokenizer(dir, "/");
		String path = dir.startsWith("/") ? "/" : "";
		while (tokens.hasMoreElements()) {
			path += (String) tokens.nextElement();
			try {
				stat(path);
			} catch (SftpException ex) {
				if (ex.getCode() == SftpException.SSH_FX_NO_SUCH_FILE) {
					mkdir(path, permissions);
				} else {
					throw ex;
				}
			}
			path += "/";
		}
	}

	@Override
	public final TransferMode mode() {
		return transferMode;
	}

	@Override
	public final SftpClient mode(TransferMode transferMode) {
		if (!provider.getCapabilities().contains(Capability.SFTP_TRANSFER_MODE))
			throw new UnsupportedOperationException("This provider does not support file transfer modes.");
		if (!Objects.equals(transferMode, this.transferMode)) {
			this.transferMode = transferMode;
			onTransferModeChange(transferMode);
		}
		return this;
	}

	@Override
	public final void put(File source, String path) throws SshException, IOException {
		put(source, path, -1);
	}

	@Override
	public final void put(File source, String path, int permissions) throws SshException, IOException {
		try (InputStream fin = new BufferedInputStream(new FileInputStream(source), configuration.getStreamBufferSize()) {
			@Override
			public String toString() {
				return source.toString();
			}
		}) {
			put(path, fin, permissions);
		}
		if(provider.getCapabilities().contains(Capability.SET_LAST_MODIFIED))
			setLastModified(path, source.lastModified());
	}

	//
	// Put
	//
	@Override
	public final OutputStream put(final String path) throws SshException {
		return put(path, -1, 0);
	}

	@Override
	public final void put(String path, InputStream in) throws SshException {
		put(path, in, -1, 0);
	}

	@Override
	public final void put(String path, InputStream in, int permissions) throws SshException {
		put(path, in, permissions, 0);
	}

	@Override
	public final void put(String path, InputStream in, int permissions, long offset) throws SshException {
		if (isUseRawSFTP(offset)) {
			ByteBuffer buf = ByteBuffer.allocate(configuration.getStreamBufferSize());
			try (SftpHandle h = file(path, offset > 0 ? new OpenMode[] { OpenMode.SFTP_WRITE, OpenMode.SFTP_CREAT } : new OpenMode[] { OpenMode.SFTP_WRITE, OpenMode.SFTP_CREAT, OpenMode.SFTP_TRUNC })) {
				in = new SftpInputStream(in, this, path, in.toString());
				int r;
				byte[] b = buf.array();
				h.position(offset);
				while ((r = in.read(b, 0, b.length)) != -1) {
					buf.clear();
					buf.put(b, 0, r);
					buf.flip();
					h.write(buf);
				}
			} catch (IOException e) {
				if (e instanceof SshException)
					throw (SshException) e;
				throw new SshException(SshException.IO_ERROR, e);
			}
		} else
			doPut(path, in, offset);
		if (permissions > -1) {
			chmod(path, permissions);
		}
	}

	@Override
	public final OutputStream put(final String path, final int permissions) throws SshException {
		return put(path, permissions, 0);
	}

	@Override
	public final OutputStream put(final String path, final int permissions, long offset) throws SshException {
		if (isUseRawSFTP(offset)) {
			ByteBuffer buf = ByteBuffer.allocate(configuration.getStreamBufferSize());
			SftpHandle h = file(path, OpenMode.SFTP_WRITE, OpenMode.SFTP_APPEND, OpenMode.SFTP_CREAT);
			OutputStream out = new SftpHandleOutputStream(this, buf, h) {
				@Override
				public void close() throws IOException {
					super.close();
					if (permissions > -1) {
						chmod(path, permissions);
					}
				}
			};
			return new SftpOutputStream(out, this, path, out.toString());
		} else {
			OutputStream putOut = doPut(path, offset);
			return new FilterOutputStream(putOut) {
				@Override
				public void close() throws IOException {
					super.close();
					if (permissions > -1)
						chmod(path, permissions);
				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					putOut.write(b, off, len);
				}
			};
		}
	}

	@Override
	public void resumeGet(String path, File destination) throws SshException {
		if (destination.isDirectory())
			destination = new File(destination, Util.basename(path));
		try (OutputStream out = new FileOutputStream(destination, true)) {
			get(path, out, destination.exists() ? destination.length() : 0);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public SftpFile lstat(String path) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public final String readLink(String path) throws SshException {
		return Util.relativeTo(doReadLink(path), getDefaultPath());
	}

	protected String doReadLink(String path) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void link(String path, String target) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resumePut(File source, String path) throws SshException {
		try (InputStream in = new FileInputStream(source)) {
			try {
				SftpFile existing = stat(path);
				if (existing.isDirectory())
					throw new SftpException(SftpException.SSH_FX_FILE_IS_A_DIRECTORY,
							String.format("Remote path %s is a directory.", path));
				else if (existing.isFile()) {
					long size = existing.getSize();
					in.skip(size);
					put(path, in, -1, size);
				} else
					throw new SftpException(SftpException.SSH_FX_NO_SUCH_FILE,
							String.format("Remote path %s is not a file.", path));
			} catch (SftpException se) {
				if (se.getCode() == SftpException.SSH_FX_NO_SUCH_FILE) {
					put(path, in);
				} else
					throw se;
			}
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	//
	//
	//
	//
	//
	//
	@Override
	public void rm(String path, boolean recurse) throws SftpException, SshException {
		visit(path, new SftpFileVisitor() {
			@Override
			public FileVisitResult postVisitDirectory(SftpFile dir, IOException exc) throws IOException {
				if (exc == null) {
					rmdir(dir.getPath());
					return FileVisitResult.CONTINUE;
				} else
					throw exc;
			}

			@Override
			public FileVisitResult visitFile(SftpFile file, BasicFileAttributes attrs) throws IOException {
				rm(file.getPath());
				return FileVisitResult.CONTINUE;
			}
		});
	}

	@Override
	public SftpOperation upload(File localdir, String remotedir, boolean recurse, boolean sync, boolean commit)
			throws SshException {
		try {
			DefaultSftpOperation op = new DefaultSftpOperation();
			Path root = localdir.toPath();
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					/*
					 * List corresponding remove directory and remove any paths
					 * that do not exist locally
					 */
					String rel = root.relativize(dir).toString();
					String remotepath = rel.equals("") ? remotedir : remotedir + "/" + rel;
					for (SftpFile file : list(remotepath)) {
						Path local = dir.resolve(file.getName());
						if (!Files.exists(local)) {
							removeAndCapture(commit, op, remotepath + "/" + file.getName());
						}
					}
					return super.postVisitDirectory(dir, exc);
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					if (recurse || dir.equals(root)) {
						Path rel = root.relativize(dir);
						String remotepath = remotedir + "/" + rel.toString();
						if (commit && !exists(remotepath))
							mkdir(remotepath);
						return FileVisitResult.CONTINUE;
					} else
						return FileVisitResult.SKIP_SUBTREE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Path rel = root.relativize(file);
					String remotepath = remotedir + "/" + rel.toString();
					try {
						SftpFile f = stat(remotepath);
						if (f.isDirectory()) {
							/*
							 * Copying up a file to a directory of same name, so
							 * delete existing directory
							 */
							removeAndCapture(commit, op, remotepath);
						} else if (f.isLink()) {
							if (commit) 
								rm(remotepath);
							op.deleted().add(remotepath);
						} else if (f.isFile()) {
							long f1 = f.getLastModified() / 1000;
							long f2 = Files.getLastModifiedTime(file).to(TimeUnit.SECONDS);
							if (f1 == f2) {
								op.unchanged().add(remotepath);
								return FileVisitResult.CONTINUE;
							}
							op.updated().add(remotepath);
						} else
							throw new IllegalStateException(String.format("%s is not a directory, file or link.", remotepath));
					} catch (SftpException se) {
						if (se.getCode() != SftpException.SSH_FX_NO_SUCH_FILE)
							throw se;
						op.created().add(remotepath);
					}
					if (commit) {
						put(file.toFile(), remotepath);
					}
					return FileVisitResult.CONTINUE;
				}

				private void removeAndCapture(boolean commit, DefaultSftpOperation op, String remotepath) throws SshException {
					visit(remotepath, new SftpFileVisitor() {
						@Override
						public FileVisitResult postVisitDirectory(SftpFile dir, IOException exc) throws IOException {
							if (exc == null) {
								if (commit)
									rmdir(dir.getPath());
								return FileVisitResult.CONTINUE;
							} else
								throw exc;
						}

						@Override
						public FileVisitResult visitFile(SftpFile file, BasicFileAttributes attrs) throws IOException {
							if (commit)
								rm(file.getPath());
							op.deleted().add(file.getPath());
							return FileVisitResult.CONTINUE;
						}
					});
				}
			});
			return op;
		} catch (SshException se) {
			throw se;
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to upload local files.", e);
		}
	}

	//
	// Other
	//
	@Override
	public FileVisitResult visit(String path, FileVisitor<SftpFile> visitor) throws SshException {
		/* Providers probably don't need to override this, it uses the {@link #directory()} methods,
		 * which are optimised when possible anyway. */
		SftpFile attrs = stat(path);
		try {
			if (attrs.isDirectory()) {
				FileVisitResult preVisitResult = visitor.preVisitDirectory(attrs, fileToBasicAttributes(attrs));
				try {
					if (preVisitResult != FileVisitResult.CONTINUE)
						return preVisitResult;
					try(DirectoryStream<SftpFile> stream = directory(path)) {
						for(SftpFile file : stream) {
							if (file.isLink() || file.isFile()) {
								FileVisitResult fileVisitResult = visitor.visitFile(file, fileToBasicAttributes(attrs));
								if (fileVisitResult != FileVisitResult.CONTINUE && fileVisitResult != FileVisitResult.SKIP_SUBTREE)
									return fileVisitResult;
							} else if (file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..")) {
								switch (visit(file.getPath(), visitor)) {
								case SKIP_SIBLINGS:
									break;
								case TERMINATE:
									return FileVisitResult.TERMINATE;
								default:
									continue;
								}
							}
						}
					}
					FileVisitResult postVisitResult = visitor.postVisitDirectory(attrs, null);
					if (postVisitResult != FileVisitResult.CONTINUE && postVisitResult != FileVisitResult.SKIP_SUBTREE)
						return postVisitResult;
				} catch (SftpException ioe) {
					FileVisitResult postVisitResult = visitor.postVisitDirectory(attrs, ioe);
					if (postVisitResult != FileVisitResult.CONTINUE && postVisitResult != FileVisitResult.SKIP_SUBTREE)
						return postVisitResult;
				}
			} else {
				FileVisitResult fileVisitResult = visitor.visitFile(attrs, fileToBasicAttributes(attrs));
				if (fileVisitResult != FileVisitResult.CONTINUE && fileVisitResult != FileVisitResult.SKIP_SUBTREE)
					return fileVisitResult;
			}
		} catch (IOException ioe) {
			if (ioe instanceof SshException)
				throw (SshException) ioe;
			else
				throw new SshException(SshException.IO_ERROR, String.format("I/O error when visiting %s", path), ioe);
		}
		return FileVisitResult.CONTINUE;
	}

	protected InputStream doDownload(String path) throws SshException {
		try {
			final PipedOutputStream pout = new PipedOutputStream();
			PipedInputStream pin = new PipedInputStream(pout);
			new Thread() {
				@Override
				public void run() {
					try {
						get(path, pout);
					} catch (SshException sshe) {
					}
				}
			}.start();
			return pin;
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	protected void doDownload(String path, OutputStream out) throws SshException {
		throw new UnsupportedOperationException("This provider does not support downloading to an OutputStream.");
	}

	protected InputStream doGet(String path, long filePointer) throws SshException {
		if (filePointer > 0) {
			throw new UnsupportedOperationException("This provider does not support setting of file pointer for downloads.");
		}
		return doDownload(path);
	}

	protected void doGet(String path, OutputStream out, long filePointer) throws SshException {
		if (filePointer > 0) {
			throw new UnsupportedOperationException("This provider does not support setting of file pointer for downloads.");
		}
		doDownload(path, out);
	}

	protected void doPut(String path, InputStream in, long offset) throws SshException {
		if (offset > 0) {
			throw new UnsupportedOperationException("This provider does not support setting of file pointer for uploads.");
		}
		doUpload(path, in);
	}

	protected OutputStream doPut(String path, long offset) throws SshException {
		if (offset > 0) {
			throw new UnsupportedOperationException("This provider does not support setting of file pointer for uploads.");
		}
		return doUpload(path);
	}

	protected OutputStream doUpload(String path) throws SshException {
		final PipedInputStream pin = new PipedInputStream();
		try {
			PipedOutputStream pout = new PipedOutputStream(pin);
			new Thread() {
				@Override
				public void run() {
					try {
						put(path, pin);
					} catch (SshException sshe) {
					}
				}
			}.start();
			return pout;
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	protected void doUpload(String path, InputStream in) throws SshException {
		throw new UnsupportedOperationException("This provider does not support uploading from an InputStream.");
	}

	protected boolean isUseRawSFTP(long offset) {
		return provider.getCapabilities().contains(Capability.RAW_SFTP);
	}

	protected void onEOLPolicyChange(EOLPolicy... eolPolicy2) {
	}

	protected void onTransferModeChange(TransferMode transferMode) {
	}
	
	protected boolean isOpenSSH() {
		return client.getRemoteIdentification() != null && client.getRemoteIdentification().contains("OpenSSH");
	}

	private BasicFileAttributes fileToBasicAttributes(SftpFile attrs) {
		return new BasicFileAttributes() {
			@Override
			public FileTime creationTime() {
				return FileTime.fromMillis(attrs.getCreated());
			}

			@Override
			public Object fileKey() {
				return attrs;
			}

			@Override
			public boolean isDirectory() {
				return attrs.isDirectory();
			}

			@Override
			public boolean isOther() {
				return !attrs.isDirectory() && !attrs.isFile() && !attrs.isLink();
			}

			@Override
			public boolean isRegularFile() {
				return attrs.isFile();
			}

			@Override
			public boolean isSymbolicLink() {
				return attrs.isLink();
			}

			@Override
			public FileTime lastAccessTime() {
				return FileTime.fromMillis(attrs.getAccessed());
			}

			@Override
			public FileTime lastModifiedTime() {
				return FileTime.fromMillis(attrs.getLastModified());
			}

			@Override
			public long size() {
				return attrs.getSize();
			}
		};
	}

	private void recurseMarkForDeletion(File file, DefaultSftpOperation op) throws SshException {
		String[] list = file.list();
		op.deleted().add(file.toString());
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				file = new File(list[i]);
				if (file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..")) {
					recurseMarkForDeletion(file, op);
				} else if (file.isFile()) {
					op.deleted().add(file.toString());
				}
			}
		}
	}

}
