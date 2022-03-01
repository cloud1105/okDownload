package com.leo.okdownload.core;

import android.os.Environment;

import com.leo.okdownload.constant.Constants;
import com.leo.okdownload.model.DownloadEntry;
import com.leo.okdownload.util.LogUtls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread implements Runnable {
    private final boolean isSingleDownload;
    private DownloadEntry.Status status;
    private boolean isPause,isCancel;
    private final String fileName;
    private String url;
    private DownloadListener listener;
    private long startPos;
    private long endPos;
    private int index;
    private String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator;

    public DownloadThread(String url, int index, DownloadListener listener, long startPos, long endPos, String filename) {
        this.url = url;
        this.index = index;
        this.listener = listener;
        this.startPos = startPos;
        this.endPos = endPos;
        this.fileName = filename;
        if (startPos == 0 && endPos == 0) {
            isSingleDownload = true;
        } else {
            isSingleDownload = false;
        }
    }

    @Override
    public void run() {
        status = DownloadEntry.Status.DOWNLOADING;
        HttpURLConnection connection = null;
        RandomAccessFile raf = null;
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            // 区分单线程还是多线程断点续传
            if(!isSingleDownload){
                connection.setRequestProperty("Range","bytes=" + startPos + "-" + endPos);
            }
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setConnectTimeout(Constants.CONNECT_TIMEOUT);
            connection.setReadTimeout(Constants.READ_TIMEOUT);
            int responseCode = connection.getResponseCode();
            File file = new File(path+fileName);
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                //支持断点下载
                byte[] buffer = new byte[2048];
                int len = -1;
                raf = new RandomAccessFile(file, "rw");
                raf.seek(startPos);
                is = connection.getInputStream();

                while ((len = is.read(buffer)) != -1) {
                    if(isPause || isCancel){
                        LogUtls.info("is pause or cancel by user");
                        break;
                    }
                    raf.write(buffer, 0, len);
                    listener.onProgressChanged(index, len);
                }
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                // 不支持断点续传
                byte[] buffer = new byte[2048];
                int len = -1;
                fos = new FileOutputStream(file);
                is = connection.getInputStream();
                while ((len = is.read(buffer)) != -1) {
                    if(isPause || isCancel){
                        LogUtls.info("is pause or cancel by user");
                        break;
                    }
                    fos.write(buffer, 0, len);
                    listener.onProgressChanged(index, len);
                }
            } else {
                // 异常
                listener.onDownloadError(index, "server error:" + responseCode);
                return;
            }
            if(isPause){
               status = DownloadEntry.Status.PAUSED;
               listener.onDownloadPaused(index);
            }else if(isCancel){
                status = DownloadEntry.Status.CANCELED;
                listener.onDownloadCanceled(index);
            }else {
                listener.onDownloadCompleted(index);
            }
        } catch (IOException e) {
            if(isPause){
                status = DownloadEntry.Status.PAUSED;
                listener.onDownloadPaused(index);
            }else if(isCancel){
                status = DownloadEntry.Status.CANCELED;
                listener.onDownloadCanceled(index);
            }else {
                listener.onDownloadError(index, "io error:" + e.getMessage());
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void pause() {
        isPause = true;
        Thread.currentThread().interrupt();
    }

    public void cancel(){
        isCancel = true;
        Thread.currentThread().interrupt();
    }

    public boolean isRunning() {
        return status == DownloadEntry.Status.DOWNLOADING;
    }

    public interface DownloadListener {

        void onProgressChanged(int index, int len);

        void onDownloadError(int index, String s);

        void onDownloadCompleted(int index);

        void onDownloadPaused(int index);

        void onDownloadCanceled(int index);
    }

}
