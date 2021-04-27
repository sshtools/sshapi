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
package net.sf.sshapi.util;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import net.sf.sshapi.Logger;

/**
 * Simple console logger
 */
public class ConsoleLogger implements Logger {
	private Level level = Level.valueOf(System.getProperty("sshapi.logLevel", Level.ERROR.name()));
	
	/**
	 * Set the default level.
	 * 
	 * @param level level
	 */
	public void setDefaultLevel(Level level) {
		this.level = level;
	}

	@Override	
	public void log(Level level, String message, Object... args) {
		if (level.compareTo(this.level) >= 0) {
			try {
				System.out.println("SSHAPI [" + Thread.currentThread().getName() + "/" + Thread.currentThread().getId() + ":" + level + "@"
						+ new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]: " + MessageFormat.format(message, args));
			}
			catch(Exception nfe) {
				System.out.println("*SSHAPI [" + Thread.currentThread().getName() + "/" + Thread.currentThread().getId() + ":" + level + "@"
						+ new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]: " + message + " " + Arrays.asList(args));	
			}
		}
	}

	@Override
	public void log(Level level, String message, Throwable exception, Object... args) {
		if (level.compareTo(this.level) >= 0) {
			log(level, message, args);
			if(exception != null)
				exception.printStackTrace(System.out);
		}
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		return level.compareTo(this.level) >= 0;
	}

	@Override
	public void raw(Level level, String message) {
		if (level.compareTo(this.level) >= 0)
			System.out.println(message);
	}

	@Override
	public void newline() {
		System.out.println();
	}
}
