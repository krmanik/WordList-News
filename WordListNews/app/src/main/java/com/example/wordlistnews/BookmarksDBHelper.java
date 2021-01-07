package com.example.wordlistnews;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookmarksDBHelper extends SQLiteOpenHelper {
    public BookmarksDBHelper(Context context) {
        super(context, "words.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Bookmarks(_id integer primary key autoincrement, _title text not null, _url text not null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists Bookmarks");
    }

    public boolean insertBookmark(String title, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("_title", title);
        //contentValues.put("meanings", DatabaseUtils.sqlEscapeString(meanings));
        contentValues.put("_url", url);

        long result = db.insert("Bookmarks", null, contentValues);
        return result != -1;
    }

    public boolean updateBookmark(int id, String title, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("_url", url);
        contentValues.put("_title", title);

        Cursor cursor = db.rawQuery("Select * from Bookmarks where _id = ?", new String[] {String.valueOf(id)});

        if (cursor.getCount() > 0) {
            long result = db.update("Bookmarks", contentValues, "_id=?", new String[] {String.valueOf(id)});
            return result != -1;
        } else {
            return false;
        }
    }

    public boolean deleteBookmark(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from Bookmarks where _id = ?", new String[] {String.valueOf(id)});

        if (cursor.getCount() > 0) {
            long result = db.delete("Bookmarks", "_id=?", new String[] {String.valueOf(id)});
            return result != -1;
        } else {
            return false;
        }
    }

    public Cursor getBookmarks() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from Bookmarks", null);
        return cursor;
    }

    public boolean isWordExists(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from Bookmarks where _url = ?", new String[] {url});
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();

        return true;
    }

}
