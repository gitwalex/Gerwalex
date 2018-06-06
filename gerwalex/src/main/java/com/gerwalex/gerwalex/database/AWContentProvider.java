package com.gerwalex.gerwalex.database;

/*
 * AWLib: Eine Bibliothek  zur schnellen Entwicklung datenbankbasierter Applicationen
 *
 * Copyright [2015] [Alexander Winkler, 2373 Dahme/Germany]
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses/>.
 */

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.gerwalex.gerwalex.activities.AWInterface;
import com.gerwalex.gerwalex.application.AWApplication;

/**
 * Erweiterung des MainContentProviders.
 *
 * @author alex
 */
public class AWContentProvider extends ContentProvider implements AWInterface {
    protected AbstractDBHelper db;
    private boolean batchMode;
    private AWApplication mApplication;

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        db = getDBHelper();
        batchMode = true;
        db.beginTransaction();
        int result = 0;
        try {
            for (ContentValues cv : values) {
                long erg = db.insert(uri, null, cv);
                if (erg == -1) {
                    AWApplication.Log("Insert fehlgeschlagen! Werte: " + cv.toString());
                } else {
                    result++;
                }
                db.setTransactionSuccessful();
            }
        } finally {
            db.endTransaction();
            db = null;
            batchMode = false;
        }
        return result;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        if (!batchMode) {
            db = getDBHelper();
        }
        int rowsDeleted = db.delete(uri, selection, selectionArgs);
        if (!batchMode) {
            db = null;
        }
        return rowsDeleted;
    }

    protected AbstractDBHelper getDBHelper() {
        if (mApplication == null) {
            mApplication = ((AWApplication) getContext().getApplicationContext());
        }
        return mApplication.getDBHelper();
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (!batchMode) {
            db = getDBHelper();
        }
        long id = db.insert(uri, null, values);
        if (!batchMode) {
            db = null;
        }
        return Uri.withAppendedPath(uri, Uri.encode(String.valueOf(id)));
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] from, String selection, String[] selectionArgs, String sortOrder) {
        db = getDBHelper();
        SQLiteDatabase database = db.getReadableDatabase();
        String table = uri.getLastPathSegment();
        Cursor c = database.query(table + " t1", from, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (!batchMode) {
            db = getDBHelper();
        }
        int rowsUpdated = db.update(uri, values, selection, selectionArgs);
        if (!batchMode) {
            db = null;
        }
        return rowsUpdated;
    }
}
