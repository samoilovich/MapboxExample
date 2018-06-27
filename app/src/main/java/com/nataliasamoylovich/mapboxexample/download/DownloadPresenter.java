package com.nataliasamoylovich.mapboxexample.download;

import android.content.Context;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.nataliasamoylovich.mapboxexample.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DownloadPresenter implements DownloadContract.Presenter {

    private final String TAG = DownloadPresenter.class.getSimpleName();
    private final int MAP_DEFAULT_MAX_ZOOM = 10;
    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";

    private OfflineManager offlineManager;
    private OfflineRegion downloadingOfflineRegion;
    private DownloadContract.View view;

    protected int offlineRegionStatusCounter;
    private HashMap<Long, OfflineRegion> regions;
    protected ArrayList<Long> regionsIds;

    public DownloadPresenter(Context context, DownloadContract.View view) {
        offlineManager = OfflineManager.getInstance(context);
        this.view = view;
    }

    @Override
    public void downloadBelarusMap(Context context) {
        // Coordinates for Belarus region
        // TODO play with zoom
        downloading(context, 56.368051, 32.960718, 51.072102, 23.000266,
                10, 10, "Belarus");
    }

    @Override
    public void removeBelarusMap() {
        getOfflineRegionsAndRemoveFirst();
    }

    @Override
    public void downloadMinskMap(Context context) {
        downloading(context, 53.9727333432466, 27.451443773653523, 53.8276542131691, 27.636504565440532,
                12, 12, "Minsk");
    }

    private void downloading(Context context, double latMax, double lngMax, double latMin, double lngMin,
                             double maxZoom, double minZoom, String regionName) {
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(latMax, lngMax))
                .include(new LatLng(latMin, lngMin))
                .build();
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                context.getString(R.string.mapbox_style_light),
                latLngBounds,
                minZoom,
                maxZoom,
                context.getResources().getDisplayMetrics().density);

        // Implementation that uses JSON to store offline region name.
        byte[] metadata = getRegionMetadata(regionName);
        // Create the region asynchronously
        offlineManager.createOfflineRegion(definition, metadata, new OfflineManager.CreateOfflineRegionCallback() {
            @Override
            public void onCreate(OfflineRegion offlineRegion) {
                downloadingOfflineRegion = offlineRegion;
                downloadingOfflineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
                // Monitor the download progress using setObserver
                downloadingOfflineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
                    @Override
                    public void onStatusChanged(OfflineRegionStatus status) {
                        view.updateRegionDownloadingStatus(status);
                        if (status.isComplete()) {
                            Log.d(TAG, "Region downloaded successfully.");
                            resetDownloadingOfflineRegion();
                        }
                    }

                    @Override
                    public void onError(OfflineRegionError error) {
                        view.onError(error.getMessage());
                    }

                    @Override
                    public void mapboxTileCountLimitExceeded(long limit) {
                        view.onError("Mapbox tile count limit has been exceeded " + limit);
                    }
                });
            }

            @Override
            public void onError(String error) {
                view.onError(error);
            }
        });
    }

    private byte[] getRegionMetadata(String regionName) {
        byte[] metadata;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_FIELD_REGION_NAME, regionName);
            String json = jsonObject.toString();
            metadata = json.getBytes(JSON_CHARSET);
        } catch (Exception exception) {
            Log.e(TAG, "Failed to encode metadata: " + exception.getMessage());
            metadata = null;
        }
        return metadata;
    }

    private void resetDownloadingOfflineRegion() {
        if (downloadingOfflineRegion != null) {
            downloadingOfflineRegion.setObserver(null);
            downloadingOfflineRegion = null;
        }
    }

    private void getOfflineRegionsAndRemoveFirst() {
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(OfflineRegion[] offlineRegions) {
                handleOfflineRegions(offlineRegions, true);
            }

            @Override
            public void onError(String error) {
                view.onError(error);
            }
        });
    }

    private void handleOfflineRegions(final OfflineRegion[] offlineRegions, final boolean needRegionsIds) {
        regions = new HashMap<>();
        regionsIds = new ArrayList<>();
        offlineRegionStatusCounter = 0;
        if (offlineRegions != null && offlineRegions.length > 0) {
            for (final OfflineRegion offlineRegion : offlineRegions) {
                offlineRegion.getStatus(new OfflineRegion.OfflineRegionStatusCallback() {
                    @Override
                    public void onStatus(OfflineRegionStatus status) {
                        offlineRegionStatusCounter++;
                        if (status.isComplete()) {
                            regions.put(offlineRegion.getID(), offlineRegion);
                            regionsIds.add(offlineRegion.getID());

                        }
                        if (offlineRegionStatusCounter >= offlineRegions.length && regionsIds.size() > 0) {
                            removing(regionsIds.get(0));
                        }
                    }

                    @Override
                    public void onError(String error) {
                        view.onError(error);
                    }
                });
            }
        }
    }

    private void removing(long regionId) {
        OfflineRegion offlineRegion = regions.get(regionId);
        if (offlineRegion == null) {
            view.onError();
            return;
        }
        offlineRegion.delete(new OfflineRegion.OfflineRegionDeleteCallback() {
            @Override
            public void onDelete() {
                view.onDeletedBelarusRegion();
            }

            @Override
            public void onError(String error) {
                view.onError(error);
            }
        });
    }
}
