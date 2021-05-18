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
package net.sf.sshapi.cli;

import java.io.File;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import net.sf.sshapi.sftp.SftpClient;

/**
 * The Interface SftpContainer.
 */
public interface SftpContainer {
	
	/**
	 * Gets the client.
	 *
	 * @return the client
	 */
	SftpClient getClient();

	/**
	 * Gets the cwd.
	 *
	 * @return the cwd
	 */
	String getCwd();

	/**
	 * Sets the cwd.
	 *
	 * @param path the new cwd
	 */
	void setCwd(String path);
	
	/**
	 * Gets the terminal.
	 *
	 * @return the terminal
	 */
	Terminal getTerminal();
	
	/**
	 * Gets the line reader.
	 *
	 * @return the line reader
	 */
	LineReader getLineReader();

	/**
	 * Sets the lcwd.
	 *
	 * @param lcwd the new lcwd
	 */
	void setLcwd(File lcwd);

	/**
	 * Gets the lcwd.
	 *
	 * @return the lcwd
	 */
	File getLcwd();
}