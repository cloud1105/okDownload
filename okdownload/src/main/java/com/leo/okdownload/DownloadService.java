package com.leo.okdownload;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.leo.okdownload.constant.Constants;
import com.leo.okdownload.model.DownloadEntry;
import com.leo.okdownload.util.LogUtls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DownloadService extends Service {
    private Map<String, DownloadTask> downloadTaskMap = new HashMap<>();
    private BlockingDeque<DownloadEntry> waitingDeque = new LinkedBlockingDeque<>();

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            LogUtls.debug("receive msg");
            DownloadEntry entry = (DownloadEntry) msg.obj;
            LogUtls.debug("id:" + entry.getTaskId());
            switch (entry.getStatus()) {
                case PAUSED:
                case CANCELED:
                case COMPLETED:
                    DownloadEntry newEntry = waitingDeque.poll();
                    if (newEntry != null) {
                        startDownload(newEntry);
                    }
                    break;
                default:
                    break;
            }
            DownloadWatcher.getInstance(DownloadService.this).updateDownloadStatus(entry);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return START_NOT_STICKY;
        }
        DownloadEntry entry = (DownloadEntry) intent.getSerializableExtra(Constants.EXTRA_DOWNLOAD_ENTRY);
        String action = intent.getAction();
        doAction(action, entry);
        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(String action, DownloadEntry entry) {
        switch (action) {
            case Constants.START_DOWNLOAD:
                startDownload(entry);
                break;
            case Constants.PAUSE_DOWNLOAD:
                pauseDonwload(entry);
                break;
            case Constants.CANCEL_DOWNLOAD:
                cancelDownload(entry);
                break;
            case Constants.RESUME_DOWNLOAD:
                resumeDownload(entry);
                break;
            case Constants.RECOVER_ALL:
                recoverAll();
                break;
            case Constants.PAUSE_ALL:
                pauseAll();
                break;
            default:
                break;
        }
    }

    private void pauseAll() {
        while (waitingDeque.iterator().hasNext()) {
            DownloadEntry entry = waitingDeque.poll();
            if (entry != null) {
                changeDownloadStatus(entry, DownloadEntry.Status.PAUSED);
            }
        }
        for(Map.Entry<String, DownloadTask> entry: downloadTaskMap.entrySet()){
            entry.getValue().pauseDownload();
        }
        downloadTaskMap.clear();
    }

    private void recoverAll() {
        ArrayList<DownloadEntry> arrayList = DownloadWatcher.getInstance(this).queryAllRecoverableEntries();
        for (DownloadEntry entry : arrayList) {
            startDownload(entry);
        }
    }

    private void resumeDownload(DownloadEntry entry) {
        startDownload(entry);
    }

    private void cancelDownload(DownloadEntry entry) {
        DownloadTask task = downloadTaskMap.remove(entry.getTaskId());
        if (task != null) {
            task.cancelDownload();
        } else {
            changeDownloadStatus(entry, DownloadEntry.Status.CANCELED);
        }
        waitingDeque.remove(entry);
    }

    private void changeDownloadStatus(DownloadEntry entry, DownloadEntry.Status status) {
        entry.setStatus(status);
        DownloadWatcher.getInstance(this).updateDownloadStatus(entry);
    }

    private void pauseDonwload(DownloadEntry entry) {
        DownloadTask task = downloadTaskMap.remove(entry.getTaskId());
        if (task != null) {
            task.pauseDownload();
        } else {
            changeDownloadStatus(entry, DownloadEntry.Status.PAUSED);
        }
        waitingDeque.remove(entry);
    }

    private void startDownload(DownloadEntry entry) {
        if (downloadTaskMap.size() >= Constants.MAX_DOWNLOAD_THREAD_SIZE) {
            waitingDeque.offer(entry);
            changeDownloadStatus(entry, DownloadEntry.Status.WAIT);
        } else {
            waitingDeque.remove(entry);
            DownloadTask task = new DownloadTask(entry, handler);
            task.addDownload();
            downloadTaskMap.put(entry.getTaskId(), task);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
