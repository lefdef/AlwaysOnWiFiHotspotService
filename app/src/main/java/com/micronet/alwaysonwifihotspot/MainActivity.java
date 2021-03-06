package com.micronet.alwaysonwifihotspot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by brigham.diaz on 12/12/2016.
 */

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // start the auto date/time reset service
        Intent service = new Intent(this, AlwaysOnWiFiHotspotService.class);
        startService(service);
    }

    @Override
    protected void onStart() {
        super.onStart();
        finish();
    }
}