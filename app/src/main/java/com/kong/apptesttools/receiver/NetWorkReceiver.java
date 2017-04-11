package com.kong.apptesttools.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by Kong on 2017/3/24.
 */

public class NetWorkReceiver extends BroadcastReceiver {

    private static final String TAG = "NetWorkReceiver";

    public interface NetWorkCallBack {
        void WiFiIsConnection(boolean isConn);

        void EthernetIsConnection(boolean isConn);
    }

    NetWorkCallBack netWorkCallBack;

    public void setNetWorkCallBack(NetWorkCallBack netWorkCallBack) {
        this.netWorkCallBack = netWorkCallBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ConnectivityManager.CONNECTIVITY_ACTION:
                ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo etherNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
                if (netWorkCallBack != null) {
                    if (wifiNetInfo != null && wifiNetInfo.isConnected()) {
                        netWorkCallBack.WiFiIsConnection(true);
                    } else {
                        netWorkCallBack.WiFiIsConnection(false);
                    }
                    if (etherNetInfo != null && etherNetInfo.isConnected()) {
                        netWorkCallBack.EthernetIsConnection(true);
                    } else {
                        netWorkCallBack.EthernetIsConnection(false);
                    }
                }
                break;
        }
    }

}
