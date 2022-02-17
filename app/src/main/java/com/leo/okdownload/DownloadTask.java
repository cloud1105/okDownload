package com.leo.okdownload;

import android.os.Handler;
import android.os.Message;

import com.leo.okdownload.model.DownloadEntry;
import com.leo.okdownload.util.LogUtls;
import com.leo.okdownload.util.TaskPool;


public class DownloadTask implements Runnable {
    private DownloadEntry entry;
    private Handler handler;
    private volatile boolean isCancel = false;
    private volatile boolean isPause = false;

    public DownloadTask(DownloadEntry entry, Handler handler) {
        this.entry = entry;
        this.handler = handler;
    }

    public void addDownload() {
        LogUtls.info("addDownload");
        entry.setStatus(DownloadEntry.Status.DOWNLOADING);
        TaskPool.getInstance().execute(this);
    }

    public void pauseDownload() {
        LogUtls.info("pauseDownload");
        isPause = true;
        entry.setStatus(DownloadEntry.Status.PAUSED);
    }

    public void cancelDownload() {
        LogUtls.info("cancelDownload");
        isCancel = true;
        entry.setStatus(DownloadEntry.Status.CANCELED);
        entry.setCurrentSize(0);
    }

    public void resumeDownload() {
        LogUtls.info("resumeDownload");
        isPause = false;
        isCancel = false;
        addDownload();
    }

    private void sendMessage(DownloadEntry entry){
        Message message = Message.obtain();
        message.obj = entry;
        handler.sendMessage(message);
    }

    @Override
    public void run() {
        LogUtls.debug("downloadTask run");
        // todo true download
        for (int i = entry.getCurrentSize(); i <= entry.getTotalSize();) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isPause || isCancel) {
                sendMessage(entry);
                //todo if cancel, delete file
                return;
            }
            i += 1024;
            entry.setCurrentSize(i);
            sendMessage(entry);
        }
        entry.setCurrentSize(entry.getTotalSize());
        entry.setStatus(DownloadEntry.Status.COMPLETED);
        sendMessage(entry);
    }
}
