package net.sf.sshapi.impl.nassh.asn1;

import java.nio.ByteBuffer;
import java.util.Iterator;

public interface ASN1Parser {

	Iterator<ASN1Type<?>> parse(ByteBuffer data);
}
