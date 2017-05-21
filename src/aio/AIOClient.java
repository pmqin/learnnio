package aio;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AIOClient {
	
	AsynchronousSocketChannel client;
	
	InetSocketAddress serverAddress = new InetSocketAddress("localhost",8080);
	
	ByteBuffer sendBuff = ByteBuffer.allocate(1024);
	
	public AIOClient() throws IOException, InterruptedException, ExecutionException{
		client = AsynchronousSocketChannel.open();
		Future<?> f = client.connect(serverAddress);
		System.out.println("�ͻ���������");
	}
	
	
	public void send(String content){
		sendBuff.clear();
		sendBuff.put(content.getBytes());
		sendBuff.flip();
		client.write(sendBuff);
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		AIOClient client = new AIOClient();
		Scanner sc = new Scanner(System.in);
		while(sc.hasNextLine()){
			String content = sc.nextLine();
			client.send(content);
		}
	}
	
}
