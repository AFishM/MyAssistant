package com.name.myassistant.deskclock;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.name.myassistant.util.LogUtil;

public class AlarmProvider extends ContentProvider {
    private SQLiteOpenHelper mOpenHelper;

    private static final int ALARMS = 1;
    private static final int ALARMS_ID = 2;
    private static final UriMatcher sURLMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURLMatcher.addURI("com.name.myassistant.deskclock", "alarm", ALARMS);
        sURLMatcher.addURI("com.name.myassistant.deskclock", "alarm/#", ALARMS_ID);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "alarms.db";
        private static final int DATABASE_VERSION = 5;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            LogUtil.d("xzx");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            LogUtil.d("xzx");
            db.execSQL("CREATE TABLE alarms (" +
                       "_id INTEGER PRIMARY KEY," +
                       "hour INTEGER, " +
                       "minutes INTEGER, " +
                       "daysofweek INTEGER, " +
                       "alarmtime INTEGER, " +
                       "enabled INTEGER, " +
                       "vibrate INTEGER, " +
                       "message TEXT, " +
                       "alert TEXT);");

            // insert default alarms
            String insertMe = "INSERT INTO alarms " +
                    "(hour, minutes, daysofweek, alarmtime, enabled, vibrate, message, alert) " +
                    "VALUES ";
            db.execSQL(insertMe + "(8, 30, 31, 0, 0, 1, '', '');");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
            LogUtil.v("xzx", "Upgrading alarms database from version " +
                            oldVersion + " to " + currentVersion +
                            ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS alarms");
            onCreate(db);
        }
    }

    public AlarmProvider() {
    }

    @Override
    public boolean onCreate() {
        LogUtil.d("xzx");
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        // Generate the body of the query
        int match = sURLMatcher.match(url);
        switch (match) {
            case ALARMS:
                sqLiteQueryBuilder.setTables("alarms");
                break;
            case ALARMS_ID:
                sqLiteQueryBuilder.setTables("alarms");
                sqLiteQueryBuilder.appendWhere("_id=");
                sqLiteQueryBuilder.appendWhere(url.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }

        LogUtil.d("xzx");
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = sqLiteQueryBuilder.query(db, projectionIn, selection, selectionArgs,
                null, null, sort);

        if (cursor == null) {
            LogUtil.v("xzx", "Alarms.query: failed");
        } else {
            cursor.setNotificationUri(getContext().getContentResolver(), url);
        }

        return cursor;
    }

    @Override
    public String getType(Uri url) {
        int match = sURLMatcher.match(url);
        switch (match) {
            case ALARMS:
                return "vnd.android.cursor.dir/alarms";
            case ALARMS_ID:
                return "vnd.android.cursor.item/alarms";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int count;
        long rowId = 0;
        int match = sURLMatcher.match(url);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case ALARMS_ID: {
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update("alarms", values, "_id=" + rowId, null);
                break;
            }
            default: {
                throw new UnsupportedOperationException(
                        "Cannot update URL: " + url);
            }
        }
        LogUtil.v("xzx", "*** notifyChange() rowId: " + rowId + " url " + url);
        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        if (sURLMatcher.match(url) != ALARMS) {
            throw new IllegalArgumentException("Cannot insert into URL: " + url);
        }

        ContentValues values = new ContentValues(initialValues);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert("alarms", Alarm.Columns.MESSAGE, values);
        if (rowId < 0) {
            throw new SQLException("Failed to insert row into " + url);
        }
        LogUtil.v("xzx", "Added alarm rowId = " + rowId);

        Uri newUrl = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(newUrl, null);
        return newUrl;
    }

    public int delete(Uri url, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        long rowId = 0;
        switch (sURLMatcher.match(url)) {
            case ALARMS:
                count = db.delete("alarms", where, whereArgs);
                break;
            case ALARMS_ID:
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                if (TextUtils.isEmpty(where)) {
                    where = "_id=" + segment;
                } else {
                    where = "_id=" + segment + " AND (" + where + ")";
                }
                count = db.delete("alarms", where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + url);
        }

        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }
}
