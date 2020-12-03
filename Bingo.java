package Bingo;

//classes of JAVA
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.regex.Pattern;

/**
 * This class is for performing actions within the program.
 *
 */
public class Bingo extends JFrame implements Runnable, ActionListener {

	JPanel panel_bingo;
	JPanel panel_chat;
	JPanel waitRoom;
	JFrame gameFrame;
	JButton bingoButten[][];
	JButton roomButten[][];
	JTextField textField;
	JTextArea textArea;
	JScrollPane jscrollbar;

	PrintWriter writer;
	BufferedReader reader;
	Socket socket;
	
	//Showing whose turn is it now
	boolean isPlay = false;

	int nameCount = 0;
	int roomNumber = 0;
	int bingoLine = 0;
	
	String pressedNumber;
	String receivedMessage;
	String string;
	String userName;
	String roomNum;
	
	
	/**
	 * It is a waiting room where client can choose which room to enter.
	 */
	public Bingo() {
		super("대기실");

		int num1 = 1;
		waitRoom = new JPanel(new GridLayout(2, 2));
		roomButten = new JButton[2][2];
		
		// Implemented with 4 buttons in the waiting room
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) { 
				roomButten[i][j] = new JButton(num1 + "");
				num1++;
				
				waitRoom.add(roomButten[i][j]);
				roomButten[i][j].addActionListener(this);
			}
		}
		
		textField = new JTextField(30);
		textArea = new JTextArea();
		jscrollbar = new JScrollPane(textArea);
		
		//Create a frame with bingo and chat
		panel_bingo = new JPanel(new GridLayout(5, 5));
		panel_bingo.setPreferredSize(new Dimension(700, 500));
		panel_chat = new JPanel();

		panel_chat.setLayout(new BorderLayout());
		panel_chat.add(jscrollbar, BorderLayout.CENTER);
		panel_chat.add(textField, BorderLayout.SOUTH);

		bingoButten = new JButton[5][5];
		
		int num = 1;
		
		//Bingo 5x5 button implementation
		for (int i = 0; i < 5; i++) { 
			for (int j = 0; j < 5; j++) {
				bingoButten[i][j] = new JButton(num + "");
				num++;
				
				panel_bingo.add(bingoButten[i][j]);
				bingoButten[i][j].setBackground(Color.LIGHT_GRAY);
				bingoButten[i][j].addActionListener(this);
			}
		}

		this.add(waitRoom);
		this.setLocation(10, 20);
		this.setSize(300, 300);
		this.setResizable(false);
		this.setVisible(true);
	}

	
	/**
	 * Shuffle the numbers on the bingo board.
	 */
	public void MixNumber() {
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				int ri = (int) (Math.random() * 5);
				int rj = (int) (Math.random() * 5);
				
				String temp = bingoButten[i][j].getText();
				bingoButten[i][j].setText(bingoButten[ri][rj].getText());
				bingoButten[ri][rj].setText(temp);
			}
		}
	}
	
	
	/**
	 * Get number of bingo lines.
	 * 
	 * @return number of bingo lines
	 */
	public int getBingLine() { 
		bingoLine = LineCheck();
		
		return bingoLine;
	}

	
	/**
	 * Check each line and count the bingo line.
	 * 
	 * @return bingo line
	 */
	public int LineCheck() {
		int lineCount = 0;
		int blockCount = 0;
		
		//Horizontal
		for (int i = 0; i < 5; i++) { 
			blockCount = 0;
			
			for (int j = 0; j < 5; j++) {
				if (bingoButten[i][j].getBackground() == Color.YELLOW) {
					blockCount++;
				}
			}
			
			if (blockCount == 5) {
				lineCount++;
			}
		}

		//Vertical
		for (int i = 0; i < 5; i++) {
			blockCount = 0;
			
			for (int j = 0; j < 5; j++) {
				if (bingoButten[j][i].getBackground() == Color.YELLOW) {
					blockCount++;
				}
			}
			
			if (blockCount == 5) {
				lineCount++;
			}
		}

		//Left diagonal
		blockCount = 0;
		for (int i = 0; i < 5; i++) {
			if (bingoButten[i][i].getBackground() == Color.YELLOW) {
				blockCount++;
			}
			
			if (blockCount == 5) {
				lineCount++;
			}
		}

		//Right diagonal
		blockCount = 0;
		for (int i = 0; i < 5; i++) {
			if (bingoButten[i][4 - i].getBackground() == Color.YELLOW) {
				blockCount++;
			}
			
			if (blockCount == 5) {
				lineCount++;
			}
		}
		
		return lineCount;
	}

	
	/**
	 * Check the received message then give it to the correct protocol.
	 */
	@Override
	public void run() {
		String voidDuplication = "";
		try {
			while ((receivedMessage = reader.readLine()) != null) {

				//If the protocol is "OK", then float the game&chat window.
				if(receivedMessage.startsWith("OK")) {
					voidDuplication = receivedMessage;
					roomNumber = Integer.parseInt(receivedMessage.substring(2));
					
					gameFrame = new JFrame(roomNumber + "번 방 Bingo");
					gameFrame.add(panel_bingo, BorderLayout.CENTER);
					gameFrame.add(panel_chat, BorderLayout.EAST);
					
					textArea.setLineWrap(true); 
					jscrollbar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
					textArea.setEditable(false);
					textField.addActionListener(this);
					
					gameFrame.setResizable(false);
					gameFrame.setVisible(true);
					gameFrame.pack();
					
					textField.requestFocus();
					gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					
					MixNumber();	
				} 
				
				//If the protocol is "COUNT" and there are two people in the room, then start game&chat.
				else if(receivedMessage.startsWith("COUNT")) {
					voidDuplication = receivedMessage;
		               int count = Integer.parseInt(receivedMessage.substring(5));
		               
		               textArea.append("현재 " + roomNumber + " 번 방 인원: " + count + "명\n");
		               
		               for (int i = 0; i < 5; i++) {
		                     for (int j = 0; j < 5; j++) {
		                        bingoButten[i][j].setEnabled(false);
		                     }
		                  }
		               
		               if(count < 2) {
		                  textField.setEnabled(false);
		                
		               }
		               
		               if(count == 2) { 
		                  textField.setEnabled(true);
		                  
		                  if(userName==null) {
		                	  textArea.append("사용할 이름을 입력하세요\n");
		                  }
		               }
		        }
				
				//If the protocol is "FULL", then alert the message that the room is full.
				else if(receivedMessage.startsWith("FULL")) { 
					voidDuplication = receivedMessage;
					JOptionPane.showMessageDialog(null, roomNum + "번 방 FULL!", "full", JOptionPane.PLAIN_MESSAGE);
				} 

				//If the protocol is "MESSAGE", then show the message.
				else if (receivedMessage.startsWith("MESSAGE")) { 
					voidDuplication = receivedMessage;
					textArea.append(receivedMessage.substring(7) + "\n");
					textArea.setCaretPosition(textArea.getDocument().getLength());
				} 

				//If the protocol is "isPlay", then set the client who came in first as the first player.
				else if (receivedMessage.startsWith("isPlay")) { 
					voidDuplication = receivedMessage;
					isPlay = true;
				}
				
				//If the protocol is "JUDGE", then send the message with SCORE protocol to decide whether win or lose.
				else if (receivedMessage.startsWith("JUDGE")) {
					voidDuplication = receivedMessage;
					if (!receivedMessage.substring(5).equals(Boolean.toString(isPlay))) {
						writer.println("SCORE" + Integer.toString(getBingLine()));
					}
				}

				//If the protocol is "NAME", then store the name and show it.
				else if (receivedMessage.startsWith("NAME")) { 
					voidDuplication = receivedMessage;
					userName = string;
					textArea.append(receivedMessage.substring(4) + "\n");
					nameCount = 0; 
				} 

				//If the protocol is "RENAME" and the client came in later, then alert the message for duplicated name.
				else if (receivedMessage.startsWith("RENAME")) { 
					voidDuplication = receivedMessage;
					if (nameCount == 1) { 
						JOptionPane.showMessageDialog(null, string + " Nickname Duplicated!",
								"Nickname Duplicated!", JOptionPane.PLAIN_MESSAGE);
						nameCount = 0;
					} else { 
						nameCount = 0;
					}
				} 

				//If the protocol is "DRAW", then show the message for draw and change the order the play.
				else if (receivedMessage.startsWith("DRAW") && !voidDuplication.startsWith("DRAW")) {
					voidDuplication = receivedMessage;
					JOptionPane.showMessageDialog(null, userName + " Draw!", "Draw", JOptionPane.PLAIN_MESSAGE);
					
					reset();
					
					if (isPlay == true) {
						isPlay = true;
					} else {
						isPlay = false;
					}
				} 

				//If the protocol is "RESET", then show the message for win or lose and change the order the play to the loser first.
				else if (receivedMessage.startsWith("RESET")) {
					voidDuplication = receivedMessage;
					if(getBingLine() >= 3) { 
						JOptionPane.showMessageDialog(null, userName + " Victory!", "Victory", JOptionPane.PLAIN_MESSAGE);
						reset();
						
						isPlay=false;
						
					} else {
						JOptionPane.showMessageDialog(null, userName + " Lose!", "Lose", JOptionPane.PLAIN_MESSAGE);
						reset();
						
						isPlay=true;
					}
				} 

				//If the protocol is "EXIT", then reset bingo board and wait for another client to come in.
				else if(receivedMessage.startsWith("EXIT")) { 
					voidDuplication = receivedMessage;
					textArea.setText("");				
					reset();
					
					textArea.append(receivedMessage.substring(4) + "\n");
					textField.setEnabled(false);
					
					for (int i = 0; i < 5; i++) {
						for (int j = 0; j < 5; j++) {
							bingoButten[i][j].setEnabled(false);
						}
					}
					isPlay=true;
				}
				
				//If the protocol is "EXIT", then mark the selected bingo board.
				else if(receivedMessage.startsWith("BINGO")) { 
					voidDuplication = receivedMessage;
					for (int i = 0; i < 5; i++) {
						for (int j = 0; j < 5; j++) {
							if (bingoButten[i][j].getText().equals(receivedMessage.substring(5))) {
								if (bingoButten[i][j].getBackground() == Color.LIGHT_GRAY) {
									bingoButten[i][j].setBackground(Color.YELLOW);
									isPlay = true;
								}
								
								//If bingo lines are more than 3, then request to judge.
								if (getBingLine() >= 3) {
									writer.println("JUDGE"+Boolean.toString(isPlay));
								}
								break;
							}
						}
					}
				}
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Performing various actions within the program.
	 */
	
	public void actionPerformed(ActionEvent e) {
		
		//If a client chose the room, then send the room number with ROOM protocol to server.
		if (e.getSource() == roomButten[0][0] || e.getSource() == roomButten[0][1] || e.getSource() == roomButten[1][0]
				|| e.getSource() == roomButten[1][1]) {
			roomNum = ((JButton) e.getSource()).getText();
			writer.println("ROOM" + roomNum); 
		} 
		
		//If an article is written in the chat, then sends it to the server with NAME or MESSAGE protocol.
		else if (e.getSource() == textField) {
			string = textField.getText();
			textField.setText("");
	        
	        if (!string.equals("")) {
	        	if (userName == null) {
	        		if(getType(string)) { //Check if the userName condition is correct
	        		nameCount += 1; 
	                writer.println("NAME" + string); 
	                for (int i = 0; i < 5; i++) {
	                     for (int j = 0; j < 5; j++) {
	                        bingoButten[i][j].setEnabled(true);
	                     }
	                  }
	        		}
	        		else {
	        			textArea.append("2~10길이 문자,숫자로 이루어진 이름을 다시 입력해주세요.\n");
	        		}
	            } else {
	            	if(string.trim() != "") { //Do not send blank characters.
	                writer.println("MESSAGE" + string);
	            	}
	            }
	        }
	    } 
		
		//If a client press a bingo button, then send bingo protocol to server.
		else if (((JButton) e.getSource()).getBackground() == Color.LIGHT_GRAY && isPlay) {
			isPlay = false;
			
			((JButton) e.getSource()).setBackground(Color.YELLOW); 
			pressedNumber = ((JButton) e.getSource()).getText(); 

			writer.println("BINGO" + pressedNumber); 
		}	
		
		//If a client press the already selected bingo button, then alert the re-touch message.
		else if (((JButton) e.getSource()).getBackground() == Color.YELLOW) {
			JOptionPane.showMessageDialog(null, "Retouch!", "Retouch", JOptionPane.WARNING_MESSAGE);
		} 
		
		//If the client who is not in his turn press bingo button, then alert the message.
		else if (!isPlay) {
			JOptionPane.showMessageDialog(null, "Not your turn!", "Not your turn", JOptionPane.WARNING_MESSAGE);
		}
	}

	
	/**
	 * Reset all bingo buttons.
	 */
	public void reset() {
		for (int i = 0; i < bingoButten.length; i++) {
			for (int j = 0; j < bingoButten[i].length; j++) {
				bingoButten[i][j].setBackground(Color.LIGHT_GRAY);
			}
		}
	}
	
	/**
	 * Check Conditions that can be userName.
	 */
	public static boolean getType(String word) {
		return Pattern.matches("^[A-Za-z0-9]{2,10}$", word);
	}
	
	/**
	 * Open a socket and create reader and writer.
	 */
	void sockConnect() {
		try {
			socket = new Socket("127.0.0.1", 8743);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			new Thread(this).start();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	
	public static void main(String args[]) {
		Bingo client = new Bingo();
		client.sockConnect();
	}
}