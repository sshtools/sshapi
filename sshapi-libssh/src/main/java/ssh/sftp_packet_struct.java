package ssh;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import ssh.SshLibrary.ssh_buffer;
/**
 * <i>native declaration : /usr/include/libssh/sftp.h:30</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class sftp_packet_struct extends Structure {
	/** C type : sftp_session */
	public ssh.sftp_session_struct.ByReference sftp;
	public byte type;
	/** C type : ssh_buffer */
	public ssh_buffer payload;
	public sftp_packet_struct() {
		super();
	}
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("sftp", "type", "payload");
	}
	/**
	 * @param sftp C type : sftp_session<br>
	 * @param payload C type : ssh_buffer
	 */
	public sftp_packet_struct(ssh.sftp_session_struct.ByReference sftp, byte type, ssh_buffer payload) {
		super();
		this.sftp = sftp;
		this.type = type;
		this.payload = payload;
	}
	public sftp_packet_struct(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends sftp_packet_struct implements Structure.ByReference {
		
	};
	public static class ByValue extends sftp_packet_struct implements Structure.ByValue {
		
	};
}
