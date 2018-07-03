#include <DmxMaster.h>
#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

AndroidAccessory acc("DannyBoy",
        "DmxController",
        "Controller for DMX lights",
        "1.0",
        "http://www.android.com",
        "0000000012345678");

void defaultColors() {
    int devices = 16;
    for (int i = 0; i < devices; i++) {
        int offset = i * 7/*channels*/;
        DmxMaster.write(6+offset,0); // set no color
        DmxMaster.write(2+offset,255); // all red
        DmxMaster.write(3+offset,194); // some green
        DmxMaster.write(4+offset,0); // no blue
        DmxMaster.write(1+offset,255); // max brightness
    }
}

void setup() {
    Serial.begin(115200);
    Serial.print("\r\nStart");
    acc.powerOn();
    DmxMaster.usePin(3);
}

int value = 0;
int channel;
bool setDefault = true;

void loop() {

    byte msg[256];
    memset(msg, 0, sizeof(msg));

    if (acc.isConnected()) {
        int len = acc.available();
        if (len > 0) {
            Serial.print("Message length: ");
            Serial.println(len, DEC);
            for (int i = 0; i < len; i++) {
                int c = acc.read();
                if (c == -1) {
                    Serial.println("Unable to read data");
                    continue;
                } else if ((c>='0') && (c<='9')) {
                    value = 10*value + c - '0';
                } else {
                    if (c=='c') channel = value;
                    else if (c=='w') {
                        DmxMaster.write(channel, value);
                        Serial.print("sending ");
                        Serial.print(value);
                        Serial.print(" to channel ");
                        Serial.println(channel);
                    }
                    value = 0;
                }
            }
        }
    } else if (setDefault) {
        defaultColors();
        setDefault = false;
    }
    delay(100);
}
