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

import android.content.Intent;
import android.os.Bundle;
import android.support.v14.preference.PreferenceDialogFragment;
import android.support.v7.preference.DialogPreference;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.activities.AWInterface;

/**
 * Fragment zur Ermittlung und speicherung einer Zeit in Minuten seit Mitternacht.
 * <p>
 * In der in {@link DatePreferenceFragment#newInstance(DatePreference)} uebergebenen Preference wird
 * das eingestellte datum als long gespeichert. In der Summary der Preference wird die gewaehlte
 * Uhrzeit im Format 'HH:mm' eingestellt.
 * <p>
 * Ausserdem wird das rufende Fragent durch {@link PreferenceDialogFragment#onActivityResult(int,
 * int, Intent)} mit folgenden Daten benachrichtig: requestCode: wird beim Erstellen des Dialogs
 * eingestellt.
 * <p>
 * resultCode: Konstant {@link AWInterface#DIALOGRESULT}
 * <p>
 * intent: null
 */
public class DatePreferenceFragment extends PreferenceDialogFragment
        implements AWInterface {
    private DatePicker mDatePicker;

    /**
     * Erstellt einen TimeDialog zur TimePreference
     *
     * @param pref
     *         TimePreference
     * @return Fragment
     */
    public static DatePreferenceFragment newInstance(DatePreference pref) {
        final DatePreferenceFragment fragment = new DatePreferenceFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, pref.getKey());
        fragment.setArguments(b);
        return fragment;
    }

    /**
     * Erstellt den Dialog. View muss ein Element {@link TimePicker} mit der id 'pTimePicker'
     * enthalten
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mDatePicker = (DatePicker) view.findViewById(R.id.pDatePicker);
        // Exception when there is no TimePicker
        if (mDatePicker == null) {
            throw new IllegalStateException(
                    "Dialog view must contain" + " a DatePicker with id 'pDatePicker'");
        }
        DialogPreference preference = getPreference();
        if (preference instanceof DatePreference) {
            Calendar date = ((DatePreference) preference).getDate();
            mDatePicker.updateDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                    date.get(Calendar.DAY_OF_MONTH));
        }
    }

    /**
     * Wenn der Dailog durch OK geschlossen wurde, wird das Ergebnis persisted, die Summary
     * aktualisiert und das TargetFragment benachrichtigt..
     */
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // generate value to save
            int day = mDatePicker.getDayOfMonth();
            int month = mDatePicker.getMonth();
            int year = mDatePicker.getYear();
            // Get the related Preference and save the value
            DatePreference datePreference = ((DatePreference) getPreference());
            datePreference.setDate(year, month, day);
            Intent intent = new Intent();
            intent.putExtra(DIALOGRESULT, datePreference.getTimeInMillis());
            getTargetFragment().onActivityResult(getTargetRequestCode(), 0, intent);
        }
    }
}
