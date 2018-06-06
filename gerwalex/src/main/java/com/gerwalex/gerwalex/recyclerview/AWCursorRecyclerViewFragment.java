package com.gerwalex.gerwalex.recyclerview;

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
import android.widget.TextView;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.adapters.AWCursorAdapter;
import com.gerwalex.gerwalex.application.AWApplication;
import com.gerwalex.gerwalex.database.AWDBConvert;
import com.gerwalex.gerwalex.database.AbstractDBHelper;

/**
 * Erstellt eine Liste ueber Daten einer Tabelle.
 * <p/>
 * In der RecyclerView wird als Tag der Name der nutzenden Klasse gespeichert und damit bei
 * OnRecyclerItemClick() bzw. OnRecyclerItemLongClick() im Parent mitgeliefert.
 * <p/>
 * Als Standard erhaelt die RecyclerView als ID den Wert des Layout. Durch args.setInt(VIEWID,
 * value) erhaelt die RecyclerView eine andere ID.
 */
public abstract class AWCursorRecyclerViewFragment extends AWBaseRecyclerViewFragment
        implements AWCursorAdapter.AWCursorAdapterBinder {
    protected int indexColumn;
    private AWCursorAdapter mAdapter;
    private int[] viewResIDs;
    private int[] fromResIDs;

    @Override
    protected final AWCursorAdapter createBaseAdapter() {
        mAdapter = createCursorAdapter();
        return mAdapter;
    }

    /**
     * Liefert den Adapter fuer die RecyclerView.
     *
     * @return Als Default wird ein Plain-{@link AWCursorAdapter} zurueckgegeben.
     */
    protected AWCursorAdapter createCursorAdapter() {
        return new AWCursorAdapter(this);
    }

    /**
     * @return Liefert den in {@link AWCursorRecyclerViewFragment#createCursorAdapter()} erstellten
     * Adapter zuruck.
     */
    public final AWCursorAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = createCursorAdapter();
        }
        return mAdapter;
    }

    /**
     * Startet den Loadermanager
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startOrRestartLoader(layout, args);
    }

    /**
     * Binden von Daten an eine View, die keine TextView ist.
     *
     * @param holder
     *         AWLibViewHolder. Hier sind alle Views zu finden.
     * @param view
     *         View
     * @param resID
     *         ResID der der Spalte des Cursors. Ist -1, wenn es mehr Views als CursorSpalten gibt.
     * @param cursor
     *         Aktueller Cursor
     * @param cursorPosition
     *         Position innerhalb des Cursors, dessen Daten gebunden werden sollen.
     *
     * @return true, wenn die View vollstaendig bearbeitet wurde.
     */
    protected boolean onBindView(AWLibViewHolder holder, View view, int resID, Cursor cursor,
                                 int cursorPosition) {
        return false;
    }

    /**
     * Belegt anhand der viewResIDs in Args die View. Das Format wird automatisch konvertiert.
     * Sollte nur gerufen werden, wenn die View nicht anderweitig belegt wird, sonden z.B. durch
     * Databinding
     *
     * @throws NullPointerException
     *         wenn viewResIDs oder fromResIDs null ist oder die in viewResIDs aufgefuehrte View
     *         nicht gefunden wird
     * @throws IllegalStateException
     *         Wenn eine View bearbeitet wird, die TextView ist und fillView(...) hat false
     *         zurueckgegeben.
     */
    public void onBindViewHolder(AWLibViewHolder holder, Cursor cursor, int position) {
        for (int viewPosition = 0; viewPosition < viewResIDs.length; viewPosition++) {
            int resID = viewResIDs[viewPosition];
            View view = holder.itemView.findViewById(resID);
            if (!onBindView(holder, view, resID, cursor, viewPosition) && fromResIDs != null &&
                    viewPosition < fromResIDs.length) {
                try {
                    AbstractDBHelper mDBHelper =
                            ((AWApplication) getContext().getApplicationContext()).getDBHelper();
                    TextView tv = (TextView) view;
                    String text = AWDBConvert.convert(mDBHelper, fromResIDs[viewPosition],
                            cursor.getString(viewPosition));
                    tv.setText(text);
                } catch (ClassCastException e) {
                    throw new IllegalStateException(
                            "View mit ResID " + resID + " [" + getString(resID) +
                                    "] ist keine TextView und muss in onBindView belegt werden.");
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewResIDs = args.getIntArray(VIEWRESIDS);
        fromResIDs = args.getIntArray(FROMRESIDS);
    }

    @Override
    public void onItemDismiss(long itemID, int position) {
    }

    @Override
    public void onItemMoved(long itemId, int fromPosition, int toPosition) {
    }

    @CallSuper
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
        if (cursor != null) {
            indexColumn = cursor.getColumnIndexOrThrow(getString(R.string._id));
        }
        getAdapter().swapCursor(cursor); // swap the new cursor in.
    }

    @Override
    public void onLoaderReset(Loader<Cursor> p1) {
        if (getAdapter() != null) {
            getAdapter().swapCursor(null);
        }
    }
}