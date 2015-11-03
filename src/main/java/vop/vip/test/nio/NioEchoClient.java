package vop.vip.test.nio;

import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * nio客户端
 * 
 * @author laien.liang
 *
 */
public class NioEchoClient {
	private static final String host="127.0.0.1";
	private static final int port = 8888;
	
	public static void main(String[] args) throws IOException {
		for (int i=0; i<1; i++) {
			new Thread(()->{
				try {
					ConnectServer();
				} catch (Exception ex){
					ex.printStackTrace();
				}
				}).start();
			
		}
	}

	private static void ConnectServer() throws IOException,
			ClosedChannelException {
		// 1、创建channel
		SocketChannel channel = SocketChannel.open();
		// 2、设置为非阻塞
		channel.configureBlocking(false);
		// 3、打开多路复用器
		Selector selector = Selector.open();
		// 4、连接远程端口
		boolean isConnected = channel.connect(new InetSocketAddress(host, port));
		if (isConnected) {
			// 5、监听读事件
			channel.register(selector, SelectionKey.OP_READ);
			// 6、向服务端写请求
			writeRequest(channel);
		} else {
			//
			channel.register(selector, SelectionKey.OP_CONNECT);
		}
		
		
		while (!Thread.currentThread().isInterrupted()) {
			// 阻塞等待网络事件
			selector.select(2000L);
			// 所有就绪事件
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
			while (keyIterator.hasNext()) {
				SelectionKey key = keyIterator.next();
				try {
					// 处理网络事件
					handle(key, selector);
				} catch (Exception ex) {
					
				}
				
				// 处理完事件后，必须把key从就绪队列中删除
				keyIterator.remove();
			}
			
		}
	}

	/**
	 * 处理网络事件
	 * 
	 * @param key
	 * @throws IOException 
	 */
	private static void handle(SelectionKey key, Selector selector) throws IOException {
		if (key.isValid()) {
			SocketChannel channel = (SocketChannel) key.channel();
			// 连接事件
			if (key.isConnectable()) {
				if (channel.finishConnect()) {
					// 网络连接成功事件
					channel.register(selector, SelectionKey.OP_READ);
					// 向服务端写请求
					writeRequest(channel);
					
				}
			}
			
			// 处理读io事件
			if (key.isReadable()) {
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				int bytes = channel.read(buffer);
				if (bytes > 0) {
					buffer.flip();
					byte[] byteArr = new byte[buffer.remaining()];
					buffer.get(byteArr);
					
					String resp = new String(byteArr, CharsetUtil.UTF_8);
					
					System.out.println(resp);
				}
			}
			
		}
	}

	private static void writeRequest(SocketChannel channel) throws IOException {
		byte[] req = "测试请求".getBytes(CharsetUtil.UTF_8);
		ByteBuffer buffer = ByteBuffer.allocate(req.length);
		buffer.put(req);
		buffer.flip();
		channel.write(buffer);
	}

}
