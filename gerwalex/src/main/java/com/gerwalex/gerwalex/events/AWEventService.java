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

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;

import java.util.Calendar;

import com.gerwalex.gerwalex.activities.AWInterface;
import com.gerwalex.gerwalex.application.AWApplication;

import static com.gerwalex.gerwalex.events.AWEvent.AWLibDailyEvent;
import static com.gerwalex.gerwalex.events.AWEvent.DoDatabaseSave;
import static com.gerwalex.gerwalex.events.AWEvent.doVaccum;

/**
 * Bearbeitet Events innerhalb MonMa.
 */
public class AWEventService extends IntentService implements AWInterface {
    public static final String DODATABASESAVE = "DoDatabaseSave";

    public AWEventService() {
        super("AWEventService");
    }

    /**
     * Setzt den taeglichen Alarm auf den naechsten Tag 00:00 Uhr. Das Geraet wird nicht geweckkt.
     *
     * @param context Context
     */
    public static void setDailyAlarm(Context context) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long nextAlarm = cal.getTimeInMillis();
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent newIntent = new Intent(context, AWEventService.class);
        newIntent.setAction(AWLIBEVENT);
        newIntent.putExtra(AWLIBEVENT, AWLibDailyEvent);
        PendingIntent newAlarmIntent = PendingIntent.getService(context, 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.set(AlarmManager.RTC, nextAlarm, newAlarmIntent);
        AWApplication.Log("AWLIB next Daily-Alarmset to: " + cal.getTime().toString());
    }

    /**
     * Handelt Intents.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        assert extras != null;
        int event = extras.getInt(AWLIBEVENT);
        switch (event) {
            case AWLibDailyEvent:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                long nextDBSave = prefs.getLong(DODATABASESAVE, Long.MAX_VALUE);
                if (nextDBSave < System.currentTimeMillis()) {
                    new EventDBSave().execute(getApplicationContext());
                    prefs.edit().putLong(DODATABASESAVE, System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS * 5).apply();
                }
                setDailyAlarm(getApplicationContext());
                break;
            case DoDatabaseSave:
                new EventDBSave().execute(getApplicationContext());
                break;
            case doVaccum:
                ((AWApplication) getApplicationContext()).getDBHelper().optimize();
                break;
        }
    }
}


