import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jline.ConsoleReader;
import jline.Terminal;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.Logger;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshFileTransferListener;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshSCPClient;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.util.BatchHostKeyValidator;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsoleKeyboardInteractiveAuthenticator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.PEMFilePublicKeyAuthenticator;
import net.sf.sshapi.util.Util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;

/**
 * Java clone of the de-facto standard OpenSSH scp command.
 */
public class scp implements SshFileTransferListener, Logger {

	private SshConfiguration configuration;
	private String source;
	private String target;
	private Options options;
	private int port;
	private File identityFile;
	private boolean recursive;
	private long transferLength;
	private String transferPath;
	private long transferProgressed;
	private long transferLastUpdate;
	private int transferSpeed;
	private Terminal terminal;
	private ConsoleReader reader;
	private long transferBlock;
	private String providerClass;
	private SshProvider provider;
	private boolean compress;
	private boolean batch;
	private String cipher;
	private int verbosity;
	private Level level;
	private boolean traces;

	/**
	 * Constructor.
	 * 
	 * @throws SshException
	 */
	public scp() throws SshException {
		SshConfiguration.setLogger(this);
		configuration = new SshConfiguration();
		options = new Options();
		buildOptions(options);

		try {
			terminal = Terminal.setupTerminal();
			reader = new ConsoleReader();
			// terminal.beforeReadLine(reader, "", (char)0);
		} catch (Exception e) {
			e.printStackTrace();
			terminal = null;
		}

	}

	/**
	 * Start the command.
	 * 
	 * @throws SshException
	 * @throws IOException
	 */
	public void start() throws SshException, IOException {
		provider = null;
		if (providerClass != null) {
			try {
				provider = (SshProvider) Class.forName(providerClass).newInstance();
			} catch (Exception e) {
				log(Level.WARN, "SSH provider " + providerClass + " not accessible: Falling back to first available provider.");
			}
		}
		if (provider == null) {
			provider = DefaultProviderFactory.getInstance().getProvider(configuration);
		}

		if (compress) {
			if (provider.getSupportedCompression().size() > 0) {
				configuration.setPreferredClientToServerCompression((String) provider.getSupportedCompression().get(0));
				configuration.setPreferredServerToClientCompression((String) provider.getSupportedCompression().get(0));
			} else {
				log(Level.WARN, "SSH provider " + providerClass + " does not support compression, disabling.");
			}
		}

		if (cipher != null) {
			List ciphers = provider.getSupportedCiphers(configuration.getProtocolVersion());
			if (ciphers.contains(cipher)) {
				throw new SshException(SshException.UNSUPPORTED_FEATURE, "The cipher " + cipher + " is not supported.");
			}
			configuration.setPreferredClientToServerCipher(cipher);
			configuration.setPreferredServerToClientCipher(cipher);
		}

		SshHostKeyManager keyManager = provider.createHostKeyManager(configuration);
		SshHostKeyValidator validator = null;
		if (batch) {
			if (keyManager == null) {
				throw new SshException("This provider does not support key management, so batch mode may not be used.");
			}
			validator = new BatchHostKeyValidator(keyManager);
		} else {
			validator = new ConsoleHostKeyValidator();
		}
		configuration.setHostKeyValidator(validator);
		if (isRemotePath(source)) {
			if (isRemotePath(target)) {
				// Remote to remote
				remoteToRemote();
			} else {
				// Remote to local
				remoteToLocal();
			}
		} else {
			if (isRemotePath(target)) {
				localToRemote();
				// Local to remote
			} else {
				// Local to local
			}
		}
	}

	void remoteToRemote() throws SshException, IOException {
		connect(getConnectionDetails(source));
		connect(getConnectionDetails(target));
	}

	void remoteToLocal() throws SshException, IOException {
		connect(getConnectionDetails(source));
	}

	void localToRemote() throws SshException, IOException {
		String targetPath = getPath(target);
		SshClient client = connect(getConnectionDetails(target));
		try {
			SshSCPClient scp = client.createSCPClient();
			scp.addFileTransferListener(this);
			scp.open();
			try {
				scp.put(targetPath, "0770", checkPath(source), recursive);
			} finally {
				scp.close();
			}
		} finally {
			client.disconnect();
		}
	}

	File checkPath(String path) throws FileNotFoundException {
		File file = new File(path);
		if (!file.exists()) {
			throw new FileNotFoundException(path);
		}
		return file;
	}

	SshClient connect(String connectionDetails) throws SshException, IOException {
		// Connect
		SshClient client = provider.createClient(configuration);
		client.connect(extractUsername(connectionDetails), extractHostname(connectionDetails), port);
		List authenticators = new ArrayList();
		SshPasswordAuthenticator pwAuth = null;

		// Create the non-batch authenticators
		if (!batch) {
			if (terminal == null) {
				pwAuth = new ConsolePasswordAuthenticator();
			} else {
				pwAuth = new JLinePasswordAuthenticator(terminal, reader);
			}
		}

		// Key authentication
		if (identityFile != null && identityFile.exists()) {
			authenticators.add(new PEMFilePublicKeyAuthenticator(pwAuth, identityFile));
		}

		// Add the non-batch authenticators
		if (!batch) {
			authenticators.add(pwAuth);
			authenticators.add(new ConsoleKeyboardInteractiveAuthenticator());
		}

		// Now authenticate
		for (Iterator it = authenticators.iterator(); it.hasNext();) {
			SshAuthenticator auth = (SshAuthenticator) it.next();
			for (int i = 0; i < 3; i++) {
				if (client.authenticate(new SshAuthenticator[] { auth })) {
					return client;
				}
			}
		}

		// Failed
		throw new SshException("Permission denied.");
	}

	String extractUsername(String connectionDetails) {
		int idx = connectionDetails.indexOf('@');
		if (idx == -1) {
			throw new IllegalArgumentException("User name for remote path not provided.");
		}
		return connectionDetails.substring(0, idx);
	}

	String extractHostname(String connectionDetails) {
		int idx = connectionDetails.indexOf('@');
		return idx == -1 ? connectionDetails : connectionDetails.substring(idx + 1);
	}

	String getConnectionDetails(String path) {
		int idx = path.indexOf(":");
		if (idx == -1) {
			return null;
		}
		return path.substring(0, idx);
	}

	String getPath(String path) {
		int idx = path.indexOf(":");
		if (idx == -1) {
			return null;
		}
		return path.substring(idx + 1);
	}

	void parseCommandLine(String[] args) throws ParseException, URISyntaxException {
		Parser parser = new PosixParser();
		CommandLine commandLine = parser.parse(options, args);
		process(commandLine, args);

		List argList = commandLine.getArgList();
		if (argList.size() != 2) {
			throw new ParseException("");
		}

		source = (String) argList.get(0);
		target = (String) argList.get(1);

	}

	boolean isRemotePath(String path) {
		int idx = path.indexOf("@");
		int idx2 = path.indexOf(":");
		return idx > -1 && idx2 > -1 && idx2 > idx;
	}

	void process(CommandLine commandLine, String[] allArgs) throws ParseException {
		// Protocol version
		if (commandLine.hasOption('1')) {
			configuration.setProtocolVersion(SshConfiguration.SSH1_ONLY);
			if (commandLine.hasOption('2')) {
				throw new ParseException("Conflicting options -1 and -2. You may only specify one or the other or neither.");
			}
		} else if (commandLine.hasOption('2')) {
			configuration.setProtocolVersion(SshConfiguration.SSH1_ONLY);
		}

		// Don't actually enable these till we have the provider
		compress = commandLine.hasOption('C');
		cipher = commandLine.getOptionValue('c');

		//
		batch = commandLine.hasOption('B');
		boolean quiet = commandLine.hasOption('q');
		recursive = commandLine.hasOption('r');
		port = Integer.parseInt(commandLine.getOptionValue('P', String.valueOf("22")));
		identityFile = new File(commandLine.getOptionValue('i', new File(
			new File(new File(System.getProperty("user.home")), ".ssh"), "id_dsa").getAbsolutePath()));
		if (!identityFile.exists() && commandLine.hasOption('i') && !quiet) {
			System.out.println("Warning: Identity file " + identityFile.getPath() + " not accessible: No such file or directory");
		}

		verbosity = 0;
		for (int i = 0; i < allArgs.length; i++) {
			if (allArgs[i].equals("-v")) {
				verbosity += 1;
			}
		}
		traces = false;
		switch (verbosity) {
		case 0:
			level = quiet ? Level.ERROR : Level.WARN;
			break;
		case 1:
			level = Level.INFO;
			break;
		case 2:
			level = Level.DEBUG;
			break;
		default:
			level = Level.DEBUG;
			traces = true;
			break;
		}
		providerClass = commandLine.getOptionValue('S');
	}

	void buildOptions(Options options) {
		options.addOption("1", false, "Forces scp to use protocol 1");
		options.addOption("2", false, "Forces scp to use protocol 2");
		options.addOption("3", false, "Copies between two remote hosts are transferred through the local host.  Without "
			+ "this option the data is copied directly between the two remote hosts.  Note that this option disables "
			+ "the progress meter.");
		options.addOption("P", true, "Specifies the port to connect to on the remote host.  Note "
			+ "that this option is written with a capital ‘P’, because -p is already reserved for "
			+ "preserving the times and modes of the file");
		options.addOption("c", true, "Selects the cipher to use for encrypting the data transfer.");
		options.addOption("i", true, "Selects the file from which the identity (private key) for public key authentication"
			+ " is read.");
		options.addOption("r", false, "Recursively copy entire directories.  Note that scp follows symbolic links "
			+ "encountered in the tree traversal.");
		options.addOption("F", true,
			"Specifies an alternative per-user configuration file (IGNORED - Only present for OpenSSH compatibility).");
		options.addOption("o", true, "Additional options (IGNORED - Only present for OpenSSH compatibility).");
		options.addOption("4", false, "Forces use of IPv4 addresses only (IGNORED - Only present for OpenSSH compatibility).");
		options.addOption("6", false, "Forces use of IPv6 addresses only (IGNORED - Only present for OpenSSH compatibility).");
		options.addOption("p", false,
			"Preserves modification times, access times, and modes from the original file (IGNORED - Only present for "
				+ "OpenSSH compatibility).");
		options.addOption("v", false,
			"Verbose mode.  Print debugging messages about their progress. This is helpful in debugging connection, "
				+ "authentication, and configuration problems.");
		options.addOption("l", false,
			"Limits the used bandwidth, specified in Kbit/s. (IGNORED - Only present for OpenSSH compatibility).");
		options.addOption("B", false, "Selects batch mode (prevents asking for passwords or passphrases).");
		options.addOption("C", false, "Compression enable. Will be ignored if the provider does not support compression.");
		options.addOption("q", false, "Quiet mode: disables the progress meter as well as warning and diagnostic messages.");
		options
			.addOption(
				"S",
				true,
				"Classname of SSH provider to use (Note, in OpenSSH this option is 'program' which is an executable. In this case, it must be the classname of an SSHAPI provider.");
	}

	void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("scp", options, true);
	}

	/**
	 * Entry point.
	 * 
	 * @param args command line arguments
	 * @throws SshException
	 */
	public static void main(String[] args) throws SshException {
		scp scp = new scp();
		try {
			scp.parseCommandLine(args);
			scp.start();
		} catch (ParseException pe) {
			pe.printStackTrace();
			if (!pe.getMessage().equals("")) {
				System.err.println("scp: " + pe.getMessage());
			}
			scp.printUsage();
			System.exit(2);
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage() + ": No such file or directory");
			System.exit(1);
		} catch (SshException sshe) {
			if (sshe.getCode().equals(SshException.HOST_KEY_REJECTED)) {
				// Already displayed a message, just exit
				System.exit(1);
			} else {
				System.err.println("scp: " + sshe.getMessage());
				System.exit(1);
			}
		} catch (Exception e) {
			System.err.println("scp: " + e.getMessage());
			System.exit(1);
		}
	}

	public void startedTransfer(String path, String targetPath, long length) {
		this.transferLength = length;
		this.transferLastUpdate = System.currentTimeMillis();
		this.transferProgressed = 0;
		this.transferSpeed = 0;
		this.transferBlock = 0;
		this.transferPath = Util.basename(path);
		updateProgress(false);
	}

	public void transferProgress(String path, String targetPath, long progress) {
		transferBlock += progress;
		if ((System.currentTimeMillis() - this.transferLastUpdate) > 1000) {
			updateBlock(false);
		}
	}

	public void finishedTransfer(String path, String targetPath) {
		updateBlock(true);
	}

	private void updateBlock(boolean newline) {
		long now = System.currentTimeMillis();
		long taken = now - this.transferLastUpdate;
		this.transferLastUpdate = now;
		this.transferSpeed = (int) (((double) taken / 1000.0) * (double) transferBlock);
		transferProgressed += transferBlock;
		transferBlock = 0;
		updateProgress(newline);
	}

	private void updateProgress(boolean newline) {
		int pc = (int) (((double) transferProgressed / (double) transferLength) * 100.0);
		String sizeSoFar = formatSize(transferProgressed);
		// width - ( 5+ 10 + 8 + 3 + 1 + 1 + 1 + 1 )
		int w = reader == null ? 80 : reader.getTermwidth();
		int filenameWidth = w - 32;

		String result = String.format("%-" + filenameWidth + "s %3d%% %-8s %10s %5s",
			new Object[] { transferPath, Integer.valueOf(pc), sizeSoFar, formatSpeed(transferSpeed), "??:??" });
		if (terminal == null) {
			System.out.print(result + "\r");
			if (newline) {
				System.out.println();
			}
		} else {
			reader.getCursorBuffer().clearBuffer();
			reader.getCursorBuffer().write(result);
			try {
				reader.setCursorPosition(w);
				reader.redrawLine();
				if (newline) {
					reader.printNewline();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private String formatSpeed(long bytesPerSecond) {
		String speedText = String.valueOf(bytesPerSecond) + "B/s";
		if (bytesPerSecond > 9999) {
			bytesPerSecond = bytesPerSecond / 1024;
			speedText = bytesPerSecond + "KB/s";
			if (bytesPerSecond > 9999) {
				bytesPerSecond = bytesPerSecond / 1024;
				speedText = bytesPerSecond + "MB/s";
				if (bytesPerSecond > 9999) {
					bytesPerSecond = bytesPerSecond / 1024;
					speedText = bytesPerSecond + "GB/s";
				}
			}
		}
		return speedText;
	}

	private String formatSize(long bytes) {
		String sizeSoFar = String.valueOf(bytes);
		long size = bytes;
		if (size > 9999) {
			size = size / 1024;
			sizeSoFar = size + "KB";
			if (size > 9999) {
				size = size / 1024;
				sizeSoFar = size + "MB";
				if (size > 9999) {
					size = size / 1024;
					sizeSoFar = size + "GB";
				}
			}
		}
		return sizeSoFar;
	}

	public void log(Level level, String message) {
		if (isLevelEnabled(level)) {
			System.err.println(level.getName() + ": " + message);
		}
	}

	public void log(Level level, String message, Throwable exception) {
		log(level, message);
		if (traces) {
			exception.printStackTrace();
		}
	}

	public boolean isLevelEnabled(Level level) {
		return this.level.compareTo(level) <= 0;
	}
}
