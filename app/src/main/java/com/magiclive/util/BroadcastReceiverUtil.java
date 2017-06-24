package com.magiclive.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.SparseArray;

import com.magiclive.AppApplication;

/**
 * Created by liyanju on 2017/6/23.
 */

public class BroadcastReceiverUtil {

    public static final String ACTION = "action_BroadcastReceiver";

    public static final String KEY = "key";

    private static BroadcastReceiverUtil brUtil = new BroadcastReceiverUtil();

    private MyBroadcastReceiver myBroadcastReceiver;

    private boolean isRegister;

    private SparseArray<IReceiver> receiverArray = new SparseArray();

    public static final int UPDATE_DOWNLOAD_COUNT = 1;

    private BroadcastReceiverUtil() {
        myBroadcastReceiver = new MyBroadcastReceiver();
    }

    public void addReceiver(int key, IReceiver receiver) {
        if (receiverArray.get(key) != null) {
            throw new IllegalArgumentException(" key has exist");
        }
        receiverArray.put(key, receiver);
    }

    public static void sendReceiver(int key, Intent intent) {
        if (intent == null) {
            intent = new Intent();
        }
        intent.setAction(ACTION);
        intent.putExtra(KEY, key);
        AppApplication.getContext().sendBroadcast(intent);
    }

    public static BroadcastReceiverUtil get() {
        return brUtil;
    }

    public void register(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        context.registerReceiver(myBroadcastReceiver, intentFilter);
        isRegister = true;
    }

    public void unRegister(Context context) {
        try {
            receiverArray.clear();
            if (isRegister) {
                context.unregisterReceiver(myBroadcastReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int key = intent.getIntExtra(KEY, 0);
                IReceiver iReceiver = receiverArray.get(key);
                if (iReceiver != null) {
                    iReceiver.onReceive(intent);
                }
            }
        }
    }

    public interface IReceiver {

        void onReceive(Intent intent);
    }
}
