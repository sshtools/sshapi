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
import ssh.SshLibrary.ssh_thread_callback;
import ssh.SshLibrary.ssh_thread_id_callback;
/**
 * <i>native declaration : /usr/include/libssh/callbacks.h:497</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class ssh_threads_callbacks_struct extends Structure {
	/** C type : const char* */
	public Pointer type;
	/** C type : ssh_thread_callback */
	public ssh_thread_callback mutex_init;
	/** C type : ssh_thread_callback */
	public ssh_thread_callback mutex_destroy;
	/** C type : ssh_thread_callback */
	public ssh_thread_callback mutex_lock;
	/** C type : ssh_thread_callback */
	public ssh_thread_callback mutex_unlock;
	/** C type : ssh_thread_id_callback */
	public ssh_thread_id_callback thread_id;
	public ssh_threads_callbacks_struct() {
		super();
	}
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("type", "mutex_init", "mutex_destroy", "mutex_lock", "mutex_unlock", "thread_id");
	}
	/**
	 * @param type C type : const char*<br>
	 * @param mutex_init C type : ssh_thread_callback<br>
	 * @param mutex_destroy C type : ssh_thread_callback<br>
	 * @param mutex_lock C type : ssh_thread_callback<br>
	 * @param mutex_unlock C type : ssh_thread_callback<br>
	 * @param thread_id C type : ssh_thread_id_callback
	 */
	public ssh_threads_callbacks_struct(Pointer type, ssh_thread_callback mutex_init, ssh_thread_callback mutex_destroy, ssh_thread_callback mutex_lock, ssh_thread_callback mutex_unlock, ssh_thread_id_callback thread_id) {
		super();
		this.type = type;
		this.mutex_init = mutex_init;
		this.mutex_destroy = mutex_destroy;
		this.mutex_lock = mutex_lock;
		this.mutex_unlock = mutex_unlock;
		this.thread_id = thread_id;
	}
	public ssh_threads_callbacks_struct(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends ssh_threads_callbacks_struct implements Structure.ByReference {
		
	};
	public static class ByValue extends ssh_threads_callbacks_struct implements Structure.ByValue {
		
	};
}
