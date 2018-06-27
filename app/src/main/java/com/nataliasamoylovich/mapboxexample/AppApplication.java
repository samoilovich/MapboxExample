package com.nataliasamoylovich.mapboxexample;

import android.app.Application;

import com.mapbox.mapboxsdk.Mapbox;

public class AppApplication extends Application {

    public static final String MAPBOX_TOKEN = "fakeapitocken";

    @Override
    public void onCreate() {
        super.onCreate();

        Mapbox.getInstance(getApplicationContext(), MAPBOX_TOKEN);
    }
}
