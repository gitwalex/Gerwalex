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

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * AWLibViewHolder fuer RecyclerView
 */
public class AWLibViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {
    private OnHolderClickListener mOnHolderClickListener;
    private OnHolderLongClickListener mOnLonClickListener;
    private ViewDataBinding viewDataBinding;
    private Object mBindingVariable;

    /**
     * Erstellt AWLibViewHolder.
     *
     * @param view
     *         View fuer den Holder
     */
    public AWLibViewHolder(View view) {
        super(view);
    }

    /**
     * @return Liefert die in {@link AWLibViewHolder#setVariable(int, Object)} gesetzte Variable
     * zurueck
     */
    public Object getBindingVariable() {
        return mBindingVariable;
    }

    @Override
    public void onClick(View v) {
        if (mOnHolderClickListener != null) {
            mOnHolderClickListener.onViewHolderClick(this);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return mOnLonClickListener != null && mOnLonClickListener.onViewHolderLongClick(this);
    }

    /**
     * Databinding. Setzt das uebergebene Object als Handler fuer Databinding
     *
     * @param handlerID BR.handlerID
     * @param handler   handler fuer Databinding
     */
    public void setHandler(int handlerID, Object handler) {
        if (viewDataBinding == null) {
            viewDataBinding = DataBindingUtil.bind(itemView);
        }
        viewDataBinding.setVariable(handlerID, handler);
    }

    /**
     * Setzt einen OnHolderClickListener auf die View
     *
     * @param onHolderClickListener
     *         OnHolderClickListener
     */
    public void setOnClickListener(OnHolderClickListener onHolderClickListener) {
        mOnHolderClickListener = onHolderClickListener;
        itemView.setOnClickListener(this);
    }

    /**
     * Setzt einen OnLongClickListenerauf die View
     *
     * @param onHolderLongClickListener
     *         OnLongClickListenerauf
     */
    public void setOnLongClickListener(OnHolderLongClickListener onHolderLongClickListener) {
        mOnLonClickListener = onHolderLongClickListener;
        itemView.setOnLongClickListener(this);
    }

    /**
     * Setzt das uebergebene Tag mit dem Key direkt in der View.
     *
     * @param resID
     *         resID des TAG
     * @param object
     *         TAG
     */
    public void setTag(int resID, Object object) {
        itemView.setTag(resID, object);
    }

    /**
     * Databinding. Setzt das uebergebene Object als Item fuer Databinding. Das Object kann kit
     * {@link AWLibViewHolder#getBindingVariable()} wieder verwendet werden
     *
     * @param variableID
     *         BR.varialbleID
     * @param item
     *         Item fuer Databinding
     */
    public void setVariable(int variableID, Object item) {
        if (viewDataBinding == null) {
            viewDataBinding = DataBindingUtil.bind(itemView);
        }
        viewDataBinding.setVariable(variableID, item);
        viewDataBinding.executePendingBindings();
        mBindingVariable = item;
    }

    /**
     * Wird bei Click auf View gerufen, wenn durch {@link AWLibViewHolder#setOnClickListener(OnHolderClickListener)}
     * ein Listener gesetzt wurde.
     */
    public interface OnHolderClickListener {
        void onViewHolderClick(AWLibViewHolder holder);
    }

    /**
     * Wird bei LongClick auf View gerufen, wenn durch {@link AWLibViewHolder#setOnLongClickListener(OnHolderLongClickListener)}
     * ein Listener gesetzt wurde.
     */
    public interface OnHolderLongClickListener {
        boolean onViewHolderLongClick(AWLibViewHolder holder);
    }
}
