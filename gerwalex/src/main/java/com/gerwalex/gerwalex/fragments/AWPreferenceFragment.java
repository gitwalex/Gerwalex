/**
 *
 */
package com.gerwalex.gerwalex.fragments;

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

import android.content.SharedPreferences;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;

/**
 * Erstellt und bearbeitet die allgemeinen Preferences. Die einzelnen Preferences werden als Wert in
 * die Summary eingestellt.
 *
 * @author alex
 */
public abstract class AWPreferenceFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * Initialisiert die Summaries, wenn das Fragment gestartet wird.
     *
     * @param p
     *         Preference.
     */
    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    /**
     * De-Registriert sich als {@link SharedPreferences.OnSharedPreferenceChangeListener}
     */
    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                             .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Registriert sich als {@link SharedPreferences.OnSharedPreferenceChangeListener}
     */
    @Override
    public void onResume() {
        super.onResume();
        initSummary(getPreferenceScreen());
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Beim Aufruf wird die Summary-Zeile der Preference aktualisiert. Siehe {@link
     * AWPreferenceFragment#updatePrefSummary(Preference)}
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference p = findPreference(key);
        if (p != null) {
            updatePrefSummary(findPreference(key));
        }
    }

    /**
     * Stellt den Wert der Preference in die Summary-Zeile ein. Dies gilt fuer ListPreferences und
     * EditTextPreferences. Alle anderen Preferences werden nicht barbeitet. Enthaelt der Titel der
     * EditTextPreference als Teilstring 'asswor'  werden statt des Textes Sterne in die
     * Summaryzeile gesetzt.
     *
     * @param p
     *         Preference
     */
    protected void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
            return;
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (p.getTitle().toString().contains("asswor")) {
                p.setSummary("******");
            } else {
                p.setSummary(editTextPref.getText());
            }
            return;
        }
    }
}
