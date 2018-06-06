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

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Convenience- Methoden fuer onViewHolderClick(View view), OnLongClick (View view). Liefert zu der View
 * weitere Parameter mit
 */
public interface AWItemListRecyclerViewListener<T> {
    /**
     * Convenience- Methode fuer onViewHolderClick(View v). Liefert zu der View weitere Parameter mit
     *
     * @param parent
     *         RecyclerView, die parent der View ist
     * @param view
     *         View, die gecliccked wurde
     * @param object,
     *         welches selektiert wurde
     */
    void onItemListRecyclerItemClick(RecyclerView parent, View view, T object);

    /**
     * Convenience- Methode fuer onViewHolderLongClick(View v). Liefert zu der View weitere Parameter mit
     *
     * @param recyclerView
     *         RecyclerView, die parent der View ist
     * @param view
     *         View, die gecliccked wurde
     * @param object,
     *         welches selektiert wurde
     */
    boolean onItemListRecyclerItemLongClick(RecyclerView recyclerView, View view, T object);
}
