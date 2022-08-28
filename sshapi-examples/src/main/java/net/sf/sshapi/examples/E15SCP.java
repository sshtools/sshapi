package net.sf.sshapi.examples;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshSCPClient;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Demonstrates use of simple file transfer using SCP
 */
public class E15SCP {
	/**
	 * Entry point.
	 * 
	 * @param arg
	 *            command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.addRequiredCapability(Capability.SCP);
		config.setHostKeyValidator(new ConsoleHostKeyValidator());
		config.setBannerHandler(new ConsoleBannerHandler());

		// Connect, authenticate
		try (SshClient client = config.open(Util.promptConnectionSpec(), new ConsolePasswordAuthenticator())) {

			// Create and open the sftp client
			try (SshSCPClient scp = client.scp()) {

				//
				// Copying a single file
				//

				// Delete the test files
				System.out.println("Removing temporary files");
				File fileToUpload = new File("lorumipsum-upload.txt");
				fileToUpload.delete();
				File destinationFile = new File("lorumipsum-download.txt");
				destinationFile.delete();

				/*
				 * Write out a file locally that we can upload. Although some providers may
				 * natively support streams for SCP copies, some do not, so the SCP client only
				 * works with local files.
				 * 
				 * If you need to work with streams, use the {@link SftpClient} instead.
				 */
				createNewFile(fileToUpload);

				// Now upload the file,
				System.out.println("Uploading first file ..");
				scp.put(fileToUpload.getName(), null, fileToUpload, false);
				System.out.println("File uploaded ..");

				// Download the file
				System.out.println("Downloading file ..");
				scp.get(fileToUpload.getName(), destinationFile, false);
				System.out.println("File downloaded to " + destinationFile);

				//
				// Copying a directory
				//

				/*
				 * Create a simple directory structure to test copying directories
				 */
				File dir = new File("upload-test-directory");
				File destinationDir = new File("download-test-directory");

				System.out.println("Clearing temporary directory to upload");
				if(dir.exists())
					Util.delTree(dir);
				if(destinationDir.exists())
					Util.delTree(destinationDir);

				System.out.println("Creating temporary directory to upload");
				dir.mkdirs();
				createNewFile(new File(dir, "test1"));
				createNewFile(new File(dir, "test2"));
				createNewFile(new File(dir, "test3"));

				// Now upload the directory,
				System.out.println("Uploading directory ..");
				scp.put(".", null, dir, true);
				System.out.println("Directory uploaded ..");

				// Download the directory
				System.out.println("Downloading directory ..");
				scp.get(dir.getName(), destinationDir, true);
				System.out.println("Directory downloaded to " + destinationDir);
			}
		}

	}

	static void createNewFile(File file) throws UnsupportedEncodingException, IOException {
		String content = "Lorem ipsum dolor sit amet, consectetur adipisicing "
				+ "elit, sed do eiusmod tempor incididunt ut labore et dolore magna "
				+ "aliqua. Ut enim ad minim veniam, quis nostrud exercitation "
				+ "ullamco laboris nisi ut aliquip ex ea commodo consequat. "
				+ "Duis aute irure dolor in reprehenderit in voluptate velit esse "
				+ "cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat "
				+ "cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n";
		System.out.println("Creating temporary file to upload");
		FileOutputStream fout = new FileOutputStream(file);
		try {
			fout.write(content.getBytes("UTF-8"));
		} finally {
			fout.close();
		}
	}

}
