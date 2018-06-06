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
import android.database.Cursor;
import android.databinding.BindingAdapter;
import android.graphics.Rect;
import android.support.annotation.CallSuper;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.TextView;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.activities.AWInterface;
import com.gerwalex.gerwalex.database.AWAbstractDBDefinition;

/**
 * AutoCompleteTextView (siehe  {@link AWAutoCompleteTextView#initialize (DBDefinition, String,
 * String[], boolean, int[])}.<br> Sendet eine Message nach einer TextAenderung. Threshold ist
 * standardmaessig 3.
 *
 * @see AWAutoCompleteTextView#onTextChanged(String newText)
 */
public abstract class AWAutoCompleteTextView
        extends android.support.v7.widget.AppCompatAutoCompleteTextView implements AWInterface, FilterQueryProvider, AdapterView.OnItemClickListener {
    protected OnTextChangedListener mOnTextChangeListener;
    private int columnIndex;
    private int fromResID;
    private String mMainColumn;
    private String mOrderBy;
    private String[] mProjection;
    private String mSelection;
    private String cursorText = "";
    private long selectionID;
    private AWAbstractDBDefinition tbd;
    private boolean initializedCalled;
    private boolean doValidateInput;

    public AWAutoCompleteTextView(Context context) {
        super(context);
    }

    public AWAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AWAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @BindingAdapter({"onTextChanged"})
    public static void onTextChanged(AWAutoCompleteTextView view, OnTextChangedListener listener) {
        view.setOnTextChangedListener(listener);
    }

    /**
     * @return Liefert die ID des selektierten Textes. Wenn {@link AWAutoCompleteTextView#validateInput(boolean
     * doValidateInput)} mit true gerufen wurde, die erste ID aus dem Cursor,ansonsten NOID.
     */
    public final long getSelectionID() {
        if (!doValidateInput) {
            return selectionID;
        } else {
            String text = getText().toString();
            if (text.equals(cursorText)) {
                return selectionID;
            }
        }
        return NOID;
    }

    /**
     * Initialisiert AutoCompleteTextView.
     *
     * @param tbd
     *         DBDefinition. Aus dieser Tabelle wird das Feld gelesen
     * @param selection
     *         selection
     * @param selectionArgs
     *         Argumente zur Selection
     * @param fromResID
     *         Feld, welches fuer die Selection benutzt werden soll.
     * @param orderBy
     *
     * @throws NullPointerException,
     *         wenn LoaderManager null ist.
     */
    public final void initialize(AWAbstractDBDefinition tbd, String selection,
                                 String[] selectionArgs, int fromResID, String orderBy) {
        if (!isInEditMode()) {
            initializedCalled = true;
            this.tbd = tbd;
            this.fromResID = fromResID;
            mMainColumn = tbd.columnName(this.fromResID);
            mProjection = new String[]{tbd.columnName(fromResID), tbd.columnName(R.string._id)};
            mSelection = tbd.columnName(fromResID) + " Like ? ";
            mOrderBy = orderBy;
            if (mOrderBy == null) {
                mOrderBy = "LENGTH(" + mMainColumn + ")";
            }
            if (selection != null) {
                if (selectionArgs != null) {
                    for (String sel : selectionArgs) {
                        selection = selection.replaceFirst("\\?", "'" + sel + "'");
                    }
                }
                mSelection = mSelection + " AND (" + selection + ")";
            }
            mSelection = mSelection + "  GROUP BY " + mMainColumn;
            mOrderBy = mOrderBy + ", " + mMainColumn;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) getAdapter();
        if (adapter != null) {
            Cursor c = adapter.swapCursor(null);
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            if (!initializedCalled) {
                throw new IllegalStateException("Method 'initialize(AWDBDefinition, String, " + "String[], int, String)' not called");
            }
            SimpleCursorAdapter mSimpleCursorAdapter = new SimpleCursorAdapter(getContext(),
                    android.R.layout.simple_dropdown_item_1line, null, mProjection,
                    new int[]{android.R.id.text1}, 0);
            mSimpleCursorAdapter
                    .setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
                        @Override
                        public CharSequence convertToString(Cursor cursor) {
                            return cursor.getString(columnIndex);
                        }
                    });
            mSimpleCursorAdapter.setFilterQueryProvider(this);
            setAdapter(mSimpleCursorAdapter);
            setOnItemClickListener(this);
            setSelectAllOnFocus(true);
            setFocusable(true);
            setFocusableInTouchMode(true);
            setThreshold(0);
            setDropDownBackgroundResource(R.color.white);
            setDropDownHeight(getLineHeight() * 18);
        }
    }

    /**
     * Wenn die View den Fokus verliert, wird geprueft, ob neue Eintraeg zugelassen sind. Ist dies
     * nicht der Fall, wird der Text auf den zuletzt  gueltigen Text zurueckgesetzt und dieser Text
     * versendet.
     */
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!focused) {
            dismissDropDown();
            if (doValidateInput) {
                setText(cursorText);
                onTextChanged(cursorText);
            }
        } else {
            if (!isInEditMode()) {
                performFiltering(getText(), 0);
            }
        }
    }

    /**
     * Wenn ein List-Item ausgewaehlt wird, wird eine Message mit dem ausgewaehlten Text gesendet.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectionID = id;
        cursorText = ((TextView) view).getText().toString().trim();
        onTextChanged(cursorText);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (doValidateInput) {
            onTextChanged(cursorText);
        } else {
            onTextChanged(text.toString());
        }
    }

    /**
     * Wird bei Textaenderungen gerufen.
     *
     * @param currentText
     *         Text
     */
    @CallSuper
    protected void onTextChanged(String currentText) {
        if (mOnTextChangeListener != null) {
            mOnTextChangeListener.onTextChanged(this, currentText, cursorText, selectionID);
        }
    }

    /**
     * Nach tippen wird hier nachgelesen. Es wird mit 'LIKE %constraint%' ausgewaehlt.
     * Hat der Cursor Daten und validierung ist eingeschaltet (es ist kein neuer Wert zugelassen),
     * wird die erste ID aus dem Cursor geholt und der  Text auf den entsprechenden Wert des Cursors
     * gesetzt.
     * Gibt es nur einen oder gar keinen Wert, wird Dropdown ausgeblendet
     *
     * @param constraint
     *         Text
     *
     * @return den neuen Cursor
     */
    @Override
    public Cursor runQuery(final CharSequence constraint) {
        selectionID = NOID;
        final String mConstraint = constraint == null ? "" : constraint.toString().trim();
        String[] mSelectionArgs = new String[]{"%" + constraint + "%"};
        final Cursor data = getContext().getContentResolver().query(tbd.getUri(), mProjection, mSelection, mSelectionArgs, mOrderBy);
        if (data.moveToFirst()) {
            selectionID = data.getLong(1);
            cursorText = data.getString(0).trim();
            if (data.getCount() == 1) {
                onTextChanged(cursorText);
            }
        }
        post(new Runnable() {
            @Override
            public void run() {
                if (data.getCount() == 1 && mConstraint.equals(cursorText)) {
                    dismissDropDown();
                } else {
                    if (hasFocus()) {
                        showDropDown();
                    }
                }
            }
        });
        columnIndex = data.getColumnIndexOrThrow(tbd.columnName(fromResID));
        return data;
    }

    public void setOnTextChangedListener(OnTextChangedListener onTextChangedListener) {
        mOnTextChangeListener = onTextChangedListener;
    }

    /**
     * Wenn diese Methode true zurueckliefert, sind nur Werte aus dem Cursor erlaubt. Wir ein
     * Wert erfasst, der nicht im Cursor vorhanden ist, wird der eingegebene Wert mit dem ersten
     * Wert aus dem Cursor ersetzt.
     *
     * @return default false. Neue Werte sind immer erlaubt.
     */
    protected void validateInput(boolean doValidating) {
        this.doValidateInput = doValidating;
    }

    /**
     * Interface fuer Listener auf Textaenderungen
     */
    public interface OnTextChangedListener {
        /**
         * Wird gerufen, wenn sich der Text einer View geaendert hat
         *
         * @param view
         *         view, deren Text sich geaendert hat
         * @param currentText
         *         Neuer Text.
         * @param newID
         *         ID aus der DB, wenn Nutzer ein Item aus dem Pulldown selektiert hat oder wenn
         *         der
         */
        void onTextChanged(View view, String currentText, String cursorText, long newID);
    }
}
