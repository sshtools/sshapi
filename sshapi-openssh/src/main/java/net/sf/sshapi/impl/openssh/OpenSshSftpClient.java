package net.sf.sshapi.impl.openssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;

import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpInputStream;
import net.sf.sshapi.sftp.SftpOutputStream;
import net.sf.sshapi.util.CaptureInputStream;
import net.sf.sshapi.util.CaptureInputStream.Match;
import net.sf.sshapi.util.CaptureInputStream.MatchResult;
import net.sf.sshapi.util.CaptureInputStream.Matcher;
import net.sf.sshapi.util.Util;

public class OpenSshSftpClient extends AbstractSftpClient implements AbstractOpenSshClient {
	private static final String SFTP_PROMPT = "sftp> ";

	public enum Type {
		BLOCK, CHARACTER, DIRECTORY, FILE, LINK, SPECIAL;
		public static Type parse(char p) {
			switch (p) {
			case 'd':
				return DIRECTORY;
			case 'c':
				return CHARACTER;
			case 'b':
				return BLOCK;
			case 'l':
				return LINK;
			default:
				return FILE;
			}
		}

		public int toSSHAPIType() {
			switch (this) {
			case DIRECTORY:
				return SftpFile.TYPE_DIRECTORY;
			case CHARACTER:
				return SftpFile.TYPE_CHARACTER;
			case BLOCK:
				return SftpFile.TYPE_BLOCK;
			case LINK:
				return SftpFile.TYPE_LINK;
			default:
				return SftpFile.TYPE_FILE;
			}
		}
	}

	private static long parsePermissions(String perm) {
		int len = perm.length();
		long cp = 0;
		if (len >= 1) {
			cp = cp | ((perm.charAt(0) == 'r') ? SftpFile.S_IRUSR : 0);
		}
		if (len >= 2) {
			cp = cp | ((perm.charAt(1) == 'w') ? SftpFile.S_IWUSR : 0);
		}
		if (len >= 3) {
			cp = cp | ((perm.charAt(2) == 'x') ? SftpFile.S_IXUSR : 0);
		}
		if (len >= 4) {
			cp = cp | ((perm.charAt(3) == 'r') ? SftpFile.S_IRGRP : 0);
		}
		if (len >= 5) {
			cp = cp | ((perm.charAt(4) == 'w') ? SftpFile.S_IWGRP : 0);
		}
		if (len >= 6) {
			cp = cp | ((perm.charAt(5) == 'x') ? SftpFile.S_IXGRP : 0);
		}
		if (len >= 7) {
			cp = cp | ((perm.charAt(6) == 'r') ? SftpFile.S_IROTH : 0);
		}
		if (len >= 8) {
			cp = cp | ((perm.charAt(7) == 'w') ? SftpFile.S_IWOTH : 0);
		}
		if (len >= 9) {
			cp = cp | ((perm.charAt(8) == 'x') ? SftpFile.S_IXOTH : 0);
		}
		return cp;
	}

	private static String quoteArg(String arg) {
		// Wildcards go outside of quotes
		if (arg.indexOf(" ") != -1) {
			StringBuilder bui = new StringBuilder("\"");
			boolean inQuote = false;
			char[] charArray = arg.toCharArray();
			for (int i = 0; i < charArray.length; i++) {
				char c = charArray[i];
				if (c == '*') {
					if (inQuote) {
						bui.append("\"");
						inQuote = false;
					}
				} else {
					if (!inQuote) {
						bui.append("\"");
						inQuote = true;
					}
				}
				bui.append(c);
			}
			if (inQuote) {
				bui.append("\"");
			}
			return bui.toString();
		} else {
			return arg;
		}
	}

	// private CaptureOutputStream capture;
	private String defaultPath;
	private Object lock = new Object();
	private OutputStream out;
	private ProcessBuilder pb;
	private Process process;
	private OpenSshClient client;
	private CaptureInputStream in;

	public OpenSshSftpClient(OpenSshClient client, ProcessBuilder pb) {
		super(client.getProvider(), client.getConfiguration());
		this.client = client;
		this.pb = pb;
	}

	@Override
	public void chgrp(String path, int gid) throws SshException {
		synchronized (lock) {
			String[] results = processResults(runCommand(createCommand("chgrp", gid, path)));
			checkCommonErrors(null, results);
		}
	}

	@Override
	public void chmod(String path, int permissions) throws SshException {
		synchronized (lock) {
			permissions = permissions & ~0xffffe00;
			String ps = String.valueOf(permissions >> 6) + String.valueOf(((permissions >> 3) & ~0x1f8))
					+ String.valueOf((permissions & ~0x1f8));
			String[] results = processResults(runCommand(createCommand("chmod", ps, path)));
			checkCommonErrors(null, results);
		}
	}

	@Override
	public void chown(String path, int uid) throws SshException {
		synchronized (lock) {
			String[] results = processResults(runCommand(createCommand("chown", uid, path)));
			checkCommonErrors(null, results);
		}
	}

	@Override
	public void get(String path, File destination) throws SshException {
		synchronized (lock) {
			try {
				// TODO progress events somehow
				String[] results = processResults(runCommand(createCommand("get", path, destination)));
				checkCommonErrors(null, results);
			} catch (IOException ioe) {
				throw new SshException(SshException.IO_ERROR, ioe);
			}
		}
	}

	@Override
	protected InputStream doDownload(String path) throws SshException {
		try {
			final File file = createTempFile();
			String[] results = processResults(runCommand(createCommand("get", path, file.getAbsolutePath())));
			checkCommonErrors(null, results);
			FileInputStream in = new FileInputStream(file) {
				@Override
				public void close() throws IOException {
					try {
						super.close();
					} finally {
						file.delete();
					}
				}
			};
			return new SftpInputStream(in, this, path, in.toString());
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
	}

	@Override
	protected void doDownload(String path, OutputStream out) throws SshException {
		synchronized (lock) {
			try {
				final File file = createTempFile();
				try {
					String[] results = processResults(runCommand(createCommand("get", path, file.getAbsolutePath())));
					out = new SftpOutputStream(out, this, path, out.toString());
					checkCommonErrors(null, results);
					try (FileInputStream fin = new FileInputStream(file)) {
						Util.joinStreams(fin, out);
					}
				} finally {
					file.delete();
				}
			} catch (IOException ioe) {
				throw new SshException(SshException.IO_ERROR, ioe);
			}
		}
	}

	@Override
	public String getDefaultPath() throws SshException {
		return defaultPath;
	}

	@Override
	public SftpFile[] ls(String path) throws SshException {
		synchronized (lock) {
			StringBuilder bui = new StringBuilder("ls -aln");
			bui.append(" ");
			bui.append(quoteArg(path));
			String resultString = runCommand(bui.toString());
			String[] presults = processResults(resultString);
			checkCommonErrors(path, presults);
			List<SftpFile> files = new ArrayList<>();
			for (String r : presults) {
				if (!r.equals("")) {
					files.add(createFile(path, r));
				}
			}
			return files.toArray(new SftpFile[0]);
		}
	}

	@Override
	public void mkdir(String path, int permissions) throws SshException {
		synchronized (lock) {
			String[] results = processResults(runCommand(createCommand("mkdir", path)));
			checkCommonErrors(path, results);
		}
	}

	@Override
	protected void doUpload(String path, InputStream in) throws SshException {
		synchronized (lock) {
			try {
				File file = createTempFile();
				try {
					FileUtils.copyInputStreamToFile(in, file);
					String[] results = processResults(runCommand(createCommand("put", file.getAbsolutePath(), path)));
					checkCommonErrors(null, results);
				} finally {
					file.delete();
				}
			} catch (IOException ioe) {
				throw new SshException(SshException.IO_ERROR, ioe);
			}
		}
	}

	@Override
	public void rename(String oldpath, String newpath) throws SshException {
		synchronized (lock) {
			String[] results = processResults(runCommand(createCommand("rename", oldpath, newpath)));
			checkCommonErrors(oldpath, results);
		}
	}

	@Override
	public void rm(String path) throws SshException {
		synchronized (lock) {
			String[] results = processResults(runCommand(createCommand("rm", path)));
			checkCommonErrors(path, results);
		}
	}

	@Override
	public void rmdir(String path) throws SshException {
		synchronized (lock) {
			String[] results = processResults(runCommand(createCommand("rmdir", path)));
			checkCommonErrors(path, results);
		}
	}

	@Override
	public void setLastModified(String path, long modtime) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SftpFile stat(String path) throws SshException {
		synchronized (lock) {
			String resultString = runCommand(createCommand("ls", "-aln", path));
			String[] presults = processResults(resultString);
			checkCommonErrors(path, presults);
			List<SftpFile> files = new ArrayList<>();
			boolean isDir = false;
			for (String r : presults) {
				if (!r.equals("")) {
					SftpFile e = createFile(path, r);
					if (!e.getPath().equals(path)) {
						isDir = true;
					}
					files.add(e);
				}
			}
			// Maverick returns no output, OpenSSH returns . and ..
			if (isDir || files.size() == 0 || files.get(0).getName().equals(".")) {
				// Is a directory, so instead read the parent directory
				int idx = path.lastIndexOf("/");
				String name = null;
				if (idx == -1) {
					name = path;
					path = getDefaultPath();
				} else {
					name = path.substring(idx + 1);
					path = path.substring(0, idx);
				}
				for (SftpFile f : ls(path)) {
					if (f.getName().equals(name)) {
						return f;
					}
				}
				throw new SftpException(SftpException.SSH_FX_NO_SUCH_FILE, String.format("No file %s", path));
			} else {
				return files.get(0);
			}
		}
	}

	@Override
	public void symlink(String oldpath, String newpath) throws SshException {
		synchronized (lock) {
			String[] results = processResults(runCommand(createCommand("ln", "-s", oldpath, newpath)));
			checkCommonErrors(oldpath, results);
		}
	}

	@Override
	public void resumeGet(String path, File destination) throws SshException {
		synchronized (lock) {
			try {
				SftpFile existing = stat(path);
				long remoteLength = existing.getSize();
				long length = destination.exists() ? (remoteLength - destination.length()) : remoteLength;
				fireFileTransferStarted(path, destination.getPath(), length);
				String[] results = processResults(runCommand(createCommand("reget", path, destination)));
				checkCommonErrors(null, results);
				fireFileTransferProgressed(path, destination.getPath(), length);
				fireFileTransferFinished(path, destination.getPath());
			} catch (IOException ioe) {
				throw new SshException(SshException.IO_ERROR, ioe);
			}
		}
	}

	@Override
	public void resumePut(File source, String path) throws SshException {
		synchronized (lock) {
			try {
				long length = source.length();
				try {
					SftpFile existing = stat(path);
					length = length - existing.getSize();
				}
				catch(SftpException se) {
					if(se.getCode() != SftpException.SSH_FX_NO_SUCH_FILE)
						throw se;
				}
				fireFileTransferStarted(source.getPath(), path, length);
				String[] results = processResults(runCommand(createCommand("reput", source.getAbsolutePath(), path)));
				checkCommonErrors(null, results);
				fireFileTransferProgressed(source.getPath(), path, length);
				fireFileTransferFinished(source.getPath(), path);
			} catch (IOException ioe) {
				throw new SshException(SshException.IO_ERROR, ioe);
			}
		}
	}

	@Override
	protected void onClose() throws SshException {
		synchronized (lock) {
			runCommand(createCommand("exit"), false);
			try {
				out.close();
			} catch (IOException e1) {
			}
			try {
				int result = process.waitFor();
				if (result != 0)
					SshConfiguration.getLogger().log(Level.WARN, String.format("Sftp client exited with non-zero code %d", result));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				process = null;
			}
		}
	}

	@Override
	protected void onOpen() throws SshException {
		try {
			Semaphore sftpPromptSem = new Semaphore(1);
			sftpPromptSem.acquire();
			pb.redirectErrorStream(true);
			process = client.setupAuthentication(pb).start();
			out = process.getOutputStream();
			in = new CaptureInputStream(new Matcher() {
				@Override
				public MatchResult matched(Match pattern, CaptureInputStream capture) throws IOException {
					sftpPromptSem.release();
					return MatchResult.END;
				}

				@Override
				public MatchResult noMatch(Match pattern, CaptureInputStream capture) throws IOException {
					sftpPromptSem.release();
					return MatchResult.END;
				}
			}, SFTP_PROMPT, process.getInputStream());
			in.setCapture(true);
			Thread readErr = new Thread("ReadSftpStderr") {
				@Override
				public void run() {
					try {
						Util.joinStreams(process.getErrorStream(), System.err);
					} catch (Exception ioe) {
					}
				}
			};
			readErr.start();
			try {
				if (in.readUntilEOFEndedOrActive() > 0) {
					sftpPromptSem.acquire();
				}
			} finally {
				sftpPromptSem.release();
			}
			try {
				readErr.join();
				if (process.exitValue() != 0)
					throw new SshException(SshException.AUTHENTICATION_FAILED,
							"Failed to connect (deferred authentication failed).");
			} catch (IllegalThreadStateException ise) {
				// Still running
			}
			in.reset();
			in.setCapture(false);
			Thread readInput = new Thread("ReadSftpStdin") {
				@Override
				public void run() {
					try {
						Util.joinStreams(in, new NullOutputStream());
					} catch (Exception ioe) {
					}
				}
			};
			readInput.start();
			defaultPath = pwd();
		} catch (SshException sshe) {
			throw sshe;
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, "Failed to connect.", ioe);
		} catch (InterruptedException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to connect.", e);
		}
	}

	private void checkCommonErrors(String path, String[] results) throws SshException {
		for (String r : results) {
			if (r.endsWith(": No such file or directory") || r.endsWith("\" not found")) {
				throw new SftpException(SftpException.SSH_FX_NO_SUCH_FILE.getServerCode(), "Could not find remote path " + path);
			}
			if (r.endsWith(": Failure")) {
				SftpFile f = null;
				try {
					f = stat(path);
				} catch (Exception e) {
					throw new SftpException(SftpException.SSH_FX_NO_SUCH_FILE, r);
				}
				if (r.equals("Couldn't delete file: Failure") && f != null && f.isDirectory()) {
					throw new SftpException(SftpException.SSH_FX_FILE_IS_A_DIRECTORY, r);
				}
				throw new SshException(SshException.GENERAL, r);
			}
		}
	}

	private String createCommand(Object... args) {
		StringBuilder b = new StringBuilder();
		for (Object o : args) {
			String a = String.valueOf(o);
			if (a.length() > 0 && b.length() > 0) {
				b.append(" ");
			}
			if (a.indexOf(" ") == -1) {
				b.append(a);
			} else {
				b.append("\"");
				b.append(a);
				b.append("\"");
			}
		}
		return b.toString();
	}

	private SftpFile createFile(String parentPath, String r) throws SshException {
		StringBuilder bui = new StringBuilder();
		StringBuilder dateWord = new StringBuilder();
		int p = 0;
		int uid = 0, gid = 0, permissions = 0;
		long size = 0;
		Type type = Type.FILE;
		for (int i = 0; i < r.length(); i++) {
			char c = r.charAt(i);
			if (c == ' ' && p < 8) {
				if (bui.length() > 0) {
					String s = bui.toString();
					switch (p) {
					case 0:
						type = Type.parse(s.charAt(0));
						permissions = (int) parsePermissions(s.substring(1));
						p++;
						break;
					case 1:
						p++;
						break;
					case 2:
						uid = Integer.parseInt(s);
						p++;
						break;
					case 3:
						gid = Integer.parseInt(s);
						p++;
						break;
					case 4:
						size = Long.parseLong(s);
						p++;
						break;
					case 5:
					case 6:
					case 7:
						if (dateWord.length() > 0) {
							dateWord.append(" ");
						}
						dateWord.append(s);
						p++;
						break;
					}
					bui.setLength(0);
				}
			} else {
				bui.append(c);
			}
		}
		String lsname = bui.toString();
		String name;
		if (lsname.equals(parentPath)) {
			int idx = lsname.lastIndexOf('/');
			if (idx == -1)
				name = lsname;
			else
				name = lsname.substring(idx + 1);
		} else
			name = lsname.substring(parentPath.length() + 1);
		String dateString = dateWord.toString();
		String dateStr = dateString.substring(0, dateString.lastIndexOf(' '));
		String timeStr = dateString.substring(dateString.lastIndexOf(' ') + 1);
		// type = Type.parse(r.charAt(0));
		// permissions = parsePermissions(r.substring(1, 10));
		// links = Integer.parseInt(r.substring(10, 15).trim());
		// uid = Integer.parseInt(r.substring(15, 20).trim());
		// gid = Integer.parseInt(r.substring(20, 29).trim());
		// size = Long.parseLong(r.substring(29, 42).trim());
		// name = r.substring(56);
		// String dateStr = r.substring(43, 49).trim();
		// String timeStr = r.substring(50, 55).trim();
		String path;
		if (name.startsWith("/")) {
			path = name;
		} else {
			path = parentPath + "/" + name;
		}
		int idx = name.lastIndexOf("/");
		if (idx != -1) {
			name = name.substring(idx + 1);
		}
		Calendar c = Calendar.getInstance();
		try {
			Date d = new SimpleDateFormat("MMM dd").parse(dateStr);
			Calendar c2 = Calendar.getInstance();
			c2.setTime(d);
			c.set(Calendar.MONTH, c2.get(Calendar.MONTH));
			c.set(Calendar.DAY_OF_MONTH, c2.get(Calendar.DAY_OF_MONTH));
		} catch (ParseException pe) {
			c.setTimeInMillis(0);
		}
		if (timeStr.indexOf(":") == -1) {
			c.set(Calendar.YEAR, Integer.parseInt(timeStr));
			c.set(Calendar.HOUR, 0);
			c.set(Calendar.MINUTE, 0);
		} else {
			String[] dm = timeStr.split(":");
			c.set(Calendar.HOUR, Integer.parseInt(dm[0]));
			c.set(Calendar.MINUTE, Integer.parseInt(dm[1]));
		}
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long lastModified = c.getTimeInMillis();
		return new SftpFile(type.toSSHAPIType(), path, size, lastModified, 0, 0, gid, uid, permissions);
	}

	private File createTempFile() throws IOException {
		File file = File.createTempFile("javaopensshftp", "put");
		file.deleteOnExit();
		return file;
	}

	private String[] processResults(String results) {
		String[] s = results.split("\n");
		for (int i = 0; i < s.length; i++) {
			s[i] = s[i].trim();
		}
		return s;
	}

	private String pwd() throws IOException {
		String[] results = processResults(runCommand("pwd"));
		checkCommonErrors(null, results);
		String r = results[0];
		if (r.startsWith("Remote working directory: ")) {
			r = r.substring(26);
		}
		return r;
	}

	private String runCommand(String cmd) throws SshException {
		return runCommand(cmd, true);
	}

	private String runCommand(String cmd, boolean waitForReply) throws SshException {
		Semaphore sftpPromptSem = new Semaphore(1);
		try {
			in.reset();
			in.setMatcher((m, c) -> {
				sftpPromptSem.release();
				return MatchResult.CONTINUE;
			});
			if (waitForReply) {
				in.setCapture(true);
				sftpPromptSem.acquire();
			}
			SshConfiguration.getLogger().log(Level.DEBUG, String.format("Run command '%s'", cmd));
			String cmdString = cmd + "\n";
			out.write(cmdString.getBytes());
			out.flush();
			if (waitForReply) {
				sftpPromptSem.acquire();
				String capturedString = in.getCapturedString();
				int sidx = cmdString.length() + 1;
				int idx = Math.max(sidx, capturedString.length() - SFTP_PROMPT.length() - 2);
				String result = capturedString.substring(sidx, idx);
				SshConfiguration.getLogger().log(Level.DEBUG, String.format("Got %d bytes result for '%s'", result.length(), cmd));
				return result;
			} else
				return null;
		} catch (IOException ioe) {
			throw new SshException(SshException.GENERAL, ioe);
		} catch (InterruptedException e) {
			throw new SshException(SshException.GENERAL, e);
		} finally {
			in.setCapture(false);
			if (waitForReply) {
				sftpPromptSem.release();
			}
		}
	}
}
