package com.gerwalex.gerwalex.application;

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

import android.app.Application;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ViewConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.Date;

import com.gerwalex.gerwalex.database.AWAbstractDBDefinition;
import com.gerwalex.gerwalex.database.AbstractDBHelper;
import com.gerwalex.gerwalex.events.AWEventService;

import static com.gerwalex.gerwalex.activities.AWInterface.linefeed;

/**
 * AWApplication: Einschalten von StrictModus, wenn im debug-mode. Erstellt eine HProf-Log bei
 * penaltyDeath().
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class AWApplication extends Application {
    /**
     * Debugging Cursor einschalten
     */
    public static final boolean EnableCursorLogging = true;
    /**
     * true: Debugging FragmentManager einschalten
     */
    public static final boolean EnableFragmentManagerLogging = true;
    /**
     * true: Debugging LoaderManager einschalten
     */
    public static final boolean EnableLoaderManagerLogging = false;
    public static final String STACKTRACE = "STACKTRACE", TAG = "de.aw";
    /**
     * Pfad, indem alle de.aw.-Applications abgelegt werden
     */
    public static final String DE_AW_APPLICATIONPATH = Environment.getExternalStorageDirectory() + "/de.aw";
    /**
     * Pfad, indem alle Backups zu de.aw.-Applications abgelegt werden
     */
    private static final String PICTUREPATH = "/pictures";
    /**
     * Pfad, indem alle Backups zu de.aw.-Applications abgelegt werden
     */
    private static final String BACKUPPATH = "/backup";
    /**
     * Pfad, indem alle Exports zu de.aw.-Applications abgelegt werden
     */
    private static final String EXPORTPATH = "/export";
    /**
     * Pfad, indem alle Imports zu de.aw.-Applications abgelegt werden
     */
    private static final String IMPORTPATH = "/import";
    private static final String DATABASEPATH = "/database";
    private String APPLICATIONPATH;
    private AbstractDBHelper mDBHelper;

    /**
     * Loggt Warnungen
     *
     * @param message message
     */
    public static void Log(String message) {
        Log.d(AWApplication.TAG, message);
    }

    /**
     * Loggt Fehler. Die Meldung wird auch in das File Applicationpath/LOG.txt geschrieben
     *
     * @param message Fehlermeldung
     */
    public static void LogError(String message) {
        File logFile = new File(DE_AW_APPLICATIONPATH + "/LOG.txt");
        try {
            FileOutputStream fileout = new FileOutputStream(logFile, true);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            CharSequence date = DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date());
            outputWriter.write(date + ": " + message);
            outputWriter.write(linefeed);
            outputWriter.close();
        } catch (IOException e) {
            //TODO Execption bearbeiten
            e.printStackTrace();
        }
        Log.e(AWApplication.TAG, message);
    }

    @NonNull
    protected abstract AbstractDBHelper createDBHelper(Context context);

    @CallSuper
    public void createFiles() {
        File folder = new File(getApplicationPath());
        if (!folder.exists()) {
            folder.mkdir();
        }
        folder = new File(getApplicationDatabasePath());
        if (!folder.exists()) {
            folder.mkdir();
        }
        folder = new File(getApplicationBackupPath());
        if (!folder.exists()) {
            folder.mkdir();
        }
        folder = new File(getApplicationExportPath());
        if (!folder.exists()) {
            folder.mkdir();
        }
        folder = new File(getApplicationImportPath());
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    /**
     * @return Liefert ein HTML-File  fuer die Auswahl der Preferences 'About'. Das file wird in
     * /assets/html erwartet. Default: Anzeige kein About
     */
    public String getAboutHTML() {
        return "no_about.html";
    }

    public final String getApplicationBackupPath() {
        return APPLICATIONPATH + BACKUPPATH;
    }

    public final String getApplicationDatabaseAbsoluteFilename() {
        return getDatabasePath(theDatenbankname()).getAbsolutePath();
    }

    public String getApplicationDatabasePath() {
        return APPLICATIONPATH + DATABASEPATH;
    }

    public final String getApplicationExportPath() {
        return APPLICATIONPATH + EXPORTPATH;
    }

    public final String getApplicationImportPath() {
        return APPLICATIONPATH + IMPORTPATH;
    }

    public final String getApplicationPath() {
        return APPLICATIONPATH;
    }

    public final String getApplicationPicturePath() {
        return APPLICATIONPATH + PICTUREPATH;
    }

    public abstract String getAuthority();

    /**
     * @return Liefert ein HTML-File  fuer die Auswahl der Preferences 'Copyright'. Das file wird in
     * /assets/html erwartet. Default: Anzeige kein Copyright
     */
    public String getCopyrightHTML() {
        return "no_copyright.html";
    }

    protected abstract AWAbstractDBDefinition[] getDBDefinitionValues();

    public AbstractDBHelper getDBHelper() {
        if (mDBHelper == null) {
            mDBHelper = createDBHelper(this);
        }
        return mDBHelper;
    }


    @Override
    public void onCreate() {
        APPLICATIONPATH = AWApplication.DE_AW_APPLICATIONPATH + "/" + theApplicationDirectory().replace("/", "");
        AWAbstractDBDefinition[] tbds = getDBDefinitionValues();
        if (tbds.length > 0) {
            tbds[0].setAuthority(getAuthority());
        }
        AbstractDBHelper.AWDBDefinition.values()[0].setAuthority(getAuthority());
        createDBHelper(this);
        super.onCreate();
        AWEventService.setDailyAlarm(this);
        FragmentManager.enableDebugLogging(EnableFragmentManagerLogging);
        LoaderManager.enableDebugLogging(EnableLoaderManagerLogging);
        //-Ausschalten Device - Option - Button, wenn vorhanden.
        // If an Android device has an option button, the overflow menu is not
        // shown.
        // While it it not recommended as the user except a certain behavior
        // from his device, you can trick you device in thinking it has no
        // option button with the following code.
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Wird gerufen, wenn die Datenbank restored wurde
     */
    public void onRestoreDatabase(Context context) {
    }

    /**
     * @return Verzeichnis, in dem die Appplicationsdaten abgelegt werden sollen
     */
    public abstract String theApplicationDirectory();

    /**
     * @return Datenbankversion
     */
    public abstract int theDatenbankVersion();

    /**
     * @return Datenbankname Default: "database.db"
     */
    public String theDatenbankname() {
        return "database.db";
    }

    /**
     * Liefert die ChannelID der Notifiication der APp zuruck.
     *
     * @return ChannelID. Default ist "awlib-channel-01"
     */
    public String getNotficationChannelID() {
        return "awlib-channel-01";
    }

}
