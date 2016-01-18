package com.dipcore.mk8900temp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {


    BroadcastReceiver updateUIReceiver;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.textView);

        startService(new Intent(this, BackgroundService.class));

        updateUIReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(Constants.LOG_MESSAGE)) {
                    String message = intent.getStringExtra("value");
                    textView.append(message + "\n");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.LOG_MESSAGE);
        registerReceiver(updateUIReceiver, filter);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(updateUIReceiver);
        super.onStop();
    }

    public void showSettings(View v) {
        Intent myIntent = new Intent(this, CustomSettingsActivity.class);
        startActivity(myIntent);
    }

}
