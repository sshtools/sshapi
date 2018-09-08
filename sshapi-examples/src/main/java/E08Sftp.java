import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.FileSystemException;
import java.text.DateFormat;
import java.util.Date;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsoleKeyboardInteractiveAuthenticator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * A simple Sftp client
 */
public class E08Sftp {
	/**
	 * Entry point.
	 * 
	 * @param arg
	 *            command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.addRequiredCapability(Capability.SFTP);
		config.setHostKeyValidator(new ConsoleHostKeyValidator());
		config.setBannerHandler(new ConsoleBannerHandler());

		// Prompt for the host and username
		String connectionSpec = Util.prompt("Enter username@hostname", System.getProperty("user.name") + "@localhost");
		String host = ExampleUtilities.extractHostname(connectionSpec);
		String user = ExampleUtilities.extractUsername(connectionSpec);
		int port = ExampleUtilities.extractPort(connectionSpec);

		// Create the client using that configuration and connect and authenticate
		try (SshClient client = config.open(user, host, port, new ConsolePasswordAuthenticator(),
				new ConsoleKeyboardInteractiveAuthenticator())) {
			// Create and open the sftp client
			try (SftpClient sftp = client.sftp()) {
				String cwd = sftp.getDefaultPath();
				File lcwd = new File(System.getProperty("user.dir"));
				while (true) {
					String cmd = Util.prompt("sftp");
					try {
						if (cmd.equals("help")) {
							System.out.println("ls - list directory");
							System.out.println("cd <directory> - change directory");
							System.out.println("pwd - print working directory");
							System.out.println("lcd <diretory> - change local working directory");
							System.out.println("lpwd - print local working directory");
							System.out.println("mkdir <directory> - create directory");
							System.out.println("rmdir <directory> - remove directory");
							System.out.println("rm <filename>- remove file");
							System.out.println("get <filename> - download remote file");
							System.out.println("put <filename> - upload local file");
							System.out.println("rename <old> <new> - rename file");
						} else if (cmd.equals("ls")) {
							SftpFile[] files = sftp.ls(cwd);
							for (int i = 0; i < files.length; i++) {
								System.out.println(String.format("%10s %-30s %8d %15s",
										new Object[] {
												Util.getPermissionsString(files[i].getType(),
														files[i].getPermissions()),
												files[i].getName(), Long.valueOf(files[i].getSize()),
												DateFormat.getDateTimeInstance()
														.format(new Date(files[i].getLastModified())) }));
							}
						} else if (cmd.equals("exit")) {
							break;
						} else if (cmd.equals("pwd")) {
							System.out.println(cwd);
						} else if (cmd.equals("lpwd")) {
							System.out.println(lcwd.getCanonicalPath());
						} else if (cmd.startsWith("cd ")) {
							if (cmd.length() > 3) {
								String newCwd = cmd.substring(3);
								newCwd = translatePath(cwd, newCwd);
								try {
									SftpFile file = sftp.stat(newCwd);
									if (file.isDirectory()) {
										cwd = newCwd;
									} else {
										System.out.println("Not a directory!");
									}
								} catch (SftpException sftpe) {
									if (sftpe.getCode().equals(SftpException.SSH_FX_NO_SUCH_FILE))
										System.out.println("Directory " + newCwd + " not found");
								}
							} else {
								cwd = sftp.getDefaultPath();
							}
						} else if (cmd.startsWith("mkdir ")) {
							if (cmd.length() > 6) {
								String toMake = cmd.substring(6);
								toMake = translatePath(cwd, toMake);
								sftp.mkdir(toMake, 0755);
							} else {
								System.out.println("mkdir requires a directory na7me to create");
							}
						} else if (cmd.startsWith("rm ")) {
							if (cmd.length() > 3) {
								String toRm = cmd.substring(3);
								toRm = translatePath(cwd, toRm);
								sftp.rm(toRm);
							} else {
								System.out.println("rm requires a file name to delete");
							}
						} else if (cmd.startsWith("rmdir ")) {
							if (cmd.length() > 6) {
								String toRm = cmd.substring(6);
								toRm = translatePath(cwd, toRm);
								sftp.rmdir(toRm);
							} else {
								System.out.println("rmdir requires a directory name to create");
							}
						} else if (cmd.startsWith("rename ")) {
							if (cmd.length() > 7) {
								String[] toRename = cmd.substring(7).split(" ");
								toRename[0] = translatePath(cwd, toRename[0]);
								toRename[1] = translatePath(cwd, toRename[1]);
								sftp.rename(toRename[0], toRename[1]);
							} else {
								System.out.println("rename requires two file or directory names to create");
							}
						} else if (cmd.startsWith("get ")) {
							if (cmd.length() > 4) {
								String toGet = cmd.substring(4);
								toGet = translatePath(cwd, toGet);
								String base = Util.basename(toGet);
								File file = new File(lcwd, base);
								FileOutputStream fout = new FileOutputStream(file);
								try {
									sftp.get(toGet, fout);
								} finally {
									fout.close();
								}
								System.out.println("Downloaded " + toGet + " to " + file);
							} else {
								System.out.println("get requires a file name to retrieve");
							}
						} else if (cmd.startsWith("put ")) {
							if (cmd.length() > 4) {
								String toPut = cmd.substring(4);
								String base = translatePath(cwd, Util.basename(toPut));
								FileInputStream fin = new FileInputStream(new File(lcwd, toPut));
								try {
									sftp.put(base, fin, 0644);
								} finally {
									fin.close();
								}
								System.out.println("Uploaded " + toPut + " to " + base);
							} else {
								System.out.println("get requires a file name to retrieve");
							}
						} else if(cmd.startsWith("lcd ")){
							File d = new File(cmd.substring(4));
							if(!d.isAbsolute())
								d = new File(lcwd, cmd.substring(4));
							if(d.isDirectory())
								lcwd = d.getCanonicalFile();
							else
								System.out.println(d + " is not a directory.");
						}
						else {
							System.out.println("Unknown command.");
						}
					} catch (SftpException sftpe) {
						System.out.println("ERR: " + sftpe.getCode() + " - " + sftpe.getLocalizedMessage());
					} catch (FileNotFoundException fnfe) {
						System.out.println("ERR: " + fnfe.getMessage());
					} catch (FileSystemException fnfe) {
						System.out.println("ERR: " + fnfe.getMessage());
					}
				}
			}
		}

	}

	private static String translatePath(String cwd, String newCwd) {
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
}
