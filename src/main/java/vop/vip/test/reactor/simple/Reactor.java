package vop.vip.test.reactor.simple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * reactor
 * 
 * @author laien.liang
 *
 */
public class Reactor implements Runnable {

	final Selector selector;
	final ServerSocketChannel serverSocket;

	Reactor(int port) throws IOException {
		// 1、打开多路复用器
		selector = Selector.open();
		// 2、打开serversocketChannel
		serverSocket = ServerSocketChannel.open();
		// 3、绑定端口
		serverSocket.socket().bind(new InetSocketAddress(port));
		// 4、配置为非阻塞
		serverSocket.configureBlocking(false);
		// 5、serverSocketChannel注册到多路复用器，监听accept事件
		SelectionKey sk = serverSocket.register(selector,
				SelectionKey.OP_ACCEPT);
		// 6、把分发器绑定到key中，在响应key事件时，可以取出分发器来，对io任务进行分发
		sk.attach(new Acceptor());
	}

	@Override
	public void run() {
		try {
			// 无限循环，接收io事件
			while (!Thread.interrupted()) {
				// 阻塞，直到有网络事件发生
				selector.select();
				Set<SelectionKey> selected = selector.selectedKeys();
				Iterator<SelectionKey> it = selected.iterator();
				while (it.hasNext())
					// 因为仅注册了accept事件，所以这里的是客户端连接请求事件
					dispatch(it.next());
				selected.clear();
			}
		} catch (IOException ex) {
		}
	}

	void dispatch(SelectionKey k) {
		// 从key中取出分发器，对任务进行分发
		Runnable r = (Runnable) (k.attachment());
		if (r != null)
			r.run();
	}

	/**
	 * 任务分发器
	 * 
	 * @author laien.liang
	 *
	 */
	class Acceptor implements Runnable { 
		public void run() {
			try {
				// 接受一个客户端连接
				SocketChannel c = serverSocket.accept();
				if (c != null)
					// 处理客户端连接
					new Handler(selector, c);
			} catch (IOException ex) { /* ... */
			}
		}
	}

}
