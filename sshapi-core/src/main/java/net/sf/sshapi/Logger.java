/* 
 * Copyright (c) 2010 The JavaSSH Project
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.sf.sshapi;

/**
 * Really simple logging interface. This is used by SSHAPI itself. Individiual
 * provides may use their own logging system.
 */
public interface Logger {

	/**
	 * Log level
	 */
	public class Level implements Comparable {
		/**
		 * Most extreme logging, should only be required to diagnose
		 * problems
		 */
		public final static Level DEBUG = new Level("DEBUG", 0);
		/**
		 * Log only information, usually high level events.
		 */
		public final static Level INFO = new Level("INFO", 1);
		/**
		 * Only log warnings. Unusual events or possible problems.
		 */
		public final static Level WARN = new Level("WARN", 2);
		/**
		 * Only log errors. 
		 */
		public final static Level ERROR = new Level("ERROR", 3);

		private String name;
		private int val;

		Level(String name, int val) {
			this.name = name;
			this.val = val;
		}

		/**
		 * Get the name of this log level.
		 * 
		 * @return log level name
		 */
		public String getName() {
			return name;
		}

		public String toString() {
			return getName();
		}

		public int compareTo(Object o) {
			return new Integer(val).compareTo(new Integer(((Level) o).val));
		}
	}

	/**
	 * Log a message at the specified level.
	 * 
	 * @param level level
	 * @param message message
	 */
	void log(Level level, String message);

	/**
	 * Log a message and an optional exception at the specified level.
	 * 
	 * @param level level
	 * @param message message
	 * @param exception exception
	 */
	void log(Level level, String message, Throwable exception);

	/**
	 * Determine if messages at the specified log level will be displayed.
	 * 
	 * @param required level
	 * @return level enabled
	 */
	boolean isLevelEnabled(Level required);
}
