package nio.reactor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

/**
 * 
 * @author songzg
 * @version 1.0.0
 */
public class Reactor implements Runnable {

	private InetAddress hostAddress;
	private int port;

	ServerSocketChannel serverChannel;

	Selector selector;

	public Reactor(InetAddress hostAddress, int port) throws IOException {
		this.hostAddress = hostAddress;
		this.port = port;
		// 初始化selector，绑定服务端监听套接字、感兴趣事件及对应的handler
		this.selector = initSelector();
	}

	public static void main(String[] args) {
		try {
			// 启动服务器
			new Thread(new Reactor(null, 9090)).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				// 选择事件已经ready的selectionKey，该方法是阻塞的，只有当至少存在selectionKey，或者wakeup方法被调用，或者当前线程被中断，才会返回
				selector.select();
				// 循环处理每一个事件
				Iterator<SelectionKey> items = selector.selectedKeys().iterator();
				while (items.hasNext()) {
					SelectionKey key = (SelectionKey) items.next();
					items.remove();

					if (!key.isValid()) {
						continue;
					}
					// 事件处理分发
					dispatch(key);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 事件处理分发
	 * 
	 * @param sk 已经ready的selectionKey
	 */
	private void dispatch(SelectionKey sk) {
		// 获取绑定的handler
		Runnable r = (Runnable) sk.attachment();
		if (r != null) {
			r.run();
		}
	}

	/**
	 * 初始化selector，绑定服务端监听套接字、感兴趣事件及对应的handler
	 * 
	 * @return
	 * @throws IOException
	 */
	private Selector initSelector() throws IOException {
		// 创建一个selector
		Selector socketSelector = Selector.open();
		// 创建并打开ServerSocketChannel
		serverChannel = ServerSocketChannel.open();
		// 设置为非阻塞
		serverChannel.configureBlocking(false);
		// 绑定端口
		serverChannel.socket().bind(new InetSocketAddress(hostAddress, port));
		// 用selector注册套接字，并返回对应的SelectionKey，同时设置Key的interest set为监听客户端连接事件
		SelectionKey selectionKey = serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
		// 绑定handler
		selectionKey.attach(new Acceptor(this));

		return socketSelector;
	}

}