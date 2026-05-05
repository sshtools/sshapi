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
package com.maverick.ssh.tests;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Util {
	public static void monitorProcessErr(final Process process) {
		new Thread() {
			public void run() {
				try {
					IOUtils.copy(process.getErrorStream(), System.err);
				} catch (IOException ioe) {
				}
			}
		}.start();
	}

	public static void monitorProcessOut(final Process process) {
		new Thread() {
			public void run() {
				try {
					IOUtils.copy(process.getInputStream(), System.out);
				} catch (IOException ioe) {
				}
			}
		}.start();
	}

	public static ProcessBuilder configureCommand(File dir, ProcessBuilder pb) {
		pb.directory(dir);
		pb.redirectErrorStream(true);
		return pb;
	}

	public static String toString(List<Object> o) {
		return Arrays.toString(o.toArray());
	}

	public static String toString(Object... o) {
		return Arrays.toString(o);
	}

	public static String arrayToSeparatedString(char separator, Object... o) {
		return listToSeparatedString(separator, Arrays.asList(o));
	}

	public static String listToSeparatedString(char separator, List<? extends Object> o) {
		StringBuilder bui = new StringBuilder();
		for (Object x : o) {
			if (bui.length() > 0) {
				bui.append(separator);
			}
			bui.append(x);
		}
		return bui.toString();
	}

	public static Process runToFileAndCheckReturn(File output, ProcessBuilder pb) throws IOException, InterruptedException {
		Process process = pb.start();
		FileOutputStream out = new FileOutputStream(output);
		try {
			IOUtils.copy(process.getInputStream(), out);
		} finally {
			out.close();
		}
		if (process.waitFor() != 0) {
			throw new IOException("Command returned " + process.exitValue() + ".");
		}
		return process;
	}

	public static Process runAndCheckReturn(ProcessBuilder pb) throws IOException, InterruptedException {
		Process process = pb.start();
		IOUtils.copy(process.getInputStream(), System.out);
		if (process.waitFor() != 0) {
			throw new IOException("Command returned " + process.exitValue() + ".");
		}
		return process;
	}

	public static Process run(ProcessBuilder pb) throws IOException, InterruptedException {
		Process process = pb.start();
		IOUtils.copy(process.getInputStream(), System.out);
		int res = process.waitFor();
		if (res != 0) {
			System.out.println("WARNING: Command " + pb.command() + " exited with " + res);
		}
		return process;
	}

	public static Process runAndCheckReturnWithFileInput(final File input, ProcessBuilder pb)
			throws IOException, InterruptedException {
		return runWithFileInput(input, pb, true);
	}

	public static Process runWithFileInput(final File input, ProcessBuilder pb, boolean checkReturn)
			throws IOException, InterruptedException {
		final Process process = pb.start();
		final FileInputStream fin = new FileInputStream(input);
		try {
			Thread t = new Thread("Stdin") {
				public void run() {
					try {
						IOUtils.copy(process.getInputStream(), System.out);
					} catch (IOException ioe) {
						throw new RuntimeException(ioe);
					}
				}
			};
			t.start();
			IOUtils.copy(fin, process.getOutputStream());
			process.getOutputStream().close();
			process.getInputStream().close();
			if (process.waitFor() != 0 && checkReturn) {
				throw new IOException("Command returned " + process.exitValue() + ".");
			}
		} finally {
			fin.close();
		}
		return process;
	}

	public static File createAskPass(char[] password) throws IOException, InterruptedException {
		File f = createAskPass(File.createTempFile("askpass", ".exe"), password);
		return f;
	}

	public static File createAskPass(File outputFile, char[] password) throws IOException, InterruptedException {
		// Create an "askpass" script. This supplies root password to sudo
		// throws IOException, InterruptedException {
		File resultFile = new File(outputFile.getParentFile(), outputFile.getName() + ".out");
		String cTemplate = IOUtils.toString(Util.class.getResource("/askpass.c.template"), "UTF-8")
				.replace("%PASSWORD%", new String(password)).replace("%OUTPUT_FILE%", resultFile.getAbsolutePath());
		File parentFile = outputFile.getParentFile();
		File askPassFileSource = new File(parentFile, outputFile.getName() + ".c");
		FileUtils.writeStringToFile(askPassFileSource, cTemplate, "UTF-8");
		ProcessBuilder pb = new ProcessBuilder("gcc", "-o", outputFile.getAbsolutePath(), askPassFileSource.getName());
		configureCommand(parentFile, pb);
		runAndCheckReturn(pb);
		askPassFileSource.delete();
		// LDP this breaks 1.5 build (does it break the test??)
		// outputFile.setExecutable(true, true);
		resultFile.deleteOnExit();
		outputFile.deleteOnExit();
		return outputFile;
	}

	public static void compare(File file1, File file2) throws IOException {
		compare(file1, file2, true);
	}

	public static <T extends Comparable<T>> T[] sort(T[] original) {
		List<T> l = Arrays.asList(original);
		Collections.sort(l);
		return l.toArray(original);
	}

	public static void compare(File file1, File file2, boolean checkNames) throws IOException {
		if (checkNames) {
			assertEquals("Files must have same name", file1.getName(), file2.getName());
		}
		assertEquals("Files must have same type", file1.isDirectory(), file2.isDirectory());
		assertEquals("Files must have same type", file1.isFile(), file2.isFile());
		if (file1.isDirectory()) {
			File[] list1 = sort(file1.listFiles());
			File[] list2 = sort(file1.listFiles());
			assertEquals("Listing must have same length", list1.length, list2.length);
			for (int i = 0; i < list1.length; i++) {
				compare(list1[i], list2[i], true);
			}
		} else {
			assertEquals("Files must have same size", file1.length(), file2.length());
			assertEquals("Files must have same checksum", FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2));
		}
	}

	public static int findRandomPort() {
		try {
			ServerSocket ss = new ServerSocket(0);
			ss.setReuseAddress(true);
			try {
				return ss.getLocalPort();
			} finally {
				ss.close();
			}
		} catch (IOException ioe) {
			throw new IllegalStateException("Could not get free port.");
		}
	}
}
