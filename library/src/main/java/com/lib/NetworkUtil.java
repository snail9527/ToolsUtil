package com.lib;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.text.TextUtils;

import java.util.HashSet;

/**
 * 网络状态变化监听
 * 区分21以上和21以下处理
 * 21以上使用ConnectivityManager.registerNetworkCallback
 * 21以下使用广播监听
 */

public class NetworkUtil {
    public static final String TAG = "NetworkUtil";

    private static NetworkSubCompat sNetworkCompat;
    /**
     * 注册网络变化监听
     * @param context Context
     */
    public static void registerCallback(Context context){
        if(sNetworkCompat == null){
            sNetworkCompat = NetworkSubCompat.getNetworkCompat();
        }
        sNetworkCompat.subListener(context);

    }
    //反注册网络变化监听
    public static void unRegister(Context context){
        if(sNetworkCompat != null){
            sNetworkCompat.unSubListener(context);
            sNetworkCompat = null;
        }
    }

    /**
     * 判断当前网络是否可用
     * @return
     */
    public static boolean isNetworkAvailable(){
        if(sNetworkCompat == null){
            return true;
        }else{
            return sNetworkCompat.isNetworkConnect();
        }
    }

    static abstract class NetworkSubCompat {
        public static final String TAG = "NetworkSubCompat";
        void subListener(Context context){

        }
        void unSubListener(Context context){

        }

        boolean isNetworkConnect(){
            return true;
        }
        static NetworkSubCompat getNetworkCompat(){
            //注册监听，分版本处理21以上，和21以下
            if(Build.VERSION.SDK_INT >= 21){
                return new NetworkSubCompatVL();
            }else{
                return new NetworkSubCompatV16();
            }
        }
    }

    @TargetApi(21)
    static class  NetworkSubCompatVL extends NetworkSubCompat{

        private HashSet<String> mNetworkNames = new HashSet<>();
        private ConnectivityManager.NetworkCallback mCallback = new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                mNetworkNames.add(network.toString());
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                mNetworkNames.remove(network.toString());

            }

            @Override
            public void onLosing(Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
            }
        };
        @Override
        public void subListener(Context context) {
            context = context.getApplicationContext();
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm != null){
                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                NetworkRequest request = builder.build();
                cm.registerNetworkCallback(request, mCallback);
            }
        }

        @Override
        public void unSubListener(Context context) {
            context = context.getApplicationContext();
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm != null){
                try {
                    cm.unregisterNetworkCallback(mCallback);
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public boolean isNetworkConnect() {
            return mNetworkNames.size() > 0;
        }
    }
    static class  NetworkSubCompatV16 extends NetworkSubCompat{

        private boolean isConnect = true;
        private BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)){
                    ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    if(mConnectivityManager != null){
                        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                        if (mNetworkInfo != null) {
                            isConnect = mNetworkInfo.isAvailable();
                        }else{
                            isConnect = false;
                        }
                    }
                }
            }
        };
        @Override
        public void subListener(Context context) {
            context.getApplicationContext();
            try {
                context.registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void unSubListener(Context context) {
            context.getApplicationContext();
            try {
                context.unregisterReceiver(mReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean isNetworkConnect() {
            return isConnect;
        }
    }
}
