package vop.vip.test.bio;

import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class BioEchoClient {
	private static final String host = "127.0.0.1";
	private static final int port = 8888;

	public static void main(String[] args) throws UnknownHostException,
			IOException {

		for (int i = 0; i< 20; i++) {
			final int j = i;
			new Thread(() -> {
				try {
					connectServer(j);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		}
		

	}

	private static void connectServer(int i) throws IOException,	UnknownHostException {
		
		try (Socket socket = new Socket(host, port);
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), CharsetUtil.UTF_8));
				PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);) {

			pw.println("tefasldfjasdlkfj测试"+i);
			String response = br.readLine();
			System.out.println(response);
		}
	}

}
