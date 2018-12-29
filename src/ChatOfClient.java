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

//�������칤��1.1�汾--�ͻ���

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
		//��ʾͼ�ν���
		new ChatOfClient().LaunchFrame();
	}
	
	//��ʾͼ�ν���
	public void LaunchFrame() {
		
		setBounds(200,200,300,300);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				disconnect();  //�رմ��ڵ�ʱ�򣬹ر��ڴ�
				setVisible(false);
				System.exit(0);
			}
		});
		
		//�ײ������
		Panel panel = new Panel();
		panel.setBackground(Color.lightGray);
		Button sendMess = new Button("Send");
		panel.add(inputBox,BorderLayout.WEST);
		panel.add(sendMess,BorderLayout.EAST);
		
		//��ť�¼�����
		sendMess.addActionListener(new ButtonMonitor());
		
		//�������ӵ�frame
		add(tArea,BorderLayout.CENTER);
		add(panel,BorderLayout.SOUTH);
		setVisible(true);
		
		connect();
	}
	
	//������������˽�������
	private void connect() {
		
		try {
			s = new Socket("10.16.183.188", 6666);
//System.out.println(s);  //���ɶ���ͻ���ʱ�����ǵ�port����6666������local port��һ��	
System.out.println("Connected");
			//�������ݵĹܵ�
			dos = new DataOutputStream(s.getOutputStream());
			bConncted = true;
			
//1.1����******************���߳�ѭ�� ���շ�������ת���Ŀͻ�����Ϣ*******************
			rThread.start();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//�ر��ڴ�
	private void disconnect() {
		try {
			dos.close();
			dis.close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//�������߳��˳�������ô����������Ϊ�߳�����һ������ʽ��readUTF()������
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
			String string = inputBox.getText().trim();  //ȥ��ǰ��ո�
			if((string != null) && (string.length() != 0)) {  //����������������д�뵽�ı�����
				tArea.append("You:" + string + "\n" + "\n");
				inputBox.setText("");
			}
			else {
				System.out.println("There Is No Message!");
			}
		
			try {
				//��Ҫ���͵�����
				dos.writeUTF(string);
				dos.flush();
				//��������  ���ڴ˴�
				//DataInputStream dis = new DataInputStream(s.getInputStream());
				//System.out.println(dis.readUTF());
				//tArea.append(dis.readUTF() + "\n");
				
				//dos.close();  //�رպ󣬵ڶ���������Ϣʱ���ᱨ��DataOutputStream �ر��ˣ�������ʽ�ر�
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
	}

	//��һ���̣߳����ϵؽ��շ������˵����ݡ������main�������ã����յ���ѭ������ô����д��ѭ������(connect()�����Ժ�)����佫ִ�в���
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
				System.out.println("����Ϊ�������˳���");  //������ͨ��jion()���������߳�ֹͣ�����Ǹ��߳�����һ��readUTF()����ʽ����
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}

