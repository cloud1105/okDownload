package com.leo.okdownload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;


public class DownloadDbHelper extends SQLiteOpenHelper {
    public DownloadDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, SqliteConfig.DB_NAME, null, SqliteConfig.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DbController.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1 && newVersion == 2){
            // 通过 SQLiteDatabase 对数据库进行需改（对表添加字段，或者添加新表）
        }
    }
}
