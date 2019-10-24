package net.sf.sshapi.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CaptureInputStream extends FilterInputStream {
	public class Match {
		private int idx;
		private byte[] pattern;

		public Match(byte[] pattern) {
			super();
			this.pattern = pattern;
		}

		public int getIdx() {
			return idx;
		}

		public byte[] getPattern() {
			return pattern;
		}

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

		public String getPatternString() {
			return new String(pattern);
		}

		public void reset() {
			idx = 0;
		}
	}

	public interface Matcher {
		MatchResult matched(Match pattern, CaptureInputStream capture) throws IOException;

		default MatchResult noMatch(Match pattern, CaptureInputStream capture) throws IOException {
			return MatchResult.CONTINUE;
		}
	}

	public enum MatchResult {
		ACTIVATE, ACTIVATE_NEXT_BYTE, CONTINUE, DEACTIVATE, DEACTIVATE_NEXT_BYTE, END, TERMINATE;
	}

	public enum MatchType {
		FULL, NONE, PARTIAL
	}

	public static void main(String[] args) throws Exception {
		Util.joinStreams(new CaptureInputStream((p, capture) -> {
			return MatchResult.ACTIVATE;
		}, "Password:", new ByteArrayInputStream("Password:\nA load of other stuff\nmore stuff.\n".getBytes())), System.out);
		System.out.flush();
	}

	private static byte[][] toByteArray(String[] patterns) {
		byte[][] b = new byte[patterns.length][];
		for (int i = 0; i < patterns.length; i++) {
			b[i] = patterns[i].getBytes();
		}
		return b;
	}

	private boolean active;
	private boolean ended;
	private Matcher matcher;
	private Match[] patterns;
	private ByteArrayOutputStream capture;
	private boolean sinking;

	public CaptureInputStream(InputStream in) {
		this(null, (String) null, in);
	}

	public CaptureInputStream(Matcher matcher, byte[] pattern, InputStream out) {
		this(matcher, pattern == null ? null : new byte[][] { pattern }, out);
	}

	public CaptureInputStream(Matcher matcher, byte[][] patterns, InputStream out) {
		super(out);
		this.matcher = matcher;
		setPatterns(patterns);
	}

	public CaptureInputStream(Matcher matcher, String pattern, InputStream out) {
		this(matcher, pattern == null ? null : pattern.getBytes(), out);
	}

	public CaptureInputStream(Matcher matcher, String[] patterns, InputStream out) {
		this(matcher, toByteArray(patterns), out);
	}

	public Matcher getMatcher() {
		return matcher;
	}

	public Match[] getPatterns() {
		return patterns;
	}

	public void setCapture(boolean capture) {
		this.capture = capture ? new ByteArrayOutputStream() : null;
	}

	public boolean isActive() {
		return active;
	}

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

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

	public void setPatterns(String... patterns) {
		setPatterns(toByteArray(patterns));
	}

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

	public void setPattern(String pattern) {
		setPatterns(new String[] { pattern });
	}

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
	
	public void reset() {
		active = false;
		ended = false;
		getCapturedString();
		for(Match m : patterns)
			m.reset();
	}

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