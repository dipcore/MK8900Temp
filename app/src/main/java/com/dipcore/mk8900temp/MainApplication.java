package com.dipcore.mk8900temp;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.view.WindowManager;


public class MainApplication extends Application {

    static MainApplication self;
    static FloatingView floatingView;
    static Handler handler = new Handler();

    private WindowManager.LayoutParams mWindowManagerLayoutParams = new WindowManager.LayoutParams();
    public WindowManager.LayoutParams getWindowManagerLayoutParams() {
        return mWindowManagerLayoutParams;
    }

    public static MainApplication getInstance() {
        return self;
    }

    public static void updateTextView(String text) {
        floatingView.show(text);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
        floatingView = new FloatingView(this);
        this.startService (new Intent(this, BackgroundService.class));
    }
}
