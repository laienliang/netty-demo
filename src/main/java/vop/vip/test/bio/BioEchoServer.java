package vop.vip.test.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 回显功能客户端
 * 
 * @author laien.liang
 *
 */
public class BioEchoServer {
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(8888);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		int processors = Runtime.getRuntime().availableProcessors();
		System.out.println(processors);
		System.out.println("服务启动成功。。。");
		
		ThreadFactory tFactory = new ThreadFactoryBuilder().setNameFormat("EchoTaskThread-%d").setDaemon(true).build();
		ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 4, 2L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(4), tFactory);
		Socket socket = null;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				socket = serverSocket.accept();
				System.out.println(String.format("poolsize:%d,taskCount:%d,largestPoolsize:%d, queueSize:%d",executor.getPoolSize(),executor.getTaskCount(), executor.getLargestPoolSize(), executor.getQueue().size()));
				executor.submit(new EchoTask(socket));
			} catch (Exception e) {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				e.printStackTrace();
			}
		}
	}
}
