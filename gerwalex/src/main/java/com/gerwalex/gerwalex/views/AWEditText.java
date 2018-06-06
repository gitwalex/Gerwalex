package com.gerwalex.gerwalex.views;

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
import android.databinding.BindingAdapter;
import android.util.AttributeSet;

import com.gerwalex.gerwalex.activities.AWInterface;

/**
 * Created by alex on 02.03.2015.
 */
public class AWEditText extends android.support.design.widget.TextInputEditText
        implements AWInterface {
    private int mIndex;
    private OnTextChangedListener mOnTextChangedListener;
    private CharSequence oldText;

    @BindingAdapter({"onTextChanged"})
    public static void onTextChanged(AWEditText view, OnTextChangedListener listener) {
        view.setOnTextChangedListener(listener);
    }

    public AWEditText(Context context) {
        super(context);
    }

    public AWEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AWEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setFocusable(true);
        setFocusableInTouchMode(true);
        setSelectAllOnFocus(true);
    }

    /**
     * Informiert den OnTextChangedListener, wenn sich der Text geaendert hat.
     */
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (!(text.toString().equals(oldText)) && mOnTextChangedListener != null) {
            oldText = text;
            mOnTextChangedListener.onTextChanged(this, text.toString(), mIndex);
        }
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public void setOnTextChangedListener(OnTextChangedListener listener) {
        mOnTextChangedListener = listener;
    }

    public interface OnTextChangedListener {
        void onTextChanged(AWEditText view, String newText, int index);
    }
}
