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

import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.recyclerview.AWLibViewHolder;
import com.gerwalex.gerwalex.recyclerview.AWSimpleItemTouchHelperCallback;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static android.support.v7.widget.RecyclerView.OnScrollListener;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;

/**
 * Basis-Adapter fuer RecyclerView. Unterstuetzt Swipe und Drag.
 */
public abstract class AWBaseAdapter extends RecyclerView.Adapter<AWLibViewHolder> implements AWLibViewHolder.OnHolderClickListener, AWLibViewHolder.OnHolderLongClickListener {
    static final int UNDODELETEVIEW = -1;
    private static final int CHANGEDVIEW = UNDODELETEVIEW - 1;
    private final AWBaseAdapterBinder mBinder;
    private final int[] mViewHolderResIDs;
    private RecyclerView mRecyclerView;
    private int mTextResID = R.string.tvGeloescht;
    private AWOnScrollListener mOnScrollListener;
    private AWSimpleItemTouchHelperCallback callbackTouchHelper;
    private ItemTouchHelper mTouchHelper;
    private int onTouchStartDragResID = -1;
    private OnDragListener mOnDragListener;
    private OnSwipeListener mOnSwipeListener;
    private int mPendingChangedItemPosition = NO_POSITION;
    private int mPendingDeleteItemPosition = NO_POSITION;
    private int mPendingChangeLayout;

    /**
     * Erstellt Adapter mit Liste der ResIDs der moeglichen ViewHolder. Wenn dieser Kontruktor
     * benutzt wird, wird gemaess des in {@link AWBaseAdapter#getViewType(int)}} zuruckgegebenen
     * Wertes die View erstellt. Es sollte dann ggfs uberprueft werden, ob {@link
     * AWBaseAdapter#onViewHolderClick(AWLibViewHolder)} oder {@link AWBaseAdapter#onViewHolderLongClicked(AWLibViewHolder)}
     * ueberschrieben werden muss.
     *
     * @param viewHolderResIDs ResIds der moeglichen ViewHolder.
     */
    public AWBaseAdapter(@NonNull @LayoutRes int... viewHolderResIDs) {
        mBinder = null;
        mViewHolderResIDs = viewHolderResIDs;
    }

    /**
     * Initialisiert Adapter.
     *
     * @param binder Binder fuer onBindView
     */
    public AWBaseAdapter(AWBaseAdapterBinder binder) {
        mBinder = binder;
        mViewHolderResIDs = null;
    }

    public AWBaseAdapter(@NonNull AWBaseAdapterBinder binder, @NonNull @LayoutRes int... viewResIds) {
        mBinder = binder;
        mViewHolderResIDs = viewResIds;
    }

    /**
     * Cancels PendingSwipe
     */
    @CallSuper
    public void cancelPendingChange() {
        int position = mPendingChangedItemPosition;
        if (position != NO_POSITION) {
            mPendingChangedItemPosition = NO_POSITION;
            notifyItemChanged(position);
        }
    }

    /**
     * Cancels PendingDelete
     */
    @CallSuper
    public void cancelPendingDelete() {
        int position = mPendingDeleteItemPosition;
        if (position != NO_POSITION) {
            mPendingDeleteItemPosition = NO_POSITION;
            notifyItemChanged(position);
        }
    }

    private void configure() {
        if (callbackTouchHelper == null) {
            callbackTouchHelper = new AWSimpleItemTouchHelperCallback(this);
        }
        callbackTouchHelper.setIsSwipeable(mOnSwipeListener != null);
        callbackTouchHelper.setIsDragable(mOnDragListener != null);
        mTouchHelper = new ItemTouchHelper(callbackTouchHelper);
    }

    /**
     * Wird gerufen, wenn in einem Konstruktor ein {@link AWBaseAdapterBinder} gesetzt wurde.
     *
     * @param holderView ViewHolder
     * @return default: null
     */
    protected AWLibViewHolder createViewHolder(View holderView) {
        return null;
    }

    /**
     * Entfernt ein Item an der Position. Funktioniert nur, wenn {@link
     * AWBaseAdapter#setOnSwipeListener(OnSwipeListener)} mit einem SwipeListener gerufen wurde.
     * Adapter wird mittels notify informiert.
     *
     * @param position Position des items im Adapter, das entfernt werden soll.
     */
    private void dismissItem(int position) {
        if (position != NO_POSITION) {
            mPendingDeleteItemPosition = NO_POSITION;
            onItemDismissed(position);
        }
    }

    /**
     * @return Liste der IDs der Items, die nach remove bzw. drag noch vorhanden ist.
     */
    public List<Long> getItemIDs() {
        int size = getItemCount();
        List<Long> mItemIDList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            mItemIDList.add(getItemId(i));
        }
        return mItemIDList;
    }

    @Override
    public final int getItemViewType(int position) {
        if (mPendingDeleteItemPosition == position) {
            return UNDODELETEVIEW;
        }
        if (mPendingChangedItemPosition == position) {
            return CHANGEDVIEW;
        }
        return getViewType(position);
    }

    /**
     * @return Die Position eines PendingDeleteItems oder NO_POSITION
     */
    protected final int getPendingDeleteItemPosition() {
        return mPendingDeleteItemPosition;
    }

    /**
     * Hier kann eine Item durch Angabe der Position zum loeschen vorgemerkt werden. In diesem Fall
     * wird eine View mit 'Geloescht' bzw. 'Rueckgaengig' angezeigt. Wenn dann die RecyclerView
     * bewegt wird oder ein anderes Item zu Loeschung vorgemerkt wird, wird das Item tatsaechlich
     * aus dem Adapter entfernt.
     * Der Binder wird durch {@link AWBaseAdapterBinder#onItemDismissed(int)} informiert.
     *
     * @param position Position des Items
     */
    @CallSuper
    public void setPendingDeleteItemPosition(int position) {
        int mPending = mPendingDeleteItemPosition;
        if (mPendingDeleteItemPosition != NO_POSITION) {
            dismissItem(mPending);
        }
        if (mPending != position) {
            mPendingDeleteItemPosition = position;
            notifyItemChanged(position);
        }
    }

    /**
     * @return Die Position eines PendingSwipeItems oder NO_POSITION
     */
    protected int getPendingSwipeItemPosition() {
        return mPendingChangedItemPosition;
    }

    public final RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    /**
     * Liefert den Typ der View zu eine Position im Adapter. Ist in einem Konstruktor ein
     * {@link AWBaseAdapterBinder} gesetzt worden, wird dieser fuer die Ermittliung des ViewTypes
     * aufgerufen.
     * Sollte ueberschrieben werden, wenn in einem Konstruktor mehrer ResIds fuer Views gesetzt
     * wurden.
     *
     * @param position Position im Adapter
     * @return Typ der View. Siehe {@link RecyclerView.Adapter#getItemViewType}
     */
    public int getViewType(int position) {
        if (mBinder != null) {
            return mBinder.getItemViewType(position);
        }
        return super.getItemViewType(position);
    }

    @CallSuper
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        if (mTouchHelper != null) {
            mTouchHelper.attachToRecyclerView(mRecyclerView);
        }
        mOnScrollListener = new AWOnScrollListener();
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        // Erzwingen, dass Holder vom Typ CHANGEDVIEW und UNDOLETEVIEW immer
        // wieder neu erstellt werden
        mRecyclerView.getRecycledViewPool().setMaxRecycledViews(CHANGEDVIEW, 0);
        mRecyclerView.getRecycledViewPool().setMaxRecycledViews(UNDODELETEVIEW, 0);
    }

    /**
     * Wird aus {@link AWBaseAdapter#onBindViewHolder(AWLibViewHolder, int)}  gerufen.
     * Erbende Klassen muesen pruefen, ob der ItemViewType < 0 ist, in diesem Fall wird eine View
     * gezeigt, die hier bearbeitet wurde.
     */
    @CallSuper
    @Override
    public void onBindViewHolder(final AWLibViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case UNDODELETEVIEW:
                View view = holder.itemView.findViewById(R.id.llUndo);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int mPosition = mPendingDeleteItemPosition;
                        mPendingDeleteItemPosition = NO_POSITION;
                        notifyItemChanged(mPosition);
                    }
                });
                TextView tv = (TextView) holder.itemView.findViewById(R.id.tvGeloescht);
                tv.setText(mTextResID);
                view = holder.itemView.findViewById(R.id.llGeloescht);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position != NO_POSITION) {
                            mPendingDeleteItemPosition = NO_POSITION;
                        }
                        onItemDismissed(mPendingDeleteItemPosition);
                    }
                });
                break;
            default:
                if (onTouchStartDragResID != -1) {
                    holder.itemView.setHapticFeedbackEnabled(true);
                    View handleView = holder.itemView.findViewById(onTouchStartDragResID);
                    handleView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                                AWBaseAdapter.this.onStartDrag(holder);
                            }
                            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
                                AWBaseAdapter.this.onStopDrag(holder);
                            }
                            v.performClick();
                            return true;
                        }
                    });
                }
        }
    }

    @Override
    public AWLibViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View rowView;
        switch (itemType) {
            case UNDODELETEVIEW:
                rowView = inflater.inflate(R.layout.can_undo_view, viewGroup, false);
                break;
            case CHANGEDVIEW:
                rowView = inflater.inflate(mPendingChangeLayout, viewGroup, false);
                break;
            default:
                if (mViewHolderResIDs != null) {
                    rowView = inflater.inflate(mViewHolderResIDs[itemType], viewGroup, false);
                } else
                    rowView = mBinder.onCreateViewHolder(inflater, viewGroup, itemType);
        }
        AWLibViewHolder holder = createViewHolder(rowView);
        if (holder == null) {
            holder = new AWLibViewHolder(rowView);
        }
        holder.setOnClickListener(this);
        holder.setOnLongClickListener(this);
        return holder;
    }

    @CallSuper
    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
        mRecyclerView = null;
    }

    public final void onDragged(RecyclerView recyclerView, RecyclerView.ViewHolder from, RecyclerView.ViewHolder to) {
        mOnDragListener.onDragged(recyclerView, from, to);
        onItemMoved(from.getAdapterPosition(), to.getAdapterPosition());
    }

    /**
     * Wird gerufen, wenn ein Item entfernt wurde
     *
     * @param position Position des Items
     */
    protected void onItemDismissed(int position) {
    }

    /**
     * Wird gerufen, wenn ein Item in der Position veraendert wurde
     *
     * @param fromPosition alte Position
     * @param toPosition   neue Position
     */
    protected void onItemMoved(int fromPosition, int toPosition) {
    }

    private void onStartDrag(RecyclerView.ViewHolder holder) {
        holder.itemView.setPressed(true);
        mTouchHelper.startDrag(holder);
    }

    private void onStopDrag(AWLibViewHolder holder) {
        holder.itemView.setPressed(false);
    }

    public final void onSwiped(AWLibViewHolder viewHolder, int direction, int position, long id) {
        mOnSwipeListener.onSwiped(viewHolder, direction, position, id);
    }

    @Override
    public final void onViewHolderClick(AWLibViewHolder holder) {
        switch (holder.getItemViewType()) {
            case UNDODELETEVIEW:
                break;
            default:
                onViewHolderClicked(holder);
        }
    }

    /**
     * Wird gerufen, wenn ein ViewHolder gecklicked wurde.
     *
     * @param holder ViewHolder
     */
    protected void onViewHolderClicked(AWLibViewHolder holder) {
    }

    @Override
    public final boolean onViewHolderLongClick(AWLibViewHolder holder) {
        return onViewHolderLongClicked(holder);
    }

    /**
     * Wird gerufen, wenn ein ViewHolder long-gecklicked wurde.
     *
     * @param holder ViewHolder
     * @return default: false.
     */
    protected boolean onViewHolderLongClicked(AWLibViewHolder holder) {
        return false;
    }


    /**
     * Setzt den OnDragListener. In diesem Fall wird die RecyclerView Dragable     *
     *
     * @param listener OnDragListener
     */
    public final void setOnDragListener(OnDragListener listener) {
        mOnDragListener = listener;
        configure();
    }

    /**
     * Setzt den OnSwipeListener. In diesem Fall wird die RecyclerView Swipeable
     *
     * @param listener OnSwipeListener
     */
    public final void setOnSwipeListener(OnSwipeListener listener) {
        mOnSwipeListener = listener;
        configure();
    }

    /**
     * Durch setzen der resID der DetailView wird diese View als OneToch-Draghandler benutzt, d.h.
     * dass bei einmaligen beruehren dieses Items der Drag/Drop-Vorgang startet. Die resID muss in
     * onCreate() gesetzt werden.
     *
     * @param resID resID der View, bei deren Beruehrung der Drag/Drop Vorgand starten soll
     */
    public final void setOnTouchStartDragResID(@IdRes int resID) {
        this.onTouchStartDragResID = resID;
    }

    /**
     * Hier kann ein Item gesetzt werden, dass eine separate View anzeigt. Diese View ist entweder
     * vom Binder entsprechend zu setzen (in getItemViewType, OnCreateViewHolder) oder durch die
     * im Konstruktor uebergebeben viewResIds zu bestimmen. Wenn dann die
     * RecyclerView bewegt wird oder ein anderes Item zu gesetzt wird, wird die View wieder
     * zureuckgesetzt
     *
     * @param position Position des Items
     */
    @CallSuper
    public void setPendingChangedItemPosition(int position, @LayoutRes int layout) {
        if (mPendingChangedItemPosition != NO_POSITION) {
            cancelPendingChange();
        }
        mPendingChangeLayout = layout;
        this.mPendingChangedItemPosition = position;
        notifyItemChanged(position);
    }

    /**
     * Setzt die ResID des Textes, der in einer UndoleteView angezeigt wird. Wird die nicht gesetzt,
     * wird 'Geloescht' angezeigt.
     *
     * @param textresID textResID
     */
    public final void setTextResID(@StringRes int textresID) {
        mTextResID = textresID;
    }

    /**
     * Swap den Adapter nochmals in die RecyclerView. Sinnvoll, wenn sich Inhalte geaendert haben,
     * aber keine neuen Daten generiert werden sollen.
     */
    public final void swap() {
        mRecyclerView.swapAdapter(this, false);
    }

    public interface AWBaseAdapterBinder {
        int getItemViewType(int position);

        View onCreateViewHolder(LayoutInflater inflater, ViewGroup viewGroup, int itemType);
    }

    public interface OnDissmissListener {
        void onDismiss(int position);
    }

    public interface OnDragListener {
        void onDragged(RecyclerView recyclerView, RecyclerView.ViewHolder from,
                       RecyclerView.ViewHolder to);
    }

    public interface OnSwipeListener {
        void onSwiped(AWLibViewHolder viewHolder, int direction, final int position, final long id);
    }

    private class AWOnScrollListener extends OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case SCROLL_STATE_DRAGGING:
                    if (mPendingDeleteItemPosition != NO_POSITION) {
                        dismissItem(mPendingDeleteItemPosition);
                        cancelPendingDelete();
                    }
                    if (mPendingChangedItemPosition != NO_POSITION) {
                        cancelPendingChange();
                    }
                    break;
            }
            super.onScrollStateChanged(recyclerView, newState);
        }
    }
}

