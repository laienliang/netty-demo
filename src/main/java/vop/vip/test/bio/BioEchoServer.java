package vop.vip.test.bio;

import io.netty.util.CharsetUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 回显功能客户端
 * 
 * @author laien.liang
 *
 */
public class BioEchoServer {
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(8888);
		while (!Thread.currentThread().isInterrupted()) {
			Socket connection = serverSocket.accept();
			InputStream is = new BufferedInputStream(connection.getInputStream());
			OutputStream os = new BufferedOutputStream(connection.getOutputStream());
			os.write("hello".getBytes(CharsetUtil.UTF_8));
			os.flush();
		}
	}
}
