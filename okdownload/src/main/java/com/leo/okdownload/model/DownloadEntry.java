package com.leo.okdownload.model;

import java.io.Serializable;

import androidx.annotation.Nullable;

public class DownloadEntry implements Serializable {
    private String id;
    private String url;
    private String taskId;
    private int currentSize;
    private int totalSize;
    private String fileName;
    private Status status;

    public DownloadEntry(String taskId,String url, int currentSize, int totalSize, String fileName, Status status) {
        this.url = url;
        this.taskId = taskId;
        this.currentSize = currentSize;
        this.totalSize = totalSize;
        this.fileName = fileName;
        this.status = status;
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

    public enum Status {
        IDLE,
        WAIT,
        DOWNLOADING,
        PAUSED,
        COMPLETED,
        CANCELED;
    }

    @Override
    public int hashCode() {
        return taskId.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DownloadEntry)) {
            return false;
        } else {
            return taskId.equals(((DownloadEntry) obj).getTaskId());
        }
    }
}
