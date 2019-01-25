package ssh;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * <i>native declaration : /usr/include/libssh/libssh.h:5</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class ssh_counter_struct extends Structure {
	public long in_bytes;
	public long out_bytes;
	public long in_packets;
	public long out_packets;
	public ssh_counter_struct() {
		super();
	}
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("in_bytes", "out_bytes", "in_packets", "out_packets");
	}
	public ssh_counter_struct(long in_bytes, long out_bytes, long in_packets, long out_packets) {
		super();
		this.in_bytes = in_bytes;
		this.out_bytes = out_bytes;
		this.in_packets = in_packets;
		this.out_packets = out_packets;
	}
	public ssh_counter_struct(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends ssh_counter_struct implements Structure.ByReference {
		
	};
	public static class ByValue extends ssh_counter_struct implements Structure.ByValue {
		
	};
}
