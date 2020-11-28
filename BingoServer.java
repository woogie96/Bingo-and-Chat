package Bingo;

import java.io.*;
import java.net.*;
import java.util.*;


public class BingoServer {
	public static final int PORT = 8742;
	ServerSocket ss;
	Socket sock;
	Vector v;

	public void startServer() {
		try {
			ss = new ServerSocket(PORT);
			v = new Vector();

			while (true) {
				sock = ss.accept();
				BingoThread bi = new BingoThread(this, sock);
				this.addThread(bi);
				bi.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				sock.close();
				ss.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addThread(BingoThread bi) {
		v.add(bi);
	}

	public void remove(BingoThread bi) {
		v.remove(bi);
	}

	public BingoThread getOT(int i) {
		return (BingoThread) v.elementAt(i);
	}

	public int getRoomNumber1(int i) {  //i��° �ִ� Ŭ���̾�Ʈ �� �ѹ� ����
		return getOT(i).getRoomNumber();
	}

	public static void main(String args[]) {
		BingoServer server = new BingoServer();
		server.startServer();
	}
	
	public void broadCast(int RoomNumber, String str) {  //�ڽ��� �� �濡 �ִ� Ŭ���̾�Ʈ���Ը� �޼��� ����
		for (int i = 0; i < v.size(); i++) {
			if (RoomNumber == getRoomNumber1(i)) {
				BingoThread BI = getOT(i);
				BI.send(str);
			}
		}
	}
}

class BingoThread extends Thread {
	Socket socket1;
	BufferedReader reader;
	PrintWriter writer;
	BingoServer cg2;
	String currentNumber;
	String name;
	int roomNumber = 0;
	static String[] nameArray = new String[8];
	static int nameCount = 0;

	public BingoThread(BingoServer cg1, Socket socket) {
		this.cg2 = cg1;
		this.socket1 = socket;
		try {
			reader = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
			writer = new PrintWriter(socket1.getOutputStream(), true);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void send(String currentNumber) { //�޼��� Ŭ���̾�Ʈ���� ������
		writer.println(currentNumber);
		writer.flush();
	}

	public int getRoomNumber() {
		return roomNumber;
	}

	public int roomUserCount(int roomNumber) { //�濡 �� ���� �� 
		int count = 0;
		for(int i=0; i<cg2.v.size(); i++) {
			if(roomNumber == cg2.getRoomNumber1(i)) {
				count++;
			}
		}
		return count;
	}
	boolean roomFull(int roomNumber) { //���� ����á���� ��á����
		int count1 = roomUserCount(roomNumber);
		
		if(count1>=2) {
			return true;
		}
		writer.println("isPlay");    //�濡 ���� ���� ����� ����
		writer.flush(); 
		return false;
	}
	public static boolean compare_name(String newName) { // ���ο� name�� �ö� nameArray�� �ִ��� �Ǵ�.
	    for (String eachName : nameArray) {
	       if (newName.equals(eachName))
	          return true;
	    }
	    return false;
	}
	
	@Override
	public void run() {
		try {
			
			while ((currentNumber = reader.readLine()) != null) {
				if (currentNumber.startsWith("Room")) {
					
					int roomNum = Integer.parseInt(currentNumber.substring(4));
					if(!roomFull(roomNum)){
						roomNumber = roomNum;

						writer.println("OK" + roomNumber); // �濡 ���� ���� �ƴϸ� OK �������� Ŭ���̾�Ʈ �濡 ����
						cg2.broadCast(roomNumber, "count" + roomUserCount(roomNumber)); //�� �濡 ����ִ��� count ��������
					}
					else {
						writer.println("FULL" + roomNumber); //�濡 �������� full �������ݷ� FULL �޼��� ������
					}
				}
				

				else if (currentNumber.startsWith("MSG")) { //MSG ���������̸� �濡 �ִ� �����鿡�� �޼��� ������
					cg2.broadCast(roomNumber, "MSG[" + name + "]: " + currentNumber.substring(3));
				}
				else if (currentNumber.startsWith("Judge")) {
					cg2.broadCast(roomNumber, currentNumber);
				}
				else if (currentNumber.startsWith("Score")) {
					if (Integer.parseInt(currentNumber.substring(5))>=3) {
						cg2.broadCast(roomNumber, "Draw");
					} else {
						cg2.broadCast(roomNumber, "reset");
					}
				}
				
				else if (currentNumber.startsWith("Name")) { //Name ���������̸� Ŭ�� �̸� ���� �� �濡 ���� ���� ������
					name = currentNumber.substring(4);
					if (compare_name(name)) { //�ߺ����� �Ǵ�
						cg2.broadCast(roomNumber, "Rename"); //�ߺ��̸� Rename ����
					} else { //�ƴϸ� Ŭ�� �̸� ���� �� �濡 ���� ���� ������
						cg2.broadCast(roomNumber, "Name[" + name + "]" + "���� �����߽��ϴ�.");
						try {
							nameArray[nameCount] = name;
							nameCount += 1;
						} catch (Exception e) {
							nameArray[7] = name;
						}
					}
				}

				else if (currentNumber.startsWith("reset")) { //reset ���������̸� �濡 �ִ� ���� ��� reset
					cg2.broadCast(roomNumber, currentNumber);
				}
				else { // ���� �������ݷ� �� �޼����� �ٸ� Ŭ������ �ѱ��.
					cg2.broadCast(roomNumber, currentNumber);
				}
			}
		} catch (Exception e) {
		}finally {
		try {
			cg2.remove(this);
			if (this.name == null) {
				cg2.broadCast(roomNumber,"exit[�͸�]" + "���� �����߽��ϴ�."); //������ �߰��� ������ exit �������� �ٿ��� ������
				cg2.broadCast(roomNumber,"exit���� �ο�: " +  roomUserCount(roomNumber));
				for (int i = 0; i<8; i++) {
				       if (this.name.equals(nameArray[i]))
				          nameArray[i] = "";
				}
			}
			else {
			cg2.broadCast(roomNumber, "exit[" + name + "]" + "���� �����߽��ϴ�.");
			cg2.broadCast(roomNumber, "exit���� �ο�: " +  roomUserCount(roomNumber));
			for (int i = 0; i<8; i++) {
			       if (this.name.equals(nameArray[i]))
			          nameArray[i] = "";
			}
			}
				socket1.close();
				reader.close();
				writer.close();
			} catch (Exception e1) {
			}
		}
	}
}