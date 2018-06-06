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

import com.gerwalex.gerwalex.R;

/**
 * Created by alex on 04.03.2017.
 */
public class EmailPreference extends DialogPreference {
    private int mDialogLayoutResId = R.layout.awlib_pref_dialog_email;
    private String mText;

    public EmailPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EmailPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public EmailPreference(Context context) {
        this(context, null);
    }

    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

    public String getText() {
        return mText;
    }

    /**
     * Persisitiert die Mail-Adresse und stellt sie in die Summary ein.
     *
     * @param text
     *         Text
     */
    public void setText(String text) {
        mText = text;
        persistString(text);
        setSummary(text);
        callChangeListener(text);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Default value from attribute. Fallback value is set to 0.
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // Read the value. Use the default value if it is not possible.
        setText(restorePersistedValue ? getPersistedString(mText) : (String) defaultValue);
    }
}
