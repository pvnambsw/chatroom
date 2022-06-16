package nam.Client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import nam.model.FileInfo;
import nam.model.Message;
import nam.model.Message.TYPE;

public class Client extends JFrame implements ActionListener {
	private JButton send, clear, exit, login, logout, file, downFile;
	private JPanel p_login, p_chat;
	private JTextField nick, nick1, message;
	private JTextArea msg, online;
	private JComboBox<String> combobox;
	private String[] files;

	private Socket client;
	private DataStream dataStream;
	private DataOutputStream dos;
	private DataInputStream dis;

	public Client() {
		super("Nam : Client");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		setSize(600, 400);
		addItem();
		setVisible(true);
	}

//-----[ Tạo giao diện ]--------//
	private void addItem() {
		setLayout(new BorderLayout());

		exit = new JButton("Thoát");
		exit.addActionListener(this);
		send = new JButton("Gửi");
		send.addActionListener(this);
		clear = new JButton("Xóa");
		clear.addActionListener(this);
		login = new JButton("Vào phòng");
		login.addActionListener(this);
		logout = new JButton("Thoát");
		logout.addActionListener(this);

		file = new JButton("File");
		file.addActionListener(this);

		p_chat = new JPanel();
		p_chat.setLayout(new BorderLayout());

		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		nick = new JTextField(20);
		// Đã vào phòng
		p1.add(new JLabel("Tài khoản: "));
		p1.add(nick);
		p1.add(exit);

		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		files = new String[10000];
		combobox =  new JComboBox<>();
		downFile = new JButton("Down");
		downFile.addActionListener(this);
		

		JPanel p22 = new JPanel();
		p22.setLayout(new FlowLayout(FlowLayout.CENTER));
		p22.add(new JLabel("Thành viên"));
//		p2.add(p22, BorderLayout.NORTH);
		
		JPanel p222 = new JPanel();
		p222.setLayout(new FlowLayout(FlowLayout.RIGHT));
		p222.add(combobox);
		
		p222.add(downFile);
		p2.add(p222, BorderLayout.NORTH);

		online = new JTextArea(10, 10);
		online.setEditable(false);
		p2.add(new JScrollPane(online), BorderLayout.CENTER);
		p2.add(new JLabel("     "), BorderLayout.SOUTH);
		p2.add(new JLabel("     "), BorderLayout.EAST);
		p2.add(new JLabel("     "), BorderLayout.WEST);

		msg = new JTextArea(10, 20);
		msg.setEditable(false);

		JPanel p3 = new JPanel();
		p3.setLayout(new FlowLayout(FlowLayout.LEFT));
		p3.add(new JLabel("Tin nhắn"));
		message = new JTextField(30);
		p3.add(message);
		p3.add(send);
		p3.add(file);
		p3.add(clear);

		p_chat.add(new JScrollPane(msg), BorderLayout.CENTER);
		p_chat.add(p1, BorderLayout.NORTH);
		p_chat.add(p2, BorderLayout.EAST);
		p_chat.add(p3, BorderLayout.SOUTH);
		p_chat.add(new JLabel("     "), BorderLayout.WEST);

		p_chat.setVisible(false);
		add(p_chat, BorderLayout.CENTER);
		// -------------------------
		p_login = new JPanel();
		p_login.setLayout(new FlowLayout(FlowLayout.CENTER));
		p_login.add(new JLabel("Tài khoản: "));
		nick1 = new JTextField(20);
		p_login.add(nick1);
		p_login.add(login);
		p_login.add(logout);

		add(p_login, BorderLayout.NORTH);
	}

//---------[ Socket ]-----------//	
	private void go() {
		try {
			client = new Socket("localhost", 2207);
			dos = new DataOutputStream(client.getOutputStream());
			dis = new DataInputStream(client.getInputStream());

			// client.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Lỗi kết nối, trạng thái Server chưa mở", "Message Dialog",
					JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		new Client().go();
	}

	public void closeStream(InputStream inputStream) {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void closeStream(OutputStream outputStream) {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private FileInfo getFileInfo(String sourceFilePath, String destinationDir) {
		FileInfo fileInfo = null;
		BufferedInputStream bis = null;
		try {
			File sourceFile = new File(sourceFilePath);
			bis = new BufferedInputStream(new FileInputStream(sourceFile));
			fileInfo = new FileInfo();
			byte[] fileBytes = new byte[(int) sourceFile.length()];
			// get file info
			bis.read(fileBytes, 0, fileBytes.length);
			fileInfo.setFilename(sourceFile.getName());
			fileInfo.setDataBytes(fileBytes);
			fileInfo.setDestinationDirectory(destinationDir);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			closeStream(bis);
		}
		return fileInfo;
	}

	public void sendFile(String sourceFilePath, String destinationDir) {
		DataOutputStream outToServer = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		try {
			// make greeting
			Message message = new Message();
			message.setType(TYPE.FILE);
			// get file info
			FileInfo fileInfo = getFileInfo(sourceFilePath, destinationDir);
			message.setFile(fileInfo);
			message.setMessage(fileInfo.getFilename());

			// send file
			oos = new ObjectOutputStream(client.getOutputStream());
			oos.writeObject(message);

			// get confirmation
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			// close all stream
//			closeStream(oos);
//			closeStream(ois);
//			closeStream(outToServer);
		}
	}

	private void sendMSG(String data, TYPE type) {
		ObjectOutputStream oos = null;
//        ObjectInputStream ois = null;
		try {
			Message message = new Message();
			message.set_id(UUID.randomUUID().toString());
			message.setType(type);
			message.setMessage(data);

			oos = new ObjectOutputStream(client.getOutputStream());
			oos.writeObject(message);

		} catch (Exception e) {
			exit();
			e.printStackTrace();
		}
	}

	public Message getMSG() {
		String data = null;
		ObjectInputStream ois = null;
		Message message = new Message();
		try {
			ois = new ObjectInputStream(client.getInputStream());
			message = (Message) ois.readObject();
			System.out.println(message.toString());
		} catch (Exception e) {
			e.printStackTrace();
			exit();
			System.out.println(e);
		}
		return message;
	}

	public void getMSG(Message message) {
//		int stt = Integer.parseInt(msg1);
//		System.out.println(message.getMessage() + " jhj");
		switch (message.getType()) {
		// tin nhắn của người khác
			case SEND:
				this.msg.append("Tui: " + message.getMessage());
				break;
			case MESSAGE:
				this.msg.append(message.getMessage());
				break;
			case FILE:
				this.files = addX(1000, this.files, message.getMessage());
				System.out.println(Arrays.toString(this.files));
				this.msg.append(message.getMessage());
				break;
			// update danh sách online
			case LIST:
				this.online.setText(message.getMessage());
				break;
			case DOWNLOAD:
				FileInfo file = message.getFile();
				file.setDestinationDirectory("D:\\chat\\file\\");
				createFile(file);
				break;
			// server đóng cửa
			case EXIT:
				dataStream.stopThread();
				exit();
				break;
			// bổ sung sau
			default:
				break;
		}
	}

	private boolean createFile(FileInfo fileInfo) {
		BufferedOutputStream bos = null;
		try {
			if (fileInfo != null) {
				File fileReceive = new File(fileInfo.getDestinationDirectory() + fileInfo.getFilename());
				bos = new BufferedOutputStream(new FileOutputStream(fileReceive));
				// write file content
				bos.write(fileInfo.getDataBytes());
				bos.flush();
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
	            closeStream(bos);
		}
	}
	
	
	public String[] addX(int n, String arr[], String x) 
    { 
        int i = 0; 
  
        String newarr[] = new String[n + 1]; 
  
        while (i<n && arr[i++] !=null ) {}
  
        System.out.println(i);
        	
        newarr[--i] = x; 
        combobox.addItem(x);
  
        return newarr; 
    } 
	
//----------------------------------------------
	private void checkSend(String msg) {
		if (msg.compareTo("\n") != 0) {
//			this.msg.append("Tui: " + msg);
//			sendMSG("1", TYPE.MESSAGE);
			sendMSG(msg, TYPE.MESSAGE);
		}
	}

	private boolean checkLogin(String nick) {
		if (nick.compareTo("") == 0)
			return false;
		else if (nick.compareTo("0") == 0) {
			return false;
		} else {
			sendMSG(nick, TYPE.MESSAGE);
			Message message = getMSG();
			if (message.getType() == TYPE.LOGIN_SUCCESS)
				return true;
			else
				return false;
		}
	}

	private void exit() {
		try {
			sendMSG("0", TYPE.EXIT);
			dos.close();
			dis.close();
			client.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.exit(0);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == exit) {
			dataStream.stopThread();
			exit();
		} else if (e.getSource() == clear) {
			message.setText("");
		} else if (e.getSource() == send) {
			checkSend(message.getText() + "\n");
			message.setText("");
		} else if (e.getSource() == login) {
			if (checkLogin(nick1.getText())) {
				p_chat.setVisible(true);
				p_login.setVisible(false);
				nick.setText(nick1.getText());
				nick.setEditable(false);
				this.setTitle(nick1.getText());
				msg.append(" Join ok\n");
				dataStream = new DataStream(this, this.dis);
			} else {
				JOptionPane.showMessageDialog(this,
						"Tài khoản này đã tồn tại trong phòng, bạn vui lòng nhập lại tài khoản khác", "Message Dialog",
						JOptionPane.WARNING_MESSAGE);
			}
		} else if (e.getSource() == logout) {
			exit();
		} else if (e.getSource() == file) {
			chooseFile();
		} else if (e.getSource() == downFile) {
			downloadFile();
		}
	}

	private void downloadFile() {
		String file = (String) this.combobox.getSelectedItem();
		
		sendMSG(file.split(": ")[1], TYPE.DOWNLOAD);
	}
	
	public void chooseFile() {
		final JFileChooser fc = new JFileChooser();
		fc.showOpenDialog(this);
		try {
			if (fc.getSelectedFile() != null) {
//	                textFieldFilePath.setText(fc.getSelectedFile().getPath());
				System.out.println(fc.getSelectedFile().getPath());
				String destinationDir = "D:\\server\\";
				sendFile(fc.getSelectedFile().getPath(), destinationDir);
//				this.msg.append("Tui: " + fc.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
