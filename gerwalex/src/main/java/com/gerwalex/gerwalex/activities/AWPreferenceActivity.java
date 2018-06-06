/**
 *
 */
package com.gerwalex.gerwalex.activities;

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

import android.preference.PreferenceActivity;
import android.support.v7.preference.PreferenceManager;

import java.util.List;

import com.gerwalex.gerwalex.R;

/**
 * Activity fuer Preferences
 */
@SuppressWarnings("ConstantConditions")
public class AWPreferenceActivity extends PreferenceActivity {
    private static final int layout = R.layout.awlib_activity_preferences;

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        PreferenceManager.setDefaultValues(this, R.xml.awlib_preferences_allgemein, false);
        loadHeadersFromResource(R.xml.awlib_preference_headers, target);
    }
}
