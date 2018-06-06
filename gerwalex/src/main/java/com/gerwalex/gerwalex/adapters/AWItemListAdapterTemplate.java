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
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gerwalex.gerwalex.recyclerview.AWLibViewHolder;

/**
 * Template eines Adapters mit Liste.
 */
public abstract class AWItemListAdapterTemplate<T> extends AWBaseAdapter {
    protected final AWListAdapterBinder<T> mBinder;
    private ItemGenerator<T> mItemgenerator;
    private Cursor mCursor;
    private T mPendingChangedItem;
    private T mPendingDeleteItem;

    public AWItemListAdapterTemplate(@NonNull AWListAdapterBinder<T> binder) {
        super(binder);
        mBinder = binder;
    }

    /**
     * Fuegt ein Item der Liste hinzu.
     */
    public abstract int add(@NonNull T item);

    /**
     * Fuegt alle Items einer Liste hinzu.
     *
     * @param items
     *         Liste mit Items.
     */
    public abstract void addAll(@NonNull List<T> items);

    /**
     * Fuegt alle Items zu einer Liste hinzu.
     *
     * @param items
     *         Array mit Items.
     */
    public abstract void addAll(@NonNull T[] items);

    @Override
    public final void cancelPendingChange() {
        mPendingChangedItem = null;
        super.cancelPendingChange();
    }

    @Override
    public final void cancelPendingDelete() {
        mPendingDeleteItem = null;
        super.cancelPendingDelete();
    }

    /**
     * Erstellt Items aus einem Cursor.
     *
     * @param start
     *         Startposition des Cursors, ab der Items generiert werden sollen
     *
     * @return Liste mit neuen Items
     */
    protected final T fillItemList(int start) {
        mCursor.moveToPosition(start);
        return mItemgenerator.createItem(mCursor);
    }

    /**
     * @param position
     *         Position des Items
     *
     * @return Liefert ein Item an der Position zuruck.
     */
    public abstract T get(int position);

    /**
     * @return Liefert die ID zuruck.
     */
    protected abstract long getID(@NonNull T item);

    /**
     * @return die Anzahl der Items. Gibt es einen Cursor, wird die Anzahl der Cursorzeilen
     * zurueckgeliefert. Erbende Klassen muessen in {@link AWItemListAdapterTemplate#onBindViewHolder(AWLibViewHolder,
     * int)} entsprechend nachlesen.
     */
    @Override
    public final int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }
        return getItemListCount();
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
    public abstract long getItemId(int position);

    public List<T> getItemList() {
        List<T> itemList = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            itemList.add(get(i));
        }
        return itemList;
    }

    /**
     * @return Liefert die aktuelle Anzahl der Items in der Liste zuruck
     */
    public abstract int getItemListCount();

    /**
     * @return Das aktuelle PendingChangedItem. Ist keins gesetzt, dann null.
     */
    public final T getPendingChangedItem() {
        return mPendingChangedItem;
    }

    /**
     * @return Das aktuelle PendingDeleteItem. Ist keins gesetzt, dann null.
     */
    public final T getPendingDeleteItem() {
        return mPendingDeleteItem;
    }

    /**
     * Hier kann eine Item zu loeschen vorgemerkt werden. In diesem Fall wird eine View mit
     * 'Geloescht' bzw. 'Rueckgaengig' angezeigt. Wenn dann die RecyclerView bewegt wird oder ein
     * anderes Item zu Loeschung vorgemerjt wird, wird das Item tatsaechlich aus dem Adapter
     * entfernt.
     * Der Binder wird durch {@link AWBaseAdapterBinder#onItemDismissed(int)} informiert.
     *
     * @param item
     *         Item
     */
    public final void setPendingDeleteItem(@NonNull T item) {
        super.setPendingDeleteItemPosition(getPosition(item));
        mPendingDeleteItem = item;
    }

    /**
     * @param item
     *         Item
     *
     * @return Liefert die Position des Items
     */
    public abstract int getPosition(@NonNull T item);

    /**
     * @return Liefert die Liste der entfernten Items zurueck
     */
    public abstract List<T> getRemovedItemList();

    /**
     * @param item
     *         Item
     *
     * @return Liefert den Index eines Items zuruck
     */
    public abstract int indexOf(@NonNull T item);

    @CallSuper
    @Override
    public void onBindViewHolder(AWLibViewHolder holder, int position) {
        if (holder.getItemViewType() != UNDODELETEVIEW) {
            mBinder.onBindViewHolder(holder, get(position), position);
        }
        super.onBindViewHolder(holder, position);
    }

    /**
     * Wird gerufen, wenn ein Item entfernt wird.
     *
     * @param position
     *         Position
     */
    @Override
    protected final void onItemDismissed(int position) {
        onItemDismissed(mPendingDeleteItem, position);
    }

    /**
     * Wird gerufen, wenn ein Item geloscht wird
     *
     * @param mPendingDeleteItem
     *         Item
     * @param position
     *         Position des Items
     */
    protected abstract void onItemDismissed(T mPendingDeleteItem, int position);

    /**
     * Wird gerufen, wenn ein Item die Position aendert
     *
     * @param fromPosition
     *         Urspruenglich Position des Items
     * @param toPosition
     *         Neue Position des Items
     */
    @Override
    public abstract void onItemMoved(int fromPosition, int toPosition);

    /**
     * Ruft bei Klick auf Item in der RecyclerView
     * {@link AWListAdapterBinder#onRecyclerItemClick(View, int, Object)}
     *
     * @param holder
     *         ViewHolder
     */
    @Override
    protected final void onViewHolderClicked(AWLibViewHolder holder) {
        View v = holder.itemView;
        int position = getRecyclerView().getChildAdapterPosition(holder.itemView);
        T item = get(position);
        mBinder.onRecyclerItemClick(v, position, item);
    }

    /**
     * Ruft bei LongKlick auf Item in der RecyclerView .
     * {@link AWListAdapterBinder#onRecyclerItemLongClick(View, int, Object)}
     *
     * @param holder
     *         ViewHolder
     */
    @Override
    protected final boolean onViewHolderLongClicked(AWLibViewHolder holder) {
        View v = holder.itemView;
        int position = getRecyclerView().getChildAdapterPosition(v);
        T item = get(position);
        return mBinder.onRecyclerItemLongClick(v, position, item);
    }

    /**
     * Kalkuliert Positionen von Items neu. Die Default-Implementierung macht hier nichts.
     */
    public void recalculatePositions() {
    }

    /**
     * Entfernt ein Item
     *
     * @param item
     *         Item
     *
     * @return true, wenn erfolgreich.
     */
    public abstract boolean remove(T item);

    /**
     * Entfernt ein Item an position
     *
     * @param position
     *         Position des Items
     *
     * @return das Item
     */
    public final T removeItemAt(int position) {
        T item = get(position);
        remove(item);
        return item;
    }

    /**
     * Setzt die Liste zurueck.
     */
    @CallSuper
    public void reset() {
        cancelPendingChange();
        cancelPendingDelete();
    }

    /**
     * Hier kann ein Item gesetzt werden, dass eine separate View anzeigt. Diese View ist vom Binder
     * entsprechend zu setzen (in getItemViewType, OnCreateViewHolder). Wenn dann die RecyclerView
     * bewegt wird oder ein anderes Item zu gesetzt wird, wird die View wieder zureuckgesetzt
     *
     * @param item
     *         Item
     */
    public final void setPendingChangedItem(T item, @LayoutRes int layout) {
        super.setPendingChangedItemPosition(getPosition(item), layout);
        mPendingChangedItem = item;
    }

    /**
     * Hier kann die Position eines Items gesetzt werden, dass eine separate View anzeigt. Diese
     * View ist vom Binder entsprechend zu setzen (in getItemViewType, OnCreateViewHolder). Wenn
     * dann die RecyclerView bewegt wird oder ein anderes Item  gesetzt wird, wird die View wieder
     * zureuckgesetzt
     *
     * @param position
     *         Position des Items
     */
    @Override
    public final void setPendingChangedItemPosition(int position, @LayoutRes int layout) {
        super.setPendingChangedItemPosition(position, layout);
        mPendingChangedItem = get(position);
    }

    @Override
    public final void setPendingDeleteItemPosition(int position) {
        super.setPendingDeleteItemPosition(position);
        mPendingDeleteItem = get(position);
    }

    /**
     * Tauscht den Cursor aus
     *
     * @param cursor
     *         cursor
     * @param generator
     *         Itemgenerator
     */
    @CallSuper
    public void swap(@NonNull Cursor cursor, @NonNull ItemGenerator<T> generator) {
        mItemgenerator = generator;
        mCursor = cursor;
    }

    /**
     * Tauscht die Liste aus
     *
     * @param items
     *         Liste mit Items
     */
    public abstract void swap(@NonNull List<T> items);

    /**
     * Tauscht die Liste aus
     *
     * @param items
     *         Array mit Items
     */
    public final void swap(T[] items) {
        if (items != null) {
            swap(Arrays.asList(items));
        } else {
            reset();
        }
    }

    /**
     * Tauscht das Item an der Stelle position aus.
     *
     * @param position
     *         Position
     * @param item
     *         Item
     */
    public abstract void updateItemAt(int position, @NonNull T item);

    /**
     * Binder fuer Adapter-Aktionen
     */
    public interface AWListAdapterBinder<T> extends AWBaseAdapterBinder {
        int getItemViewType(T item, int position);

        /**
         * Wird zum Binden des ViewHolders gerufen
         *
         * @param holder
         *         ViewHolder
         * @param item
         *         Item zum binden
         * @param position
         *         Position des Items
         */
        void onBindViewHolder(AWLibViewHolder holder, T item, int position);

        /**
         * Wird vom Adapter gerufen, wenn ein Item entfernt wird.
         *
         * @param item
         *         Item
         * @param position
         *         Position des Items
         */
        void onItemDismiss(T item, int position);

        /**
         * Wird vom Adapter gerufen, wenn ein Item verschoben wird
         *
         * @param fromPosition
         *         urspruengliche Position des Items
         * @param toPosition
         *         neue Position des Items
         */
        void onItemMoved(int fromPosition, int toPosition);

        /**
         * Wird bei Click auf RecyclerView gerufen
         *
         * @param v
         *         ItemView
         * @param position
         *         Position des Items
         * @param item
         *         Item
         */
        void onRecyclerItemClick(View v, int position, T item);

        /**
         * Wird bei LongClick auf RecyclerView gerufen
         *
         * @param v
         *         ItemView
         * @param position
         *         Position des Items
         * @param item
         *         Item
         */
        boolean onRecyclerItemLongClick(View v, int position, T item);
    }

    /**
     * Generator fuer Items. Wird im Zusammenhang mit einem Cursor verwendet. Siehe {@link
     * AWItemListAdapterTemplate#swap(Cursor, ItemGenerator)}
     */
    public interface ItemGenerator<T> {
        /**
         * Erstellt das Items ab der Position im Curosr. Der Cursor steht an der ersten
         * zu generierenden Position.
         *
         * @param c
         *         Aktueller Cursor. Steht schon an der Stelle, die Daten fuer das erste Item
         *         beinhaltet
         *
         * @return Item
         */
        T createItem(Cursor c);
    }
}
