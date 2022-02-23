package com.leo.okdownload;


import com.leo.okdownload.constant.Constants;
import com.leo.okdownload.util.LogUtls;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectThread implements Runnable {
    private String url;
    private ConnectListener listener;

    public ConnectThread(String url, ConnectListener listener) {
        this.url = url;
        this.listener = listener;
    }

    @Override
    public void run() {
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
                listener.onError("server error:" + responseCode);
            }
        } catch (IOException e) {
            listener.onError(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public interface ConnectListener {
        void onConnected(boolean isSupportRange, int contentLength);

        void onError(String message);
    }
}
