package com.dipcore.mk8900temp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive (Context context, Intent intent) {
        String action = intent.getAction();
        if ( action.equals (Intent.ACTION_BOOT_COMPLETED ) ) {
            context.startService(new Intent(context, BackgroundService.class));
        }

    }

}
