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

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.content.Loader;
import android.view.View;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.database.AWLoaderManagerEngine;

/**
 * LoaderFragment. Laedt mittels LoaderManager einen Cursor. Es werden folgende Argumente erwartet:
 * LAYOUT: Layout des Fragments
 * <p>
 * <p>
 * AWAbstractDBDefinition: AWAbstractDBDefinition des Fragments
 * <p>
 * VIEWRESIDS: VIEWRESIDS des Fragments FROMRESIDs: FROMRESIDs des Fragments
 * <p>
 * Ausserdem folgende optionale Argumente:
 * <p>
 * PROJECTION: Welche Spalten als Ergebnis erwartet werden. Ist diese nicht vorhanden, werden die
 * Spalten gemaess FROMRESIDs  geliefert
 * <p>
 * SELECTION: Selection fuer Cursor
 * <p>
 * SELECTIONARGS: Argumente fuer Selection
 * <p>
 * GROUPBY: GroupBy-Clause fuer Cursor
 * <p>
 * ORDERBY: OrderBy-Clause fuer Cursor. Ist diese nicht belegt, wird die OrderBy-Clause der
 * Tabellendefinition verwendet.
 * <p>
 * Beim Start des Loader wird in der ActionBar die ProgressBar-Indeterminate-Visibility auf true
 * gesetzt. Nach dem Laden wird diese wieder abgeschaltet.
 */
public abstract class AWLoaderFragment extends AWFragment
        implements AWLoaderManagerEngine.Callback {
    private View mProgressbar;
    private AWLoaderManagerEngine mLoaderEngine;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startOrRestartLoader(layout, args);
    }

    @CallSuper
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (mProgressbar != null) {
            mProgressbar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> p1) {
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mProgressbar != null) {
            mProgressbar.setVisibility(View.INVISIBLE);
        }
    }

    @CallSuper
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressbar = view.findViewById(R.id.awlib_default_recyclerview_progressbar);
    }

    @Override
    public void setCursorLoaderArguments(int p1, Bundle args) {
        if (mProgressbar != null) {
            mProgressbar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Initialisiert oder restartet einen Loader.
     *
     * @param loaderID
     *         id des loaders, der (nach-) gestartet werden soll</br>
     * @param args
     *         Argumente fuer Cursor
     */
    protected void startOrRestartLoader(int loaderID, Bundle args) {
        if (mLoaderEngine == null) {
            mLoaderEngine = new AWLoaderManagerEngine(this);
        }
        mLoaderEngine.startOrRestartLoader(loaderID, args);
    }
}
