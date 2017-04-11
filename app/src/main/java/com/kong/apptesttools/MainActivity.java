package com.kong.apptesttools;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import com.kong.apptesttools.adapter.WiFiSignalSourceAdapter;
import com.kong.apptesttools.receiver.NetWorkReceiver;
import com.kong.apptesttools.receiver.USBInterfaceReceiver;
import com.kong.apptesttools.utils.Utils;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView tv_storage_capacity, tv_software_version, tv_hardware_version, tv_equipment_model,
            tv_sn_code, tv_product_id, tv_product_id_authorize_state, tv_usb_interface_01, tv_usb_01_insert_state,
            tv_usb_interface_02, tv_usb_02_insert_state, tv_ethernet_info, tv_ethernet_insert_state_and_ip,
            tv_wifi_info, tv_wifi_connection_state_signal_source, tv_show_wifi_signal_source;

    private RecyclerView recycler_view;
    private WiFiSignalSourceAdapter adapter;

    private VideoView vv_video_test;

    private NetWorkReceiver netWorkReceiver;
    private USBInterfaceReceiver usbInterfaceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRecycler();
        initView();
        initViewData();
    }

    private void initViewData() {

        if (Utils.getWiFiIsConn(this)) {
//            Log.i(TAG, " -=-=-=-=- initViewData: WIFI已连接:");
        } else {
//            Log.i(TAG, " -=-=-=-=- initViewData: WIFI未连接:");
        }

        if (Utils.getWiFiIsOpen(this)) {
            tv_wifi_connection_state_signal_source.setText(getResources().getString(R.string.open) + " 共有 " + Utils.getWiFiSignalSourceList(this).size() + " 个信号源");
            tv_wifi_connection_state_signal_source.setTextColor(getResources().getColor(R.color.colorGreen));
        } else {
            tv_wifi_connection_state_signal_source.setText(getResources().getString(R.string.no_open));
            tv_wifi_connection_state_signal_source.setTextColor(getResources().getColor(R.color.colorRed));
        }

        if (Utils.getEthernetIsConn(this)) {
            tv_ethernet_insert_state_and_ip.setText(getResources().getString(R.string.insert) + " IP:" + Utils.getIPAddress(true));
            tv_ethernet_insert_state_and_ip.setTextColor(getResources().getColor(R.color.colorGreen));
        } else {
            tv_ethernet_insert_state_and_ip.setText(getResources().getString(R.string.no_insert));
            tv_ethernet_insert_state_and_ip.setTextColor(getResources().getColor(R.color.colorRed));
        }

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        switch (usbManager.getDeviceList().entrySet().size()) {
            case 0:
                tv_usb_01_insert_state.setText(getResources().getString(R.string.no_insert));
                tv_usb_01_insert_state.setTextColor(getResources().getColor(R.color.colorRed));
                tv_usb_02_insert_state.setText(getResources().getString(R.string.no_insert));
                tv_usb_02_insert_state.setTextColor(getResources().getColor(R.color.colorRed));
                break;
            case 1:
                for (Map.Entry<String, UsbDevice> stringUsbDeviceEntry : usbManager.getDeviceList().entrySet()) {
                    if (stringUsbDeviceEntry.getKey().contains("/dev/bus/usb/001")) {
                        tv_usb_01_insert_state.setText(getResources().getString(R.string.insert));
                        tv_usb_01_insert_state.setTextColor(getResources().getColor(R.color.colorGreen));
                        tv_usb_02_insert_state.setText(getResources().getString(R.string.no_insert));
                        tv_usb_02_insert_state.setTextColor(getResources().getColor(R.color.colorRed));
                    } else if (stringUsbDeviceEntry.getKey().contains("/dev/bus/usb/002")) {
                        tv_usb_01_insert_state.setText(getResources().getString(R.string.no_insert));
                        tv_usb_01_insert_state.setTextColor(getResources().getColor(R.color.colorRed));
                        tv_usb_02_insert_state.setText(getResources().getString(R.string.insert));
                        tv_usb_02_insert_state.setTextColor(getResources().getColor(R.color.colorGreen));
                    }
                }
                break;
            case 2:
                tv_usb_01_insert_state.setText(getResources().getString(R.string.insert));
                tv_usb_01_insert_state.setTextColor(getResources().getColor(R.color.colorGreen));
                tv_usb_02_insert_state.setText(getResources().getString(R.string.insert));
                tv_usb_02_insert_state.setTextColor(getResources().getColor(R.color.colorGreen));
                break;
        }
    }

    private void initRecycler() {

        netWorkReceiver = new NetWorkReceiver();
        IntentFilter networkIF = new IntentFilter();
        networkIF.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkReceiver, networkIF);

        usbInterfaceReceiver = new USBInterfaceReceiver();
        IntentFilter usbIF = new IntentFilter();
        usbIF.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbIF.addAction(Intent.ACTION_MEDIA_CHECKING);
        usbIF.addAction(Intent.ACTION_MEDIA_EJECT);
        usbIF.addAction(Intent.ACTION_MEDIA_REMOVED);

        usbIF.addDataScheme("file"); // 必须要有此行，否则无法收到广播  
        registerReceiver(usbInterfaceReceiver, usbIF);

    }

    private void initView() {
        vv_video_test = (VideoView) findViewById(R.id.vv_video_test);
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);

        tv_storage_capacity = (TextView) findViewById(R.id.tv_storage_capacity);
        tv_software_version = (TextView) findViewById(R.id.tv_software_version);
        tv_hardware_version = (TextView) findViewById(R.id.tv_hardware_version);
        tv_equipment_model = (TextView) findViewById(R.id.tv_equipment_model);
        tv_sn_code = (TextView) findViewById(R.id.tv_sn_code);

        tv_product_id = (TextView) findViewById(R.id.tv_product_id);
        tv_product_id_authorize_state = (TextView) findViewById(R.id.tv_product_id_authorize_state);
        tv_usb_interface_01 = (TextView) findViewById(R.id.tv_usb_interface_01);
        tv_usb_01_insert_state = (TextView) findViewById(R.id.tv_usb_01_insert_state);
        tv_usb_interface_02 = (TextView) findViewById(R.id.tv_usb_interface_02);
        tv_usb_02_insert_state = (TextView) findViewById(R.id.tv_usb_02_insert_state);
        tv_ethernet_info = (TextView) findViewById(R.id.tv_ethernet_info);
        tv_ethernet_insert_state_and_ip = (TextView) findViewById(R.id.tv_ethernet_insert_state_and_ip);
        tv_wifi_info = (TextView) findViewById(R.id.tv_wifi_info);
        tv_wifi_connection_state_signal_source = (TextView) findViewById(R.id.tv_wifi_connection_state_signal_source);

        if (Utils.getWiFiIsOpen(this)) {
            adapter = new WiFiSignalSourceAdapter(this);
            adapter.setTextView(tv_wifi_connection_state_signal_source);
        }

        initVideoView();
        initRecyclerView();
        initTextData();
        receiverCallBack();

    }

    private void receiverCallBack() {
        netWorkReceiver.setNetWorkCallBack(new NetWorkReceiver.NetWorkCallBack() {
            @Override
            public void WiFiIsConnection(boolean isConn) {
                if (isConn) {
                    tv_wifi_connection_state_signal_source.setText(getResources().getString(R.string.open) + " 共有多少个信号源");
                    tv_wifi_connection_state_signal_source.setTextColor(getResources().getColor(R.color.colorGreen));
                } else {
                    if (Utils.getWiFiIsOpen(MainActivity.this)) {
                        tv_wifi_connection_state_signal_source.setText(getResources().getString(R.string.open) + " 共有 " + Utils.getWiFiSignalSourceList(MainActivity.this).size() + " 个信号源");
                        tv_wifi_connection_state_signal_source.setTextColor(getResources().getColor(R.color.colorGreen));
                    } else {
                        tv_wifi_connection_state_signal_source.setText(getResources().getString(R.string.no_open));
                        tv_wifi_connection_state_signal_source.setTextColor(getResources().getColor(R.color.colorRed));
                    }
                }
            }

            @Override
            public void EthernetIsConnection(boolean isConn) {
                if (isConn) {
                    tv_ethernet_insert_state_and_ip.setText(getResources().getString(R.string.insert) + " IP:" + Utils.getIPAddress(true));
                    tv_ethernet_insert_state_and_ip.setTextColor(getResources().getColor(R.color.colorGreen));
                } else {
                    tv_ethernet_insert_state_and_ip.setText(getResources().getString(R.string.no_insert));
                    tv_ethernet_insert_state_and_ip.setTextColor(getResources().getColor(R.color.colorRed));
                }
            }
        });

        usbInterfaceReceiver.setUsbCallBack(new USBInterfaceReceiver.USBCallBack() {
            @Override
            public void USB_0_IsInsert(boolean isInsert) {
                if (isInsert) {
                    tv_usb_01_insert_state.setText(getResources().getString(R.string.insert));
                    tv_usb_01_insert_state.setTextColor(getResources().getColor(R.color.colorGreen));
                } else {
                    tv_usb_01_insert_state.setText(getResources().getString(R.string.no_insert));
                    tv_usb_01_insert_state.setTextColor(getResources().getColor(R.color.colorRed));
                }
            }

            @Override
            public void USB_1_IsInsert(boolean isInsert) {
                if (isInsert) {
                    tv_usb_02_insert_state.setText(getResources().getString(R.string.insert));
                    tv_usb_02_insert_state.setTextColor(getResources().getColor(R.color.colorGreen));
                } else {
                    tv_usb_02_insert_state.setText(getResources().getString(R.string.no_insert));
                    tv_usb_02_insert_state.setTextColor(getResources().getColor(R.color.colorRed));
                }
            }
        });
    }

    private void initTextData() {
        tv_storage_capacity.setText(getResources().getString(R.string.storage_capacity) + Utils.showRAMInfo(this) + " | " + Utils.showROMInfo());
        tv_software_version.setText(getResources().getString(R.string.software_version) + Build.VERSION.RELEASE);
        tv_hardware_version.setText(getResources().getString(R.string.hardware_version) + Build.DISPLAY);
        tv_equipment_model.setText(getResources().getString(R.string.equipment_model) + Build.MODEL);
        tv_sn_code.setText(getResources().getString(R.string.sn_code) + getResources().getString(R.string.null_text));
        tv_product_id.setText(getResources().getString(R.string.product_id) + getResources().getString(R.string.null_text));
        tv_usb_interface_01.setText(getResources().getString(R.string.usb_interface_01));
        tv_usb_interface_02.setText(getResources().getString(R.string.usb_interface_02));
        tv_ethernet_info.setText(getResources().getString(R.string.ethernet_info) + Utils.getEthernetMacAddress());
        tv_wifi_info.setText(getResources().getString(R.string.wifi_info) + Utils.getWiFiMacAddress(this));
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_view.setLayoutManager(linearLayoutManager);
        recycler_view.setAdapter(adapter);
        recycler_view.setFocusable(true);
        recycler_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (recycler_view.getChildCount() > 0) {
                        recycler_view.getChildAt(0).requestFocus();
                    }
                }
            }
        });
    }

    private void initVideoView() {
        vv_video_test.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test_video));
        vv_video_test.start();
        vv_video_test.setFocusable(false);
        vv_video_test.setFocusableInTouchMode(false);
        vv_video_test.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                vv_video_test.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.setRun(false);
        }
        unregisterReceiver(netWorkReceiver);
        unregisterReceiver(usbInterfaceReceiver);
    }
}
