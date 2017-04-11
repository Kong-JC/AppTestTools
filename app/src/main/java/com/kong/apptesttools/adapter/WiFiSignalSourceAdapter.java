package com.kong.apptesttools.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kong.apptesttools.R;
import com.kong.apptesttools.utils.Utils;

import java.util.List;

/**
 * Created by Kong on 2017/3/24.
 */

public class WiFiSignalSourceAdapter extends RecyclerView.Adapter<WiFiSignalSourceAdapter.ViewHolder> {

    private static final String TAG = "WiFiSignalSourceAdapter";

    private List<ScanResult> mScanResultList;
    private Context mContext;
    private static long time = 0;
    private static final long outTime = 5 * 1000;
    private boolean isRun;
    private TextView tv_wifi_connection_state_signal_source;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                notifyDataSetChanged();
                if (tv_wifi_connection_state_signal_source != null && Utils.getWiFiIsOpen(mContext)) {
                    tv_wifi_connection_state_signal_source.setText(mContext.getResources().getString(R.string.open) + " 共有 " + Utils.getWiFiSignalSourceList(mContext).size() + " 个信号源");
                    tv_wifi_connection_state_signal_source.setTextColor(mContext.getResources().getColor(R.color.colorGreen));
                }
            }
        }
    };

    public WiFiSignalSourceAdapter(Context mContext) {
        this.mContext = mContext;
        this.mScanResultList = Utils.getWiFiSignalSourceList(mContext);
        time = System.currentTimeMillis();
        isRun = true;
        new Thread(new UpdateData()).start();
    }

    public void setTextView(TextView mTextView) {
        this.tv_wifi_connection_state_signal_source = mTextView;
    }

    public void setRun(boolean run) {
        isRun = run;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wifi_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ScanResult scanResult = mScanResultList.get(position);
        holder.tv_wifi_name.setText(scanResult.SSID);
        holder.tv_source_intensity.setText(scanResult.level + "");
        if (scanResult.level >= 0 && scanResult.level <= 50){
            holder.iv_source_icon.setImageResource(R.drawable.wifi_icon_4);
        } else if (scanResult.level <= 0 && scanResult.level >= -50) {
            holder.iv_source_icon.setImageResource(R.drawable.wifi_icon_3);
        } else if (scanResult.level <= -51 && scanResult.level >= -80) {
            holder.iv_source_icon.setImageResource(R.drawable.wifi_icon_2);
        } else if (scanResult.level <= -81 && scanResult.level >= -200) {
            holder.iv_source_icon.setImageResource(R.drawable.wifi_icon_1);
        }
        holder.rl_item_view.setFocusable(true);
    }

    @Override
    public int getItemCount() {
        return mScanResultList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_wifi_name, tv_source_intensity;
        ImageView iv_source_icon;
        RelativeLayout rl_item_view;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_wifi_name = (TextView) itemView.findViewById(R.id.tv_wifi_name);
            tv_source_intensity = (TextView) itemView.findViewById(R.id.tv_source_intensity);
            iv_source_icon = (ImageView) itemView.findViewById(R.id.iv_source_icon);
            rl_item_view = (RelativeLayout) itemView.findViewById(R.id.rl_item_view);
        }
    }

    class UpdateData implements Runnable {
        @Override
        public void run() {
            while (isRun) {
                if (System.currentTimeMillis() - time > outTime) {
                    time = System.currentTimeMillis();
                    mScanResultList = Utils.getWiFiSignalSourceList(mContext);
                    handler.sendEmptyMessage(1);
                }
            }
        }
    }

}
