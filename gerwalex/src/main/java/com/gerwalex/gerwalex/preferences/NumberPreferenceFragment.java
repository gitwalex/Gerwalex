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
import android.widget.NumberPicker;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.activities.AWInterface;

/**
 * Fragment zur Ermittlung und speicherung einer Zeit in Minuten seit Mitternacht.
 * In der in {@link NumberPreferenceFragment#newInstance(NumberPreference)} uebergebenen Preference
 * wird der eingestellte Wert als int gespeichert.
 * Ausserdem wird das rufende Fragent durch {@link PreferenceDialogFragment#onActivityResult(int,
 * int, Intent)} mit folgenden Daten benachrichtig:
 * requestCode: wird beim Erstellen des Dialogs eingestellt.
 * resultCode: Konstant {@link AWInterface#DIALOGRESULT}
 * intent: null
 */
public class NumberPreferenceFragment
        extends PreferenceDialogFragment implements AWInterface {
    private static final String MINVALUE = "MINVALUE";
    private static final String MAXVALUE = "MAXVALUE";
    private NumberPicker mNumberPicker;
    private int maxValue = 1;
    private int minValue = 180;

    /**
     * Erstellt einen NumberDialog zur NumberPreference
     *
     * @param pref
     *         NumberPreference
     *
     * @return Fragment
     */
    public static NumberPreferenceFragment newInstance(NumberPreference pref) {
        final NumberPreferenceFragment fragment = new NumberPreferenceFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, pref.getKey());
        b.putInt(MAXVALUE, 180);
        b.putInt(MINVALUE, 1);
        fragment.setArguments(b);
        return fragment;
    }

    /**
     * Erstellt einen NumberDialog zur NumberPreference
     *
     * @param pref
     *         NumberPreference
     * @param minValue
     *         Mindestwert im Picker
     * @param maxValue
     *         Maximalwert im Picker
     *
     * @return Fragment
     */
    public static NumberPreferenceFragment newInstance(NumberPreference pref, int minValue, int
            maxValue) {
        final NumberPreferenceFragment fragment = new NumberPreferenceFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, pref.getKey());
        if (minValue > maxValue) {
            b.putInt(MAXVALUE, minValue);
            b.putInt(MINVALUE, minValue);
        } else {
            b.putInt(MINVALUE, minValue);
            b.putInt(MAXVALUE, minValue);
        }
        fragment.setArguments(b);
        return fragment;
    }

    /**
     * Erstellt den Dialog. View muss ein Element {@link NumberPicker} mit der id 'pNumberPicker'
     * enthalten
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mNumberPicker = (NumberPicker) view.findViewById(R.id.pNumberPicker);
        // Exception when there is no NumberPicker
        if (mNumberPicker == null) {
            throw new IllegalStateException(
                    "Dialog view must contain a NumberPicker with id 'pNumberPicker'");
        }
        DialogPreference preference = getPreference();
        if (preference instanceof NumberPreference) {
            int number = ((NumberPreference) preference).getNumber();
            mNumberPicker.setMinValue(minValue);
            mNumberPicker.setMaxValue(maxValue);
            mNumberPicker.setValue(number);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        minValue = args.getInt(MINVALUE, 1);
        maxValue = args.getInt(MAXVALUE, 180);
    }

    /**
     * Wenn der Dailog durch OK geschlossen wurde, wird das Ergebnis persisted, die Summary
     * aktualisiert und das TargetFragment benachrichtigt..
     */
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // generate value to save
            mNumberPicker.clearFocus();
            int number = mNumberPicker.getValue();
            // Get the related Preference and save the value
            NumberPreference timePreference = ((NumberPreference) getPreference());
            timePreference.setNumber(number);
            Intent intent = new Intent();
            intent.putExtra(DIALOGRESULT, number);
            getTargetFragment().onActivityResult(getTargetRequestCode(), 0, intent);
        }
    }
}
