package com.dipcore.mk8900temp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public  class FloatingView {

    private static TextView mFloatView;
    static Context context;

    public FloatingView(Context context){
        this.context = context;
        createFloatView();
    }

    public void show(String text) {
        mFloatView.setText(text);
    }

    private static void createFloatView() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        SharedPreferences mSharedPreferences = context.getSharedPreferences("SharedPreferences", 0);
        int Lx = mSharedPreferences.getInt("x", -1);
        int Ly = mSharedPreferences.getInt("y", -1);
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wmParams.type = 2010;
        wmParams.format = 1;
        wmParams.flags = 1288;
        wmParams.gravity = 49;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = -2;
        wmParams.height = -2;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View oView = layoutInflater.inflate(R.layout.overlay_window, null);
        LinearLayout mFloatLayout = (LinearLayout) oView;
        mWindowManager.addView(mFloatLayout, wmParams);
        mFloatView = (TextView) mFloatLayout.findViewById(R.id.textTemp);
        mFloatView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(context, "Opening main view. Please wait", Toast.LENGTH_LONG);
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return false;
            }
        });
        ApplyFloatViewAppearanceSettings();
        mFloatLayout.measure(android.view.View.MeasureSpec.makeMeasureSpec(0, 0), android.view.View.MeasureSpec.makeMeasureSpec(0, 0));
        int statusbar = getStatusBarHeight(context);
        if (Lx != -1 && Ly != -1) {
            wmParams.x = Lx;
            wmParams.y = Ly;
            if (Ly < statusbar) {
                mFloatView.setTextSize(19F);
            } else {
                mFloatView.setTextSize(27F);
            }
            mWindowManager.updateViewLayout(mFloatLayout, wmParams);
        }
    }

    public static void ApplyFloatViewAppearanceSettings() {
        if (mFloatView == null)
            return;

        NotificationAppearanceSettings settings = NotificationAppearanceSettings.GetCurrent();
        if (settings.BackColor != -1)
            mFloatView.setBackgroundColor(settings.BackColor);
        if (settings.ForeColor != -1)
            mFloatView.setTextColor(settings.ForeColor);
        if (settings.FontSize != -1)
            mFloatView.setTextSize(settings.FontSize);
        int[] paddings = new int[]{mFloatView.getPaddingLeft(),mFloatView.getPaddingTop(),mFloatView.getPaddingRight(),mFloatView.getPaddingBottom() };
        if (settings.PaddingLeft != -1)
            paddings[0] = settings.PaddingLeft;
        if (settings.PaddingTop != -1)
            paddings[1] = settings.PaddingTop;
        if (settings.PaddingRight != -1)
            paddings[2] = settings.PaddingRight;
        if (settings.PaddingBottom != -1)
            paddings[3] = settings.PaddingBottom;
        mFloatView.setPadding(paddings[0],paddings[1],paddings[2],paddings[3]);

    }

    public static int getStatusBarHeight(Context context) {
        int j;
        try {
            Class class1 = Class.forName("com.android.internal.R$dimen");
            Object obj = class1.newInstance();
            int i = Integer.parseInt(class1.getField("status_bar_height").get(obj).toString());
            j = context.getResources().getDimensionPixelSize(i);
        } catch (Exception exception) {
            exception.printStackTrace();
            return 0;
        }
        return j;
    }
}

