package Bingo;

//classes of JAVA
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class is for managing the threads of each client and broadcast the messages.
 * 
 */
public class BingoServer {
	
	//the int type value of port number
	public static final int PORT = 8743;
	
	ServerSocket serverSocket;
	Socket socket;
	Vector vector;

	
	/**
	 * Open a server socket, client sockets and create each thread.
	 */
	public void startServer() {
		try {
			serverSocket = new ServerSocket(PORT);
			vector = new Vector();

			while (true) {
				socket = serverSocket.accept();
				BingoThread bingoThread = new BingoThread(this, socket);
				this.addThread(bingoThread);
				bingoThread.start();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			try {
				socket.close();
				serverSocket.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	
	/**
	 * Add a bingo thread to the vector.
	 * 
	 * @param bingoThread
	 */
	public void addThread(BingoThread bingoThread) {
		vector.add(bingoThread);
	}

	
	/**
	 * Remove the bingo thread in the vector.
	 * 
	 * @param bingoThread
	 */
	public void removeThread(BingoThread bingoThread) {
		vector.remove(bingoThread);
	}

	
	/**
	 * Get a i-th bingo thread in the vector.
	 * 
	 * @param i
	 * @return i-th element
	 */
	public BingoThread getElement(int i) {
		return (BingoThread)vector.elementAt(i);
	}

	
	/**
	 * Get the room number of the i-th element.
	 * 
	 * @param i
	 * @return i-th element's room number
	 */
	public int getRoomNumber1(int i) { 
		return getElement(i).getRoomNumber();
	}

	
	/**
	 * Broadcast a message to another client in the same room.
	 * 
	 * @param RoomNumber
	 * @param message
	 */
	public void broadCast(int RoomNumber, String message) {
		for (int i = 0; i < vector.size(); i++) {
			if (RoomNumber == getRoomNumber1(i)) {
				BingoThread tempThread = getElement(i);
				tempThread.send(message);
			}
		}
	}
	
	
	public static void main(String args[]) {
		BingoServer server = new BingoServer();
		server.startServer();
	}
	
}


/**
 * This class is for performing actions within the program.
 *
 */
class BingoThread extends Thread {

	//Store every name of clients
	static String[] nameArray = new String[8];
	
	//Index of nameArray
	static int nameCount = 0;
	
	int roomNumber = 0;

	BingoServer bingoServer;
	BufferedReader reader;
	PrintWriter writer;
	Socket socket;
	
	String receivedMessage;
	String name;

	
	/**
	 * Create reader and writer for server and socket.
	 * 
	 * @param bingoServer1
	 * @param socket1
	 */
	public BingoThread(BingoServer bingoServer1, Socket socket1) {
		this.bingoServer = bingoServer1;
		this.socket = socket1;
		
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			
		} catch (Exception e) {
			System.out.println(e);
		}
		
	}

	
	/**
	 * Send a message to client.
	 * 
	 * @param message
	 */
	public void send(String message) { 
		writer.println(message);
		writer.flush();
	}

	
	/**
	 * Get a room number.
	 * 
	 * @return room number
	 */
	public int getRoomNumber() {
		return roomNumber;
	}

	
	/**
	 * Count the number of user in the room.
	 * 
	 * @param roomNumber
	 * @return
	 */
	public int roomUserCount(int roomNumber) { 
		int count = 0;
		
		for(int i = 0; i < bingoServer.vector.size(); i++) {
			if(roomNumber == bingoServer.getRoomNumber1(i)) {
				count++;
			}
		}
		
		return count;
	}
	
	
	/**
	 * Check if the room is full and set the client who came in first as the first player.
	 * 
	 * @param roomNumber
	 * @return boolean value of full status
	 */
	boolean isRoomFull(int roomNumber) {      
	      int count1 = roomUserCount(roomNumber);
	      
	      if(count1 == 2) {
	         return true;
	      }
	      
	      if(count1 == 0 ) {
	    	  writer.println("isPlay");
	    	  writer.flush();
	      }
	      
	      return false;
	}
	
	
	/**
	 * Check if the name is duplicated.
	 * 
	 * @param newName
	 * @return boolean value of duplicate status
	 */
	public static boolean isNameDuplicate(String newName) {    
		for (String eachName : nameArray) {
	       if (newName.equals(eachName)) {
	          return true;
	       }
	    }
	    
	    return false;
	}
	
	
	/**
	 * Check the received message then give it to the correct protocol.
	 */
	@Override
	public void run() {
		try {			
			while ((receivedMessage = reader.readLine()) != null) {
				
				//If the protocol is "ROOM", then check if the room is full.
				if (receivedMessage.startsWith("ROOM")) {
					int roomNum = Integer.parseInt(receivedMessage.substring(4));
					
					if(!isRoomFull(roomNum)) {
						roomNumber = roomNum;
						writer.println("OK" + roomNumber);	//If the room is not full, then send the message with OK protocol.
						bingoServer.broadCast(roomNumber, "COUNT" + roomUserCount(roomNumber));		//Broadcast the number of client in the room.
					
					} else {
						writer.println("FULL" + roomNumber);	//If the room is full, then send the message with FULL protocol.
					}
				} 
				
				//If the protocol is "MESSAGE", then broadcast the message to clients.
				else if (receivedMessage.startsWith("MESSAGE")) {
					bingoServer.broadCast(roomNumber, "MESSAGE[" + name + "]: " + receivedMessage.substring(7));
				}
				
				//If the protocol is "JUDGE", then broadcast the message to request client's score. 
				else if (receivedMessage.startsWith("JUDGE")) { 
					bingoServer.broadCast(roomNumber, receivedMessage);
				} 
				
				//If the protocol is "SCORE", then broadcast the result of judging to clients. 
				else if (receivedMessage.startsWith("SCORE")) {
					if (Integer.parseInt(receivedMessage.substring(5)) >= 3) {
						bingoServer.broadCast(roomNumber, "DRAW");
					} else {
						bingoServer.broadCast(roomNumber, "RESET");
					}
				}
				
				//If the protocol is "NAME", then check for duplication and store the name. 
				else if (receivedMessage.startsWith("NAME")) {
					name = receivedMessage.substring(4);
					
					if (isNameDuplicate(name)) {
						bingoServer.broadCast(roomNumber, "RENAME"); 
						
					} else {
						bingoServer.broadCast(roomNumber, "NAME[" + name + "]" + "님이 입장했습니다.");
						
						try {
							nameArray[nameCount] = name;
							nameCount += 1;
						} catch (Exception e) {
							for (int i = 0; i<8; i++) {
								if (nameArray[i].equals("")) {
									nameArray[i] = name;
									break;
								}
							}
						}
					}
				} 
				
				//If the protocol is "BINGO", then Broadcast to clients. 
				else if (receivedMessage.startsWith("BINGO")) {
					bingoServer.broadCast(roomNumber, receivedMessage);
				}
			}
			
		} catch (Exception e) {
			
		} finally {
			try {
				bingoServer.removeThread(this);
				
				//If a client is out, then broadcast client's name and reduce the number of total client. 
				if (this.name == null) { 
					bingoServer.broadCast(roomNumber,"EXIT[익명]" + "님이 퇴장했습니다."); //
					bingoServer.broadCast(roomNumber,"MESSAGE남은 인원: " +  roomUserCount(roomNumber)); // Sending the remaining number using the MESSAGE protocol
					
				} else {
					bingoServer.broadCast(roomNumber, "EXIT[" + name + "]" + "님이 퇴장했습니다.");
					bingoServer.broadCast(roomNumber, "MESSAGE남은 인원: " +  roomUserCount(roomNumber));
					
					for (int i = 0; i < 8; i++) {
					       if (this.name.equals(nameArray[i]))
					          nameArray[i] = "";
					}
				}	
				socket.close();
				reader.close();
				writer.close();
				
			} catch (Exception e1) {
				
			}
		}
	}
}