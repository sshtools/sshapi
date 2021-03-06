package ssh;
import com.ochafik.lang.jnaerator.runtime.Structure;
import com.sun.jna.ptr.PointerByReference;
import ssh.SshLibrary.sftp_ext;
import ssh.SshLibrary.ssh_channel;
import ssh.SshLibrary.ssh_session;
/**
 * <i>native declaration : /usr/include/libssh/sftp.h:75</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class sftp_session_struct extends Structure<sftp_session_struct, sftp_session_struct.ByValue, sftp_session_struct.ByReference > {
	/// C type : ssh_session
	public ssh_session session;
	/// C type : ssh_channel
	public ssh_channel channel;
	public int server_version;
	public int client_version;
	public int version;
	/// C type : sftp_request_queue
	public ssh.sftp_request_queue_struct.ByReference queue;
	public int id_counter;
	public int errnum;
	/// C type : void**
	public PointerByReference handles;
	/// C type : sftp_ext
	public sftp_ext ext;
	public sftp_session_struct() {
		super();
		initFieldOrder();
	}
	protected void initFieldOrder() {
		setFieldOrder(new java.lang.String[]{"session", "channel", "server_version", "client_version", "version", "queue", "id_counter", "errnum", "handles", "ext"});
	}
	protected ByReference newByReference() { return new ByReference(); }
	protected ByValue newByValue() { return new ByValue(); }
	protected sftp_session_struct newInstance() { return new sftp_session_struct(); }
	public static sftp_session_struct[] newArray(int arrayLength) {
		return Structure.newArray(sftp_session_struct.class, arrayLength);
	}
	public static class ByReference extends sftp_session_struct implements Structure.ByReference {
		
	};
	public static class ByValue extends sftp_session_struct implements Structure.ByValue {
		
	};
}
