package com.gerwalex.gerwalex.bottomsheet;

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
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.List;

import com.gerwalex.gerwalex.R;

/**
 * BottomSheetDialog. Zeigt Texte zur Auswahl in einer RecyclerView als BottomSheet.
 */
public class AWBottomSheetDialog extends ExpandedBottomSheetDialog {
    private int checkMarkDrawableResId;
    private List<Integer> checkedItems;
    private int layout = R.layout.awlib_bottomsheet_recycler_view;
    private BottomSheetItemAdapter mAdapter;
    private BottomSheetListener mBottomSheetItemClickListener;
    private Bundle mExtras;
    private CharSequence mTitle;

    /**
     * @param context
     *         Context
     * @param arrayID
     *         ID eines StringArray. Diese ID wird bei {@link AWBottomSheetDialog#onBottomSheetItemClick(int,
     *         int, Bundle, CheckedTextView)} zuruckgeliefert
     */
    public AWBottomSheetDialog(Context context, int arrayID) {
        super(context);
        mAdapter =
                new BottomSheetItemAdapter(context.getResources().getStringArray(arrayID), arrayID);
    }

    /**
     * @param context
     *         Context
     * @param id
     *         ID, die bei click of Sheet zuruckgeliefert werden soll. {@link
     *         AWBottomSheetDialog#onBottomSheetItemClick(int, int, Bundle, CheckedTextView)}
     * @param items
     *         Items fuer BottomSheet
     */
    public AWBottomSheetDialog(Context context, int id, String[] items) {
        super(context);
        mAdapter = new BottomSheetItemAdapter(items, id);
    }

    /**
     * @param context
     *         Context
     * @param id
     *         ID, die bei click of Sheet zuruckgeliefert werden soll. {@link
     *         AWBottomSheetDialog#onBottomSheetItemClick(int, int, Bundle, CheckedTextView)}
     * @param items
     *         Items fuer BottomSheet
     */
    public AWBottomSheetDialog(Context context, int id, List<String> items) {
        super(context);
        String[] mItems = items.toArray(new String[items.size()]);
        mAdapter = new BottomSheetItemAdapter(mItems, id);
    }

    /**
     * Dissmiss des Dialogs, wenn nicht checkable.
     * <p/>
     * Ruf eines ggfs in {@link AWBottomSheetDialog#setItemClickListener(BottomSheetListener)}
     * gestzten Listeners. ArrayID und Bundle werden dann mit zurueckgeliefert.
     *
     * @param id
     *         ID des StringArray der MenuItems.
     * @param position
     *         Position des Item, welches geklickt wurde
     * @param extras
     *         Bundle, welches durch {@link AWBottomSheetDialog#setExtras(Bundle)} gesetzt wurde.
     * @param view
     *         CheckedTextView, die geclickt wurde
     */
    public void onBottomSheetItemClick(int id, int position, Bundle extras, CheckedTextView view) {
        if (checkedItems == null) {
            dismiss();
        }
        if (mBottomSheetItemClickListener != null) {
            mBottomSheetItemClickListener.onBottomSheetItemClick(id, position, extras, view);
        }
    }

    /**
     * Aufbau des Dialogs. Erstellt RecyclerView mit  Texten.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View contentView = View.inflate(getContext(), layout, null);
        RecyclerView recyclerView =
                (RecyclerView) contentView.findViewById(R.id.awlib_bottomSheetRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (mTitle != null) {
            TextView title = (TextView) contentView.findViewById(R.id.awlib_bottomSheetTitle);
            title.setText(mTitle);
            title.setVisibility(View.VISIBLE);
        }
        setContentView(contentView);
    }

    public void setCheckable(List<Integer> checkedItems) {
        this.checkedItems = checkedItems;
        TypedValue value = new TypedValue();
        getContext().getTheme()
                    .resolveAttribute(android.R.attr.listChoiceIndicatorMultiple, value, true);
        checkMarkDrawableResId = value.resourceId;
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Speichert ein Bundle, welches bei Click auf View wieder mitgeliefert wird.
     *
     * @param extras
     *         Bundle
     */
    public void setExtras(Bundle extras) {
        mExtras = extras;
    }

    /**
     * @param mBottomSheetItemClickListener
     *         Setzt den ClickListener fuer RecyclerView
     */
    public void setItemClickListener(BottomSheetListener mBottomSheetItemClickListener) {
        this.mBottomSheetItemClickListener = mBottomSheetItemClickListener;
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getContext().getString(titleId));
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
    }

    /**
     * Interface fuer Click auf Item der Recyclerview
     */
    public interface BottomSheetListener {
        /**
         * Wird bei Click auf Item der Recyclerview gerufen
         *
         * @param arrayID
         *         ID des arrays, dem die String entnommen wurden (gem. Konstruktor)
         * @param position
         *         Position, die geclickt wurde
         * @param extras
         *         Bundle, welches durch {@link BottomSheetItemAdapter#setExtras(Bundle extras) }
         * @param view
         *         CheckedTextView, die ausgewaehlt wurde
         */
        void onBottomSheetItemClick(int arrayID, int position, Bundle extras, CheckedTextView view);
    }

    /**
     * Adapter fuer die RecyclerView eines BottomSheets
     */
    public class BottomSheetItemAdapter
            extends RecyclerView.Adapter<BottomSheetItemAdapter.ViewHolder> {
        private final int mID;
        private final String[] mItems;

        /**
         * Erstellt Adapter
         *
         * @param items
         *         StringArray fuer Menutexte
         * @param id
         *         ID, die bei Click auf BottomSheet mitgeliefert werden soll
         */
        public BottomSheetItemAdapter(String[] items, int id) {
            mItems = items;
            mID = id;
        }

        @Override
        public int getItemCount() {
            return mItems.length;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setData(mItems[position], position);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                                                .inflate(R.layout.awlib_bottomsheet_item, parent,
                                                        false));
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public CheckedTextView textView;
            private int position;

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                textView = (CheckedTextView) itemView.findViewById(R.id.awlib_bottomSheetTextView);
                if (checkedItems != null) {
                    textView.setCheckMarkDrawable(checkMarkDrawableResId);
                }
            }

            @Override
            public void onClick(View v) {
                onBottomSheetItemClick(mID, position, mExtras, textView);
            }

            public void setData(String item, int position) {
                this.position = position;
                textView.setText(item);
                if (checkedItems != null && checkedItems.contains(position)) {
                    textView.setChecked(true);
                }
            }
        }
    }
}


