import java.net.*;
import java.io.*;
import java.util.Date;
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
			InputStream is = socket.getInputStream();

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while (true) {
				nRead = is.read(data);
				System.out.println("nRead: " + nRead);
				if (nRead < 0)
					break;
				char[] test = bytesToHex(data, nRead);
				byte[] clientHello = toBytes(data, nRead);
				for (int i=0; i<test.length; i=i+2) {
					if (i+1 < test.length) {
						System.out.print(Character.toString(test[i]) + Character.toString(test[i+1]) + " ");
					} else {
						System.out.println((char)test[i]);
					}

					if (i % 20 == 18)
						System.out.println();
				}

				System.out.println();
				parseRecord(clientHello);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static byte[] toBytes(byte[] data, int nRead) {
		byte[] ret = new byte[nRead];
		System.arraycopy(data, 0, ret, 0, nRead);

		return ret;
	}

	private static char[] bytesToHex(byte[] bytes, int nRead) {
		char[] hexChars = new char[nRead * 2];
		for (int i=0; i < nRead; i++) {
			int v = bytes[i] & 0xFF;
			hexChars[i*2] = hexArray[ v >>> 4 ];
			hexChars[i*2 + 1] = hexArray[ v & 0x0F ];
		}

		return hexChars;
	}

	static String getTime() {
		SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
		return f.format(new Date());
	}

	private static void parseRecord(byte[] record) {
		System.out.println("[FUNC] parseRecord");
		byte contentType = record[0];
		if (contentType == 0x16)
			System.out.println("Message: Handshake");

		if (record[1] == 0x03 && record[2] == 0x03)
			System.out.println("Version: TLS 1.2");

		int length;
		length = ((record[3] & 0xFF) << 8) | (record[4] & 0xFF);
		System.out.println("Length: " + length);

		byte[] content = new byte[length];
		System.arraycopy(record, 5, content, 0, length);

		if (contentType == 0x16)
			parseHandshake(content);
	}

	private static void parseHandshake(byte[] content) {
		System.out.println("[FUNC] parseHandshake");
		byte handshakeType = content[0];
		int length = ((content[1] & 0xFF) << 16) | ((content[2] & 0xFF) << 8) | (content[3] & 0xFF);
		System.out.println("Length: " + length);
		byte[] body = new byte[length];
		System.arraycopy(content, 4, body, 0, length);

		if (handshakeType == 0x1) {
			System.out.println("Handshake Type: Client Hello");
			parseClientHello(body);
		}
	}

	private static void parseClientHello(byte[] body) {
		int version = ((body[0] & 0xFF) << 8) | (body[1] & 0xFF);
		switch (version) {
		case 0x0301:
			System.out.println("Client Version: TLS 1.0");
			break;
		case 0x0302:
			System.out.println("Client Version: TLS 1.1");
			break;
		case 0x0303:
			System.out.println("Client Version: TLS 1.2");
			break;
		default:
			System.out.println("Client Version: none");
		}

		byte[] random = new byte[32];
		System.arraycopy(body, 2, random, 0, 32);
		int time = ((random[0] & 0xFF) << 24) | ((random[1] & 0xFF) << 16) | ((random[2] & 0xFF) << 8) | (random[3] & 0xFF);
		System.out.println("Client Unix Time: " + time);

		byte[] sessionID = new byte[32];
		System.arraycopy(body, 34, sessionID, 0, 32);
	}
}
