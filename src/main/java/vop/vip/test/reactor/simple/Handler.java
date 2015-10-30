package vop.vip.test.reactor.simple;

import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Handler implements Runnable {
	final SocketChannel socket;
	final SelectionKey sk;
	ByteBuffer input = ByteBuffer.allocate(1024);
	ByteBuffer output = ByteBuffer.allocate(1024);
	static final int READING = 0, SENDING = 1;
	int state = READING;

	Handler(Selector sel, SocketChannel c) throws IOException {
		socket = c;
		c.configureBlocking(false);
		// Optionally try first read now
		// 客户端连接注册到多路复用器上
		sk = socket.register(sel, 0);
		// 绑定分发器为当前类
		sk.attach(this);
		// 监听读事件
		sk.interestOps(SelectionKey.OP_READ);
		sel.wakeup();
	}

	boolean inputIsComplete() { 
		input.flip();
		byte[] bytes =  new byte[input.remaining()];
		input.get(bytes);
		output = ByteBuffer.allocate(bytes.length);
		output.put(bytes);
		output.flip();
		
		return true;
	}

	boolean outputIsComplete() {
		return true ;
	}

	void process() { 
		// 处理业务逻辑
		System.out.println("process logic...");
	}

	public void run() {
		try {
			if (state == READING)
				read();
			else if (state == SENDING)
				send();
		} catch (IOException ex) { /* ... */
		}
	}

	void read() throws IOException {
		socket.read(input);
		if (inputIsComplete()) {
			process();
			state = SENDING;
			// Normally also do first write now
			sk.interestOps(SelectionKey.OP_WRITE);
		}
	}

	void send() throws IOException {
		socket.write(output);
		if (outputIsComplete())
			sk.cancel();
	}
}
