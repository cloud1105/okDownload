package com.leo.okdownload;

import com.leo.okdownload.model.DownloadEntry;
import com.leo.okdownload.util.LogUtls;

import java.util.Observable;

public class DownloadWatcher extends Observable {
    private static DownloadWatcher watcher;

    public static DownloadWatcher getInstance() {
        if (watcher == null) {
            watcher = new DownloadWatcher();
        }
        return watcher;
    }

    public void updateDownloadStatus(DownloadEntry entry) {
        LogUtls.info("updateDownloadStatus status: "+entry.getStatus());
        setChanged();
        notifyObservers(entry);
    }

    public void notifyThreadReject(DownloadTask task) {
        setChanged();
        notifyObservers(task);
    }

    public void registerCallback(DownloadCallback callback) {
        addObserver(callback);
    }

    public void unregisterCallback(DownloadCallback callback) {
        deleteObserver(callback);
    }
}
