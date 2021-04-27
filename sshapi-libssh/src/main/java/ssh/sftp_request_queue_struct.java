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
/**
 * <i>native declaration : /usr/include/libssh/sftp.h:74</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class sftp_request_queue_struct extends Structure {
	/** C type : sftp_request_queue */
	public sftp_request_queue_struct.ByReference next;
	/** C type : sftp_message */
	public ssh.sftp_message_struct.ByReference message;
	public sftp_request_queue_struct() {
		super();
	}
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("next", "message");
	}
	/**
	 * @param next C type : sftp_request_queue<br>
	 * @param message C type : sftp_message
	 */
	public sftp_request_queue_struct(sftp_request_queue_struct.ByReference next, ssh.sftp_message_struct.ByReference message) {
		super();
		this.next = next;
		this.message = message;
	}
	public sftp_request_queue_struct(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends sftp_request_queue_struct implements Structure.ByReference {
		
	};
	public static class ByValue extends sftp_request_queue_struct implements Structure.ByValue {
		
	};
}
