package com.example.dan.dmxlightcontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbAccessory;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.usb.UsbManager;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.util.ArrayList;

public class Home extends AppCompatActivity {

    private static final String TAG = "DmxLightController";
    private TextView mTextMessage;
    private UsbAccessoryCommunicator mUsbComm;
    private ColorPicker mColorPicker;
    private ArrayList<Integer> mPrevoiusColor;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                Log.d(TAG, "Got disconnect event");
                updateDmxStatus("Not attached");
                if (mUsbComm != null) {
                    mUsbComm.close();
                    mUsbComm = null;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
                Log.d(TAG, "Got attach event");
                if (mUsbComm == null) {
                    UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                    mUsbComm = new UsbAccessoryCommunicator(manager, accessory);
                    updateDmxStatus(accessory.toString());
                }

            }
        }
    };

    public void onSelectColor(View view) {
        mColorPicker.show();
    }

    public void onPreviousColor(View view) {
        int color = 0;
         switch (view.getId()) {
             case R.id.prevColor1:
                 color = mPrevoiusColor.get(0);
                 break;
             case R.id.prevColor2:
                 color = mPrevoiusColor.get(1);
                 break;
             case R.id.prevColor3:
                 color = mPrevoiusColor.get(2);
                 break;
             case R.id.prevColor4:
                 color = mPrevoiusColor.get(3);
                 break;
         }
        Log.d("Red", Integer.toString(Color.red(color)));
        Log.d("Green", Integer.toString(Color.green(color)));
        Log.d("Blue", Integer.toString(Color.blue(color)));

        if (mUsbComm != null) {
             String s = (new DmxSerialConverter(1)).getColor(color);
             Log.d(TAG, "sending color: " + s);
             if (mUsbComm != null)
                 mUsbComm.send(s);
        }
    }

    ColorPickerCallback mPickerCallback = new ColorPickerCallback()
    {
        @Override
        public void onColorChosen ( @ColorInt int color){
            // Do whatever you want
            // Examples

            Log.d("Red", Integer.toString(Color.red(color)));
            Log.d("Green", Integer.toString(Color.green(color)));
            Log.d("Blue", Integer.toString(Color.blue(color)));

            String s = (new DmxSerialConverter(1)).getColor(color);
            Log.d(TAG, "sending colors: " + s);
            if (mUsbComm != null)
                mUsbComm.send(s);

            ((Button)findViewById(R.id.prevColor1)).setBackgroundColor(color);
            ((Button)findViewById(R.id.prevColor2)).setBackgroundColor(mPrevoiusColor.get(0));
            ((Button)findViewById(R.id.prevColor3)).setBackgroundColor(mPrevoiusColor.get(1));
            ((Button)findViewById(R.id.prevColor4)).setBackgroundColor(mPrevoiusColor.get(2));
            mPrevoiusColor.set(3, mPrevoiusColor.get(2));
            mPrevoiusColor.set(2, mPrevoiusColor.get(1));
            mPrevoiusColor.set(1, mPrevoiusColor.get(0));
            mPrevoiusColor.set(0, color);
            /*
            Log.d("Pure Hex", Integer.toHexString(color));
            Log.d("#Hex no alpha", String.format("#%06X", (0xFFFFFF & color)));
            Log.d("#Hex with alpha", String.format("#%08X", (0xFFFFFFFF & color)));
            */
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mTextMessage = (TextView) findViewById(R.id.message);

        mColorPicker = new ColorPicker(this, 255, 194, 0);
        mColorPicker.setCallback(mPickerCallback);
        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        mPrevoiusColor = new ArrayList<>(4);
        mPrevoiusColor.add(0);
        mPrevoiusColor.add(0);
        mPrevoiusColor.add(0);
        mPrevoiusColor.add(0);

        establishConnection();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUsbComm != null)  {
            mUsbComm.close();
            mUsbComm = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUsbComm == null) {
            establishConnection();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
    }

    private void establishConnection()
    {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbAccessory[] accessoryList = manager.getAccessoryList();

        if (accessoryList != null) {
            UsbAccessory accessory = accessoryList[0]; // supposedly only 1 accessory allowed
            Log.d(TAG, "Found accessory " + accessory);
            updateDmxStatus(accessory.toString());
            mUsbComm = new UsbAccessoryCommunicator(manager, accessory);
            mUsbComm.open();
        }
    }

    private void updateDmxStatus(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.message)).setText(msg);
            }
        });
    }

}
