package com.leo.okdownload;


import com.leo.okdownload.constant.Constants;
import com.leo.okdownload.util.LogUtls;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectThread implements Runnable {
    private String url;
    private ConnectListener listener;
    private boolean isRunning;

    public ConnectThread(String url, ConnectListener listener) {
        this.url = url;
        this.listener = listener;
    }

    @Override
    public void run() {
        isRunning = true;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setConnectTimeout(Constants.CONNECT_TIMEOUT);
            connection.setReadTimeout(Constants.READ_TIMEOUT);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            LogUtls.debug("responseCode:" + responseCode + " contentLength:" + contentLength);
            boolean isSupportRange = false;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String ranges = connection.getHeaderField("Accept-Ranges");
                if ("bytes".equals(ranges)) {
                    isSupportRange = true;
                }
                listener.onConnected(isSupportRange, contentLength);
            } else {
                listener.onConnectFailed("server error:" + responseCode);
            }
            isRunning = false;
        } catch (IOException e) {
            listener.onConnectFailed(e.getMessage());
            isRunning = false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isRunning() {
      return isRunning;
    }

    public void cancel() {
      Thread.currentThread().interrupt();
    }

    public interface ConnectListener {
        void onConnected(boolean isSupportRange, int contentLength);

        void onConnectFailed(String message);
    }
}
