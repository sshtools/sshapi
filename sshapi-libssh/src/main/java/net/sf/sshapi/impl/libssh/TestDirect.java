package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TestDirect {

	public static void main(String[] args) throws IOException {


		try (Socket s = new Socket("localhost", 9999)) {
			OutputStream out = s.getOutputStream();
			out.write("HELLO_CLIENT________".getBytes());
			out.flush();
			InputStream in = s.getInputStream();
			byte[] b = new byte[20];
			int r = in.read(b);
			if (r != -1) {
				System.out.println("Got: " + r + " : " + new String(b, 0, r));
				r = in.read();
				if (r != -1) {
					System.out.println("Unexpected byte!");
				}
			}

		}

	}
}
