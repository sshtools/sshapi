package net.sf.sshapi.impl.libssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TestSrv {

	public static void main(String[] args) throws Exception {
		try (ServerSocket ss = new ServerSocket(9999)) {
			while (true) {
				try (Socket s = ss.accept()) {
					InputStream in = s.getInputStream();
					byte[] b = new byte[20];
					int r = in.read(b);
					if (r != -1) {
						System.out.println("Got: " + r + " : " + new String(b, 0, r) + " from " + s.getPort());
						OutputStream out = s.getOutputStream();
						out.write("HELLO_SERVER________".getBytes());
						out.flush();
					} else
						System.out.println("Broke early");
				}
			}
		}
	}
}
