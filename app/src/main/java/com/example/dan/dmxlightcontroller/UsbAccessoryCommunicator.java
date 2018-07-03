package com.example.dan.dmxlightcontroller;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class UsbAccessoryCommunicator implements Runnable {
    static final boolean DEBUG = false;
    static final String TAG = "DmxLight_UsbComm";

    private UsbManager mUsbManager;
    private UsbAccessory mAccessory;
    private ParcelFileDescriptor mFileDescriptor;
    private FileInputStream mInputStream;
    private FileOutputStream mOutputStream;



    private LinkedBlockingQueue<Integer> mEventQueue;
    private LinkedBlockingQueue<String> mSendMsgQueue;
    private Thread mEventsThread;
    private static final int MAX_EVENTS = 10;
    private static final int EVENT_OPEN = 0;
    private static final int EVENT_CLOSE = 1;
    private static final int EVENT_CANCEL = 2;
    private static final int EVENT_SEND = 3;
    private static final int EVENT_TERMINATE = 4;

    @Override
    public void run() {
        Integer event;
        boolean quitThread = false;

        try {
            while (true) {
                Log.d(TAG, "waiting for event");
                event = mEventQueue.take();
                if (event != null) {
                    Log.d(TAG, "got event " + event);
                    switch (event) {
                        case EVENT_OPEN:
                            openAccessory();
                            break;
                        case EVENT_CLOSE:
                            closeAccessory();
                            break;
                        case EVENT_SEND:
                            sendToAccessory(mSendMsgQueue.take());
                            break;
                        case EVENT_TERMINATE:
                            quitThread = true;
                            break;
                        case EVENT_CANCEL:
                            mEventQueue.offer(EVENT_CLOSE);
                            break;
                    }
                }
                if (quitThread)
                    break;
            }
        } catch (Exception e) {
            Log.d(TAG, "got exception in thread" + e.toString());
            e.printStackTrace();
        } finally {
            Log.d(TAG, "Shutting down thread");
        }
    }


    UsbAccessoryCommunicator(UsbManager manager, UsbAccessory accessory) {
        mAccessory = accessory;
        mUsbManager = manager;
        mEventQueue = new LinkedBlockingQueue<>(MAX_EVENTS);
        mSendMsgQueue = new LinkedBlockingQueue<>(MAX_EVENTS);
        mEventsThread = new Thread(this);
        mEventsThread.start();
    }

    public void open() {
        mEventQueue.offer(EVENT_OPEN);
    }

    public void close() {
        mEventQueue.offer(EVENT_CLOSE);
    }

    public void send(String str) {
        mSendMsgQueue.offer(str);
        mEventQueue.offer(EVENT_SEND);
    }

    private void openAccessory() {
        Log.d(TAG, "openAccessory: " + mAccessory);
        mFileDescriptor = mUsbManager.openAccessory(mAccessory);
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
        } else {
            //TODO: add failure case here and shutdown class
        }
    }

    private void closeAccessory() {
        Log.d(TAG, "closeAccessory: " + mAccessory);
        try {
            if (mFileDescriptor != null)
                mFileDescriptor.close();
            if (mInputStream != null)
                mInputStream.close();
            if (mOutputStream != null)
                mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToAccessory(String msg) {
        if (mFileDescriptor != null && mOutputStream != null) {
            try {
                mOutputStream.write(msg.getBytes());
            } catch (IOException e) {
                Log.e(TAG, "Unable to write msg: " + msg);
                e.printStackTrace();
            }
        }
    }
}
