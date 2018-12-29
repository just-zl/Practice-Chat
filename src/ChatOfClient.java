import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JFrame;

//在线聊天工具1.1版本--客户端

public class ChatOfClient extends JFrame {
	
	Socket s = null;
	TextArea tArea = new TextArea();
	TextField inputBox = new TextField(20);
	DataOutputStream dos = null;
	DataInputStream dis = null;
	boolean bConncted = false;
	RecieveThread recieveThread = new RecieveThread();
	Thread rThread = new Thread(recieveThread);
	
	public static void main(String[] args) {
		//显示图形界面
		new ChatOfClient().LaunchFrame();
	}
	
	//显示图形界面
	public void LaunchFrame() {
		
		setBounds(200,200,300,300);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				disconnect();  //关闭窗口的时候，关闭内存
				setVisible(false);
				System.exit(0);
			}
		});
		
		//底部输入框
		Panel panel = new Panel();
		panel.setBackground(Color.lightGray);
		Button sendMess = new Button("Send");
		panel.add(inputBox,BorderLayout.WEST);
		panel.add(sendMess,BorderLayout.EAST);
		
		//按钮事件监听
		sendMess.addActionListener(new ButtonMonitor());
		
		//将组件添加到frame
		add(tArea,BorderLayout.CENTER);
		add(panel,BorderLayout.SOUTH);
		setVisible(true);
		
		connect();
	}
	
	//尝试与服务器端进行连接
	private void connect() {
		
		try {
			s = new Socket("10.16.183.188", 6666);
//System.out.println(s);  //生成多个客户端时，他们的port都是6666，但是local port不一样	
System.out.println("Connected");
			//发送数据的管道
			dos = new DataOutputStream(s.getOutputStream());
			bConncted = true;
			
//1.1新增******************起线程循环 接收服务器端转发的客户端消息*******************
			rThread.start();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//关闭内存
	private void disconnect() {
		try {
			dos.close();
			dis.close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//正常的线程退出可以这么做。本例因为线程里有一个阻塞式的readUTF()方法。
		/*try {
			bConncted = false;
			rThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			try {
				dos.close();
				dis.close();
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
	}
	
	private class ButtonMonitor implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String string = inputBox.getText().trim();  //去掉前后空格
			if((string != null) && (string.length() != 0)) {  //将单行输入框的内容写入到文本框里
				tArea.append("You:" + string + "\n" + "\n");
				inputBox.setText("");
			}
			else {
				System.out.println("There Is No Message!");
			}
		
			try {
				//需要发送的数据
				dos.writeUTF(string);
				dos.flush();
				//接收数据  不在此处
				//DataInputStream dis = new DataInputStream(s.getInputStream());
				//System.out.println(dis.readUTF());
				//tArea.append(dis.readUTF() + "\n");
				
				//dos.close();  //关闭后，第二次输入信息时，会报错：DataOutputStream 关闭了，换个方式关闭
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
	}

	//起一个线程，不断地接收服务器端的数据。如果用main方法调用，接收的死循环，那么其他写在循环后面(connect()方法以后)的语句将执行不了
	class RecieveThread implements Runnable {

		@Override
		public void run() {
			
			try {
				while (bConncted) {
					dis = new DataInputStream(s.getInputStream());
					String str = dis.readUTF();
					if((str != null) && (str.length() != 0)) {
						tArea.append(str + "\n");
					}
				}
			}catch (SocketException e) {
				System.out.println("你以为是正常退出？");  //本可以通过jion()方法，让线程停止，但是该线程中有一个readUTF()阻塞式方法
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}

