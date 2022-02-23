package com.leo.test.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.leo.okdownload.DownloadCallback;
import com.leo.okdownload.DownloadManager;
import com.leo.okdownload.DownloadWatcher;
import com.leo.okdownload.model.DownloadEntry;
import com.leo.test.R;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class MultiThreadDownloadActivity extends Activity {
    private DownloadManager downloadManager;
//    private final String url = "http://gh-game.oss-cn-hangzhou.aliyuncs.com/1434794302961350.apk";
    private final String url = "https://gdown.baidu.com/data/wisegame/c6512dcc12e969c9/3391c6512dcc12e969c906c9b615b2c5.apk";
    private DownloadEntry entry = new DownloadEntry(url,"123.apk");
    private TextView txvInfo;
    private final DownloadCallback callback = new DownloadCallback() {
        @Override
        public void refreshUi(DownloadEntry entry) {
             MultiThreadDownloadActivity.this.entry = entry;
             if(txvInfo != null){
                 txvInfo.setText(entry.toString());
             }
        }
    };
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_multi);
        txvInfo = findViewById(R.id.txv_info);
        txvInfo.setText(entry.toString());
        downloadManager = DownloadManager.getInstance(this);
        verifyStoragePermissions(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DownloadWatcher.getInstance(this).registerCallback(callback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DownloadWatcher.getInstance(this).unregisterCallback(callback);
    }

    public void startDownload(View view) {
        downloadManager.startDownload(entry);
    }

    public void startPause(View view) {
    }

    public void startCancel(View view) {
    }
}
