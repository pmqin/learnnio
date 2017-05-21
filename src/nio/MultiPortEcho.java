package nio;

/**
 * 
 */
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiPortEcho {
	private int ports[];
	private ByteBuffer echoBuffer = ByteBuffer.allocate(1024);

	public MultiPortEcho(int ports[]) throws IOException {
		this.ports = ports;

		go();
	}

	private void go() throws IOException {
		Selector selector = Selector.open();

		for (int i = 0; i < ports.length; ++i) {
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ServerSocket ss = ssc.socket();
			InetSocketAddress address = new InetSocketAddress(ports[i]);
			ss.bind(address);

			SelectionKey key = ssc.register(selector, SelectionKey.OP_ACCEPT);

			System.out.println("Going to listen on " + ports[i]);
		}

		while (true) {
			int num = selector.select();

			Set selectedKeys = selector.selectedKeys();
			Iterator it = selectedKeys.iterator();

			while (it.hasNext()) {
				SelectionKey key = (SelectionKey) it.next();

				if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
					ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
					SocketChannel sc = ssc.accept();
					sc.configureBlocking(false);

					SelectionKey newKey = sc.register(selector, SelectionKey.OP_READ);
					it.remove();

					System.out.println("Got connection from " + sc);
				} else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
					SocketChannel sc = (SocketChannel) key.channel();

					int bytesEchoed = 0;
					while (true) {
						echoBuffer.clear();

						int r = sc.read(echoBuffer);

						if (r <= 0) {
							break;
						}

						echoBuffer.flip();

						sc.write(echoBuffer);
						bytesEchoed += r;
					}

					System.out.println("Echoed " + bytesEchoed + " from " + sc);

					it.remove();
				}

			}

		}
	}

	static public void main(String args[]) throws Exception {

		int ports[] = { 8080, 8181 };

		new MultiPortEcho(ports);
	}
}
