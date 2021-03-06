package com.dipcore.mk8900temp;

import android.content.Context;
import android.content.Intent;

public class LogA {

    Context context;

    public LogA(Context context) {
        this.context = context;
    }
    public void e(String tag, String message){
        display(tag, message);
    }

    public void d(String tag, String message){
        display(tag, message);
    }

    public void i(String tag, String message){
        display(tag, message);
    }

    private void display(String tag, String message){
        android.util.Log.d(tag, message);
        Intent intent = new Intent(Constants.LOG_MESSAGE);
        intent.putExtra("value", message);
        context.sendBroadcast(intent);
    }
}