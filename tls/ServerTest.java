import java.net.*;
import java.io.*;

public class ServerTest {
	public static void main(String[] args) throws IOException {
		String ip = "147.46.114.188";
		int port = 443;

		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os);

		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

				
