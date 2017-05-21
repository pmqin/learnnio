package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;

//这个程序是被连接的程序,俗称服务器程序  
//在程序界中,网络是指2个程序产生信息交互,那么构成网络  
//所以网络必须要有2个程序以上,一端叫服务器,一端叫客户端,通常服务器和客户端的对应关系是1-N  

public class SimpleServer {

	// 用于保存当前连接的用户
	public static ArrayList<Socket> socketList = new ArrayList<Socket>();

	public static void main(String[] args) throws Exception {

		// 创建一个serverSocket，用于监听客户端Socket的连接请求
		ServerSocket serverSocket = new ServerSocket(10002);// 10002为此服务器的端口号
		System.out.println("服务启动");
		// 采用循环不断接收来自客户端的请求
		while (true) {
			// 每当接收到客户端的请求时，服务器端也对应产生一个Socket
			Socket socket = serverSocket.accept();
			System.out.println("服务连接");
			// 把连接的客户端加入到list列表中
			socketList.add(socket);
			// 启动一个新的线程
			// 任务线程,每个连接用都有一个属于自己专用的任务线程
			// 这个线程内会处理信息的发送和响应
			new MyThread(socket, socketList).start();
		}
		/*
		 * BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		 * 
		 * PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); // 把输入的内容输出到client while (true) { String msg = br.readLine(); pw.println("服务器说：" + msg); }
		 */
	}
}

class MyThread extends Thread {

	Socket client;
	ArrayList<Socket> clients;

	BufferedReader br;

	public MyThread(Socket client, ArrayList<Socket> clients) throws Exception {
		super();
		this.client = client;
		this.clients = clients;

		br = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
	}

	// 把接收到的消息发送给各客户端
	public void run() {
		try {
			String content = null;
			while (true) {
				// 从某个客户端读取信息
				if ((content = br.readLine()) != null) {
					for (Socket socket : clients) {
						if (client != socket) {
							// 把读取到的消息发送给各个客户端
							PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
							pw.println(content);
						}
					}
					content = URLDecoder.decode(content, "UTF-8");
					System.out.println(content);

				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}