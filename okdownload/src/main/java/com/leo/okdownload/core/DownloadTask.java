package com.leo.okdownload.core;

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
    private DownloadEntry.Status[] downloadStatus;
    private ConnectThread connectThread;
    private long lastStamp = 0;
    private boolean isPause, isCancel;

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
        isPause = true;
        entry.setStatus(DownloadEntry.Status.PAUSED);
        // pause connectThread & downloadThread
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel();
        }
        if (downloadThreads == null || downloadThreads.length <= 0) {
            return;
        }
        for (int i = 0; i < downloadThreads.length; i++) {
            DownloadThread thread = downloadThreads[i];
            if (thread != null && thread.isRunning()) {
                if (entry.isSupportRange()) {
                    thread.pause();
                } else {
                    thread.cancel();
                }
            }
        }
    }

    public void cancelDownload() {
        LogUtls.info("cancelDownload");
        isCancel = true;
        entry.setStatus(DownloadEntry.Status.CANCELED);
        entry.setCurrentSize(0);
        // cancel connectThread & downloadThread
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel();
        }
        if (downloadThreads == null || downloadThreads.length <= 0) {
            return;
        }
        for (int i = 0; i < downloadThreads.length; i++) {
            DownloadThread thread = downloadThreads[i];
            if (thread != null && thread.isRunning()) {
                thread.cancel();
            }
        }
    }

    private void notify(DownloadEntry entry) {
        Message message = Message.obtain();
        message.obj = entry;
        handler.sendMessage(message);
        // 睡10ms防止线程发送间隔时间太短，消息队列阻塞
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        LogUtls.debug("downloadTask start");
        if (entry.getTotalSize() > 0) {
            // 本地有数据，已经获取过文件长度，直接下载即可
            startDownload();
        } else {
            entry.setTaskId(id);
            entry.setStatus(DownloadEntry.Status.CONNECTING);
            notify(entry);
            connectThread = new ConnectThread(entry.getUrl(), this);
            TaskPool.getInstance().execute(connectThread);
        }
    }

    @Override
    public synchronized void onConnected(boolean isSupportRange, int contentLength) {
        entry.setStatus(DownloadEntry.Status.DOWNLOADING);
        notify(entry);
        entry.setSupportRange(isSupportRange);
        entry.setTotalSize(contentLength);
        startDownload();
    }

    private void startDownload() {
        if (entry.isSupportRange()) {
            startMultiThread();
        } else {
            startSingleThread();
        }
    }

    private void startSingleThread() {
        LogUtls.debug("start single thread");
        entry.setStatus(DownloadEntry.Status.DOWNLOADING);
        notify(entry);

        downloadThreads = new DownloadThread[1];
        downloadStatus = new DownloadEntry.Status[1];
        downloadThreads[0] = new DownloadThread(entry.getUrl(), 0, this, 0,
                0, entry.getFileName());
        TaskPool.getInstance().execute(downloadThreads[0]);
    }

    private void startMultiThread() {
        LogUtls.debug("start multiple thread");
        entry.setStatus(DownloadEntry.Status.DOWNLOADING);
        notify(entry);
        downloadThreads = new DownloadThread[Constants.MAX_DOWNLOAD_THREAD_SIZE];
        downloadStatus = new DownloadEntry.Status[Constants.MAX_DOWNLOAD_THREAD_SIZE];
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
            if (startPos < endPos) {
                downloadThreads[i] = new DownloadThread(entry.getUrl(),
                        i, this, startPos, endPos, entry.getFileName());
                downloadStatus[i] = DownloadEntry.Status.DOWNLOADING;
                TaskPool.getInstance().execute(downloadThreads[i]);
            } else {
                downloadStatus[i] = DownloadEntry.Status.COMPLETED;
            }
        }
    }

    @Override
    public synchronized void onConnectFailed(String message) {
        LogUtls.error(message);
        if (isPause || isCancel) {
            entry.setStatus(isPause ? DownloadEntry.Status.PAUSED : DownloadEntry.Status.CANCELED);
        } else {
            entry.setStatus(DownloadEntry.Status.ERROR);
        }
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
            int percent = (int) (entry.getCurrentSize() * 100L / entry.getTotalSize());
            entry.setPercent(percent);
            notify(entry);
        }
    }

    @Override
    public synchronized void onDownloadError(int index, String s) {
        LogUtls.debug("onDownloadError index:" + index + ",s" + s);
        entry.setStatus(DownloadEntry.Status.ERROR);
        notify(entry);
        //todo 有一个线程error，需要cancel其他线程
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        LogUtls.debug("onDownloadCompleted index:" + index);
        downloadStatus[index] = DownloadEntry.Status.COMPLETED;
        for (int i = 0; i < downloadStatus.length; i++) {
            if (downloadStatus[i] != DownloadEntry.Status.COMPLETED) {
                // 有一个线程没下载完就不往下处理
                return;
            }
        }
        if (entry.getTotalSize() > 0 && entry.getCurrentSize() != entry.getTotalSize()) {
            //下载出现异常，文件不完整,要清除，重新下载
            entry.setStatus(DownloadEntry.Status.ERROR);
            entry.reset();
            notify(entry);
            LogUtls.debug("DownloadTask==>onDownloadCompleted()#####file is error, reset it!!!!!");
        } else {
            //文件下载完成，没有异常
            entry.setStatus(DownloadEntry.Status.COMPLETED);
            entry.setPercent(100);
            notify(entry);
            LogUtls.debug("DownloadTask==>onDownloadCompleted()#####file is ok!!!!!");
        }
    }

    @Override
    public void onDownloadPaused(int index) {
        downloadStatus[index] = DownloadEntry.Status.PAUSED;
        for (int i = 0; i < downloadStatus.length; i++) {
            if (downloadStatus[i] != DownloadEntry.Status.PAUSED &&
                    downloadStatus[i] != DownloadEntry.Status.COMPLETED) {
                return;
            }
        }
        entry.setStatus(DownloadEntry.Status.PAUSED);
        notify(entry);
    }

    @Override
    public void onDownloadCanceled(int index) {
        downloadStatus[index] = DownloadEntry.Status.CANCELED;
        for (int i = 0; i < downloadStatus.length; i++) {
            if (downloadStatus[i] != DownloadEntry.Status.CANCELED &&
                    downloadStatus[i] != DownloadEntry.Status.COMPLETED) {
                return;
            }
        }
        entry.setStatus(DownloadEntry.Status.CANCELED);
        entry.reset();
        notify(entry);
    }
}
