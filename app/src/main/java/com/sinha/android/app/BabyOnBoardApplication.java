package com.sinha.android.app;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import timber.log.Timber;

public class BabyOnBoardApplication extends Application {

    private static BabyOnBoardApplication sBabyOnBoardApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sBabyOnBoardApplication = BabyOnBoardApplication.this;
        Timber.plant(new Timber.DebugTree());
    }

    public boolean hasNetworkConnection() {
        NetworkInfo activeNetwork = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static BabyOnBoardApplication getInstance() {
        return sBabyOnBoardApplication;
    }
}
