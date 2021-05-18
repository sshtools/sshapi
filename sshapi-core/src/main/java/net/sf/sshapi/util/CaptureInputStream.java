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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Class CaptureInputStream.
 */
public class CaptureInputStream extends FilterInputStream {
	
	/**
	 * The Class Match.
	 */
	public class Match {
		
		/** The idx. */
		private int idx;
		
		/** The pattern. */
		private byte[] pattern;

		/**
		 * Instantiates a new match.
		 *
		 * @param pattern the pattern
		 */
		public Match(byte[] pattern) {
			super();
			this.pattern = pattern;
		}

		/**
		 * Gets the idx.
		 *
		 * @return the idx
		 */
		public int getIdx() {
			return idx;
		}

		/**
		 * Gets the pattern.
		 *
		 * @return the pattern
		 */
		public byte[] getPattern() {
			return pattern;
		}

		/**
		 * Matched.
		 *
		 * @param b the b
		 * @return the match type
		 */
		MatchType matched(byte b) {
			if (b == pattern[idx]) {
				idx++;
				if (idx == pattern.length) {
					idx = 0;
					return MatchType.FULL;
				}
			} else
				idx = 0;
			return idx == 0 ? MatchType.NONE : MatchType.PARTIAL;
		}

		/**
		 * Gets the pattern string.
		 *
		 * @return the pattern string
		 */
		public String getPatternString() {
			return new String(pattern);
		}

		/**
		 * Reset.
		 */
		public void reset() {
			idx = 0;
		}
	}

	/**
	 * The Interface Matcher.
	 */
	public interface Matcher {
		
		/**
		 * Matched.
		 *
		 * @param pattern the pattern
		 * @param capture the capture
		 * @return the match result
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		MatchResult matched(Match pattern, CaptureInputStream capture) throws IOException;

		/**
		 * No match.
		 *
		 * @param pattern the pattern
		 * @param capture the capture
		 * @return the match result
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		default MatchResult noMatch(Match pattern, CaptureInputStream capture) throws IOException {
			return MatchResult.CONTINUE;
		}
	}

	/**
	 * The Enum MatchResult.
	 */
	public enum MatchResult {
		
		/** The activate. */
		ACTIVATE, 
 /** The activate next byte. */
 ACTIVATE_NEXT_BYTE, 
 /** The continue. */
 CONTINUE, 
 /** The deactivate. */
 DEACTIVATE, 
 /** The deactivate next byte. */
 DEACTIVATE_NEXT_BYTE, 
 /** The end. */
 END, 
 /** The terminate. */
 TERMINATE;
	}

	/**
	 * The Enum MatchType.
	 */
	public enum MatchType {
		
		/** The full. */
		FULL, 
 /** The none. */
 NONE, 
 /** The partial. */
 PARTIAL
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		Util.joinStreams(new CaptureInputStream((p, capture) -> {
			return MatchResult.ACTIVATE;
		}, "Password:", new ByteArrayInputStream("Password:\nA load of other stuff\nmore stuff.\n".getBytes())), System.out);
		System.out.flush();
	}

	/**
	 * To byte array.
	 *
	 * @param patterns the patterns
	 * @return the byte[][]
	 */
	private static byte[][] toByteArray(String[] patterns) {
		byte[][] b = new byte[patterns.length][];
		for (int i = 0; i < patterns.length; i++) {
			b[i] = patterns[i].getBytes();
		}
		return b;
	}

	/** The active. */
	private boolean active;
	
	/** The ended. */
	private boolean ended;
	
	/** The matcher. */
	private Matcher matcher;
	
	/** The patterns. */
	private Match[] patterns;
	
	/** The capture. */
	private ByteArrayOutputStream capture;
	
	/** The sinking. */
	private boolean sinking;

	/**
	 * Instantiates a new capture input stream.
	 *
	 * @param in the in
	 */
	public CaptureInputStream(InputStream in) {
		this(null, (String) null, in);
	}

	/**
	 * Instantiates a new capture input stream.
	 *
	 * @param matcher the matcher
	 * @param pattern the pattern
	 * @param out the out
	 */
	public CaptureInputStream(Matcher matcher, byte[] pattern, InputStream out) {
		this(matcher, pattern == null ? null : new byte[][] { pattern }, out);
	}

	/**
	 * Instantiates a new capture input stream.
	 *
	 * @param matcher the matcher
	 * @param patterns the patterns
	 * @param out the out
	 */
	public CaptureInputStream(Matcher matcher, byte[][] patterns, InputStream out) {
		super(out);
		this.matcher = matcher;
		setPatterns(patterns);
	}

	/**
	 * Instantiates a new capture input stream.
	 *
	 * @param matcher the matcher
	 * @param pattern the pattern
	 * @param out the out
	 */
	public CaptureInputStream(Matcher matcher, String pattern, InputStream out) {
		this(matcher, pattern == null ? null : pattern.getBytes(), out);
	}

	/**
	 * Instantiates a new capture input stream.
	 *
	 * @param matcher the matcher
	 * @param patterns the patterns
	 * @param out the out
	 */
	public CaptureInputStream(Matcher matcher, String[] patterns, InputStream out) {
		this(matcher, toByteArray(patterns), out);
	}

	/**
	 * Gets the matcher.
	 *
	 * @return the matcher
	 */
	public Matcher getMatcher() {
		return matcher;
	}

	/**
	 * Gets the patterns.
	 *
	 * @return the patterns
	 */
	public Match[] getPatterns() {
		return patterns;
	}

	/**
	 * Sets the capture.
	 *
	 * @param capture the new capture
	 */
	public void setCapture(boolean capture) {
		this.capture = capture ? new ByteArrayOutputStream() : null;
	}

	/**
	 * Checks if is active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Read.
	 *
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public int read() throws IOException {
		int b;
		do {
			b = super.read();
			if (b == -1)
				return b;
			if(capture != null)
				capture.write(b);
			if (!ended && patterns != null && matcher != null) {
				for (Match pattern : patterns) {
					MatchType type = pattern.matched((byte) b);
					MatchResult res;
					if (type == MatchType.FULL) {
						res = matcher.matched(pattern, this);
					} else if (type == MatchType.NONE) {
						res = matcher.noMatch(pattern, this);
					} else
						res = MatchResult.CONTINUE;
					switch (res) {
					case ACTIVATE:
						active = true;
						return b;
					case ACTIVATE_NEXT_BYTE:
						active = true;
						break;
					case DEACTIVATE_NEXT_BYTE:
						active = false;
						break;
					case DEACTIVATE:
						active = false;
						return b;
					case END:
						ended = true;
						break;
					case TERMINATE:
						throw new IOException("Terminated.");
					default:
						continue;
					}
				}
			}
		} while (!active && !ended);
		return b;
	}

	/**
	 * Read.
	 *
	 * @param b the b
	 * @param off the off
	 * @param len the len
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int r;
		for (int i = 0; i < len; i++) {
			r = read();
			if (r == -1)
				return i == 0 ? -1 : i;
			else
				b[off + i] = (byte) r;
		}
		return len;
	}

	/**
	 * Sets the active.
	 *
	 * @param active the new active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Sets the matcher.
	 *
	 * @param matcher the new matcher
	 */
	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

	/**
	 * Sets the patterns.
	 *
	 * @param patterns the new patterns
	 */
	public void setPatterns(String... patterns) {
		setPatterns(toByteArray(patterns));
	}

	/**
	 * Sets the patterns.
	 *
	 * @param patterns the new patterns
	 */
	public void setPatterns(byte[][] patterns) {
		if (patterns == null)
			this.patterns = null;
		else {
			this.patterns = new Match[patterns.length];
			for (int i = 0; i < patterns.length; i++) {
				this.patterns[i] = new Match(patterns[i]);
			}
		}
	}

	/**
	 * Sets the pattern.
	 *
	 * @param pattern the new pattern
	 */
	public void setPattern(String pattern) {
		setPatterns(new String[] { pattern });
	}

	/**
	 * Gets the captured string.
	 *
	 * @return the captured string
	 */
	public String getCapturedString() {
		if (capture == null)
			return null;
		else {
			try {
				return new String(capture.toByteArray());
			} finally {
				capture.reset();
			}
		}
	}
	
	/**
	 * Reset.
	 */
	public void reset() {
		active = false;
		ended = false;
		getCapturedString();
		for(Match m : patterns)
			m.reset();
	}

	/**
	 * Read until EOF ended or active.
	 *
	 * @return the long
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public long readUntilEOFEndedOrActive() throws IOException {
		if(sinking)
			throw new IllegalStateException("Already sinking");
		sinking = true;
		long read = 0;
		try {
			int r;
			while( ( r = read() ) != -1) {
				read += r;
				if(ended || active)
					return read;
			}
		}
		finally {
			sinking = false;
		}
		return read;
	}
}