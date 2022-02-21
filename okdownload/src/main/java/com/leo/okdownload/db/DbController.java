package com.leo.okdownload.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.leo.okdownload.model.DownloadEntry;
import com.leo.okdownload.util.LogUtls;

import java.util.ArrayList;

public class DbController {
    public static final String TASK_ID = "taskid";
    public static final String URL = "url";
    public static final String CURRENT_SIZE = "current_size";
    public static final String TOTAL_SIZE = "total_size";
    public static final String FILE_NAME = "file_name";
    public static final String STATUS = "status";
    public static final String _ID = "id";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "downloads";
    private DownloadDbHelper helper;
    private static DbController instance;

    private DbController(Context context) {
        helper = new DownloadDbHelper(context, TABLE_NAME, null, DB_VERSION);
    }

    public static DbController getInstance(Context context){
        if(instance == null){
            instance = new DbController(context);
        }
        return instance;
    }

    public void insertOrUpdate(String taskId, DownloadEntry entry) {
        SQLiteDatabase writableDatabase = helper.getWritableDatabase();
        writableDatabase.beginTransaction();
        ContentValues initialValues = new ContentValues();
        initialValues.put(TASK_ID, taskId);
        initialValues.put(URL, entry.getUrl());
        initialValues.put(CURRENT_SIZE, entry.getCurrentSize());
        initialValues.put(TOTAL_SIZE, entry.getTotalSize());
        initialValues.put(FILE_NAME, entry.getFileName());
        initialValues.put(STATUS, String.valueOf(entry.getStatus()));
        int id = (int) writableDatabase.insertWithOnConflict(TABLE_NAME, null,
                initialValues, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            writableDatabase.update(TABLE_NAME, initialValues, TASK_ID + "=?", new String[]{taskId});
        }
        writableDatabase.setTransactionSuccessful();
        writableDatabase.endTransaction();
    }

    public void insert(DownloadEntry entry) {
        SQLiteDatabase writableDatabase = helper.getWritableDatabase();
        writableDatabase.beginTransaction();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TASK_ID, entry.getTaskId());
        contentValues.put(URL, entry.getUrl());
        contentValues.put(CURRENT_SIZE, entry.getCurrentSize());
        contentValues.put(TOTAL_SIZE, entry.getTotalSize());
        contentValues.put(FILE_NAME, entry.getFileName());
        contentValues.put(STATUS, String.valueOf(entry.getStatus()));
        writableDatabase.insertOrThrow(TABLE_NAME, null, contentValues);
        writableDatabase.setTransactionSuccessful();
    }

    public void update(DownloadEntry entry) {
        SQLiteDatabase writableDatabase = helper.getWritableDatabase();
        writableDatabase.beginTransaction();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TASK_ID, entry.getTaskId());
        contentValues.put(URL, entry.getUrl());
        contentValues.put(CURRENT_SIZE, entry.getCurrentSize());
        contentValues.put(TOTAL_SIZE, entry.getTotalSize());
        contentValues.put(FILE_NAME, entry.getFileName());
        contentValues.put(STATUS, String.valueOf(entry.getStatus()));
        writableDatabase.update(TABLE_NAME, contentValues, TASK_ID + " = ?",
                new String[]{entry.getTaskId()});
        writableDatabase.setTransactionSuccessful();
    }

    public void delete(String taskId) {
        SQLiteDatabase writableDatabase = helper.getWritableDatabase();
        writableDatabase.beginTransaction();
        writableDatabase.delete(TABLE_NAME, TASK_ID + " = ?", new String[]{taskId});
        writableDatabase.setTransactionSuccessful();
    }

    public ArrayList<DownloadEntry> queryAll() {
        SQLiteDatabase readableDatabase = helper.getReadableDatabase();
        ArrayList<DownloadEntry> list;
        try (Cursor cursor = readableDatabase.rawQuery("select * from " + TABLE_NAME, null)) {
            list = new ArrayList<>();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String url = cursor.getString(cursor.getColumnIndex(URL));
                String taskId = cursor.getString(cursor.getColumnIndex(TASK_ID));
                int currentSize = cursor.getInt(cursor.getColumnIndex(CURRENT_SIZE));
                int totalSize = cursor.getInt(cursor.getColumnIndex(TOTAL_SIZE));
                String fileName = cursor.getString(cursor.getColumnIndex(FILE_NAME));
                DownloadEntry.Status status = DownloadEntry.Status.valueOf(cursor.getString(cursor.getColumnIndex(STATUS)));
                DownloadEntry entry = new DownloadEntry(taskId, url, currentSize, totalSize, fileName, status);
                list.add(entry);
            }
        }
        return list;
    }

    public DownloadEntry query(String taskId) {
        SQLiteDatabase readableDatabase = helper.getReadableDatabase();
        DownloadEntry entry = null;
        try (Cursor cursor = readableDatabase.rawQuery("select * from " + TABLE_NAME + " where " + TASK_ID + " = ?", new String[]{taskId})) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String url = cursor.getString(cursor.getColumnIndex(URL));
                int currentSize = cursor.getInt(cursor.getColumnIndex(CURRENT_SIZE));
                int totalSize = cursor.getInt(cursor.getColumnIndex(TOTAL_SIZE));
                String fileName = cursor.getString(cursor.getColumnIndex(FILE_NAME));
                DownloadEntry.Status status = DownloadEntry.Status.valueOf(cursor.getString(cursor.getColumnIndex(STATUS)));
                entry = new DownloadEntry(taskId, url, currentSize, totalSize, fileName, status);
            }
        }
        return entry;
    }

    public static void createTable(SQLiteDatabase db) {
        String createTableSql = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" (\n" +
                "\t" + TASK_ID + " VARCHAR(128) PRIMARY KEY,\n" +
                "\t" + URL + " VARCHAR(128) ,\n" +
                "\t" + CURRENT_SIZE + " INTEGER ,\n" +
                "\t" + TOTAL_SIZE + " INTEGER,\n" +
                "\t" + FILE_NAME + " VARCHAR(128),\n" +
                "\t" + STATUS + " VARCHAR(512) \n" +
                ")";

        try{
            db.execSQL(createTableSql);
        }catch (Exception e){
            LogUtls.debug("Sql error:"+e.getMessage());
        }
    }
}
