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

//�������칤��1.1�汾--��������
//��1�θĶ������ͻ��˷��͵��������˵����ݣ�ת���������ͻ���

public class ChatOfServer {
	
	//�洢���е�Client���������飬��Ϊ����д���˴�С
	List<Client> clients = new ArrayList<Client>();
	
	public static void main(String[] args) {
		
		new ChatOfServer().start();
	}
	
	void start() {

		ServerSocket ss = null;
		boolean flag = false;
		
		try {
			ss = new ServerSocket(6666);
			flag = true;  //ȷ�Ͻ�������
		}catch (BindException e) {
			System.out.println("�˿�ʹ���С�����ر���س��򣬲�������������");
			System.exit(0);
		}
		catch (IOException e) {
			e.printStackTrace();
		}  
		//�ֳ����ڣ�������������Ķ�ȡ�ļ��쳣
		try{
			while(flag) {
				//���տͻ�����Ϣ
				Socket s = ss.accept();
System.out.println("A Client Connect");
				Client c = new Client(s); //�������main()�����̬�����new ClientThread(s) ����ִ��,���Է�װ��start()������
				new Thread(c).start();
				
				//���ͻ��˵İ�װ�� װ������
				clients.add(c);
				
				//dis.close();  //TODO ʲôʱ��رգ�������������������
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
	
	//�ڲ��߳���
	class Client implements Runnable {
		private Socket s = null;
		private DataInputStream dis = null;
		private DataOutputStream dos = null;
		boolean bConnected = false;
		
		Client(Socket s) {
			this.s = s;
			try {
				dis = new DataInputStream(s.getInputStream());
				//*1.1����******************ת�����ͻ���***********************
				dos = new DataOutputStream(s.getOutputStream()); 
				
				bConnected = true;  //һ�����ӣ�һֱ���յ��ͻ��˵���Ϣ
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//*1.1����******************ת�����ͻ���***********************	
		public void send(String str) {
			try {
				if((str != null) && (str.length() != 0)) {  //�жϷ��͵������Ƿ�Ϊ��
					dos.writeUTF(str + "\n");
				}
				else {
					System.out.println("���ܷ��Ϳ����ݣ���");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			
			try {
				while(bConnected) {
					String string = dis.readUTF();  //readUTF()������ʽ�ģ����Բ��ܽ��ն���ͻ��˵����ݣ���ΪreadUTF()һֱ�ڵȵ�һ���ͻ��˵�����
System.out.println(string);  //���������̨
					//*1.1����******************ת���������ͻ���***********************
					for(int i = 0;i<clients.size();i++) {
						Client c = clients.get(i);
						if(!c.s.equals(this.s) && !c.s.isClosed()) {  //�������ͻ��˷���Ϣ
							c.send(string);
						}
						else if(!c.s.equals(this.s) && c.s.isClosed()) {  //��������ͻ����˳�
							System.out.println("�Է�������,δ���յ�������Ϣ");
						}
					}
					//���������ַ��������ԣ����������ڲ�ִ����������Ч�ʻ������Ҳ���Ҫ��������
					/*for(Iterator<Client> it = clients.iterator();it.hasNext();) {
						dos.writeUTF(string);
					}
					
					Iterator<Client> it = clients.iterator();
					while(it.hasNext()) {
						dos.writeUTF(string);
					}*/
				}
			}catch (EOFException e) {  //End Of File���ͻ��˹رմ���ʱ�ᱨ����ΪreadUTF()������ʽ�ģ�����һֱ�����ݴ�����
				
				System.out.println("A Client off");  //��ȡ�ļ��쳣,�ͻ�������
			}catch (IOException e) {  //�ǿͻ��˴���
				
				e.printStackTrace();
			} finally {  //�ϸ�Ĵ�����finally
				
				try {
					if(dis != null) dis.close();  //���ϸ�Ĵ����ж��Ƿ�Ϊ��
					if(dos != null) dos.close();
					if(s != null) s.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
		}
	}
}
	
