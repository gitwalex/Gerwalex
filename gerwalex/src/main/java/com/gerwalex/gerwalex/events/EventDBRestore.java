package com.gerwalex.gerwalex.events;

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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import java.io.File;

import com.gerwalex.gerwalex.AWResultCodes;
import com.gerwalex.gerwalex.activities.AWInterface;
import com.gerwalex.gerwalex.application.AWApplication;
import com.gerwalex.gerwalex.utils.AWUtils;

/**
 * Klasse fuer Sicheren/Restoren DB
 */
public class EventDBRestore implements AWResultCodes, AWInterface {
    private final Context mContext;
    private final AWApplication mApplication;

    public EventDBRestore(Context context) {
        mContext = context;
        mApplication = ((AWApplication) mContext.getApplicationContext());
    }

    public void restore(File file) {
        new DoDatabaseRestore().execute(file);
    }

    private class DoDatabaseRestore extends AsyncTask<File, Void, Integer> {
        @Override
        protected Integer doInBackground(File... params) {
            int result;
            String targetFileName = mApplication.getApplicationDatabaseAbsoluteFilename();
            mApplication.getDBHelper().close();
            result = AWUtils.restoreZipArchivToFile(targetFileName, params[0]);
            mApplication.getDBHelper();
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == RESULT_OK) {
                mApplication.onRestoreDatabase(mContext);
                PackageManager pm = mContext.getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(mContext.getPackageName());
                mContext.startActivity(intent);
            }
        }
    }
}
