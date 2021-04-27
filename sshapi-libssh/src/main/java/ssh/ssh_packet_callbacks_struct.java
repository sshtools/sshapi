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
import ssh.SshLibrary.ssh_packet_callback;
/**
 * @brief This macro declares a packet callback handler<br>
 * @code<br>
 * SSH_PACKET_CALLBACK(mycallback){<br>
 * ...<br>
 * }<br>
 * @endcode<br>
 * <i>native declaration : /usr/include/libssh/callbacks.h:261</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class ssh_packet_callbacks_struct extends Structure {
	public byte start;
	public byte n_callbacks;
	/** C type : ssh_packet_callback* */
	public ssh_packet_callback callbacks;
	/** C type : void* */
	public Pointer user;
	public ssh_packet_callbacks_struct() {
		super();
	}
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("start", "n_callbacks", "callbacks", "user");
	}
	/**
	 * @param callbacks C type : ssh_packet_callback*<br>
	 * @param user C type : void*
	 */
	public ssh_packet_callbacks_struct(byte start, byte n_callbacks, ssh_packet_callback callbacks, Pointer user) {
		super();
		this.start = start;
		this.n_callbacks = n_callbacks;
		this.callbacks = callbacks;
		this.user = user;
	}
	public ssh_packet_callbacks_struct(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends ssh_packet_callbacks_struct implements Structure.ByReference {
		
	};
	public static class ByValue extends ssh_packet_callbacks_struct implements Structure.ByValue {
		
	};
}
