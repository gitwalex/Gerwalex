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

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.gerwalex.gerwalex.AWNotification;
import com.gerwalex.gerwalex.AWResultCodes;
import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.activities.AWInterface;
import com.gerwalex.gerwalex.application.AWApplication;
import com.gerwalex.gerwalex.utils.AWUtils;

/**
 * Klasse fuer Sicheren/Restoren DB.
 * Der Filename der Sicherung lautet: yyyy_MM_dd_HH_mm.zip_
 */
public class EventDBSave extends AsyncTask<File, Void, Integer> implements AWResultCodes, AWInterface {
    private static String DATABASEFILENAME;
    private Date date;
    private Context mContext;
    private AWNotification mNotification;
    private SharedPreferences prefs;

    public EventDBSave() {
    }

    @Override
    protected Integer doInBackground(File... params) {
        int result = AWUtils.addToZipArchive(params[0], DATABASEFILENAME);
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (prefs.getBoolean(mContext.getString(R.string.pkExterneSicherung), false)) {
        }
        return result;
    }

    /**
     * Sichert die Datenbank. Wird nur auusgefuhrt, wenn Berechtigung vorhanden
     *
     * @param context Context
     */
    public void execute(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mContext = context.getApplicationContext();
            AWApplication mApplication = ((AWApplication) mContext.getApplicationContext());
            String BACKUPPATH = mApplication.getApplicationBackupPath() + "/";
            DATABASEFILENAME = mApplication.getApplicationDatabaseAbsoluteFilename();
            date = new Date(System.currentTimeMillis());
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.getDefault());
            File backupFile = new File(BACKUPPATH + fmt.format(cal.getTime()) + ".zip");
            execute(backupFile);
        }
    }

    @Override
    protected void onPostExecute(Integer ergebnis) {
        String result;
        switch (ergebnis) {
            case RESULT_OK:
                result = mContext.getString(R.string.dbSaved);
                break;
            case RESULT_FILE_ERROR:
                result = mContext.getString(R.string.dbFileError);
                break;
            case RESULT_Divers:
            default:
                result = mContext.getString(R.string.dbSaveError);
                break;
        }
        mNotification.setHasProgressBar(false);
        mNotification.replaceNotification(result);
    }

    @Override
    protected void onPreExecute() {
        String ticker = mContext.getString(R.string.tickerDBSicherung);
        String contentTitle = mContext.getString(R.string.contentTextDBSicherung);
        mNotification = new AWNotification(mContext, contentTitle);
        mNotification.setTicker(ticker);
        mNotification.setHasProgressBar(true);
        mNotification.createNotification(contentTitle);
    }
}

