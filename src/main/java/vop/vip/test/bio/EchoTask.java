package vop.vip.test.bio;

import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 回显任务
 * 
 * @author laien.liang
 *
 */
public class EchoTask implements Runnable{

	private Socket socker;
	
	
	
	public EchoTask(Socket socker) {
		this.socker = socker;
	}



	public void run() {
		try (Socket connect = this.socker;
				BufferedReader br = new BufferedReader(new InputStreamReader(connect.getInputStream(), CharsetUtil.UTF_8));
				PrintWriter bw = new PrintWriter(connect.getOutputStream(), true)) {
			// 阻塞方法
			String request = br.readLine();
			System.out.println(Thread.currentThread().getName()+":"+request);
		} catch (IOException e) {
		
			e.printStackTrace();
		}
	}

}
