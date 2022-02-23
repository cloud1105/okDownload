package com.leo.okdownload.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leo.okdownload.model.DownloadEntry;
import com.leo.okdownload.util.LogUtls;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

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
    private static final String PERCENT = "percent";
    private static final String IS_SUPPORTRANGE = "isSupportRange";
    private static final String RANGES = "ranges";
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
        initialValues.put(PERCENT,String.valueOf(entry.getPercent()));
        initialValues.put(IS_SUPPORTRANGE,entry.isSupportRange());
        Gson gson = new Gson();
        String rangesJson = gson.toJson(entry.getRanges());
        initialValues.put(RANGES,rangesJson);
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
        contentValues.put(PERCENT,String.valueOf(entry.getPercent()));
        contentValues.put(IS_SUPPORTRANGE,entry.isSupportRange());
        Gson gson = new Gson();
        String rangesJson = gson.toJson(entry.getRanges());
        contentValues.put(RANGES,rangesJson);
        writableDatabase.insertOrThrow(TABLE_NAME, null, contentValues);
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
                int percent = cursor.getInt(cursor.getColumnIndex(PERCENT));
                boolean isSupportRange = "1".equals(cursor.getString(cursor.getColumnIndex(IS_SUPPORTRANGE)));
                String rangesJson = cursor.getString(cursor.getColumnIndex(RANGES));
                Type type = new TypeToken<HashMap<Integer,Integer>>() {}.getType();
                Gson gson = new Gson();
                HashMap<Integer,Integer> ranges = gson.fromJson(rangesJson, type);
                DownloadEntry.Status status = DownloadEntry.Status.valueOf(cursor.getString(cursor.getColumnIndex(STATUS)));
                DownloadEntry entry = new DownloadEntry(taskId, url, currentSize, totalSize, fileName, status);
                entry.setPercent(percent);
                entry.setRanges(ranges);
                entry.setSupportRange(isSupportRange);
                list.add(entry);
            }
        }
        return list;
    }

    public static void createTable(SQLiteDatabase db) {
        String createTableSql = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" (\n" +
                "\t" + TASK_ID + " VARCHAR(128) PRIMARY KEY,\n" +
                "\t" + URL + " VARCHAR(128) ,\n" +
                "\t" + CURRENT_SIZE + " INTEGER ,\n" +
                "\t" + TOTAL_SIZE + " INTEGER,\n" +
                "\t" + FILE_NAME + " VARCHAR(128),\n" +
                "\t" + STATUS + " VARCHAR(512) ,\n" +
                "\t" + RANGES + " VARCHAR(512) ,\n" +
                "\t" + IS_SUPPORTRANGE + " VARCHAR(128),\n" +
                "\t" + PERCENT + " INTEGER \n" +
                ")";

        try{
            db.execSQL(createTableSql);
        }catch (Exception e){
            LogUtls.debug("Sql error:"+e.getMessage());
        }
    }
}
