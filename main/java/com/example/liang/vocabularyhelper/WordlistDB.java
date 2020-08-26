package com.example.liang.vocabularyhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class WordlistDB {
    private Context mContext;
    private SQLiteDatabase mdb;
    private long timestamp;
    private String DATABASE = "db", TABLE = "wordlist";

    WordlistDB(Context context) {
        mContext = context;
        mdb = (new DatabaseHelper(mContext, DATABASE, null, 1)).getWritableDatabase();
        mdb.execSQL("create table if not exists wordlist(\n" +
                "    " + ColNames.id + " INTEGER primary key autoincrement,\n" +
                "     " + ColNames.word + " VARCHAR not null,\n" +
                "     " + ColNames.meaning + " VARCHAR,\n" +
                "     " + ColNames.test_times + " INTEGER,\n" +
                "     " + ColNames.correct_times + " INTEGER,\n" +
                "     " + ColNames.correct_rate + " FLOAT,\n" +
                "     " + ColNames.test_date + " LONG,\n" +
                "     " + ColNames.learn_date + " LONG,\n" +
                "     " + ColNames.add_date + " LONG\n" +
                "     )");
        timestamp = System.currentTimeMillis();
    }

    public SQLiteDatabase getDB() {
        return mdb;
    }

    int addItem(String word, String meaning) {
        ContentValues values = new ContentValues();
        values.put(ColNames.word, word);
        values.put(ColNames.meaning, meaning);
        values.put(ColNames.test_times, 0);
        values.put(ColNames.test_date, 0);
        values.putNull(ColNames.correct_times);
        values.putNull(ColNames.correct_rate);
        values.putNull(ColNames.test_date);
        values.putNull(ColNames.learn_date);
        values.putNull(ColNames.test_date);
        values.putNull(ColNames.learn_date);
        values.put(ColNames.add_date, timestamp);
        SQLiteDatabase db = (new DatabaseHelper(mContext, DATABASE, null, 1)).getReadableDatabase();
        db.insert(TABLE, null, values);
        return getLastId();
    }

    int removeAll() {
        return mdb.delete(TABLE, null, null);
    }

    void removeItem(int id) {
        mdb.delete(TABLE, "id=?", new String[]{String.valueOf(id)});
    }

    void getAllItems(ItemHandlerInterface itemHandlerInterface) {
        getItem("SELECT * FROM " + TABLE, itemHandlerInterface);
    }

    void getItemById(int id, ItemHandlerInterface itemHandlerInterface) {
        getItem("SELECT * FROM " + TABLE + " WHERE id=" + String.valueOf(id), itemHandlerInterface);
    }

    void getItem(String sql, String[] selectionArgs, ItemHandlerInterface itemHandlerInterface) {
        Cursor cursor = mdb.rawQuery(sql, selectionArgs);
        if (cursor.moveToFirst()) {
            String[] columnNames = cursor.getColumnNames();
            do {
                Map<String, String> map = new HashMap<>();
                for (String name : columnNames)
                    map.put(name, cursor.getString(cursor.getColumnIndex(name)));
                itemHandlerInterface.itemHandler(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    void getItem(String sql, ItemHandlerInterface itemHandlerInterface) {
        getItem(sql, null, itemHandlerInterface);
    }

    void logDataRow(Map<String, String> dataRow) {
        for (String i : dataRow.keySet())
            Log.d("dbdbdb", i + ":" + (dataRow.get(i) == null ? "null" : dataRow.get(i)));
    }

    int getRowsCount() {
        return (int) mdb.compileStatement("SELECT COUNT(*) FROM " + TABLE).simpleQueryForLong();
    }

    int getLastId() {
        Cursor cursor = mdb.rawQuery("select * from " + TABLE + " where " + ColNames.id + " = (select max(" + ColNames.id + ") from " + TABLE + ")", null);
        cursor.moveToFirst();
        int id = cursor.getInt(cursor.getColumnIndex(ColNames.id));
        cursor.close();
        return id;
    }

    void exportDB(/*String dir*/) {
        copyFile(mdb.getPath(), Environment.getExternalStorageDirectory() + File.separator + "VocabularyHelper" + File.separator + "db");
    }

    private void copyFile(String fromfile, String tofile) {
        try {
            FileInputStream fis = new java.io.FileInputStream(fromfile);
            FileOutputStream fos = new FileOutputStream(tofile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fis.read(bt)) > 0) {
                fos.write(bt, 0, c);
            }
            fis.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    interface ItemHandlerInterface {
        void itemHandler(Map<String, String> dataRow);
    }

    static class ColNames {
        static String id = "id",
                word = "words",
                meaning = "meanings",
                test_times = "test_times ",
                correct_times = "correct_times",
                correct_rate = "correct_rate",
                learn_date = "learn_date ",
                test_date = "test_date ",
                add_date = "add_date";
    }
}
