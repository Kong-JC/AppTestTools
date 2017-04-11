package com.kong.apptesttools.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Kong on 2017/3/21.
 */

public class Utils {

    private static final String TAG = "Utils";

    // 获取版本Code
    public static int getCode(Context context) {
        String name = context.getPackageName();
        if ((name == null || name.trim().length() == 0)) return -1;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(name, 0);
            return pi == null ? -1 : pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // 获取版本名称
    public static String getName(Context context) {
        String name = context.getPackageName();
        if ((name == null || name.trim().length() == 0)) return null;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(name, 0);
            return pi == null ? null : pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取mac
    public static String getMac() {
        String macSerial = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line;
            while ((line = input.readLine()) != null) {
                macSerial += line.trim();
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return macSerial;
    }

    // 获取机型
    public static String getModel() {
        return Build.MODEL;
    }

    // 获取固件版本
    public static String getRelease() {
        return Build.VERSION.RELEASE;
    }

    // 读取系统文件
    private static String loadFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    // 从系统文件中获取以太网MAC地址
    public static String getEthernetMacAddress() {
        try {
            return loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 从系统文件中获取WIFI MAC地址
    public static String getWiFiMacAddress(Context context) {
        WifiManager my_wifiManager = ((WifiManager) context.getSystemService("wifi"));
        WifiInfo wifiInfo = my_wifiManager.getConnectionInfo();
        return wifiInfo.getMacAddress();
    }

    // 获取以太网IP地址
    public static String getIPAddress(boolean useIPv4) {
        try {
            for (Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements(); ) {
                NetworkInterface ni = nis.nextElement();
                // 防止小米手机返回10.0.2.15
                if (!ni.isUp()) continue;
                for (Enumeration<InetAddress> addresses = ni.getInetAddresses(); addresses.hasMoreElements(); ) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        boolean isIPv4 = hostAddress.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4) return hostAddress;
                        } else {
                            if (!isIPv4) {
                                int index = hostAddress.indexOf('%');
                                return index < 0 ? hostAddress.toUpperCase() : hostAddress.substring(0, index).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 显示RAM的可用和总容量，RAM相当于电脑的内存条
    public static String showRAMInfo(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        String[] available = fileSize(mi.availMem);
        String[] total = fileSize(mi.totalMem);

        return "RAM " + available[0] + available[1] + "/" + total[0] + total[1];
    }

    // 显示ROM的可用和总容量，ROM相当于电脑的C盘
    public static String showROMInfo() {
        File file = Environment.getDataDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long blockSize = statFs.getBlockSize();
        long totalBlocks = statFs.getBlockCount();
        long availableBlocks = statFs.getAvailableBlocks();

        String[] total = fileSize(totalBlocks * blockSize);
        String[] available = fileSize(availableBlocks * blockSize);

        return "ROM " + available[0] + available[1] + "/" + total[0] + total[1];
    }

    // 返回为字符串数组[0]为大小[1]为单位KB或者MB
    private static String[] fileSize(long size) {
        String str = "";
        if (size >= 1000) {
            str = "KB";
            size /= 1000;
            if (size >= 1000) {
                str = "MB";
                size /= 1000;
                if (size >= 1024) {
                    str = "GB";
                    size /= 1024;
                }
            }
        }

        /*将每3个数字用,分隔如:1,000*/
        DecimalFormat formatter = new DecimalFormat();
        formatter.setGroupingSize(3);
        String result[] = new String[2];
        result[0] = formatter.format(size);
        result[1] = str;
        return result;
    }

//    /* 返回为字符串数组[0]为大小[1]为单位KB或MB */
//    public static String[] fileSize(long size) {
//        String suffix = "";
//        float fSzie = 0;
//        if (size >= 1024) {
//            suffix = "KB";
//            fSzie = size / 1024;
//            if (fSzie >= 1024) {
//                suffix = "MB";
//                fSzie /= 1024;
//                if (fSzie >= 1024) {
//                    suffix = "GB";
//                    fSzie /= 1024;
//                }
//            }
//        }
//
////        DecimalFormat formatter = new DecimalFormat("#0.00");// 字符显示格式  
////        /* 每3个数字用,分隔，如1,000 */
////        formatter.setGroupingSize(3);
////        StringBuilder resultBuffer = new StringBuilder(formatter.format(fSzie));
////        if (suffix != null) {
////            resultBuffer.append(suffix);
////        }
////        return resultBuffer.toString();
//
//        DecimalFormat formatter = new DecimalFormat("#0.00");
//        formatter.setGroupingSize(3);
//        String result[] = new String[2];
//        result[0] = formatter.format(fSzie);
//        result[1] = suffix;
//        
//        return result;
//    }

    /*
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo.State state = networkInfo.getState();
        if (state == NetworkInfo.State.CONNECTED) {
            return true;
        } else {
            return false;
        }
     */
    public static boolean getWiFiIsConn(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ? true : false;
    }

    public static boolean getEthernetIsConn(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).getState() == NetworkInfo.State.CONNECTED ? true : false;
    }

    public static boolean getWiFiIsOpen(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            return true;
        } else if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            return false;
        }
        return false;
    }
    
    public static List<ScanResult> getWiFiSignalSourceList(Context context){
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wm.getConnectionInfo();
//        int strength = info.getRssi();
//        int speed = info.getLinkSpeed();
//        String units = WifiInfo.LINK_SPEED_UNITS;
//        String ssid = info.getSSID();

        List<ScanResult> results = wm.getScanResults();
//        String otherwifi = "The existing network is: \n\n";
//        for (ScanResult result : results) {
//            otherwifi += result.SSID  + ":" + result.level + "\n";
//        }
//        String text = "We are connecting to " + ssid + " at " + String.valueOf(speed) + "   " + String.valueOf(units) + ". Strength : " + strength;
//        otherwifi += "\n\n";
//        otherwifi += text;
        
        return results;
    }
    
    public static String getAllInfo(Context context){
        String phoneInfo = null;
        phoneInfo = " -=-=-=-=- 基本信息 -=-=-=-=-";
        phoneInfo += "\n机型: " + getModel();
        phoneInfo += "\n版本号: " + getCode(context);
        phoneInfo += "\n版本名称: " + getName(context);
        phoneInfo += "\nMAC: " + getMac();
        phoneInfo += "\n固件版本: " + getRelease();

        //BOARD 主板
        phoneInfo += "\n -=-=-=-=- 主板 -=-=-=-=-";
        phoneInfo += "\n获取设备基板名称: " + Build.BOARD;
        phoneInfo += "\n获取设备引导程序版本号: " + Build.BOOTLOADER;

        //BRAND 运营商
        phoneInfo += "\n -=-=-=-=- 运营商 -=-=-=-=-";
        phoneInfo += "\n获取设备品牌: " + Build.BRAND;
        phoneInfo += "\n获取设备指令集名称（CPU的类型）: " + Build.CPU_ABI;
        phoneInfo += "\n获取第二个指令集名称: " + Build.CPU_ABI2;

        //DEVICE 驱动
        phoneInfo += "\n -=-=-=-=- 驱动 -=-=-=-=-";
        phoneInfo += "\n获取设备驱动名称: " + Build.DEVICE;

        //DISPLAY Rom的名字 例如 Flyme 1.1.2（魅族rom） &nbsp;JWR66V（Android nexus系列原生4.3rom）
        phoneInfo += "\n -=-=-=-=- Rom -=-=-=-=-";
        phoneInfo += "\n获取设备显示的版本号: " + Build.DISPLAY;

        //指纹
        phoneInfo += "\n -=-=-=-=- 指纹 -=-=-=-=-";
        phoneInfo += "\n设备的唯一标识。由设备的多个信息拼接合成。: " + Build.FINGERPRINT;

        //HARDWARE 硬件
        phoneInfo += "\n -=-=-=-=- 硬件 -=-=-=-=-";
        phoneInfo += "\n设备硬件名称,一般和基板名称一样（BOARD）: " + Build.HARDWARE;
        phoneInfo += "\n设备主机地址: " + Build.HOST;
        phoneInfo += "\n设备版本号: " + Build.ID;

        //MANUFACTURER 生产厂家
        phoneInfo += "\n -=-=-=-=- 生产厂家 -=-=-=-=-";
        phoneInfo += "\n获取设备制造商: " + Build.MANUFACTURER;

        //MODEL 机型
        phoneInfo += "\n -=-=-=-=- 机型 -=-=-=-=-";
        phoneInfo += "\n获取手机的型号 设备名称: " + Build.MODEL;
        phoneInfo += "\n整个产品的名称: " + Build.PRODUCT;
        phoneInfo += "\n无线电固件版本号: " + Build.RADIO;
        phoneInfo += "\n设备标签: " + Build.TAGS;
        phoneInfo += "\n时间: " + Build.TIME;
        phoneInfo += "\n设备版本类型: " + Build.TYPE;
        phoneInfo += "\n设备用户名: " + Build.USER;

        //VERSION.RELEASE 固件版本
        phoneInfo += "\n -=-=-=-=- 固件版本 -=-=-=-=-";
        phoneInfo += "\n获取系统版本字符串: " + Build.VERSION.RELEASE;
        phoneInfo += "\n设备当前的系统开发代号: " + Build.VERSION.CODENAME;

        //VERSION.INCREMENTAL 基带版本
        phoneInfo += "\n -=-=-=-=- 基带版本 -=-=-=-=-";
        phoneInfo += "\n系统源代码控制值: " + Build.VERSION.INCREMENTAL;

        //VERSION.SDK SDK版本
        phoneInfo += "\n -=-=-=-=- SDK版本 -=-=-=-=-";
        phoneInfo += "\n系统的API级别: " + Build.VERSION.SDK;
        phoneInfo += "\n系统的API级别 数字表示: " + Build.VERSION.SDK_INT;

        phoneInfo += "\n -=-=-=-=- 以太网 -=-=-=-=-";
        phoneInfo += "\n以太网MAC: " + getEthernetMacAddress();
        phoneInfo += "\n以太网IP: " + getIPAddress(true);

        WifiManager my_wifiManager = ((WifiManager) context.getSystemService("wifi"));
        DhcpInfo dhcpInfo = my_wifiManager.getDhcpInfo();
        WifiInfo wifiInfo = my_wifiManager.getConnectionInfo();

        phoneInfo += "\n -=-=-=-=- 网络信息 -=-=-=-=- ";
        phoneInfo += "\nipAddress：" + intToIp(dhcpInfo.ipAddress);
        phoneInfo += "\nnetmask：" + intToIp(dhcpInfo.netmask);
        phoneInfo += "\ngateway：" + intToIp(dhcpInfo.gateway);
        phoneInfo += "\nserverAddress：" + intToIp(dhcpInfo.serverAddress);
        phoneInfo += "\ndns1：" + intToIp(dhcpInfo.dns1);
        phoneInfo += "\ndns2：" + intToIp(dhcpInfo.dns2);
        phoneInfo += "\n -=-=-=-=- Wifi信息 -=-=-=-=- ";
        phoneInfo += "\nIpAddress：" + intToIp(wifiInfo.getIpAddress());
        phoneInfo += "\nMacAddress：" + wifiInfo.getMacAddress();

        phoneInfo += "\n -=-=-=-=- WiFi -=-=-=-=-";
        phoneInfo += "\nWiFi MAC: " + wifiInfo.getMacAddress();
        phoneInfo += "\nWiFi IP: " + intToIp(wifiInfo.getIpAddress());

        phoneInfo += "\n -=-=-=-=- 存储容量 -=-=-=-=-";
        phoneInfo += "\nshow: " + showRAMInfo(context) + " | " + showROMInfo();

//        Log.i(TAG, " -=-=-=-=- 2 initData:" + phoneInfo);
        return phoneInfo;
    }

    private static String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }

}
