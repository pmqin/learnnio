package aio;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AIOServer {

	AsynchronousServerSocketChannel server;
	
	ByteBuffer receviceBuff = ByteBuffer.allocate(1024);
	
	int port = 8080;
	
	public AIOServer(int port) throws IOException{
		this.port = port;
		//Ҫ�븻������·
		server = AsynchronousServerSocketChannel.open();
		
		server.bind(new InetSocketAddress("localhost", this.port));
		
	}
	
	public void listener(){
		new Thread(){
			public void run() {
				System.out.println("AIO����������������˿�" + port);
				server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
					//成功以后的回调

					public void completed(AsynchronousSocketChannel client,Void attachment) {
						server.accept(null,this);
						process(client);
					}
					
					public void failed(Throwable exc, Void attachment) {
						System.out.println("�첽IOʧ��");
					}
					
					private void process(AsynchronousSocketChannel client){
						try{
							receviceBuff.clear();
							int len = client.read(receviceBuff).get();
							receviceBuff.flip();
							System.out.println("�ѽ��յ��ͻ��˷�������Ϣ��" + new String(receviceBuff.array(),0,len));
						}catch(Exception e){
							
						}
					}
					
				});
				while(true){}
			}
		}.run();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		new AIOServer(8080).listener();
		Thread.sleep(20000);
	}
	
}
