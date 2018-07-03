package com.example.dan.dmxlightcontroller;

import android.graphics.Color;

/**
 * Created by dan on 8/17/17.
 */

public class DmxSerialConverter {

    private static int CHANNEL_COUNT = 7;
    private static int DIMMER = 1;
    private static int RED = 2;
    private static int GREEN = 3;
    private static int BLUE = 4;
    private static int COLOR_STROBE = 5;
    private static int MODE = 6;
    private static int SPEED = 7;

    private int mDevices = 16;
    DmxSerialConverter(int devices) {
        mDevices = devices;
    }

    //6c87w13c73w
    String getColor(int color) {
        String s = "";

        for (int i = 0; i < mDevices; i++) {
            int offset = i * CHANNEL_COUNT;
            String builder = "";

            builder += Integer.toString(MODE+offset) + "c";
            builder += "0w"; // toggle rgb mode

            builder += Integer.toString(DIMMER+offset) + "c";
            builder += "255w"; // set max brightness

            builder += Integer.toString(RED+offset) + "c";
            builder += Integer.toString(Color.red(color)) + "w";

            builder += Integer.toString(GREEN+offset) + "c";
            builder += Integer.toString(Color.green(color)) + "w";

            builder += Integer.toString(BLUE+offset) + "c";
            builder += Integer.toString(Color.blue(color)) + "w";

            s += builder;
        }

        return s;
    }
}
