package vop.vip.test.reactor.multi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiHandler implements Runnable {

	private ExecutorService pool = Executors.newFixedThreadPool(3);

	final SocketChannel socket;
	final SelectionKey sk;
	ByteBuffer input = ByteBuffer.allocate(1024);
	ByteBuffer output = ByteBuffer.allocate(1024);
	static final int READING = 0, SENDING = 1;
	int state = READING;

	MultiHandler(Selector sel, SocketChannel c) throws IOException {
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
		byte[] bytes = new byte[input.remaining()];
		input.get(bytes);
		output = ByteBuffer.allocate(bytes.length);
		output.put(bytes);
		output.flip();

		return true;
	}

	boolean outputIsComplete() {
		return true;
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
			// 由线程池来处理业务逻辑
			pool.execute(new Processer());
		}
	}

	void send() throws IOException {
		socket.write(output);
		if (outputIsComplete())
			sk.cancel();
	}

	synchronized void processAndHandOff() {
		process();
		state = SENDING; 
		sk.interestOps(SelectionKey.OP_WRITE);
	}

	class Processer implements Runnable {
		public void run() {
			processAndHandOff();
		}
	}
}
