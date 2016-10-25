package org.locationprivacy.locationprivacy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.util.Log;

/** GPS 샘플 */
public class MainActivity extends Activity {

    private Button btnShowLocation;
    private TextView txtLat;
    private TextView txtLon;

    // GPSTracker class
    private GpsInfo gps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnShowLocation = (Button) findViewById(R.id.btn_start);
        txtLat = (TextView) findViewById(R.id.Latitude);
        txtLon = (TextView) findViewById(R.id.Longitude);
        // String strLat, strLon;
		String strLat;
		String strLon;

        // GPS 정보를 보여주기 위한 이벤트 클래스 등록
        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                gps = new GpsInfo(MainActivity.this);
                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    String strLat = String.valueOf(latitude);
                    String strLon = String.valueOf(longitude);

                    txtLat.setText(strLat);
                    txtLon.setText(strLon);

                    JSONArray arr = new JSONArray();
                    JSONObject obj = new JSONObject();

                    try {
                        obj.put("message", "client");
                        obj.put("latitude", strLat);
                        obj.put("longitude", strLon);
                        arr.put(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String sMsg = arr.toString();
                    String result = SendByHttp(sMsg);
                    //String[][] parsedData = jsonParserList(result);

                    Toast.makeText(
                            getApplicationContext(),
                            "당신의 위치 - \n위도: " + latitude + "\n경도: " + longitude,
                            Toast.LENGTH_LONG).show();

                    //txtRecv.setText(result);
                } else {
                    // GPS 를 사용할수 없으므로
                    gps.showSettingsAlert();
                }
            }
        });
    }

    private String SendByHttp(String msg) {
        new HttpUtil().execute(msg);
        return "";
    }

    public String[][] jsonParserList(String pRecv) {
        Log.i("서버에서 받은 전체 내용: ", pRecv);

        try {
            JSONObject json = new JSONObject(pRecv);
            JSONArray jArr = json.getJSONArray("List");

            String[] jsonName = {"message", "latitude", "longitude"};
            String[][] parsedData = new String[jArr.length()][jsonName.length];

            for (int i=0; i<jArr.length(); i++) {
                json = jArr.getJSONObject(i);

                if (json != null) {
                    for (int j=0; j<jsonName.length; j++) {
                        parsedData[i][j] = json.getString(jsonName[j]);
                    }
                }
            }

            for (int i=0; i<parsedData.length; i++) {
                Log.i("message["+i+"]: ", parsedData[i][0]);
                Log.i("latitude["+i+"]: ", parsedData[i][1]);
                Log.i("longitude["+i+"]: ", parsedData[i][2]);
            }

            return parsedData;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
