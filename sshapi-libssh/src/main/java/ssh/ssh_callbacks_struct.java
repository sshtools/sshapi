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
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import ssh.SshLibrary.SizeT;
import ssh.SshLibrary.ssh_auth_callback;
import ssh.SshLibrary.ssh_channel_open_request_auth_agent_callback;
import ssh.SshLibrary.ssh_channel_open_request_x11_callback;
import ssh.SshLibrary.ssh_global_request_callback;
import ssh.SshLibrary.ssh_log_callback;
/**
 * The structure to replace libssh functions with appropriate callbacks.<br>
 * <i>native declaration : /usr/include/libssh/callbacks.h:86</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class ssh_callbacks_struct extends Structure {
	public SizeT size;
	/** C type : void* */
	public Pointer userdata;
	/** C type : ssh_auth_callback */
	public ssh_auth_callback auth_function;
	/** C type : ssh_log_callback */
	public ssh_log_callback log_function;
	/** C type : connect_status_function_callback* */
	public ssh_callbacks_struct.connect_status_function_callback connect_status_function;
	/** C type : ssh_global_request_callback */
	public ssh_global_request_callback global_request_function;
	/** C type : ssh_channel_open_request_x11_callback */
	public ssh_channel_open_request_x11_callback channel_open_request_x11_function;
	/** C type : ssh_channel_open_request_auth_agent_callback */
	public ssh_channel_open_request_auth_agent_callback channel_open_request_auth_agent_function;
	/** <i>native declaration : /usr/include/libssh/callbacks.h:85</i> */
	public interface connect_status_function_callback extends Callback {
		void apply(Pointer userdata, float status);
	};
	public ssh_callbacks_struct() {
		super();
	}
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("size", "userdata", "auth_function", "log_function", "connect_status_function", "global_request_function", "channel_open_request_x11_function", "channel_open_request_auth_agent_function");
	}
	/**
	 * @param userdata C type : void*<br>
	 * @param auth_function C type : ssh_auth_callback<br>
	 * @param log_function C type : ssh_log_callback<br>
	 * @param connect_status_function C type : connect_status_function_callback*<br>
	 * @param global_request_function C type : ssh_global_request_callback<br>
	 * @param channel_open_request_x11_function C type : ssh_channel_open_request_x11_callback<br>
	 * @param channel_open_request_auth_agent_function C type : ssh_channel_open_request_auth_agent_callback
	 */
	public ssh_callbacks_struct(SizeT size, Pointer userdata, ssh_auth_callback auth_function, ssh_log_callback log_function, ssh_callbacks_struct.connect_status_function_callback connect_status_function, ssh_global_request_callback global_request_function, ssh_channel_open_request_x11_callback channel_open_request_x11_function, ssh_channel_open_request_auth_agent_callback channel_open_request_auth_agent_function) {
		super();
		this.size = size;
		this.userdata = userdata;
		this.auth_function = auth_function;
		this.log_function = log_function;
		this.connect_status_function = connect_status_function;
		this.global_request_function = global_request_function;
		this.channel_open_request_x11_function = channel_open_request_x11_function;
		this.channel_open_request_auth_agent_function = channel_open_request_auth_agent_function;
	}
	public ssh_callbacks_struct(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends ssh_callbacks_struct implements Structure.ByReference {
		
	};
	public static class ByValue extends ssh_callbacks_struct implements Structure.ByValue {
		
	};
}
