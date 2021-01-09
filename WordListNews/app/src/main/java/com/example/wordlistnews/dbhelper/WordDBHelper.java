package com.example.wordlistnews.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class WordDBHelper extends SQLiteOpenHelper {
    public WordDBHelper(Context context) {
        super(context, "words.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE WordList(word text primary key not null, meanings text not null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists WordList");
    }

    public boolean insertWord(String word, String meanings) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("word", word);
        //contentValues.put("meanings", DatabaseUtils.sqlEscapeString(meanings));
        contentValues.put("meanings", meanings);

        long result = db.insert("WordList", null, contentValues);
        return result != -1;
    }

    public boolean updateWord(String word, String meanings) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("meanings", meanings);

        Cursor cursor = db.rawQuery("Select * from WordList where word = ?", new String[] {word});

        if (cursor.getCount() > 0) {
            long result = db.update("WordList", contentValues, "word=?", new String[] {word});
            return result != -1;
        } else {
            return false;
        }
    }

    public boolean deleteWord(String word) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from WordList where word = ?", new String[] {word});

        if (cursor.getCount() > 0) {
            long result = db.delete("WordList", "word=?", new String[] {word});
            return result != -1;
        } else {
            return false;
        }
    }

    public Cursor getWordList() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from WordList", null);
        return cursor;
    }

    public boolean isWordExists(String word) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from WordList where word = ?", new String[] {word});
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();

        return true;
    }

}
