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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gerwalex.gerwalex.application.AWApplication;

/**
 * BroadcastReceiver fuer Alarme. Beim Booten werden die notwendigen Alrme neu gesetzt
 */
public class AWLibBootBroadcastReceiver extends BroadcastReceiver {
    public AWLibBootBroadcastReceiver() {
    }

    /**
     * Wird beim booten gerufen und setzt die naechsten Alarme
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED:
                    // Set the alarm here.
                    AWApplication.Log("Boot completed - Alarms setting");
                    AWEventService.setDailyAlarm(context);
            }
        }
    }
}
