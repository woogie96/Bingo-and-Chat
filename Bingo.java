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
		super("����");

		room = new JPanel(new GridLayout(2, 2)); // ���� ����

		int num1 = 1;
		btn1 = new JButton[2][2];
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) { //��ư���� �� �� ǥ��
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
		for (int i = 0; i < 5; i++) { // ���� ȸ��ĭ ����� ���콺 �̺�Ʈ �κ�.
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

	public void MixNumber() { // ������ ����
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

	public int getBingLine() { //���� ����
		bingoLine = LineCheck();
		return bingoLine;
	}

	public int LineCheck() { //�������� Ȯ��
		int lineCount = 0;
		int cnt = 0;
		for (int i = 0; i < 5; i++) { // ����
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

		for (int i = 0; i < 5; i++) {// ����
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
		for (int i = 0; i < 5; i++) { // �� �밢��
			if (btn[i][i].getBackground() == Color.YELLOW) {
				cnt++;
			}
			if (cnt == 5) {
				lineCount++;
			}
		}

		cnt = 0;
		for (int i = 0; i < 5; i++) { // ���� �밢��
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

				if(currentNumber2.startsWith("OK")) { //�濡 �� �� ������ ����+ä�� â ����!
					
					roomNumber = Integer.parseInt(currentNumber2.substring(2));
					frame = new JFrame(roomNumber + "���� Bingo");
					frame.add(panel_center, BorderLayout.CENTER);
					frame.add(panel, BorderLayout.EAST);
					ta.setLineWrap(true); //�ڵ� �ٹٲٱ�
					js.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); // ���� ��ũ�ѹ� ����
					ta.setEditable(false);
					tf.addActionListener(this); // text�ʵ忡 �۾� ���� �̺�Ʈ �߻�
					frame.setVisible(true);
					frame.pack();
					tf.requestFocus();
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					MixNumber();		
				}
				
				else if(currentNumber2.startsWith("count")) {
					int count = Integer.parseInt(currentNumber2.substring(5));
					ta.append("���� " + roomNumber + " ���� �ο�: " + count + "��\n");
					
					if(count < 2) { // 2���� �ɶ����� ��ٸ���
						tf.setEnabled(false);
						for (int i = 0; i < 5; i++) {
							for (int j = 0; j < 5; j++) {
								btn[i][j].setEnabled(false);
							}
						}
					}
					if(count == 2) { // 2���̸� ä�� + ���� �����ϰ� !
						tf.setEnabled(true);
						for (int i = 0; i < 5; i++) {
							for (int j = 0; j < 5; j++) {
								btn[i][j].setEnabled(true);
							}
						}
						ta.append("����� �̸��� �Է��ϼ���\n");
					}
				}
				
				else if(currentNumber2.startsWith("FULL")) { //FULL�������� �� �� ���
					JOptionPane.showMessageDialog(null, Room + "���� FULL!", "full", JOptionPane.PLAIN_MESSAGE);
				}

				else if (currentNumber2.startsWith("MSG")) { //MSG�������� �̿��ؼ� msg ���
					ta.append(currentNumber2.substring(3) + "\n");
					ta.setCaretPosition(ta.getDocument().getLength());  // �ǾƷ��� ��ũ���Ѵ�.
				}
				
				else if (currentNumber2.startsWith("isPlay")) { // isPlay �������ݷ� �濡 ���� ���� ����� ����!
					isPlay = true;
				}
				
				else if (currentNumber2.startsWith("Judge")) { // ��/��/���º� �Ǵ��� ���� �ڽ��� ���� ��Ȳ�� ����!
					if (isPlay == true) { //������ ���� ��û�� ����� �ƴ϶�� ���� ���� ������ ����
						writer.println("Score" + Integer.toString(getBingLine()));
					}
				}
				
				else if (currentNumber2.startsWith("Name")) { // name �������ݷ� name ���
					userName = str;
					ta.append(currentNumber2.substring(4) + "\n");
					nameCount = 0; // Rename ���� ����
				}
				
				else if (currentNumber2.startsWith("Rename")) { // Rename �������ݷ� name ���
					if (nameCount == 1) { // ������ name�� �Է��� ����̶��
						JOptionPane.showMessageDialog(null, str + " Nickname Duplicated!",
								"Nickname Duplicated!", JOptionPane.PLAIN_MESSAGE);
						nameCount = 0;
					} else { // �ƴϸ� pass
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
					if(getBingLine()>=3) { //�̱� ����� victory�߰� �İ�
						JOptionPane.showMessageDialog(null, userName + " Victory!", "Victory", JOptionPane.PLAIN_MESSAGE);
						reset();
						isPlay=false;
					}
					else{//�� ����� lose�߰� reset�ϰ� ����
							JOptionPane.showMessageDialog(null, userName + " Lose!", "Lose", JOptionPane.PLAIN_MESSAGE);
							reset();
							isPlay=true;
							}
					}
				
				else if(currentNumber2.startsWith("exit")) { //�߰��� ���� ������ �ٽ� 2�� �ɶ����� ��ٸ���.
					ta.append(currentNumber2.substring(4) + "\n");
					reset();
					tf.setEnabled(false);
					for (int i = 0; i < 5; i++) {
						for (int j = 0; j < 5; j++) {
							btn[i][j].setEnabled(false);
						}
					}
					isPlay=true; //�߰��� ������ �����ִ� ��� ����
					}
					
				else { //������ ����
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
			writer.println("Room" + Room); // Room �������� �ٿ��� �� ��ȣ ������ ������
		
		}
		else if (e.getSource() == tf) { // �ؽ�Ʈ���ٰ� ���� ������
			str = tf.getText();
	        tf.setText("");
	        if (!str.equals("")) {
	        	if (userName == null) {
	        		nameCount += 1; // ������ name ��������̶��� Ȯ���ϱ� ����
	                writer.println("Name" + str); // Name ���������� �ٿ��� ������ ����.
	            } else {
	                writer.println("MSG" + str); // MSG ���������� �ٿ��� ������ ����.
	            }
	        }
	    }
		// ���� ��ư�� ������
		else if (((JButton) e.getSource()).getBackground() == Color.LIGHT_GRAY && isPlay) {
			isPlay = false;
			((JButton) e.getSource()).setBackground(Color.YELLOW); // ������ �����
			currentNumber = ((JButton) e.getSource()).getText(); // ���� ���� ��ȣ ����

			writer.println(currentNumber); // bingo �������� ������ ������
			
			if (getBingLine() >= 3) {
				writer.println("Judge"); // ���� 3�� �̻��� �Ǹ� ���� ��û�� ����.
			}

		} else if (((JButton) e.getSource()).getBackground() == Color.YELLOW) {// ����� �� �ٽ� ������ ����
			JOptionPane.showMessageDialog(null, "Retouch!", "Retouch", JOptionPane.WARNING_MESSAGE);
		} else if (!isPlay) { // �ڱ����� �ƴҶ� ������ ����
			JOptionPane.showMessageDialog(null, "Not your turn!", "Not your turn", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void reset() { // ������ ����!
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