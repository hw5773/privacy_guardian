package org.socialcoding.privacyguardian.Activity;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.socialcoding.privacyguardian.R;
import org.socialcoding.privacyguardian.VPN.Vpn;

public class VPNTestActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "VpnServiceTest";
    private boolean toggle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpntest);

        findViewById(R.id.vpn_connect).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = VpnService.prepare(getApplicationContext());
        if (intent != null) {
            startActivityForResult(intent, 0);
        } else {
            onActivityResult(0, RESULT_OK, null);
        }
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            Intent intent = new Intent(this, Vpn.class);
            startService(intent);
        }
    }
}
