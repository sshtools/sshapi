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
package ssh;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import ssh.SshLibrary.ssh_string;
/**
 * file handler<br>
 * <i>native declaration : /usr/include/libssh/sftp.h:39</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class sftp_file_struct extends Structure {
	/** C type : sftp_session */
	public ssh.sftp_session_struct.ByReference sftp;
	/** C type : char* */
	public Pointer name;
	public long offset;
	/** C type : ssh_string */
	public ssh_string handle;
	public int eof;
	public int nonblocking;
	public sftp_file_struct() {
		super();
	}
	@Override
	protected List<String > getFieldOrder() {
		return Arrays.asList("sftp", "name", "offset", "handle", "eof", "nonblocking");
	}
	/**
	 * @param sftp C type : sftp_session<br>
	 * @param name C type : char*<br>
	 * @param handle C type : ssh_string
	 */
	public sftp_file_struct(ssh.sftp_session_struct.ByReference sftp, Pointer name, long offset, ssh_string handle, int eof, int nonblocking) {
		super();
		this.sftp = sftp;
		this.name = name;
		this.offset = offset;
		this.handle = handle;
		this.eof = eof;
		this.nonblocking = nonblocking;
	}
	public sftp_file_struct(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends sftp_file_struct implements Structure.ByReference {
		
	};
	public static class ByValue extends sftp_file_struct implements Structure.ByValue {
		
	};
}
