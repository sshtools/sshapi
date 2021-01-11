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
 * Really simple logging interface. This is used by SSHAPI itself. Individual
 * provides may use their own logging system, although where possible SSHAPI
 * will capture this an output through it's own log.
 */
public interface Logger {

	/**
	 * Log level
	 */
	public enum Level {
		/**
		 * Most extreme logging, should only be required to diagnose problems
		 */
		TRACE,
		/**
		 * Very extreme logging, should only be required to diagnose problems
		 */
		DEBUG,
		/**
		 * Log only information, usually high level events.
		 */
		INFO,
		/**
		 * Only log warnings. Unusual events or possible problems.
		 */
		WARN,
		/**
		 * Only log errors.
		 */
		ERROR,
		/**
		 * Don't log anything.
		 */
		QUIET
	}

	/**
	 * Log a message at the specified level.
	 * 
	 * @param level
	 *            level
	 * @param message
	 *            message
	 * @param args formatting arguments
	 */
	void log(Level level, String message, Object... args);

	/**
	 * Log a message and an optional exception at the specified level.
	 * 
	 * @param level
	 *            level
	 * @param message
	 *            message
	 * @param exception
	 *            exception
	 * @param args formatting arguments
	 */
	void log(Level level, String message, Throwable exception, Object... args);

	/**
	 * Determine if messages at the specified log level will be displayed.
	 * 
	 * @param required
	 *            level
	 * @return level enabled
	 */
	boolean isLevelEnabled(Level required);

	/**
	 * Log a raw (pre-formatted message) at the Supplied level.
	 * 
	 * @param level level
	 * @param message
	 *            message
	 */
	default void raw(Level level, String message) {
		log(level, "{0}", message);
	}

	/**
	 * Output a newline.
	 */
	default void newline() {
		log(Level.TRACE, "");
	}

	/**
	 * Log a message at the {@link Level#TRACE} level.
	 * 
	 * @param message
	 *            message
	 * @param args formatting arguments
	 */
	default void trace(String message, Object... args) {
		log(Level.TRACE, message, args);
	}

	/**
	 * Log a message at the {@link Level#DEBUG} level.
	 * 
	 * @param message
	 *            message
	 * @param args formatting arguments
	 */
	default void debug(String message, Object... args) {
		log(Level.DEBUG, message, args);
	}

	/**
	 * Log a message at the {@link Level#INFO} level.
	 * 
	 * @param message
	 *            message
	 * @param args formatting arguments
	 */
	default void info(String message, Object... args) {
		log(Level.INFO, message, args);
	}

	/**
	 * Log a message at the {@link Level#WARN} level.
	 * 
	 * @param message
	 *            message
	 * @param args formatting arguments
	 * @param exception exception
	 */
	default void warn(String message, Object... args) {
		log(Level.WARN, message, args);
	}

	/**
	 * Log a message at the {@link Level#WARN} level with an optional stack trace.
	 * 
	 * @param message
	 *            message
	 * @param args formatting arguments
	 */
	default void warn(String message, Throwable exception, Object... args) {
		log(Level.WARN, message, exception, args);
	}

	/**
	 * Log a message at the {@link Level#ERROR} level.
	 * 
	 * @param message
	 *            message
	 * @param args formatting arguments
	 */
	default void error(String message, Object... args) {
		log(Level.ERROR, message, args);
	}

	/**
	 * Log a message at the {@link Level#ERROR} level with an optional exception trace.
	 * 
	 * @param message
	 *            message
	 * @param exception exception
	 * @param args formatting arguments
	 */
	default void error(String message, Throwable exception, Object... args) {
		log(Level.ERROR, message, exception,  args);
	}
	
	/**
	 * Get if logs at level {@link Level#TRACE} will be output.
	 * 
	 * @return trace enabled
	 */
	default boolean isTrace() {
		return isLevelEnabled(Level.TRACE);
	}
	
	/**
	 * Get if logs at level {@link Level#TRACE} will be output.
	 * 
	 * @return trace enabled
	 */
	default boolean isDebug() {
		return isLevelEnabled(Level.DEBUG);
	}
	
	/**
	 * Get if logs at level {@link Level#INFO} will be output.
	 * 
	 * @return info enabled
	 */
	default boolean isInfo() {
		return isLevelEnabled(Level.INFO);
	}
	
	/**
	 * Get if logs at level {@link Level#WARN} will be output.
	 * 
	 * @return warn enabled
	 */
	default boolean isWarn() {
		return isLevelEnabled(Level.WARN);
	}
	
	/**
	 * Get if logs at level {@link Level#ERROR} will be output.
	 * 
	 * @return erro enabled
	 */
	default boolean isError() {
		return isLevelEnabled(Level.ERROR);
	}
	
}
