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

	public int getRoomNumber1(int i) {  //i번째 있는 클라이언트 방 넘버 리턴
		return getOT(i).getRoomNumber();
	}

	public static void main(String args[]) {
		BingoServer server = new BingoServer();
		server.startServer();
	}
	
	public void broadCast(int RoomNumber, String str) {  //자신이 들어간 방에 있는 클라이언트에게만 메세지 전송
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

	public void send(String currentNumber) { //메세지 클라이언트한테 보내기
		writer.println(currentNumber);
		writer.flush();
	}

	public int getRoomNumber() {
		return roomNumber;
	}

	public int roomUserCount(int roomNumber) { //방에 들어간 유저 수 
		int count = 0;
		for(int i=0; i<cg2.v.size(); i++) {
			if(roomNumber == cg2.getRoomNumber1(i)) {
				count++;
			}
		}
		return count;
	}
	boolean roomFull(int roomNumber) { //방이 가득찼는지 안찼는지
		int count1 = roomUserCount(roomNumber);
		
		if(count1>=2) {
			return true;
		}
		writer.println("isPlay");    //방에 먼저 들어온 사람이 선공
		writer.flush(); 
		return false;
	}
	public static boolean compare_name(String newName) { // 새로운 name이 올때 nameArray에 있는지 판단.
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

						writer.println("OK" + roomNumber); // 방에 가득 찬게 아니면 OK 프로토콜 클라이언트 방에 참가
						cg2.broadCast(roomNumber, "count" + roomUserCount(roomNumber)); //들어간 방에 몇명있는지 count 프로토콜
					}
					else {
						writer.println("FULL" + roomNumber); //방에 가득차면 full 프로토콜로 FULL 메세지 보내기
					}
				}
				

				else if (currentNumber.startsWith("MSG")) { //MSG 프로토콜이면 방에 있는 유저들에게 메세지 보내기
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
				
				else if (currentNumber.startsWith("Name")) { //Name 프로토콜이면 클라 이름 저장 후 방에 유저 입장 보내기
					name = currentNumber.substring(4);
					if (compare_name(name)) { //중복여부 판단
						cg2.broadCast(roomNumber, "Rename"); //중복이면 Rename 보냄
					} else { //아니면 클라 이름 저장 후 방에 유저 입장 보내기
						cg2.broadCast(roomNumber, "Name[" + name + "]" + "님이 입장했습니다.");
						try {
							nameArray[nameCount] = name;
							nameCount += 1;
						} catch (Exception e) {
							nameArray[7] = name;
						}
					}
				}

				else if (currentNumber.startsWith("reset")) { //reset 프로토콜이면 방에 있는 유저 모두 reset
					cg2.broadCast(roomNumber, currentNumber);
				}
				else { // 빙고 프로토콜로 온 메세지를 다른 클라한테 넘기기.
					cg2.broadCast(roomNumber, currentNumber);
				}
			}
		} catch (Exception e) {
		}finally {
		try {
			cg2.remove(this);
			if (this.name == null) {
				cg2.broadCast(roomNumber,"exit[익명]" + "님이 퇴장했습니다."); //상대방이 중간에 나가면 exit 프로토콜 붙여서 보내기
				cg2.broadCast(roomNumber,"exit남은 인원: " +  roomUserCount(roomNumber));
				for (int i = 0; i<8; i++) {
				       if (this.name.equals(nameArray[i]))
				          nameArray[i] = "";
				}
			}
			else {
			cg2.broadCast(roomNumber, "exit[" + name + "]" + "님이 퇴장했습니다.");
			cg2.broadCast(roomNumber, "exit남은 인원: " +  roomUserCount(roomNumber));
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