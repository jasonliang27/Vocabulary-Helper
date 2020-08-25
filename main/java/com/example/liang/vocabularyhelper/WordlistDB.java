package com.example.liang.vocabularyhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
                "    id INTEGER primary key autoincrement,\n" +
                "     words VARCHAR not null,\n" +
                "     meanings VARCHAR,\n" +
                "     test_times INTEGER,\n" +
                "     correct_times INTEGER,\n" +
                "     correct_rate FLOAT,\n" +
                "     test_date LONG,\n" +
                "     learn_date LONG,\n" +
                "     add_date LONG\n" +
                "     )");
        timestamp = System.currentTimeMillis();
    }

    public SQLiteDatabase getDB() {
        return mdb;
    }

    int addItem(String word, String meaning) {
        ContentValues values = new ContentValues();
        values.put("words", word);
        values.put("meanings", meaning);
        values.put("test_times", 0);
        values.put("correct_times", 0);
        values.putNull("correct_rate");
        values.putNull("test_date");
        values.putNull("learn_date");
        values.putNull("test_date");
        values.putNull("learn_date");
        values.put("add_date", timestamp);
        SQLiteDatabase db = (new DatabaseHelper(mContext, DATABASE, null, 1)).getReadableDatabase();
        db.insert(TABLE, null, values);
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE, null);
        cursor.moveToLast();
        int id = cursor.getInt(cursor.getColumnIndex("id"));
        cursor.close();
        return id;
    }

    int removeAll() {
        return mdb.delete(TABLE, null, null);
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

    /*int getLastId(){
        //TODO getLastId
    }*/

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
