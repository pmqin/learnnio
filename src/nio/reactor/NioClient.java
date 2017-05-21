package nio.reactor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
/**
 * 
 * @author songzg
 * @version 1.0.0
 */
public class NioClient implements Runnable {
	private InetAddress hostAddress;
	private int port;
	private Selector selector;
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	private ByteBuffer outBuffer = ByteBuffer.wrap("nice to meet you".getBytes());

	public NioClient(InetAddress hostAddress, int port) throws IOException {
		this.hostAddress = hostAddress;
		this.port = port;
		initSelector();
	}

	public static void main(String[] args) {
		try {
			NioClient client = new NioClient(InetAddress.getByName("localhost"), 9090);
			new Thread(client).start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				selector.select();

				Iterator<?> selectedKeys = selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					if (key.isConnectable()) {
						finishConnection(key);
					} else if (key.isReadable()) {
						read(key);
					} else if (key.isWritable()) {
						write(key);
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void initSelector() throws IOException {
		// 创建一个selector
		selector = SelectorProvider.provider().openSelector();
		// 打开SocketChannel
		SocketChannel socketChannel = SocketChannel.open();
		// 设置为非阻塞
		socketChannel.configureBlocking(false);
		// 连接指定IP和端口的地址
		socketChannel.connect(new InetSocketAddress(this.hostAddress, this.port));
		// 用selector注册套接字，并返回对应的SelectionKey，同时设置Key的interest set为监听服务端已建立连接的事件
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
	}

	private void finishConnection(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		try {
			// 判断连接是否建立成功，不成功会抛异常
			socketChannel.finishConnect();
		} catch (IOException e) {
			key.cancel();
			return;
		}
		// 设置Key的interest set为OP_WRITE事件
		key.interestOps(SelectionKey.OP_WRITE);
	}

	/**
	 * 处理read
	 * 
	 * @param key
	 * @throws IOException
	 */
	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		readBuffer.clear();
		int numRead;
		try {
			numRead = socketChannel.read(readBuffer);
		} catch (Exception e) {
			key.cancel();
			socketChannel.close();
			return;
		}
		if (numRead == 1) {
			System.out.println("close connection");
			socketChannel.close();
			key.cancel();
			return;
		}
		// 处理响应
		handleResponse(socketChannel, readBuffer.array(), numRead);
	}

	/**
	 * 处理响应
	 * 
	 * @param socketChannel
	 * @param data
	 * @param numRead
	 * @throws IOException
	 */
	private void handleResponse(SocketChannel socketChannel, byte[] data, int numRead) throws IOException {
		byte[] rspData = new byte[numRead];
		System.arraycopy(data, 0, rspData, 0, numRead);
		System.out.println(new String(rspData));
		socketChannel.close();
		socketChannel.keyFor(selector).cancel();
	}

	/**
	 * 处理write
	 * 
	 * @param key
	 * @throws IOException
	 */
	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		socketChannel.write(outBuffer);
		if (outBuffer.remaining() > 0) {
			return;
		}
		// 设置Key的interest set为OP_READ事件
		key.interestOps(SelectionKey.OP_READ);
	}

}