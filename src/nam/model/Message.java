package nam.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static String MESSAGE = "MESSAGE";
	public final static String FILE = "FILE";

	public static enum TYPE {
		MESSAGE, FILE, EXIT, LIST, LOGIN_SUCCESS, LOGIN_FAILED, JOINED, SEND, DOWNLOAD
	}

	private String _id;
	private TYPE type;
	private String message;
	private FileInfo file;

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		byte[] utf8data;
		try {
			utf8data = message.getBytes("UTF-8");
			String myString = new String(utf8data, "UTF-8");
			message = myString;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.message = message;
	}

	public FileInfo getFile() {
		return file;
	}

	public void setFile(FileInfo file) {
		this.file = file;
	}

	@Override
	public String toString() {
		return type + " " + message;
	}

}
