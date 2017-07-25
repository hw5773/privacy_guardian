import java.net.*;
import java.security.cert.Certificate;
import java.io.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

public class ClientTest {
	public static void main(String[] args) throws IOException {
		String httpsURL = "https://mmlab.snu.ac.kr";
//		String httpsURL = "https://147.46.114.188";
//		String httpsURL = "https://127.0.0.1:9999";
		URL url;

		try {
			url = new URL(httpsURL);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

			// Printing HTTPS Certificate.
			printHTTPSCert(con);

			// Printing the Content.
			printContent(con);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void printHTTPSCert(HttpsURLConnection con) {
		if (con != null) {
			try {
				System.out.println("Response Code: " + con.getResponseCode());
				System.out.println("Cipher Suite: " + con.getCipherSuite());
				System.out.println("\n");

				Certificate[] certs = con.getServerCertificates();

				for (Certificate cert : certs) {
					System.out.println("Cert Type: " + cert.getType());
					System.out.println("Cert Hash Code: " + cert.hashCode());
					System.out.println("Cert Public Key Algorithm: " + cert.getPublicKey().getAlgorithm());
					System.out.println("Cert Public Key Format: " + cert.getPublicKey().getFormat());
					System.out.println("\n");
				}
			} catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void printContent(HttpsURLConnection con) {
		if (con != null) {
			try {
				System.out.println("***** Content of the URL *****");
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

				String input;

				while ((input = br.readLine()) != null) {
					System.out.println(input);
				}

				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
