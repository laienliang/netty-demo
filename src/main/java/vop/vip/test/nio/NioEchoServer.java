package vop.vip.test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

/**
 * nio实现echo服务端
 * 
 * @author Administrator
 *
 */
public class NioEchoServer {
	public static void main(String[] args) {
		try {
			// 1、打开sockerChannel用于监听客户端连接
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			// 2、设置为非阻塞
			serverChannel.configureBlocking(false);
			// 3、绑定端口
			serverChannel.bind(new InetSocketAddress(8888));
			// 4、创建多路复用器
			Selector selector = Selector.open();
			// 5、把socketChannel注册到多路复用器，并监听接收连接事件
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			while (!Thread.currentThread().isInterrupted()) {
				// 阻塞，如果有监听事件，立即返回，没有的话阻塞2s后，返回
				selector.select(2000L);
				// 就绪事件集合
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> ketyIterator = selectedKeys.iterator();
				// 遍历处理所有已就绪的网络事件
				while (ketyIterator.hasNext()) {
					SelectionKey key = ketyIterator.next();
					try {
						// 处理网络事件
						handle(key, selector);
					} catch (Exception ex) {
						System.out.println(ex);
					}
					// 处理完成后，要把当前的key从就绪集合中删除掉
					ketyIterator.remove();
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
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
			if (key.isAcceptable()) {
				// 1、客户端请求连接事件(对应：serverChannel.register(selector, SelectionKey.OP_ACCEPT))
				ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
				// 2、接受并得到客户端连接
				SocketChannel channel = serverChannel.accept();
				// 3、设置为非阻塞模式
				channel.configureBlocking(false);
				// 4、得到客户端连接后，并不立即读取io,而是把客户端连接注册到多路复用器上,并监听读io事件
				channel.register(selector, SelectionKey.OP_READ);
			}
			
			if (key.isReadable()) {
				// 处理io读事件，(对应：channel.register(selector, SelectionKey.OP_READ);)
				SocketChannel channel = (SocketChannel)key.channel();
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				// 把客户端请求数据读到buffer中
				int readBytes = channel.read(buffer);
				if (readBytes > 0) {
					buffer.flip();
					byte[] bytes =  new byte[buffer.remaining()];
					buffer.get(bytes);
					// 得到输入的字符串
					String inputStr = new String(bytes, CharsetUtil.UTF_8);
					
					// 把了输入的字符串写回客户端
					if (!StringUtil.isNullOrEmpty(inputStr)) {
						byte[] resp = inputStr.getBytes(CharsetUtil.UTF_8);
						ByteBuffer wBuffer = ByteBuffer.allocate(resp.length);
						wBuffer.put(resp);
						wBuffer.flip();
						// 写回客户端
						channel.write(wBuffer);
					}
				}
				
			}
			
		}
	}

}
