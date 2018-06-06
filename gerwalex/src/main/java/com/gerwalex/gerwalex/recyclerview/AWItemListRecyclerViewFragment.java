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

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.CallSuper;
import android.support.v4.content.Loader;
import android.view.View;

import com.gerwalex.gerwalex.adapters.AWItemListAdapter;
import com.gerwalex.gerwalex.adapters.AWItemListAdapterTemplate;

/**
 * Template fuer eine RecyclerView mit {@link AWItemListAdapterTemplate <T>}
 */
public abstract class AWItemListRecyclerViewFragment<T> extends AWBaseRecyclerViewFragment
        implements AWItemListAdapter.AWListAdapterBinder<T> {
    private AWItemListRecyclerViewListener<T> mSortedListRecyclerViewListener;
    private AWItemListAdapterTemplate<T> mAdapter;

    @Override
    protected final AWItemListAdapterTemplate<T> createBaseAdapter() {
        mAdapter = createListAdapter();
        return mAdapter;
    }

    protected abstract AWItemListAdapterTemplate<T> createListAdapter();

    /**
     * @return Liefert den Adapter zurueck
     */
    public AWItemListAdapterTemplate<T> getAdapter() {
        if (mAdapter == null) {
            mAdapter = createBaseAdapter();
        }
        return mAdapter;
    }

    @Override
    public int getItemViewType(T item, int position) {
        return getItemViewType(position);
    }

    @Override
    public final int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    /**
     * Activity kann (muss aber nicht) AWSortedListRecyclerViewListener implementieren. In diesem
     * Fall wird die entsprechende Methode bei Bedarf aufgerufen.
     *
     * @see AWBaseRecyclerViewListener
     */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mSortedListRecyclerViewListener = (AWItemListRecyclerViewListener<T>) activity;
        } catch (ClassCastException e) {
            // nix tun...
        }
    }

    @Override
    public void onBindViewHolder(AWLibViewHolder holder, T item, int position) {
    }

    @Override
    public void onItemDismiss(T item, int position) {
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getAdapter().reset();
    }

    @Override
    public final void onRecyclerItemClick(View view, int position, long id) {
        super.onRecyclerItemClick(view, position, id);
    }

    @CallSuper
    public void onRecyclerItemClick(View v, int position, T item) {
        if (mSortedListRecyclerViewListener != null) {
            mSortedListRecyclerViewListener.onItemListRecyclerItemClick(mRecyclerView, v, item);
        }
        super.onRecyclerItemClick(v, position, getAdapter().getItemId(position));
    }

    @Override
    public final boolean onRecyclerItemLongClick(View view, int position, long id) {
        return super.onRecyclerItemLongClick(view, position, id);
    }

    @CallSuper
    public boolean onRecyclerItemLongClick(View v, int position, T item) {
        boolean consumed =
                super.onRecyclerItemLongClick(v, position, getAdapter().getItemId(position));
        if (mSortedListRecyclerViewListener != null) {
            consumed = mSortedListRecyclerViewListener
                    .onItemListRecyclerItemLongClick(mRecyclerView, v, item);
        }
        return consumed;
    }
}
