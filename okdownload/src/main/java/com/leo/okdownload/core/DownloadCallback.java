package com.leo.okdownload.core;

import com.leo.okdownload.model.DownloadEntry;

import java.util.Observable;
import java.util.Observer;

public abstract class DownloadCallback implements Observer {
    @Override
    public void update(Observable observable, Object o) {
        if(o instanceof DownloadEntry){
           refreshUi((DownloadEntry)o);
        }
    }

    public abstract void refreshUi(DownloadEntry entry);
}
