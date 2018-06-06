/**
 *
 */
package com.gerwalex.gerwalex.preferences;

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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;

import com.gerwalex.gerwalex.BuildConfig;
import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.activities.AWActivityActions;
import com.gerwalex.gerwalex.activities.AWInterface;
import com.gerwalex.gerwalex.activities.AWWebViewActivity;
import com.gerwalex.gerwalex.application.AWApplication;
import com.gerwalex.gerwalex.database.AWDBConvert;
import com.gerwalex.gerwalex.database.AbstractDBHelper;
import com.gerwalex.gerwalex.events.AWEventService;
import com.gerwalex.gerwalex.fragments.AWPreferenceFragment;
import com.gerwalex.gerwalex.utils.AWUtils;

import static com.gerwalex.gerwalex.application.AWApplication.DE_AW_APPLICATIONPATH;
import static com.gerwalex.gerwalex.events.AWEvent.DoDatabaseSave;
import static com.gerwalex.gerwalex.events.AWEvent.copyAndDebugDatabase;
import static com.gerwalex.gerwalex.events.AWEvent.doVaccum;
import static com.gerwalex.gerwalex.events.AWEvent.showBackupFiles;
import static com.gerwalex.gerwalex.events.AWEvent.showRemoteFileServer;
import static com.gerwalex.gerwalex.events.AWEventService.DODATABASESAVE;

/**
 * Erstellt und bearbeitet die allgemeinen Preferences.
 *
 * @author alex
 */
public final class AWPreferencesAllgemein extends AWPreferenceFragment implements Preference.OnPreferenceClickListener, AWInterface {
    private static final int[] mPrefs = new int[]{R.string.pkDBVacuum, R.string.pkDBSave, R.string.pkDBRestore, R.string.pkCopyright, R.string.pkAbout, R.string.pkCompileInfo, R.string.pkVersionInfo, R.string.pkExterneSicherung, R.string.pkServerURL, R.string.pkServerUID};
    private AWApplication mApplication;
    private int pendingEvent;

    private void buildAndShowDialog(int titleRes, int messageRes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titleRes);
        builder.setMessage(messageRes);
        builder.setPositiveButton(R.string.awlib_btnAccept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        Dialog dlg = builder.create();
        dlg.show();
    }

    private void copyAndDebugDatabase() throws IOException {
        mApplication.createFiles();
        Intent intent = new Intent(Intent.ACTION_EDIT);
        String databasePath = mApplication.getApplicationDatabasePath();
        File src = mApplication.getDatabasePath(mApplication.theDatenbankname());
        File dest = new File(databasePath + File.separator + mApplication.theDatenbankname());
        dest.delete();
        AWUtils.copyFile(getActivity(), src, dest);
        Uri uri = Uri.parse("sqlite:" + dest.getAbsolutePath());
        intent.setData(uri);
        startActivity(intent);
    }

    @CallSuper
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        mApplication = ((AWApplication) getActivity().getApplicationContext());
        addPreferencesFromResource(R.xml.awlib_preferences_allgemein);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        for (int pkKey : mPrefs) {
            String key = getString(pkKey);
            Preference preference = findPreference(key);
            if (pkKey == R.string.pkCompileInfo) {
                java.util.Date date = new Date(BuildConfig.BuildTime);
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
                StringBuilder buildInfo = new StringBuilder("Compilezeit: ").append(df.format(date));
                preference.setSummary(buildInfo);
            } else if (pkKey == R.string.pkVersionInfo) {
                StringBuilder versionInfo = new StringBuilder("Datenbankversion : ").append(mApplication.theDatenbankVersion()).append(", Version: ").append(BuildConfig.VERSION_NAME);
                preference.setSummary(versionInfo);
            } else if (pkKey == R.string.pkServerUID || pkKey == R.string.pkServerURL) {
                String value = prefs.getString(key, null);
                preference.setSummary(value);
            } else if (pkKey == R.string.pkSavePeriodic) {
                long value = prefs.getLong(DODATABASESAVE, Long.MAX_VALUE);
                setRegelmSicherungSummary(preference, prefs, value);
            }
            preference.setOnPreferenceClickListener(this);
        }
    }

    @CallSuper
    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (getString(R.string.pkDBVacuum).equals(key)) {
            buildAndShowDialog(R.string.dbTitleDatenbank, R.string.dlgDatenbankAufraeumen);
            Intent intent = new Intent(getActivity(), AWEventService.class);
            intent.putExtra(AWLIBEVENT, doVaccum);
            getActivity().startService(intent);
            return true;
        } else if (getString(R.string.pkDBSave).equals(key)) {
            // Datenbank sichern
            int permission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission == PackageManager.PERMISSION_GRANTED) {
                startDBSave(DoDatabaseSave);
            } else {
                pendingEvent = DoDatabaseSave;
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
            }
            return true;
        } else if (getString(R.string.pkDBRestore).equals(key)) {
            Intent intent = new Intent(getActivity(), AWActivityActions.class);
            intent.putExtra(AWLIBEVENT, showBackupFiles);
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.pkCopyright).equals(key)) {
            Intent intent = new Intent(getActivity(), AWWebViewActivity.class);
            intent.putExtra(ID, mApplication.getCopyrightHTML());
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.pkAbout).equals(key)) {
            Intent intent = new Intent(getActivity(), AWWebViewActivity.class);
            intent.putExtra(ID, mApplication.getAboutHTML());
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.pkExterneSicherung).equals(key)) {
            Intent intent = new Intent(getActivity(), AWActivityActions.class);
            intent.putExtra(AWLIBEVENT, showRemoteFileServer);
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.pkVersionInfo).equals(key)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.awlib_pref_dbversion, null);
            final EditText etVersion = (EditText) view.findViewById(R.id.etVersion);
            builder.setView(view);
            final AbstractDBHelper mDBHelper = ((AWApplication) getActivity().getApplicationContext()).getDBHelper();
            int oldVersion = mDBHelper.getReadableDatabase().getVersion();
            etVersion.setText("" + oldVersion);
            builder.setTitle("Aktuelle Version: " + oldVersion);
            builder.setPositiveButton(R.string.awlib_btnAccept, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int newVersion = Integer.parseInt(etVersion.getText().toString());
                    mDBHelper.getWritableDatabase().setVersion(newVersion);
                }
            });
            builder.create().show();
            return true;
        } else if (getString(R.string.pkCompileInfo).equals(key)) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    copyAndDebugDatabase();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                pendingEvent = copyAndDebugDatabase;
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_STORAGE:
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        switch (pendingEvent) {
                            case DoDatabaseSave:
                                startDBSave(pendingEvent);
                                break;
                            case copyAndDebugDatabase:
                                try {
                                    copyAndDebugDatabase();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                    i++;
                }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pkSavePeriodic))) {
            SwitchPreference pref = (SwitchPreference) findPreference(key);
            long value = Long.MAX_VALUE;
            if (pref.isChecked()) {
                value = System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS * 5;
            }
            setRegelmSicherungSummary(pref, sharedPreferences, value);
        }
    }

    private void setRegelmSicherungSummary(Preference pref, SharedPreferences prefs, long value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(DODATABASESAVE);
        if (value == Long.MAX_VALUE) {
            pref.setSummary(getString(R.string.smryDBSavePeriodic));
        } else {
            String date = AWDBConvert.convertDate(value);
            pref.setSummary(getString(R.string.smryDBSavePeriodicOn) + date);
        }
        editor.putLong(DODATABASESAVE, value).apply();
    }

    private void startDBSave(int event) {
        buildAndShowDialog(R.string.dbTitleDatenbank, R.string.dlgDatenbankSichern);
        File folder = new File(DE_AW_APPLICATIONPATH);
        if (!folder.exists()) {
            folder.mkdir();
        }
        mApplication.createFiles();
        Intent intent = new Intent(getActivity(), AWEventService.class);
        intent.putExtra(AWLIBEVENT, event);
        getActivity().startService(intent);
    }
}
