package com.leo.okdownload.core;

import android.content.Context;
import android.content.Intent;

import com.leo.okdownload.constant.Constants;
import com.leo.okdownload.db.DbController;
import com.leo.okdownload.model.DownloadEntry;
import com.leo.okdownload.util.LogUtls;

import java.util.ArrayList;

public class DownloadManager {
    public static final long HALF_SECOND = 500L;
    private static DownloadManager instance;
    private Context context;
    private long lastTime = 0L;

    private DownloadManager(Context context) {
        this.context = context;
        initMapCacheFromDb(context);
    }

    private void initMapCacheFromDb(Context context) {
        ArrayList<DownloadEntry> downloadEntries = DbController.getInstance(context).queryAll();
        for (DownloadEntry entry : downloadEntries) {
            DownloadWatcher.getInstance(context).putEntryMap(entry.getTaskId(), entry);
        }
    }

    public static DownloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new DownloadManager(context);
        }
        return instance;
    }


    public void startDownload(DownloadEntry downloadEntry) {
        if (isShortInterval()) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.EXTRA_DOWNLOAD_ENTRY, downloadEntry);
        intent.setAction(Constants.START_DOWNLOAD);
        context.startService(intent);
    }

    public void pauseDownload(DownloadEntry downloadEntry) {
        if (isShortInterval()) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.EXTRA_DOWNLOAD_ENTRY, downloadEntry);
        intent.setAction(Constants.PAUSE_DOWNLOAD);
        context.startService(intent);

    }

    public void resumeDownload(DownloadEntry downloadEntry) {
        if (isShortInterval()) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.EXTRA_DOWNLOAD_ENTRY, downloadEntry);
        intent.setAction(Constants.RESUME_DOWNLOAD);
        context.startService(intent);
    }

    public void cancelDownload(DownloadEntry downloadEntry) {
        if (isShortInterval()) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.EXTRA_DOWNLOAD_ENTRY, downloadEntry);
        intent.setAction(Constants.CANCEL_DOWNLOAD);
        context.startService(intent);
    }

    public void recoverAll() {
        if (isShortInterval()) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(Constants.RECOVER_ALL);
        context.startService(intent);
    }

    public void pauseAll() {
        if (isShortInterval()) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(Constants.PAUSE_ALL);
        context.startService(intent);
    }

    private boolean isShortInterval() {
        long now = System.currentTimeMillis();
        if ((now - lastTime) < HALF_SECOND) {
            LogUtls.info("shortClick return");
            return true;
        } else {
            lastTime = now;
            return false;
        }
    }

    public DownloadEntry queryDownloadEntry(String taskId) {
        return DownloadWatcher.getInstance(context).queryDownloadEntry(taskId);
    }
}
