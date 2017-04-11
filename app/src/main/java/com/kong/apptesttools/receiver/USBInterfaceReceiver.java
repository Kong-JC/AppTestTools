package com.kong.apptesttools.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.util.Map;

/**
 * Created by Kong on 2017/3/24.
 */

public class USBInterfaceReceiver extends BroadcastReceiver {

    private static final String TAG = "USBInterfaceReceiver";

    public interface USBCallBack {
        void USB_0_IsInsert(boolean isInsert);

        void USB_1_IsInsert(boolean isInsert);
    }

    USBCallBack usbCallBack;

    public void setUsbCallBack(USBCallBack usbCallBack) {
        this.usbCallBack = usbCallBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_MEDIA_MOUNTED:
            case Intent.ACTION_MEDIA_CHECKING:
            case Intent.ACTION_MEDIA_EJECT:
            case Intent.ACTION_MEDIA_REMOVED:
                UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                switch (usbManager.getDeviceList().entrySet().size()) {
                    case 0:
                        usbCallBack.USB_0_IsInsert(false);
                        usbCallBack.USB_1_IsInsert(false);
                        break;
                    case 1:
                        for (Map.Entry<String, UsbDevice> stringUsbDeviceEntry : usbManager.getDeviceList().entrySet()) {
                            if (stringUsbDeviceEntry.getKey().contains("/dev/bus/usb/001")) {
                                usbCallBack.USB_0_IsInsert(true);
                                usbCallBack.USB_1_IsInsert(false);
                            } else if (stringUsbDeviceEntry.getKey().contains("/dev/bus/usb/002")) {
                                usbCallBack.USB_0_IsInsert(false);
                                usbCallBack.USB_1_IsInsert(true);
                            }
                        }
                        break;
                    case 2:
                        usbCallBack.USB_0_IsInsert(true);
                        usbCallBack.USB_1_IsInsert(true);
                        break;
                }
                break;
        }
    }
}
