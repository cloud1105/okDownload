package com.leo.okdownload;

import android.os.Handler;
import android.os.Message;

import com.leo.okdownload.constant.Constants;
import com.leo.okdownload.model.DownloadEntry;
import com.leo.okdownload.util.LogUtls;
import com.leo.okdownload.util.TaskPool;

import java.util.HashMap;
import java.util.UUID;


public class DownloadTask implements ConnectThread.ConnectListener, DownloadThread.DownloadListener {
    private DownloadEntry entry;
    private Handler handler;
    private String id = UUID.randomUUID().toString();
    private DownloadThread[] downloadThreads;
    private long lastStamp = 0;

    public DownloadTask(DownloadEntry entry, Handler handler) {
        this.entry = entry;
        this.handler = handler;
    }

    public void addDownload() {
        LogUtls.info("addDownload");
        start();
    }

    public void pauseDownload() {
        LogUtls.info("pauseDownload");
        entry.setStatus(DownloadEntry.Status.PAUSED);
        //todo  pause connectThread and downloadThread
    }

    public void cancelDownload() {
        LogUtls.info("cancelDownload");
        entry.setStatus(DownloadEntry.Status.CANCELED);
        entry.setCurrentSize(0);
        //todo  cancel connectThread and downloadThread
    }

    private void notify(DownloadEntry entry) {
        Message message = Message.obtain();
        message.obj = entry;
        handler.sendMessage(message);
    }

    public void start() {
        LogUtls.debug("downloadTask start");
        entry.setTaskId(id);
        entry.setStatus(DownloadEntry.Status.CONNECTING);
        notify(entry);
        ConnectThread thread = new ConnectThread(entry.getUrl(), this);
        TaskPool.getInstance().execute(thread);
    }

    @Override
    public synchronized void onConnected(boolean isSupportRange, int contentLength) {
        entry.setStatus(DownloadEntry.Status.DOWNLOADING);
        notify(entry);
        entry.setSupportRange(isSupportRange);
        entry.setTotalSize(contentLength);
        if (isSupportRange) {
            startMultiThread();
        } else {
            startSingleThread();
        }
    }

    private void startSingleThread() {
        LogUtls.debug("start single thread");
    }

    private void startMultiThread() {
        LogUtls.debug("start multiple thread");
        downloadThreads = new DownloadThread[Constants.MAX_DOWNLOAD_THREAD_SIZE];
        /**
         *
         * 分几块 总长度/线程数  totallenth/threadcounts  = block数量
         *
         * 每个线程下载长度计算：
         * 线程号 start位置  end位置
         * 0        0        block-1
         * 1      block      2block-1
         * 2      2block     3block
         *
         * 公式
         * i   i*block+上一次已下载的进度  （i+1）*block-1
         * 最后一个end位置为totallenth
         *
         */
        if (entry.ranges == null) {
            entry.ranges = new HashMap<Integer, Integer>();
            for (int i = 0; i < Constants.MAX_DOWNLOAD_THREAD_SIZE; i++) {
                entry.ranges.put(i, 0);
            }
        }
        int block = entry.getTotalSize() / Constants.MAX_DOWNLOAD_THREAD_SIZE;
        for (int i = 0; i < Constants.MAX_DOWNLOAD_THREAD_SIZE; i++) {
            int progress = entry.getRanges().isEmpty() ? 0 : entry.getRanges().get(i);
            long startPos = i * block + progress;
            long endPos = (i + 1) * block - 1;
            if (i == Constants.MAX_DOWNLOAD_THREAD_SIZE - 1) {
                endPos = entry.getTotalSize();
            }
            downloadThreads[i] = new DownloadThread(entry.getUrl(),
                    i, this, startPos, endPos, entry.getFileName());
            TaskPool.getInstance().execute(downloadThreads[i]);
        }
    }

    @Override
    public synchronized void onError(String message) {
        LogUtls.error(message);
        entry.setStatus(DownloadEntry.Status.ERROR);
        notify(entry);
    }

    @Override
    public synchronized void onProgressChanged(int index, int len) {
        // 更新子线程进度
        if (entry.isSupportRange()) {
            HashMap<Integer, Integer> ranges = entry.getRanges();
            int downloadSize = ranges.get(index) + len;
            ranges.put(index, downloadSize);
        }

        // 计算总进度
        entry.setCurrentSize(entry.getCurrentSize() + len);
        long stamp = System.currentTimeMillis();
        // 超过1s才更新UX进度
        if (stamp - lastStamp > 1000) {
            lastStamp = stamp;
            int percent = (int) (entry.getCurrentSize() * 100l / entry.getTotalSize());
            entry.setPercent(percent);
            notify(entry);
        }
    }

    @Override
    public synchronized void onDownloadError(int index, String s) {
        LogUtls.debug("onDownloadError index:" + index + ",s" + s);
        entry.setStatus(DownloadEntry.Status.ERROR);
        notify(entry);
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        LogUtls.debug("onDownloadCompleted index:" + index);
        if (entry.getTotalSize() == entry.getCurrentSize()) {
            entry.setStatus(DownloadEntry.Status.COMPLETED);
            entry.setPercent(100);
            notify(entry);
        }
    }
}
