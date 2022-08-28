package net.sf.sshapi.impl.nassh.asn1;

public class ASN1Boolean implements ASN1Type<Boolean> {
	
	private boolean value;

	ASN1Boolean(boolean value) {
		super();
		this.value = value;
	}

	@Override
	public Boolean value() {
		return value;
	}
	
	public static ASN1Boolean valueOf(boolean val) {
		return new ASN1Boolean(val);
	}

}
