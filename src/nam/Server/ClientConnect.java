package nam.Server;

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
import java.net.Socket;
import java.util.UUID;

import nam.model.FileInfo;
import nam.model.Message;
import nam.model.Message.TYPE;

public class ClientConnect extends Thread {
	public Socket client;
	public Server server;
	private String nickName;
	private DataOutputStream dos;
	private DataInputStream dis;
	private boolean run;

	public ClientConnect(Server server, Socket client) {
		try {
			this.server = server;
			this.client = client;
			dos = new DataOutputStream(client.getOutputStream());
			dis = new DataInputStream(client.getInputStream());
			run = true;
			this.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void run() {
		// xữ lý đăng nhập
		String msg = null;
		while (run) {
			nickName = getMSG().getMessage();
			if (nickName.compareTo("0") == 0) {
				logout();
			} else {
				if (checkNick(nickName)) {
					sendMSG("0", TYPE.LOGIN_FAILED);
				} else {
					sendMSG(nickName, TYPE.LOGIN_SUCCESS);
					// Hiển thị bên server
					server.user.append("# " + nickName + " đã kết nối\n");
					// Hiển thị bên client
					server.sendAll(nickName, nickName + " đã vào phòng với anh em\n", TYPE.MESSAGE);
					server.listUser.put(nickName, this);
					server.sendAllUpdate(nickName);
					diplayAllUser();
					while (run) {
						Message message = getMSG();
						switch (message.getType()) {
						case EXIT:
							run = false;
							server.listUser.remove(this.nickName);
							exit();
							break;
						case MESSAGE:
							msg = message.getMessage();
							System.out.println("a " + msg);
							server.sendAll(nickName, msg, TYPE.MESSAGE);
							sendMSG(msg, TYPE.SEND);
							break;
						case FILE:
							FileInfo fileInfo = message.getFile();
							String _id = UUID.randomUUID().toString();
							fileInfo.setDestinationDirectory("D:\\Server\\file\\" + _id);
							createFile(fileInfo);
							msg = _id + "_" + message.getFile().getFilename();
							System.out.println(msg);
							server.sendAll(nickName, msg, TYPE.FILE);
							sendMSG(msg, TYPE.SEND);
							break;
						case DOWNLOAD:
							msg = message.getMessage();
							sendFile("D:\\Server\\file\\"+ msg, "");
							break;
						default:
							break;
						}
					}
				}
			}
		}
	}

	private boolean createFile(FileInfo fileInfo) {
		BufferedOutputStream bos = null;
		try {
			if (fileInfo != null) {
				File fileReceive = new File(fileInfo.getDestinationDirectory() + "_" + fileInfo.getFilename());
				bos = new BufferedOutputStream(new FileOutputStream(fileReceive));
				// write file content
				bos.write(fileInfo.getDataBytes());
				bos.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
//	            closeStream(bos);
		}
		return true;
	}

	private void logout() {
		try {
			dos.close();
			dis.close();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Xử lý thoát
	private void exit() {
		try {
			server.sendAllUpdate(nickName);
			dos.close();
			dis.close();
			client.close();
			server.user.append(nickName + "đã thoát");
			server.sendAll(nickName, "đã thoát", TYPE.MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendFile(String sourceFilePath, String destinationDir) {
		DataOutputStream outToServer = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		try {
			// make greeting
			Message message = new Message();
			message.setType(TYPE.DOWNLOAD);
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

	public void closeStream(InputStream inputStream) {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private boolean checkNick(String nick) {
		return server.listUser.containsKey(nick);
	}

	public void sendMSG(String data, TYPE type) {
		ObjectOutputStream oos = null;
		try {
			Message message = new Message();
			message.setType(type);
			message.setMessage(data);
			oos = new ObjectOutputStream(client.getOutputStream());
			oos.writeObject(message);
//			oos.flush();
//			oos.close();
		} catch (Exception e) {
			exit();
			e.printStackTrace();
		}
	}

//	public void sendMSG(String msg1, String msg2) {
//		sendMSG(msg1, TYPE.MESSAGE);
//		sendMSG(msg2, TYPE.MESSAGE);
//	}

	private Message getMSG() {
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

	private void diplayAllUser() {
		String name = server.getAllName();
		sendMSG(name, TYPE.LIST);
//		sendMSG(name, TYPE.MESSAGE);
	}
}
