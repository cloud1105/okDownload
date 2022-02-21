package com.leo.okdownload;

import com.leo.okdownload.model.DownloadEntry;
import com.leo.okdownload.util.LogUtls;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

public class DownloadWatcher extends Observable {
    private static DownloadWatcher watcher;
    private LinkedHashMap<String, DownloadEntry> map = new LinkedHashMap();

    public static DownloadWatcher getInstance() {
        if (watcher == null) {
            watcher = new DownloadWatcher();
        }
        return watcher;
    }

    public void updateDownloadStatus(DownloadEntry entry) {
        LogUtls.info("updateDownloadStatus status: " + entry.getStatus());
        map.put(entry.getTaskId(), entry);
        setChanged();
        notifyObservers(entry);
    }

    public ArrayList<DownloadEntry> queryAllRecoverableEntries() {
        ArrayList<DownloadEntry> list = new ArrayList();
        for (Map.Entry<String, DownloadEntry> entry : map.entrySet()) {
            if (entry.getValue().getStatus() == DownloadEntry.Status.PAUSED) {
                list.add(entry.getValue());
            }
        }
        return list;
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
