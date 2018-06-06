package com.gerwalex.gerwalex.adapters;

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
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.gerwalex.gerwalex.recyclerview.AWLibViewHolder;

/**
 * Adapter mit einer {@link ArrayList}. Diese Liste ist Swipe- und Dragable.
 */
public abstract class AWItemListAdapter<T> extends AWItemListAdapterTemplate<T> {
    private final ArrayList<T> removedItemList;
    private final ArrayList<T> itemList;

    public AWItemListAdapter(@NonNull AWListAdapterBinder<T> binder) {
        super(binder);
        itemList = new ArrayList<>();
        removedItemList = new ArrayList<>();
    }

    /**
     * @return neue Groesse der Liste
     */
    @Override
    public final int add(@NonNull T item) {
        if (itemList.add(item)) {
            notifyItemInserted(itemList.size());
        }
        return itemList.size();
    }

    /**
     * Fuegt alle Items einer Liste hinzu. Doppelte Items werden nicht erkannt!
     *
     * @param items
     *         Liste mit Items.
     */
    @Override
    public final void addAll(@NonNull List<T> items) {
        int oldSize = itemList.size();
        itemList.addAll(items);
        notifyItemRangeInserted(oldSize, itemList.size() - oldSize);
    }

    /**
     * Fuegt alle Items einer Liste hinzu. Doppelte Items werden nicht erkannt!
     *
     * @param items
     *         Array mit Items.
     */
    @Override
    public final void addAll(@NonNull T[] items) {
        addAll(Arrays.asList(items));
    }

    /**
     * @param position
     *         Position des Items
     *
     * @return Liefert ein Item an der Position zuruck.
     *
     * @throws IndexOutOfBoundsException
     *         wenn size < position oder position < 0
     */
    @Override
    public final T get(int position) {
        return itemList.get(position);
    }

    /**
     * @param position
     *         Position
     *
     * @return Liefert das Item an position zuruck
     *
     * @throws IndexOutOfBoundsException
     *         wenn size < position oder position < 0
     */
    @Override
    public final long getItemId(int position) {
        while (itemList.size() < position + 1) {
            itemList.add(fillItemList(itemList.size()));
        }
        return getID(itemList.get(position));
    }

    /**
     * @return Liefert die Liste der Items zurueck
     */
    public List<T> getItemList() {
        return itemList;
    }

    /**
     * @return Liefert die Anzahl der Items zuruck
     */
    @Override
    public final int getItemListCount() {
        return itemList.size();
    }

    @Override
    public int getPosition(@NonNull T item) {
        return itemList.indexOf(item);
    }

    @Override
    public List<T> getRemovedItemList() {
        return removedItemList;
    }

    /**
     * @param item
     *         Item
     *
     * @return Liefert den Index eines Items zuruck, -1 wenn kein Item existiert
     */
    @Override
    public final int indexOf(@NonNull T item) {
        return itemList.indexOf(item);
    }

    @CallSuper
    @Override
    public void onBindViewHolder(AWLibViewHolder holder, int position) {
        while (itemList.size() < position + 1) {
            itemList.add(fillItemList(itemList.size()));
        }
        super.onBindViewHolder(holder, position);
    }

    /**
     * Entfernt ein Item an der Position und ruft {@link AWListAdapterBinder#onItemDismissed(int)}
     *
     * @param position
     *         Position
     *
     * @throws IndexOutOfBoundsException
     *         wenn size < position oder position < 0
     */
    @Override
    public final void onItemDismissed(T item, int position) {
        remove(item);
        mBinder.onItemDismiss(item, position);
    }

    /**
     * Tauscht zwei Items in der Liste und ruft {@link AWListAdapterBinder#onItemMoved(int, int)}
     *
     * @param fromPosition
     *         Urspruenglich Position des Items
     * @param toPosition
     *         Neue Position des Items
     *
     * @throws IndexOutOfBoundsException
     *         wenn size < fromPosition oder fromPosition < 0 oder size < toPosition oder toPosition
     *         < 0
     */
    @Override
    public final void onItemMoved(int fromPosition, int toPosition) {
        Collections.swap(itemList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        mBinder.onItemMoved(fromPosition, toPosition);
    }

    /**
     * Entfernt ein Item
     *
     * @param item
     *         Item
     *
     * @return true, wenn erfolgreich.
     */
    @Override
    public final boolean remove(T item) {
        int position = getPosition(item);
        if (itemList.remove(item)) {
            removedItemList.add(item);
            notifyItemRemoved(position);
            return true;
        }
        return false;
    }

    /**
     * Setzt die Liste zurueck.
     */
    @Override
    public final void reset() {
        super.reset();
        itemList.clear();
        removedItemList.clear();
    }

    @Override
    public void swap(@NonNull Cursor cursor, @NonNull ItemGenerator<T> generator) {
        super.swap(cursor, generator);
        reset();
        if (cursor.getCount() > 0) {
            itemList.add(fillItemList(0));
        }
        notifyDataSetChanged();
    }

    @Override
    public void swap(@NonNull List<T> items) {
        reset();
        itemList.addAll(items);
        notifyDataSetChanged();
    }

    /**
     * Tauscht das Item an der Stelle position aus.
     *
     * @param position
     *         Position
     * @param item
     *         Item
     *
     * @throws IndexOutOfBoundsException
     *         wenn size < position oder position < 0
     */
    @Override
    public final void updateItemAt(int position, @NonNull T item) {
        itemList.add(position, item);
    }
}
