package com.leo.okdownload;

import android.os.Environment;

import com.leo.okdownload.constant.Constants;
import com.leo.okdownload.model.DownloadEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread implements Runnable {
    private DownloadEntry.Status status;
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
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        RandomAccessFile raf = null;
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
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
                    fos.write(buffer, 0, len);
                    listener.onProgressChanged(index, len);
                }
            } else {
                // 异常
                listener.onDownloadError(index, "server error:" + responseCode);
                return;
            }
            listener.onDownloadCompleted(index);
        } catch (IOException e) {
            listener.onDownloadError(index, "io error:" + e.getMessage());
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

    public interface DownloadListener {

        void onProgressChanged(int index, int len);

        void onDownloadError(int index, String s);

        void onDownloadCompleted(int index);
    }

}
