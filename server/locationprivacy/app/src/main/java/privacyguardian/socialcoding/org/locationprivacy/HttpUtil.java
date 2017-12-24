package privacyguardian.socialcoding.org.locationprivacy;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

// Test Comment
/**
 * Created by user on 2016-06-24.
 */
public class HttpUtil extends AsyncTask<String, Void, Void> {

    @Override
    public Void doInBackground(String... params) {
        try {
            String url = "http://192.168.0.2:7979/location";
            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

            conn.setReadTimeout(3000);
            conn.setConnectTimeout(3000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
            os.write(params[0]);
            os.flush();
            os.close();

            int retCode = conn.getResponseCode();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = br.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            br.close();

            String res = response.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
