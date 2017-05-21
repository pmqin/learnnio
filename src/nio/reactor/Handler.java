package nio.reactor;

import java.io.IOException;  
import java.nio.ByteBuffer;  
import java.nio.channels.SelectionKey;  
import java.nio.channels.Selector;  
import java.nio.channels.SocketChannel;  
  
/**
 * 
 * @author songzg
 * @version 1.0.0
 */
public class Handler implements Runnable {  
    final SocketChannel socketChannel;  
    final SelectionKey key;  
    static final int MAXIN = 8192, MAXOUT = 11240 * 1024;  
    ByteBuffer readBuffer = ByteBuffer.allocate(MAXIN);  
    ByteBuffer outBuffer = ByteBuffer.allocate(MAXOUT);  
    static final int READING = 0;  
    static final int SENDING = 1;  
    int state = READING;  
  
    Handler(Selector selector, SocketChannel socketChannel) throws IOException {  
        this.socketChannel = socketChannel;  
        // 用selector注册套接字，并返回对应的SelectionKey，同时设置Key的interest set为监听该连接上得read事件  
        this.key = socketChannel.register(selector, SelectionKey.OP_READ);  
        // 绑定handler  
        this.key.attach(this);  
    }  
  
    /** 
     * 处理write 
     *  
     * @throws IOException 
     */  
    private void write() throws IOException {  
        socketChannel.write(outBuffer);  
        if (outBuffer.remaining() > 0) {  
            return;  
        }  
        state = READING;  
        key.interestOps(SelectionKey.OP_READ);  
    }  
  
    /** 
     * 处理read 
     *  
     * @throws IOException 
     */  
    private void read() throws IOException {  
        readBuffer.clear();  
        int numRead;  
        try {  
            // 读取数据  
            numRead = socketChannel.read(readBuffer);  
        } catch (Exception e) {  
            key.cancel();  
            socketChannel.close();  
            return;  
        }  
  
        if (numRead == -1) {  
            socketChannel.close();  
            key.cancel();  
            return;  
        }  
        // 处理数据  
        process(numRead);  
  
    }  
  
    /** 
     * 处理数据 
     *  
     * @param numRead 
     */  
    private void process(int numRead) {  
        byte[] dataCopy = new byte[numRead];  
        System.arraycopy(readBuffer.array(), 0, dataCopy, 0, numRead);  
        System.out.println(new String(dataCopy));  
        outBuffer = ByteBuffer.wrap(dataCopy);  
        state = SENDING;  
        // 设置Key的interest set为监听该连接上的write事件  
        key.interestOps(SelectionKey.OP_WRITE);  
    }  
  
    @Override  
    public void run() {  
        try {  
            if (state == READING) {  
                read();  
            } else if (state == SENDING) {  
                write();  
            }  
  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
  
    }  
}  