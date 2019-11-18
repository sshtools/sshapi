package com.maverick.ssh.tests.server.openssh;

import static com.maverick.ssh.tests.Util.configureCommand;
import static com.maverick.ssh.tests.Util.runAndCheckReturn;
import static com.maverick.ssh.tests.Util.runToFileAndCheckReturn;
import static com.maverick.ssh.tests.Util.runWithFileInput;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.maverick.ssh.tests.AbstractServer;
import com.maverick.ssh.tests.ServerCapability;
import com.maverick.ssh.tests.SshTestConfiguration;
import com.maverick.ssh.tests.Util;

import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshConfiguration;

/**
 * <p>
 * This server implementation can download a specific version of OpenSSH, and
 * extract, configure, make and make install it. It then starts a daemon from
 * this installation for tests to run against.
 * 
 * <p>
 * It is also capable of apply patches to the source, such as adding X509
 * support.
 */
public class LocalOpenSSHServerServiceImpl extends AbstractServer {
	private static boolean hookAdded = false;
	private File rootDir;
	private File sourceDir;
	private SshTestConfiguration configuration;
	private Properties serviceProperties;
	private String version;
	private File rootTargetDir;
	private File targetDir;
	private File sudoAskPassFile;
	private Process serverProcess;
	private File patchesDir;
	private File patchDir;

	protected void doStop() throws Exception {
		SshConfiguration.getLogger().log(Level.INFO, String.format("Stopping server on %d", configuration.getPort()));
		if (serverProcess != null) {
			File pidFile = new File(targetDir, "sshd.pid");
			if (pidFile.exists()) {
				try (FileReader fr = new FileReader(pidFile)) {
					int pid = Integer.parseInt(IOUtils.toString(fr).trim());
					SshConfiguration.getLogger().log(Level.INFO, String.format("Attempting to kill %d", pid));
					runAndCheckReturn(
							configureCommandForSudo(rootDir, new ProcessBuilder("sudo", "-A", "kill", String.valueOf(pid))));
				}
			} else if (!serverProcess.getClass().getName().equals("java.lang.UNIXProcess")) {
				serverProcess.destroy();
			} else {
				Field field = serverProcess.getClass().getDeclaredField("pid");
				field.setAccessible(true);
				int pid = ((Long) field.getLong(serverProcess)).intValue();
				SshConfiguration.getLogger().log(Level.INFO, String.format("Attempting to kill %d", pid));
				try {
					runAndCheckReturn(
							configureCommandForSudo(rootDir, new ProcessBuilder("sudo", "-A", "kill", String.valueOf(pid))));
				} catch (IOException ioe) {
					SshConfiguration.getLogger().log(Level.WARN, "Error from killing sshd.", ioe);
				}
			}
			// Wait for server to actually stop
			for (int i = 0; i < 100; i++) {
				try {
					ServerSocket s = new ServerSocket(configuration.getPort());
					s.setReuseAddress(true);
					s.close();
					return;
				} catch (Exception e) {
					Thread.sleep(100);
				}
			}
			throw new IOException(
					"Server did not appear to stop. This may be a sign of a previously failed test that left it's SSHD process running. Check the process list for still running SSHD daemons and kill them (except your own local one if any)");
		}
	}

	public List<ServerCapability> init(SshTestConfiguration configuration, Properties serviceProperties) throws Exception {
		this.configuration = configuration;
		this.serviceProperties = serviceProperties;
		if (!configuration.getName().endsWith("openssh-local-server")) {
			throw new Exception("This server is not intended for use with this configuration.");
		}
		if (!hookAdded) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					if (isStarted()) {
						try {
							LocalOpenSSHServerServiceImpl.this.stop();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
			hookAdded = true;
		}
		return Arrays.asList(ServerCapability.SFTP_LS_RETURNS_DOTS, ServerCapability.SUPPORTS_GROUPS,
				ServerCapability.SUPPORTS_PERMISSIONS, ServerCapability.SUPPORTS_OWNERS, ServerCapability.SYMLINKS,
				ServerCapability.X509);
	}

	protected void doStart() throws Exception {
		rootDir = new File(System.getProperty("user.home"), "openssh");
		rootTargetDir = new File(rootDir, "targets");
		patchesDir = new File(rootDir, "patches");
		version = serviceProperties.getProperty("openssh.version");
		patchDir = new File(patchesDir, version);
		targetDir = new File(rootTargetDir, version);
		targetDir.mkdirs();
		sourceDir = new File(rootDir, "openssh-" + version);
		sudoAskPassFile = Util.createAskPass(configuration.getSudoPassword().toCharArray());
		if (downloadPatches()) {
			// If any patches were downloaded, then we should do everything from
			// scratch
			FileUtils.deleteDirectory(sourceDir);
		}
		if (!sourceDir.exists()) {
			/*
			 * If there is no source directory, make sure there is no target
			 * either. This means you need to delete source directory for a
			 * completely fresh install
			 */
			sudoRm(targetDir);
			download();
			applyPatches();
		}
		File configFile = new File(sourceDir, "config.log");
		if (!configFile.exists()) {
			runConfigure();
		}
		runAutoreconf();
		File sshdFile = new File(sourceDir, "sshd");
		if (!sshdFile.exists()) {
			runMake();
		}
		if (!targetDir.exists()) {
			runMakeInstall();
		}
		File serverConfigDir = new File(targetDir, "etc");
		File sshdConfig = new File(serverConfigDir, "sshd_config");
		runAndCheckReturn(
				configureCommandForSudo(rootDir, new ProcessBuilder("sudo", "-A", "chmod", "o+w", sshdConfig.getAbsolutePath())));
		OpenSSHConfigFileParser parser = new OpenSSHConfigFileParser(sshdConfig);
		parser.set("UsePAM", "yes");
		parser.set("PasswordAuthentication", "yes");
		parser.set("ChallengeResponseAuthentication", "yes");
		parser.set("PidFile", new File(targetDir, "sshd.pid").getAbsolutePath());
		parser.set("KexAlgorithms", "+diffie-hellman-group1-sha1");
		parser.set("Ciphers", "+3des-cbc");
		parser.set("PubkeyAcceptedKeyTypes", "+ssh-dss");
		// if(methods.size() > 0) {
		// parser.set("AuthenticationMethods", Util.listToSeparatedString(',',
		// methods));
		// }
		// else {
		parser.comment("AuthenticationMethods");
		// }
		parser.save(sshdConfig);
		runAndCheckReturn(
				configureCommandForSudo(rootDir, new ProcessBuilder("sudo", "-A", "chmod", "o-w", sshdConfig.getAbsolutePath())));
		// Finally actually start the server
		runServer();
		//
		//
		List<String> fps = new ArrayList<String>();
		for (File file : serverConfigDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".pub");
			}
		})) {
			ProcessBuilder pb = new ProcessBuilder(new File(new File(targetDir, "bin"), "ssh-keygen").getPath(), "-E",
					configuration.getFingerprintHashingAlgorithm(), "-lf", file.getAbsolutePath());
			pb.redirectErrorStream(true);
			SshConfiguration.getLogger().log(Level.INFO, String.format("Loading server key %s.", file));
			Process p = pb.start();
			String keystring = IOUtils.toString(p.getInputStream(), "UTF-8");
			String fp = keystring.split("\\s+")[1];
			int idx = fp.indexOf(':');
			fp = fp.substring(idx + 1);
			SshConfiguration.getLogger().log(Level.INFO, String.format("        %s.", fp));
			fps.add(fp);
		}
		configuration.setFingerprints(fps.toArray(new String[0]));
	}

	private void applyPatches() throws MalformedURLException, IOException, InterruptedException {
		for (String s : serviceProperties.getProperty("patches").split("\\|")) {
			// Extract arguments
			String[] args = new String[] { "patch", "-p", "1" };
			if (s.startsWith("[")) {
				int end = s.indexOf("]");
				args = s.substring(1, end).split(" ");
			}
			URL url = new URL(s);
			String n = url.getPath();
			int idx = n.lastIndexOf("/");
			String base = n.substring(idx + 1);
			File patchFile = new File(patchDir, base);
			if (patchFile.exists()) {
				// Decompress if needed
				if (patchFile.getName().endsWith(".gz")) {
					ProcessBuilder pb = new ProcessBuilder("gunzip", "-c", patchFile.getAbsolutePath());
					patchFile = new File(patchDir, "patch.tmp");
					pb.redirectErrorStream(true);
					runToFileAndCheckReturn(patchFile, pb);
				}
				// Patch
				SshConfiguration.getLogger().log(Level.INFO, String.format("Patching %s with %s", sourceDir, patchFile));
				runWithFileInput(patchFile, configureCommand(sourceDir, new ProcessBuilder(args).redirectErrorStream(true)), false);
				// runAndCheckReturnWithFileInput(patchFile,
				// configureCommand(sourceDir, new
				// ProcessBuilder(args).redirectErrorStream(true)));
			}
		}
	}

	private boolean downloadPatches() throws MalformedURLException, FileNotFoundException, IOException {
		boolean downloaded = false;
		String[] patches = serviceProperties.getProperty("patches", "").split("\\|");
		for (String s : patches) {
			if (s.startsWith("[")) {
				int end = s.indexOf("]");
				s = s.substring(end + 1);
			}
			URL url = new URL(s);
			String n = url.getPath();
			int idx = n.lastIndexOf("/");
			String base = n.substring(idx + 1);
			File patchFile = new File(patchDir, base);
			if (!patchFile.exists()) {
				patchDir.mkdirs();
				System.out.println("Opening patch at " + url);
				FileOutputStream fos = new FileOutputStream(patchFile);
				try {
					IOUtils.copy(url.openStream(), fos);
					downloaded = true;
				} finally {
					fos.close();
				}
				System.out.println("Downloaded patch at " + patchFile);
			}
		}
		return downloaded;
	}

	void runMake() throws IOException, InterruptedException {
		System.out.println("Compiling");
		runAndCheckReturn(configureCommand(sourceDir, new ProcessBuilder("make")));
	}

	void sudoRm(File dir) throws IOException, InterruptedException {
		SshConfiguration.getLogger().log(Level.INFO, String.format("Removing %s", dir));
		runAndCheckReturn(configureCommandForSudo(rootDir, new ProcessBuilder("sudo", "-A", "rm", "-fr", dir.getAbsolutePath())));
	}

	void runMakeInstall() throws IOException, InterruptedException {
		SshConfiguration.getLogger().log(Level.INFO, String.format("Installing server from  %s", sourceDir));
		runAndCheckReturn(configureCommandForSudo(sourceDir, new ProcessBuilder("sudo", "-A", "make", "install")));
	}

	void runServer() throws IOException, InterruptedException {
		SshConfiguration.getLogger().log(Level.INFO, String.format("Starting server on %d", configuration.getPort()));
		File sshd = new File(new File(targetDir, "sbin"), "sshd");
		List<String> args = Arrays.asList("sudo", "-A", sshd.getAbsolutePath(), "-p", String.valueOf(configuration.getPort()), "-D",
				"-e");
		SshConfiguration.getLogger().log(Level.INFO, String.format("    %s", Util.listToSeparatedString(' ', args)));
		ProcessBuilder pb = new ProcessBuilder(args);
		configureCommandForSudo(targetDir, pb);
		pb.redirectErrorStream(true);
		serverProcess = pb.start();
		Util.monitorProcessOut(serverProcess);
		// Wait for server to actually start
		for (int i = 0; i < 100; i++) {
			try {
				Socket s = new Socket(configuration.getServer(), configuration.getPort());
				s.setReuseAddress(true);
				s.close();
				return;
			} catch (ConnectException e) {
			}
			Thread.sleep(100);
		}
		throw new IOException("Server did not appear to start.");
	}

	void runConfigure() throws IOException, InterruptedException {
		SshConfiguration.getLogger().log(Level.INFO,
				String.format("Configuring (compile in %s, install to %s", sourceDir, targetDir));
		// PAM is needed for KBI
		runAndCheckReturn(configureCommand(sourceDir,
				new ProcessBuilder("./configure", "--prefix=" + targetDir.getAbsolutePath(), "--with-pam")));
	}

	void runAutoreconf() throws IOException, InterruptedException {
		SshConfiguration.getLogger().log(Level.INFO, "Running Autoreconf");
		// PAM is needed for KBI
		runAndCheckReturn(configureCommand(sourceDir, new ProcessBuilder("autoreconf")));
	}

	void download() throws IOException, ArchiveException {
		rootDir.mkdirs();
		String url = serviceProperties.getProperty("openssh.url");
		url = url.replace("${openssh.version}", serviceProperties.getProperty("openssh.version"));
		String name = url.substring(url.lastIndexOf("/") + 1);
		URL urlObj = new URL(url);
		SshConfiguration.getLogger().log(Level.INFO, String.format("Download %s", urlObj));
		File archiveFile = new File(rootDir, name);
		FileOutputStream fos = new FileOutputStream(archiveFile);
		try {
			IOUtils.copy(urlObj.openStream(), fos);
		} finally {
			fos.close();
		}
		SshConfiguration.getLogger().log(Level.INFO, String.format("Download to %s", archiveFile));
		FileInputStream fin = new FileInputStream(archiveFile);
		try {
			GzipCompressorInputStream gzIn = new GzipCompressorInputStream(fin);
			TarArchiveInputStream input = new TarArchiveInputStream(gzIn);
			try {
				TarArchiveEntry entry = null;
				while ((entry = input.getNextTarEntry()) != null) {
					System.out.println(entry.getName());
					File outFile = new File(rootDir, entry.getName());
					if (entry.isDirectory()) {
						outFile.mkdirs();
					} else {
						FileOutputStream fout = new FileOutputStream(outFile);
						try {
							IOUtils.copyLarge(input, fout, 0, entry.getSize());
						} finally {
							fout.close();
						}
						Set<PosixFilePermission> pfp = new HashSet<>();
						if ((entry.getMode() & 1) != 0)
							pfp.add(PosixFilePermission.OTHERS_EXECUTE);
						if ((entry.getMode() & 2) != 0)
							pfp.add(PosixFilePermission.OTHERS_WRITE);
						if ((entry.getMode() & 4) != 0)
							pfp.add(PosixFilePermission.OTHERS_READ);
						if ((entry.getMode() & 8) != 0)
							pfp.add(PosixFilePermission.GROUP_EXECUTE);
						if ((entry.getMode() & 16) != 0)
							pfp.add(PosixFilePermission.GROUP_WRITE);
						if ((entry.getMode() & 32) != 0)
							pfp.add(PosixFilePermission.GROUP_READ);
						if ((entry.getMode() & 64) != 0)
							pfp.add(PosixFilePermission.OWNER_EXECUTE);
						if ((entry.getMode() & 128) != 0)
							pfp.add(PosixFilePermission.OWNER_WRITE);
						if ((entry.getMode() & 256) != 0)
							pfp.add(PosixFilePermission.OWNER_READ);
						try {
							Files.setPosixFilePermissions(outFile.toPath(), pfp);
						} catch (UnsupportedOperationException uoe) {
							// LDP this breaks 1.5 build. Does it break the test
							outFile.setExecutable(((entry.getMode() & 64) != 0) || ((entry.getMode() & 8) != 0),
									((entry.getMode() & 8) == 0));
							outFile.setReadable(((entry.getMode() & 256) != 0) || ((entry.getMode() & 32) != 0),
									(entry.getMode() & 32) == 0);
							outFile.setWritable(((entry.getMode() & 128) != 0) || ((entry.getMode() & 16) != 0),
									(entry.getMode() & 16) == 0);
						}
					}
				}
			} finally {
				input.close();
			}
		} finally {
			fin.close();
		}
		// The archive will have a single directory at its root, so move this
	}

	private ProcessBuilder configureCommandForSudo(File dir, ProcessBuilder pb) {
		pb.environment().put("SUDO_ASKPASS", sudoAskPassFile.getAbsolutePath());
		return configureCommand(dir, pb);
	}
}
