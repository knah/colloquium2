package ru.ifmo.md.colloquium2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kna on 11.11.14.
 */
public class KeyValueStore extends SQLiteOpenHelper {

    private static final String DB_NAME = "vote.db";
    private static final int DB_VERSION = 2;

    public KeyValueStore(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS store(key TEXT PRIMARY KEY, value TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion != newVersion) {
            db.execSQL("DROP TABLE store;");
            onCreate(db);
        }
    }

    public boolean existsKey(SQLiteDatabase db, String key) {
        Cursor cr = db.query("store", new String[] {"key"}, "key = ?", new String[] {key}, null, null, null);
        if(cr.getCount() == 0)
            return false;

        return true;
    }

    public void setData(SQLiteDatabase db, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put("key", key);
        cv.put("value", value);
        if(existsKey(db, key)) {
            db.update("store", cv, "key = ?", new String[] {key});
        } else {
            db.insert("store", null, cv);
        }
    }

    public String getData(SQLiteDatabase db, String key) {
        Cursor cr = db.query("store", new String[] {"key", "value"}, "key = ?", new String[] {key}, null, null, null);
        if(cr.getCount() == 0)
            return null;

        cr.moveToFirst();
        return cr.getString(1);
    }
}
