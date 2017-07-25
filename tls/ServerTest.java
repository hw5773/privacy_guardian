import java.net.*;
import java.io.*;
import java.util.Data;
import java.text.SimpleDateFormat;

public class ServerTest {
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		int port = 5555;

		try {
			serverSocket = new ServerSocket(port);
			System.out.println(getTime() + " 서버가 준비되었습니다.");

			Socket socket = serverSocket.accept();
			InetAddress clientAddress = socket.getInetAddress();
			System.out.println(getTime() + clientAddress + " 에서 클라이언트가 접속했습니다.");

			OutputStream os = socket.getOutputStream();
			InputStream in = socket.getInputStream();

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();

			char[] test = bytesToHex(buffer.toByteArray());
			
			for (int i=0; i<test.length; i++) {
				// TODO: print the packet
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[test.length * 2];
		for (int j=0; j < test.length; j++) {
			int v = test[j] & 0xFF;
			hexChars[j*2] = hexArray[ v >>> 4 ];
			hexChars[j*2 + 1] = hexArray[ v & 0x0F ];
		}

		return new String(hexChars);
	}

	static String getTime() {
		SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
		return f.format(new Date());
	}
}

				
