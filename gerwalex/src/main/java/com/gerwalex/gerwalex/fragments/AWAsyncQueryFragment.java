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

import android.content.AsyncQueryHandler;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.gerwalex.gerwalex.application.AWApplication;
import com.gerwalex.gerwalex.database.AWAbstractDBDefinition;

/**
 * LoaderFragment. Laedt mittels LoaderManager einen Cursor. Es werden folgende Argumente erwartet:
 * <p>LAYOUT: Layout des Fragments</p> <p>DBDEFINITION: DBDefinition des Fragments</p>
 * <p>VIEWRESIDS: VIEWRESIDS des Fragments</p> <p>FROMRESIDs: FROMRESIDs des Fragments</p> Ausserdem
 * folgende optionale Argumente: <p>PROJECTION: Welche Spalten als Ergebnis erwartet werden. Ist
 * diese nicht vorhanden, werden alle Spalten der Tabelle geliefert</p> <p>SELECTION: Selection fuer
 * Cursor</p> <p>SELECTIONARGS: Argumente fuer Selection </p> <p>GROUPBY: GroupBy-Clause fuer
 * Cursor</p> <p>ORDERBY: OrderBy-Clause fuer Cursor. Ist diese nicht belegt, wird die
 * OrderBy-Clause der Tabellendefinition verwendet. <p> Beim Start des Loader wird in der ActionBar
 * die ProgressBar-Indeterminate-Visibility auf true gesetzt. Nach dem Laden wird diese wieder
 * abgeschaltet. Daher ist zwingend beim Ueberschreiben von {@link AWAsyncQueryFragment#onQueryComplete(int,
 * Object, Cursor)} super(...) zu rufen! }</p>
 */
public abstract class AWAsyncQueryFragment extends AWFragment {
    protected QueryHandler mQueryHelper;

    protected abstract void onQueryComplete(int token, Object cookie, Cursor cursor);

    @Override
    public void onStart() {
        super.onStart();
        mQueryHelper = new QueryHandler(this);
    }

    protected static class QueryHandler extends AsyncQueryHandler {
        private final AWAsyncQueryFragment mFragment;

        public QueryHandler(AWAsyncQueryFragment fragment) {
            super(fragment.getActivity().getContentResolver());
            mFragment = fragment;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            mFragment.onQueryComplete(token, cookie, cursor);
        }

        @Override
        public void startQuery(int token, Object cookie, Uri uri, String[] projection,
                               String selection, String[] selectionArgs, String orderBy) {
            AWApplication.Log("MonMaAsyncQuery: Starting Query");
            super.startQuery(token, cookie, uri, projection, selection, selectionArgs, orderBy);
        }

        public void startQuery(int token, Bundle args) {
            String[] mProjection;
            AWAbstractDBDefinition tbd = args.getParcelable(DBDEFINITION);
            if ((mProjection = args.getStringArray(PROJECTION)) == null) {
                mProjection = tbd.columnNames(args.getIntArray(FROMRESIDS));
            }
            String mSelection = args.getString(SELECTION);
            String[] mSelectionArgs = args.getStringArray(SELECTIONARGS);
            String mGroupBy = args.getString(GROUPBY);
            if (mGroupBy != null) {
                if (mSelection == null) {
                    mSelection = " 1=1";
                }
                mSelection = mSelection + " GROUP BY " + mGroupBy;
            }
            String mOrderBy = args.getString(ORDERBY);
            if (mOrderBy == null) {
                mOrderBy = tbd.getOrderString();
            }
            super.startQuery(token, args, tbd.getUri(), mProjection, mSelection, mSelectionArgs,
                    mOrderBy);
        }
    }
}
