/**
 * Copyright 2016 CTRIP Co.,Ltd. All rights reserved.
 */
package nio.reactor;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 处理OP_ACCEPT事件的handler
 * 
 */
class Acceptor implements Runnable {
	/**
	 * 
	 */
	private final Reactor acceptor;

	/**
	 * @param nioServer
	 */
	Acceptor(Reactor nioServer) {
		acceptor = nioServer;
	}

	@Override
	public void run() {
		try {
			accept();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void accept() throws IOException {
		System.out.println("connect");
		// 建立连接
		SocketChannel socketChannel = acceptor.serverChannel.accept();
		System.out.println("connected");
		// 设置为非阻塞
		socketChannel.configureBlocking(false);
		// 创建Handler,专门处理该连接后续发生的OP_READ和OP_WRITE事件
		new Handler(acceptor.selector, socketChannel);
	}

}