package com.example.aleks.brickcamerawithdb2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Aleks on 03-Oct-15.
 * Database Helper
 */
public class DatabaseHelper {
    private SQLiteOpenHelper _openHelper;

    private static final String DATABASE_NAME = "Pictures.db";
    private static final String TABLE_NAME = "pictures_table";
    private static final String COL_ID = "ID";
    private static final String COL_NAME = "Name";
    private static final String COL_FILEPATH = "FILEPATH";
    private static final String COL_COMMENT = "COMMENT";
    private static final String COL_ORIENTATION = "COL_ORIENTATION";

    public DatabaseHelper(Context context) {
        _openHelper = new mySQLiteOpenHelper(context);
    }


    class mySQLiteOpenHelper extends SQLiteOpenHelper {
        mySQLiteOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_NAME + " ("+
                    COL_ID +" integer primary key autoincrement, "+
                    COL_NAME +" text, "+
                    COL_FILEPATH +" text, "+
                    COL_COMMENT+" text, "+
                    COL_ORIENTATION+" text)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    public Cursor getAll() {
        SQLiteDatabase db = _openHelper.getReadableDatabase();
        if (db == null) {
            return null;
        }
        return db.rawQuery("select * from "+ TABLE_NAME +" order by "+COL_NAME+"", null);
    }

    public ContentValues get(long id) {
        SQLiteDatabase db = _openHelper.getReadableDatabase();
        if (db == null) {
            return null;
        }
        ContentValues row = new ContentValues();
        Cursor cur = db.rawQuery("select "+COL_NAME+", "+COL_COMMENT+" from "+TABLE_NAME+" where "+COL_ID+" = ?", new String[] { String.valueOf(id) });
        if (cur.moveToNext()) {
            row.put("Name", cur.getString(0));
            row.put("Comment", cur.getString(1));
        }
        cur.close();
        db.close();
        return row;
    }

    public ContentValues getComment(String name) {
        SQLiteDatabase db = _openHelper.getReadableDatabase();
        if (db == null) {
            return null;
        }
        ContentValues row = new ContentValues();
        Cursor cur = db.rawQuery("select "+COL_COMMENT+" from "+TABLE_NAME+" where "+COL_NAME+" = ?", new String[] { String.valueOf(name) });
        if (cur.moveToNext()) {
            row.put("Comment", cur.getString(0));
        }
        cur.close();
        db.close();
        return row;
    }

    public ContentValues getOrientation(String name) {
        SQLiteDatabase db = _openHelper.getReadableDatabase();
        if (db == null) {
            return null;
        }
        ContentValues row = new ContentValues();
        Cursor cur = db.rawQuery("select "+ COL_ORIENTATION +" from "+ TABLE_NAME +" where "+ COL_NAME +" = ?", new String[] { name });
        if (cur.moveToNext()) {
            row.put("Orientation", cur.getString(0));
        }
        cur.close();
        db.close();
        return row;
    }

    public long addPicture(String name, String filepath, String orientation) {
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        if (db == null) {
            return 0;
        }
        ContentValues row = new ContentValues();
        row.put(COL_NAME, name);
        row.put(COL_FILEPATH, filepath);
        row.put(COL_ORIENTATION, orientation);
        long id = db.insert(TABLE_NAME, null, row);
        db.close();
        return id;
    }

    public void delete(long id) {
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        if (db == null) {
            return;
        }
        db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateComment(String name, String comment) {
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        if (db == null) {
            return;
        }
        ContentValues row = new ContentValues();
        row.put(COL_NAME, name);
        row.put(COL_COMMENT, comment);
        db.update(TABLE_NAME, row, COL_NAME+" = ?", new String[]{String.valueOf(name)});
        db.close();
    }
}
