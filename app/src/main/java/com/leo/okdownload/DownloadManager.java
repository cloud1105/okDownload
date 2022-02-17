package com.leo.okdownload;

import android.content.Context;
import android.content.Intent;

import com.leo.okdownload.constant.Constants;
import com.leo.okdownload.model.DownloadEntry;

public class DownloadManager {
    private static DownloadManager instance;

    private DownloadManager() {
    }

    public static DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }
        return instance;
    }


    public void startDownload(Context context, DownloadEntry downloadEntry) {
        Intent intent = new Intent(context,DownloadService.class);
        intent.putExtra(Constants.EXTRA_DOWNLOAD_ENTRY,downloadEntry);
        intent.setAction(Constants.START_DOWNLOAD);
        context.startService(intent);
    }

    public void pauseDownload(Context context, DownloadEntry downloadEntry) {
        Intent intent = new Intent(context,DownloadService.class);
        intent.putExtra(Constants.EXTRA_DOWNLOAD_ENTRY,downloadEntry);
        intent.setAction(Constants.PAUSE_DOWNLOAD);
        context.startService(intent);

    }

    public void resumeDownload(Context context, DownloadEntry downloadEntry) {
        Intent intent = new Intent(context,DownloadService.class);
        intent.putExtra(Constants.EXTRA_DOWNLOAD_ENTRY,downloadEntry);
        intent.setAction(Constants.RESUME_DOWNLOAD);
        context.startService(intent);
    }

    public void cancelDownload(Context context, DownloadEntry downloadEntry) {
        Intent intent = new Intent(context,DownloadService.class);
        intent.putExtra(Constants.EXTRA_DOWNLOAD_ENTRY,downloadEntry);
        intent.setAction(Constants.CANCEL_DOWNLOAD);
        context.startService(intent);
    }
}
