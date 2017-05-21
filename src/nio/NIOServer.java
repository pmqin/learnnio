package nio;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NIOServer {
	int port = 8080;
	ServerSocketChannel server;
	
	Selector selector;
	
	
	ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
	ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
	
	
	Map<SelectionKey,String> sessionMsg = new HashMap<SelectionKey,String>();
	
	public NIOServer(int port) throws IOException{
		
		this.port = port;
		//就相当于是高速公路，同时开几排车,带宽，带宽越大，车流量就越多
		server = ServerSocketChannel.open();
		
		//关卡也打开了，可以多路复用了
		server.socket().bind(new InetSocketAddress(this.port));
		//默认是阻塞的，手动设置为非阻塞，它才是非阻塞
		server.configureBlocking(false);
		
		//叫号系统开始工作
		selector = Selector.open();
		
		//高速管家，BOSS已经准备就绪，等会有客人来了，要通知我一下
				//我感兴趣的事件
		server.register(selector, SelectionKey.OP_ACCEPT);
		
		System.out.println("NIO服务已经启动，监听端口是：" + this.port);
	}
	
	
	public void listener() throws IOException{
		//轮询
		while(true){
			//判断一下，当前有没有客户来注册，有没有排队的，有没有取号的
			//没有你感兴趣的事件触发的时候，它还是阻塞在这个位置
			//只要有你感兴趣的事件发生的时候，程序才会往下执行
			int i = selector.select();//这个位置，还是一个阻塞的
		
			if(i == 0){ continue;}
			
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = keys.iterator();
			
			while (iterator.hasNext()) {

				//来一个处理一个
				process(iterator.next());
				
				//处理完之后打发走
				iterator.remove();
				
			}
			
		}
		
		
	}
	
	
	private void process(SelectionKey key) throws IOException{
		
		//�жϿͻ���û�и�����BOSS����������
		if(key.isAcceptable()){
			SocketChannel client = server.accept();
			client.configureBlocking(false);
			//������߽к�ϵͳ����һ����Ҫ��ʼ������ˣ��ǵ�֪ͨ��
			client.register(selector, SelectionKey.OP_READ);
		}
		//�ж��Ƿ���Զ������
		else if(key.isReadable()){
			receiveBuffer.clear();
			
			//�����ܵ��ǽ�������NIO API�ڲ�ȥ�����
			SocketChannel client = (SocketChannel)key.channel();
			int len = client.read(receiveBuffer);
			if(len > 0){
				String msg = new String(receiveBuffer.array(), 0, len);
				sessionMsg.put(key, msg);
				System.out.println("��ȡ�ͻ��˷���������Ϣ��" + msg);
			}
			//���߽к�ϵͳ����һ���Ҹ���Ȥ���¼�����Ҫд�����
			client.register(selector, SelectionKey.OP_WRITE);
		}
		//�Ƿ����д���
		else if(key.isWritable()){
			
			
			if(!sessionMsg.containsKey(key)){ return;}
			
			SocketChannel client = (SocketChannel)key.channel();
			
			sendBuffer.clear();
			
			sendBuffer.put(new String(sessionMsg.get(key) + ",��ã���������Ѵ������").getBytes());
			
			sendBuffer.flip();//
			client.write(sendBuffer);
			//�ٸ������ǽк�ϵͳ����һ����Ҫ���ĵĶ��������Ƕ���
			//��˷��������ǵĳ�������뵽��һ������ȡ���ͬ����������ȥ�˵�
			client.register(selector,SelectionKey.OP_READ);
			//SelectionKey.OP_ACCEPT//����ˣ�ֻҪ�ͻ������ӣ��ʹ���
			//SelectionKey.OP_CONNECT//�ͻ��ˣ�ֻҪ�����˷���ˣ��ʹ���
			//SelectionKey.OP_READ//ֻҪ������������ʹ���
			//SelectionKey.OP_WRITE//ֻҪ����д�������ʹ���
			
		}
		
	}
	
	
	public static void main(String[] args) throws IOException {
		new NIOServer(8080).listener();
	}
}
