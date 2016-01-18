package com.dipcore.mk8900temp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundService extends Service {

    private Context context;

    final private String TAG = "TEMP_SERVICE";

    final int PRODUCT_ID = 60000;
    //final int PRODUCT_ID = 8963;
    final int VENDOR_ID = 4292;
    //final int VENDOR_ID = 1659;


    private SerialInputOutputManager mSerialIoManager;
    private static UsbSerialPort sPort = null;
    private UsbManager mUsbManager;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler();

    private LogA Log;

    public BackgroundService () {
        context = MainApplication.getInstance().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log = new LogA(context);
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.CMD);
        registerReceiver(receiver, filter);
    }

    @Override
    public IBinder onBind (Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(runnableCode);

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        Log.d(TAG, "Looking for usb device");
        for (UsbSerialDriver d : availableDrivers) {
            Log.d(TAG, "Found: " + d.getDevice().getVendorId() + ":" + d.getDevice().getProductId());
            if (d.getDevice().getProductId() == PRODUCT_ID && d.getDevice().getVendorId() == VENDOR_ID) {

                final List<UsbSerialPort> ports = d.getPorts();
                Log.d(TAG, String.format("%s: %s port%s",
                        d, Integer.valueOf(ports.size()), ports.size() == 1 ? "" : "s"));

                if (ports.size() > 0)
                    sPort = ports.get(0);

            }
        }

        if (sPort == null) {
            Log.d(TAG, "Cannot find available usb port");
            return Service.START_STICKY;
        }

        if (sPort == null) {
            Log.e(TAG, "No serial device");
        } else {
            final UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(sPort.getDriver().getDevice());
            if (connection == null) {
                Log.e(TAG, "Opening device failed");
                return Service.START_STICKY;
            }

            try {
                sPort.open(connection);
                sPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage());
                try {
                    sPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sPort = null;
                return Service.START_STICKY;
            }
            Log.e(TAG, "Serial device: " + sPort.getClass().getSimpleName());
        }
        onDeviceStateChange();

        handler.post(runnableCode);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("BackgroundService", "onDestroy");

        stopIoManager();
        if (sPort != null) {
            try {
                sPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            sPort = null;
        }

        handler.removeCallbacks(runnableCode);
        stopForeground(true);

        super.onDestroy();
    }

    /////////////////

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                private String buffer = "";
                int i = 0;

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    if (new String(data).endsWith("\n") || new String(data).endsWith("\r")) {
                        BackgroundService.this.updateTextView(buffer);
                        buffer = "";
                    } else {
                        buffer += new String(data);
                    }
                }
            };

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            if (sPort != null) {
                if (sPort != null) {
                    writeToSerial("GetC");
                } else {
                    Log.d(TAG, "Usb Port is not opened");
                }
                // Repeat this the same runnable code block again another 2 seconds
                handler.postDelayed(runnableCode, 2000);
            }
        }
    };

    private void writeToSerial(String str) {
        str = (str.startsWith("!")) ? str : "!" + str;
        try {
            byte[] bytes = str.getBytes();
            sPort.write(bytes, 500);
            sPort.write(new byte[]{0x0d}, 500);
            Log.i(TAG, "Write: " + str);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "Write error: " + e.getMessage());
        }

    }

    private void updateTextView(String text) {

        Log.d(TAG, "Data: " + text);

        text = text.replaceAll("!", "");

        // it returns always 3 symbols, ignore others
        if (text.length() == 3) {

            // Output:
            // !-05 -> -5C
            // !+05 -> +5C
            // !+20 -> +20C
            // !+21 -> +21C

            if (text.startsWith("+0") || text.startsWith("-0")) {
                text = text.substring(0,1)+text.substring(1+1);
            }
            
            text.replaceAll("00", "0");


            Log.d(TAG, "Transformed data: " + text);

            final String textValue = text + "\u00b0";

            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainApplication.updateTextView(textValue);
                }
            });

        }

    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Constants.CMD)){
                String cmd = intent.getStringExtra("cmd");
                if (!cmd.isEmpty()) {
                    writeToSerial(cmd);
                }
            }
        }
    };

}
