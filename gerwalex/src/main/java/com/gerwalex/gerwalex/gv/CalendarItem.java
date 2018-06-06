package com.gerwalex.gerwalex.gv;

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

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract.Calendars;

import com.gerwalex.gerwalex.database.AbstractDBHelper;

/**
 * Geschaeftsobject zur Anzeige der im Geraet angelegten Calendars
 */
@SuppressLint("ParcelCreator")
public class CalendarItem extends AWApplicationGeschaeftsObjekt {
    private static final AbstractDBHelper.AWDBDefinition tbd =
            AbstractDBHelper.AWDBDefinition.AndroidCalendar;
    private final ContentValues currentContent;

    public CalendarItem(Context context, Cursor c) {
        super(context, tbd, c);
        currentContent = getContent();
    }

    public String getName() {
        return currentContent.getAsString(Calendars.CALENDAR_DISPLAY_NAME);
    }
}
