package com.gerwalex.gerwalex.views;

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
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.AttributeSet;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.activities.AWInterface;
import com.gerwalex.gerwalex.database.AWAbstractDBDefinition;
import com.gerwalex.gerwalex.database.AWDBConvert;
import com.gerwalex.gerwalex.database.AWLoaderManagerEngine;

/**
 * Convenience-Klasse fuer TextView, die mit einem Loader hinterlegt ist. ID der TextView wird
 * durch xml vorgegeben. Alternativ wird die resID benutzt, die in initialize() uebergeben
 * wird. Der Wert wird als Waehrung angezeigt.
 */
public class AWLoaderTextViewCurrencyValue extends android.support.v7.widget.AppCompatTextView
        implements AWInterface, AWLoaderManagerEngine.Callback {
    private final Bundle args = new Bundle();
    private AWLoaderManagerEngine mLoaderManagerEngine;

    public AWLoaderTextViewCurrencyValue(Context context) {
        super(context);
    }

    public AWLoaderTextViewCurrencyValue(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AWLoaderTextViewCurrencyValue(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Initialisiert die TextView/ Loader und startet Loader
     *
     * @param lm
     *         LoaderManager
     * @param tbd
     *         DBDefinition fuer Loader
     * @param resID
     *         des Items, welches geladen wird. Wird fuer Anzeige der Daten im korrekten Format
     *         benoetigt.
     * @param projection
     *         Item , welches geladen werden soll.
     * @param selection
     *         Selection. Kann null sein
     * @param selectionArgs
     *         Argumente der Selection. Kann null sein.
     *
     * @throws RuntimeException
     *         wenn Argumente nicht vollstaendig sind oder nicht zueinander passen.
     */
    public void initialize(LoaderManager lm, @NonNull AWAbstractDBDefinition tbd, int resID, @NonNull String projection,
                           @Nullable String selection, @Nullable String[] selectionArgs) {
        if (resID == 0) {
            throw new RuntimeException(
                    "resID muss mit einem Wert initialisiert sein. Wird fuer Konvertieren der " +
                            "Daten benoetigt.");
        }
        // setzten der ID, wenn noch nicht vorhanden.
        if (getId() == NO_ID) {
            setId(resID);
        }
        args.putParcelable(DBDEFINITION, tbd);
        args.putStringArray(PROJECTION, new String[]{projection});
        args.putString(SELECTION, selection);
        args.putStringArray(SELECTIONARGS, selectionArgs);
        args.putIntArray(FROMRESIDS, new int[]{resID});
        // Starten des Loaders
        mLoaderManagerEngine = new AWLoaderManagerEngine(getContext(), lm, this);
        mLoaderManagerEngine.startOrRestartLoader(getId(), args);
    }

    /**
     * Belegt die TextView mit den Daten aus dem Cursor.
     *
     * @see LoaderManager.LoaderCallbacks#onLoadFinished(Loader, Object)
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        String text;
        if (data.moveToFirst()) {
            int rows = data.getCount();
            if (rows > 1) {
                throw new IllegalArgumentException(
                        "SQL-Statement liefert zu viele Daten(Rows: " + rows);
            }
            text = AWDBConvert.convertCurrency(data.getLong(0));
        } else {
            text = getContext().getString(R.string.na);
        }
        setText(text);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // keine Reference auf Daten gehalten. Nichts zu tun
    }

    /**
     * Setzt die Argumente fuer die Selection neu und restartet den Loader.
     */
    public void restart(String selection, String[] selectionArgs) {
        args.putString(SELECTION, selection);
        args.putStringArray(SELECTIONARGS, selectionArgs);
        mLoaderManagerEngine.startOrRestartLoader(getId(), args);
    }

    @Override
    public void setCursorLoaderArguments(int loaderID, Bundle args) {
    }
}
