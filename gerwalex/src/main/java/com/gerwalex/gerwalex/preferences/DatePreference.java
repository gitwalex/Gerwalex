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

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import java.util.Calendar;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.database.AWDBConvert;

/**
 * DatePreference: Liest eine Uhrzeit und speichert diese als Long in Preferences. Gibt es einen
 * Default-Wert (Format: HH:mm), wird dieser uebernommen. Ist kein DefaultWert vorgegeben, dann wird
 * 00:00 angenommen
 */
public class DatePreference extends DialogPreference {
    private static int mDialogLayoutResId = R.layout.awlib_pref_dialog_time;
    private Calendar mCalendar = Calendar.getInstance();

    public DatePreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public DatePreference(Context context) {
        this(context, null);
    }

    public DatePreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);
    }

    public Calendar getDate() {
        return mCalendar;
    }

    public void setDate(long timeInMillis) {
        mCalendar.setTimeInMillis(timeInMillis);
        setInternalValues();
        callChangeListener(timeInMillis);
    }

    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

    @Override
    public CharSequence getSummary() {
        long value = getPersistedLong(System.currentTimeMillis());
        return AWDBConvert.convertDate(value);
    }

    public long getTimeInMillis() {
        return mCalendar.getTimeInMillis();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return System.currentTimeMillis();
    }

    /**
     * Belegt den Initialwert fuer Time. Gibt es einen Default-Wert (Format: HH:mm), wird dieser
     * uebernommen. Ist kein DefaultWert vorgegeben, dann wird 00:00 angenommen
     */
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setDate(restoreValue ? getPersistedLong(mCalendar.getTimeInMillis()) : (int) defaultValue);
    }

    public void setDate(int year, int month, int day) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        setInternalValues();
    }

    private void setInternalValues() {
        persistLong(mCalendar.getTimeInMillis());
        setSummary(getSummary());
    }
}