package Bingo;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Bingo extends JFrame implements Runnable, ActionListener {

	JPanel panel_center;
	JPanel panel;
	JPanel room;
	JFrame frame;
	PrintWriter writer;
	BufferedReader reader;
	Socket sock;
	JButton btn[][];
	JButton btn1[][];
	boolean isPlay = false;

	String currentNumber;
	String currentNumber2;
	String str;
	String userName;
	String Room;
	
	int nameCount = 0;
	int roomNumber = 0;
	int bingoLine = 0;
	JTextField tf;
	JTextArea ta;
	JScrollPane js;

	public Bingo() {
		super("대기실");

		room = new JPanel(new GridLayout(2, 2)); // 대기실 구현

		int num1 = 1;
		btn1 = new JButton[2][2];
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) { //버튼으로 각 방 표시
				btn1[i][j] = new JButton(num1 + "");
				num1++;
				room.add(btn1[i][j]);
				btn1[i][j].addActionListener(this);
			}
		}
		tf = new JTextField(30);
		ta = new JTextArea();
		js = new JScrollPane(ta);

		panel_center = new JPanel(new GridLayout(5, 5));
		panel_center.setPreferredSize(new Dimension(700, 500));
		panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(js, BorderLayout.CENTER);
		panel.add(tf, BorderLayout.SOUTH);

		btn = new JButton[5][5];
		int num = 1;
		for (int i = 0; i < 5; i++) { // 빙고 회색칸 만들고 마우스 이벤트 부분.
			for (int j = 0; j < 5; j++) {
				btn[i][j] = new JButton(num + "");
				num++;
				panel_center.add(btn[i][j]);
				btn[i][j].setBackground(Color.LIGHT_GRAY);
				btn[i][j].addActionListener(this);
			}
		}

		this.add(room);
		this.setLocation(10, 20);
		this.setSize(300, 300);
		this.setResizable(false);
		this.setVisible(true);

	}

	public void MixNumber() { // 빙고판 섞기
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				int ri = (int) (Math.random() * 5);
				int rj = (int) (Math.random() * 5);
				String tmp = btn[i][j].getText();
				btn[i][j].setText(btn[ri][rj].getText());
				btn[ri][rj].setText(tmp);
			}
		}
	}

	public int getBingLine() { //빙고 개수
		bingoLine = LineCheck();
		return bingoLine;
	}

	public int LineCheck() { //빙고인지 확인
		int lineCount = 0;
		int cnt = 0;
		for (int i = 0; i < 5; i++) { // 가로
			cnt = 0;
			for (int j = 0; j < 5; j++) {
				if (btn[i][j].getBackground() == Color.YELLOW) {
					cnt++;
				}
			}
			if (cnt == 5) {
				lineCount++;
			}
		}

		for (int i = 0; i < 5; i++) {// 세로
			cnt = 0;
			for (int j = 0; j < 5; j++) {
				if (btn[j][i].getBackground() == Color.YELLOW) {
					cnt++;
				}
			}
			if (cnt == 5) {
				lineCount++;
			}
		}

		cnt = 0;
		for (int i = 0; i < 5; i++) { // 왼 대각선
			if (btn[i][i].getBackground() == Color.YELLOW) {
				cnt++;
			}
			if (cnt == 5) {
				lineCount++;
			}
		}

		cnt = 0;
		for (int i = 0; i < 5; i++) { // 오른 대각선
			if (btn[i][4 - i].getBackground() == Color.YELLOW) {
				cnt++;
			}
			if (cnt == 5) {
				lineCount++;
			}
		}
		return lineCount;
	}

	@Override
	public void run() {
		try {
			while ((currentNumber2 = reader.readLine()) != null) {

				if(currentNumber2.startsWith("OK")) { //방에 들어갈 수 있으면 빙고+채팅 창 띄우기!
					
					roomNumber = Integer.parseInt(currentNumber2.substring(2));
					frame = new JFrame(roomNumber + "번방 Bingo");
					frame.add(panel_center, BorderLayout.CENTER);
					frame.add(panel, BorderLayout.EAST);
					ta.setLineWrap(true); //자동 줄바꾸기
					js.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); // 수직 스크롤바 생성
					ta.setEditable(false);
					tf.addActionListener(this); // text필드에 글씨 쓰면 이벤트 발생
					frame.setVisible(true);
					frame.pack();
					tf.requestFocus();
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					MixNumber();		
				}
				
				else if(currentNumber2.startsWith("count")) {
					int count = Integer.parseInt(currentNumber2.substring(5));
					ta.append("현재 " + roomNumber + " 번방 인원: " + count + "명\n");
					
					if(count < 2) { // 2명이 될때까지 기다리기
						tf.setEnabled(false);
						for (int i = 0; i < 5; i++) {
							for (int j = 0; j < 5; j++) {
								btn[i][j].setEnabled(false);
							}
						}
					}
					if(count == 2) { // 2명이면 채팅 + 빙고 시작하게 !
						tf.setEnabled(true);
						for (int i = 0; i < 5; i++) {
							for (int j = 0; j < 5; j++) {
								btn[i][j].setEnabled(true);
							}
						}
						ta.append("사용할 이름을 입력하세요\n");
					}
				}
				
				else if(currentNumber2.startsWith("FULL")) { //FULL프로토콜 꽉 참 출력
					JOptionPane.showMessageDialog(null, Room + "번방 FULL!", "full", JOptionPane.PLAIN_MESSAGE);
				}

				else if (currentNumber2.startsWith("MSG")) { //MSG프로토콜 이용해서 msg 출력
					ta.append(currentNumber2.substring(3) + "\n");
					ta.setCaretPosition(ta.getDocument().getLength());  // 맨아래로 스크롤한다.
				}
				
				else if (currentNumber2.startsWith("isPlay")) { // isPlay 프로토콜로 방에 먼저 들어온 사람이 선공!
					isPlay = true;
				}
				
				else if (currentNumber2.startsWith("Judge")) { // 승/패/무승부 판단을 위해 자신의 빙고 현황을 보냄!
					if (isPlay == true) { //본인이 판정 요청한 사람이 아니라면 현재 빙고 개수를 보냄
						writer.println("Score" + Integer.toString(getBingLine()));
					}
				}
				
				else if (currentNumber2.startsWith("Name")) { // name 프로토콜로 name 출력
					userName = str;
					ta.append(currentNumber2.substring(4) + "\n");
					nameCount = 0; // Rename 오류 방지
				}
				
				else if (currentNumber2.startsWith("Rename")) { // Rename 프로토콜로 name 출력
					if (nameCount == 1) { // 본인이 name을 입력한 사람이라면
						JOptionPane.showMessageDialog(null, str + " Nickname Duplicated!",
								"Nickname Duplicated!", JOptionPane.PLAIN_MESSAGE);
						nameCount = 0;
					} else { // 아니면 pass
						nameCount = 0;
					}
				}
				
				else if (currentNumber2.startsWith("Draw")) {
					JOptionPane.showMessageDialog(null, userName + " Draw!", "Draw", JOptionPane.PLAIN_MESSAGE);
					reset();
					if (isPlay == true) {
						isPlay=true;
					} else {
						isPlay=false;
					}
				}
				
				else if (currentNumber2.startsWith("reset")) { 
					if(getBingLine()>=3) { //이긴 사람은 victory뜨고 후공
						JOptionPane.showMessageDialog(null, userName + " Victory!", "Victory", JOptionPane.PLAIN_MESSAGE);
						reset();
						isPlay=false;
					}
					else{//진 사람은 lose뜨고 reset하고 선공
							JOptionPane.showMessageDialog(null, userName + " Lose!", "Lose", JOptionPane.PLAIN_MESSAGE);
							reset();
							isPlay=true;
							}
					}
				
				else if(currentNumber2.startsWith("exit")) { //중간에 상대방 나가면 다시 2명 될때까지 기다리기.
					ta.append(currentNumber2.substring(4) + "\n");
					reset();
					tf.setEnabled(false);
					for (int i = 0; i < 5; i++) {
						for (int j = 0; j < 5; j++) {
							btn[i][j].setEnabled(false);
						}
					}
					isPlay=true; //중간에 나가면 남아있는 사람 선공
					}
					
				else { //나머지 빙고
					for (int i = 0; i < 5; i++) {
						for (int j = 0; j < 5; j++) {
							if (btn[i][j].getText().equals(currentNumber2)
									&& btn[i][j].getBackground() == Color.LIGHT_GRAY) {
								btn[i][j].setBackground(Color.YELLOW);
								isPlay = true;
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

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == btn1[0][0] || e.getSource() == btn1[0][1] || e.getSource() == btn1[1][0]
				|| e.getSource() == btn1[1][1]) {
			Room = ((JButton) e.getSource()).getText();
			writer.println("Room" + Room); // Room 프로토콜 붙여서 방 번호 서버로 보내기
		
		}
		else if (e.getSource() == tf) { // 텍스트에다가 글이 써지면
			str = tf.getText();
	        tf.setText("");
	        if (!str.equals("")) {
	        	if (userName == null) {
	        		nameCount += 1; // 본인이 name 보낸사람이란걸 확인하기 위함
	                writer.println("Name" + str); // Name 프로토콜을 붙여서 서버로 보냄.
	            } else {
	                writer.println("MSG" + str); // MSG 프로토콜을 붙여서 서버로 보냄.
	            }
	        }
	    }
		// 빙고 버튼을 누르면
		else if (((JButton) e.getSource()).getBackground() == Color.LIGHT_GRAY && isPlay) {
			isPlay = false;
			((JButton) e.getSource()).setBackground(Color.YELLOW); // 누르면 노란색
			currentNumber = ((JButton) e.getSource()).getText(); // 현재 누룬 번호 저장

			writer.println(currentNumber); // bingo 프로토콜 서버로 보내기
			
			if (getBingLine() >= 3) {
				writer.println("Judge"); // 빙고가 3개 이상이 되면 판정 요청을 보냄.
			}

		} else if (((JButton) e.getSource()).getBackground() == Color.YELLOW) {// 찍었던 곳 다시 찍으면 에러
			JOptionPane.showMessageDialog(null, "Retouch!", "Retouch", JOptionPane.WARNING_MESSAGE);
		} else if (!isPlay) { // 자기차례 아닐때 누르면 에러
			JOptionPane.showMessageDialog(null, "Not your turn!", "Not your turn", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void reset() { // 빙고판 리셋!
		for (int i = 0; i < btn.length; i++) {
			for (int j = 0; j < btn[i].length; j++) {
				btn[i][j].setBackground(Color.LIGHT_GRAY);
			}
		}
	}

	void sockConnect() {
		try {
			sock = new Socket("127.0.0.1", 8742);
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			writer = new PrintWriter(sock.getOutputStream(), true);
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