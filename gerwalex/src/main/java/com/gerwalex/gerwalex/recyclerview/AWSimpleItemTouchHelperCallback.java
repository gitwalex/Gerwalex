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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.adapters.AWBaseAdapter;

/**
 * Helper fuer Drag- und/oder Swipe-RecyclerView
 */
public class AWSimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private final Paint mPaint = new Paint();
    private final AWBaseAdapter mAdapter;
    private Bitmap mIcon;
    private boolean isDragable;
    private boolean isSwipeable;
    private int mIconRessource = R.drawable.ic_action_discard;

    /**
     * @param adapter
     *         AWBaseAdapter
     */
    public AWSimpleItemTouchHelperCallback(@NonNull AWBaseAdapter adapter) {
        mAdapter = adapter;
        mPaint.setColor(Color.LTGRAY);
    }

    @NonNull
    public AWBaseAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Setzt die MovementFlags. In der Default-Implementation wird Dragging bei nach oben/unten
     * unterstuetzt, ausserdem Swipe bei links oder rechts.
     * <p>
     * handelt es sich beim LayoutManager um einen GridLayoutManager, wird nur Dragging (in alle
     * Richtungen) unterstuetzt.
     */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Set movement flags based on the layout manager
        int dragFlags = 0;
        int swipeFlags = 0;
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            if (isDragable) {
                dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT |
                        ItemTouchHelper.RIGHT;
            }
        } else {
            if (isDragable) {
                dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            }
            if (isSwipeable) {
                swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            }
        }
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public final boolean isItemViewSwipeEnabled() {
        return isSwipeable;
    }

    @Override
    public final boolean isLongPressDragEnabled() {
        return isDragable;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (mIcon == null) {
                Context mContext = recyclerView.getContext();
                mIcon = BitmapFactory.decodeResource(mContext.getResources(), mIconRessource);
            }
            View itemView = viewHolder.itemView;
            if (dX < 0) {
                // Draw Rect with varying left side, equal to the item's right side
                // plus negative displacement dX
                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                        (float) itemView.getRight(), (float) itemView.getBottom(), mPaint);
                //Set the image icon for Left swipe
                c.drawBitmap(mIcon, (float) itemView.getRight() - mIcon.getWidth(),
                        (float) itemView.getTop() +
                                ((float) itemView.getBottom() - (float) itemView.getTop() -
                                        mIcon.getHeight()) / 2, mPaint);
            } else {

            /* Set your color for positive displacement */
                // Draw Rect with varying right side, equal to displacement dX
                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                        (float) itemView.getBottom(), mPaint);
                // Set the image icon for Right swipe
                c.drawBitmap(mIcon, (float) itemView.getLeft(), (float) itemView.getTop() +
                        ((float) itemView.getBottom() - (float) itemView.getTop() -
                                mIcon.getHeight()) / 2, mPaint);
            }
            // Fade out the view as it is swiped out of the parent's bounds
            final float alpha = 1.0f - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        getAdapter().onDragged(recyclerView, viewHolder, target);
        return true;
    }

    @Override
    public final void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        long id = viewHolder.getItemId();
        getAdapter().onSwiped((AWLibViewHolder) viewHolder, direction, position, id);
    }

    /**
     * Steuert, ob eine RecyclerView Dragable ist
     *
     * @param isDragable
     *         true: ist Dragable
     */
    public final void setIsDragable(boolean isDragable) {
        this.isDragable = isDragable;
    }

    /**
     * Steuert, ob eine RecyclerView Swapable ist
     *
     * @param isSwipeable
     *         true: ist Swapable
     */
    public final void setIsSwipeable(boolean isSwipeable) {
        this.isSwipeable = isSwipeable;
    }
}