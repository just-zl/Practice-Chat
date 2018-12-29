import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//在线聊天工具1.1版本--服务器端
//第1次改动：将客户端发送到服务器端的数据，转发给其他客户端

public class ChatOfServer {
	
	//存储所有的Client。不用数组，因为数组写死了大小
	List<Client> clients = new ArrayList<Client>();
	
	public static void main(String[] args) {
		
		new ChatOfServer().start();
	}
	
	void start() {

		ServerSocket ss = null;
		boolean flag = false;
		
		try {
			ss = new ServerSocket(6666);
			flag = true;  //确认建立连接
		}catch (BindException e) {
			System.out.println("端口使用中……请关闭相关程序，并重启服务器。");
			System.exit(0);
		}
		catch (IOException e) {
			e.printStackTrace();
		}  
		//分成两节，单独处理下面的读取文件异常
		try{
			while(flag) {
				//接收客户端信息
				Socket s = ss.accept();
System.out.println("A Client Connect");
				Client c = new Client(s); //如果放在main()这个静态方法里，new ClientThread(s) 不可执行,所以封装到start()方法里
				new Thread(c).start();
				
				//将客户端的包装类 装入容器
				clients.add(c);
				
				//dis.close();  //TODO 什么时候关闭？？？？？？答：在下面
			}

		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	//内部线程类
	class Client implements Runnable {
		private Socket s = null;
		private DataInputStream dis = null;
		private DataOutputStream dos = null;
		boolean bConnected = false;
		
		Client(Socket s) {
			this.s = s;
			try {
				dis = new DataInputStream(s.getInputStream());
				//*1.1新增******************转发到客户端***********************
				dos = new DataOutputStream(s.getOutputStream()); 
				
				bConnected = true;  //一次连接，一直接收到客户端的信息
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//*1.1新增******************转发到客户端***********************	
		public void send(String str) {
			try {
				if((str != null) && (str.length() != 0)) {  //判断发送的内容是否为空
					dos.writeUTF(str + "\n");
				}
				else {
					System.out.println("不能发送空内容！！");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			
			try {
				while(bConnected) {
					String string = dis.readUTF();  //readUTF()是阻塞式的，所以不能接收多个客户端的数据，因为readUTF()一直在等第一个客户端的数据
System.out.println(string);  //输出到控制台
					//*1.1新增******************转发到其他客户端***********************
					for(int i = 0;i<clients.size();i++) {
						Client c = clients.get(i);
						if(!c.s.equals(this.s) && !c.s.isClosed()) {  //给其他客户端发消息
							c.send(string);
						}
						else if(!c.s.equals(this.s) && c.s.isClosed()) {  //如果其他客户端退出
							System.out.println("对方已下线,未接收到您的消息");
						}
					}
					//下面这两种方法都可以，但是他们内部执行了锁定，效率会慢，且不必要进行锁定
					/*for(Iterator<Client> it = clients.iterator();it.hasNext();) {
						dos.writeUTF(string);
					}
					
					Iterator<Client> it = clients.iterator();
					while(it.hasNext()) {
						dos.writeUTF(string);
					}*/
				}
			}catch (EOFException e) {  //End Of File当客户端关闭窗口时会报错，因为readUTF()是阻塞式的，它会一直等数据传过来
				
				System.out.println("A Client off");  //读取文件异常,客户端下线
			}catch (IOException e) {  //非客户端错误
				
				e.printStackTrace();
			} finally {  //严格的处理，加finally
				
				try {
					if(dis != null) dis.close();  //更严格的处理，判断是否为空
					if(dos != null) dos.close();
					if(s != null) s.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
		}
	}
}
	
