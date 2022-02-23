package com.leo.okdownload;

import android.content.Context;

import com.leo.okdownload.db.DbController;
import com.leo.okdownload.model.DownloadEntry;
import com.leo.okdownload.util.LogUtls;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

public class DownloadWatcher extends Observable {
    private static DownloadWatcher watcher;
    private Context context;
    private LinkedHashMap<String, DownloadEntry> map = new LinkedHashMap();

    public DownloadWatcher(Context context) {
        this.context = context;
    }


    public static DownloadWatcher getInstance(Context context) {
        if (watcher == null) {
            watcher = new DownloadWatcher(context);
        }
        return watcher;
    }

    public void notify(DownloadEntry entry) {
        LogUtls.info("updateDownloadStatus status: " + entry.getStatus());
        map.put(entry.getTaskId(), entry);
        DbController.getInstance(context).insertOrUpdate(entry.getTaskId(), entry);
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

    public void putEntryMap(String taskId, DownloadEntry entry) {
        map.put(taskId, entry);
    }

    public DownloadEntry queryDownloadEntry(String taskId) {
        return map.get(taskId);
    }

    public void registerCallback(DownloadCallback callback) {
        addObserver(callback);
    }

    public void unregisterCallback(DownloadCallback callback) {
        deleteObserver(callback);
    }
}
