package com.nataliasamoylovich.mapboxexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.nataliasamoylovich.mapboxexample.download.DownloadContract;
import com.nataliasamoylovich.mapboxexample.download.DownloadPresenter;

public class MainActivity extends AppCompatActivity implements DownloadContract.View {

    private final String TAG = MainActivity.class.getSimpleName();

    private View progressView;
    private TextView progressTextView;

    private DownloadContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVariables();
        prepareViews();
    }

    protected void initVariables() {
        presenter = new DownloadPresenter(getBaseContext(), this);
    }

    protected void prepareViews() {
        prepareDownloadBelarusButton();
        prepareRemoveBelarusButton();
        prepareDownloadMinskPartlyButton();
        prepareStartMapButton();

        progressView = findViewById(R.id.progress_view);
        progressTextView = findViewById(R.id.progress_text);
    }

    private void prepareDownloadBelarusButton() {
        Button downloadBelarusButton = findViewById(R.id.btn_download_belarus);
        downloadBelarusButton.setOnClickListener(view -> {
            showProgress();
            presenter.downloadBelarusMap(getBaseContext());
        });
    }

    private void prepareRemoveBelarusButton() {
        Button removeBelarusButton = findViewById(R.id.btn_remove_belarus);
        removeBelarusButton.setOnClickListener(view -> {
            showProgress();
            presenter.removeBelarusMap();
        });
    }

    private void prepareDownloadMinskPartlyButton() {
        Button downloadMinskPartlyButton = findViewById(R.id.btn_download_minsk);
        downloadMinskPartlyButton.setOnClickListener(view -> {
            showProgress();
            presenter.downloadMinskMap(getBaseContext());
        });
    }

    private void prepareStartMapButton() {
        Button startMapButton = findViewById(R.id.btn_start_map);
        startMapButton.setOnClickListener(view -> {
            startActivity(new Intent(getBaseContext(), MapActivity.class));
        });
    }

    @Override
    public void onDownloadBelarusMap() {
        hideProgress();
        showToast(R.string.belarus_downloaded);
    }

    @Override
    public void onDownloadMinskMap() {
        hideProgress();
        showToast(R.string.minsk_downloaded);
    }

    @Override
    public void updateRegionDownloadingStatus(OfflineRegionStatus status) {
        updateDownloadingProgressDialog(status);
        if (status.isComplete()) {
            hideProgress();
            progressTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDeletedBelarusRegion() {
        hideProgress();
        showToast(R.string.belarus_removed);
    }

    @Override
    public void onError() {
        hideProgress();
        showToast(R.string.error_common_mes);
    }

    @Override
    public void onError(String message) {
        hideProgress();
        Log.d(TAG, message);
        showToast(message);
    }

    private void updateDownloadingProgressDialog(OfflineRegionStatus status) {
        // Calculate the download percentage
        double percentage = (status.getRequiredResourceCount() >= 0)
                ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                0.0;
        Log.d(TAG, "Downloading map progress is " + percentage);
        progressTextView.setText(percentage + "%");
        if (progressTextView.getVisibility() == View.GONE) {
            progressTextView.setVisibility(View.VISIBLE);
        }
    }

    private void showProgress() {
        progressView.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressView.setVisibility(View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(int messageResId) {
        Toast.makeText(getBaseContext(), messageResId, Toast.LENGTH_SHORT).show();
    }
}
