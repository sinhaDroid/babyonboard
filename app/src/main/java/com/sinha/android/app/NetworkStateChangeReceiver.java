package com.sinha.android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by deepanshu on 4/6/17.
 */

public class NetworkStateChangeReceiver extends BroadcastReceiver {
    public static final String NETWORK_AVAILABLE_ACTION = "com.sinha.android.app";
    public static final String IS_NETWORK_AVAILABLE = "isNetworkAvailable";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent networkStateIntent = new Intent(NETWORK_AVAILABLE_ACTION);
        networkStateIntent.putExtra(IS_NETWORK_AVAILABLE,  BabyOnBoardApplication.getInstance().hasNetworkConnection());
        LocalBroadcastManager.getInstance(context).sendBroadcast(networkStateIntent);
    }
}
