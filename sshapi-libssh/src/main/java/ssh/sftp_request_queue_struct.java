package ssh;
import com.ochafik.lang.jnaerator.runtime.Structure;
/**
 * <i>native declaration : /usr/include/libssh/sftp.h:136</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class sftp_request_queue_struct extends Structure<sftp_request_queue_struct, sftp_request_queue_struct.ByValue, sftp_request_queue_struct.ByReference > {
	/// C type : sftp_request_queue
	public sftp_request_queue_struct.ByReference next;
	/// C type : sftp_message
	public ssh.sftp_message_struct.ByReference message;
	public sftp_request_queue_struct() {
		super();
		initFieldOrder();
	}
	protected void initFieldOrder() {
		setFieldOrder(new java.lang.String[]{"next", "message"});
	}
	/**
	 * @param next C type : sftp_request_queue<br>
	 * @param message C type : sftp_message
	 */
	public sftp_request_queue_struct(sftp_request_queue_struct.ByReference next, ssh.sftp_message_struct.ByReference message) {
		super();
		this.next = next;
		this.message = message;
		initFieldOrder();
	}
	protected ByReference newByReference() { return new ByReference(); }
	protected ByValue newByValue() { return new ByValue(); }
	protected sftp_request_queue_struct newInstance() { return new sftp_request_queue_struct(); }
	public static sftp_request_queue_struct[] newArray(int arrayLength) {
		return Structure.newArray(sftp_request_queue_struct.class, arrayLength);
	}
	public static class ByReference extends sftp_request_queue_struct implements Structure.ByReference {
		
	};
	public static class ByValue extends sftp_request_queue_struct implements Structure.ByValue {
		
	};
}
