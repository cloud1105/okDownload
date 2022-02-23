package com.leo.okdownload.model;

import java.io.Serializable;
import java.util.HashMap;

import androidx.annotation.Nullable;

public class DownloadEntry implements Serializable {
    private String url;
    private String taskId;
    private int currentSize;
    private int totalSize;
    private String fileName;
    private Status status;
    private int percent;
    private boolean isSupportRange;
    // 每一段线程下载的进度 key：线程的index  value：进度
    public HashMap<Integer, Integer> ranges;

    public DownloadEntry(String taskId, String url, int currentSize, int totalSize, String fileName, Status status) {
        this.url = url;
        this.taskId = taskId;
        this.currentSize = currentSize;
        this.totalSize = totalSize;
        this.fileName = fileName;
        this.status = status;
    }

    public DownloadEntry(String url, String fileName) {
        this.url = url;
        this.fileName = fileName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public HashMap<Integer, Integer> getRanges() {
        return ranges;
    }

    public void setRanges(HashMap<Integer, Integer> ranges) {
        this.ranges = ranges;
    }

    public boolean isSupportRange() {
        return isSupportRange;
    }

    public void setSupportRange(boolean supportRange) {
        isSupportRange = supportRange;
    }

    public void reset() {
        //todo 清除下载数据
    }

    public enum Status {
        IDLE,
        WAIT,
        DOWNLOADING,
        PAUSED,
        COMPLETED,
        CANCELED,
        CONNECTING,
        ERROR;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DownloadEntry)) {
            return false;
        } else {
            return url.equals(((DownloadEntry) obj).getUrl());
        }
    }

    @Override
    public String toString() {
        return "DownloadEntry{" +
                ", taskId='" + taskId + '\'' +
                ", currentSize=" + currentSize +
                ", totalSize=" + totalSize +
                ", fileName='" + fileName + '\'' +
                ", status=" + status +
                '}';
    }
}
