package vop.vip.test.reactor.simple;

import java.io.IOException;

public class EchoServer {
	public static void main(String[] args) throws IOException {
		
		new Thread(new Reactor(8888)).start();
	}
}
