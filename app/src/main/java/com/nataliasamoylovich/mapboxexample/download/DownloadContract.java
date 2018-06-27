package com.nataliasamoylovich.mapboxexample.download;

import android.content.Context;

import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

public interface DownloadContract {

    interface View {

        void onDownloadBelarusMap();

        void onDownloadMinskMap();

        void updateRegionDownloadingStatus(OfflineRegionStatus status);

        void onDeletedBelarusRegion();

        void onError();

        void onError(String message);
    }

    interface Presenter {

        void downloadBelarusMap(Context context);

        void removeBelarusMap();

        void downloadMinskMap(Context context);
    }
}
