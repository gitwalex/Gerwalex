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

import android.app.DatePickerDialog;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.gerwalex.gerwalex.activities.AWInterface;
import com.gerwalex.gerwalex.database.AWDBConvert;

/**
 * TextView fuer Eingabe Datum. Bei Klick wird ein DatePickerDialog gezeigt und eine Aenderung durch
 * dem OnDateSetListener bekanntgegeben
 */
public class AWDateTextView extends android.support.v7.widget.AppCompatTextView implements AWInterface, DatePickerDialog.OnDateSetListener, OnClickListener {
    private Calendar cal = Calendar.getInstance();
    private OnDateSetListener mOnDateSetListener;
    private int year, month, day;
    private OnDateValuesSetListener mOnDateValuesSetListener;

    public AWDateTextView(Context context) {
        super(context);
    }

    public AWDateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AWDateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @BindingAdapter({"onDateChanged"})
    public static void onDateChanged(AWDateTextView view, OnDateSetListener listener) {
        view.setOnDateChangedListener(listener);
    }

    @BindingAdapter({"onValueChanged"})
    public static void onDateChanged(AWDateTextView view, OnDateValuesSetListener listener) {
        view.setOnDateChangedListener(listener);
    }

    @BindingAdapter({"value"})
    public static void setValue(AWDateTextView view, Date date) {
        view.setDate(date);
    }

    /**
     * @return Liefert das aktuell angezeigte Datum zurueck
     */
    public Date getDate() {
        return cal.getTime();
    }

    /**
     * Setzt das uebergebene Datum. Die Zeit wird auf 00:00 gesetzt
     *
     * @param date Datum
     */
    public void setDate(Date date) {
        Calendar newCal = Calendar.getInstance();
        newCal.setTime(date);
        newCal.set(Calendar.MILLISECOND, 0);
        newCal.set(Calendar.SECOND, 0);
        newCal.set(Calendar.MINUTE, 0);
        newCal.set(Calendar.HOUR_OF_DAY, 0);
        if (!(newCal.getTimeInMillis() == cal.getTimeInMillis())) {
            cal = newCal;
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
            super.setText(AWDBConvert.convertDate(date));
        }
    }

    /**
     * Setzt das uebergebene Datum.
     *
     * @param date Datum im SQLiteFormat
     * @throws ParseException, wenn das Datum nicht geparst werden kann
     */
    public void setDate(String date) throws ParseException {
        Date d = AWDBConvert.mSqliteDateFormat.parse(date);
        setDate(d);
    }

    /**
     * Startet den DatumsDialog, wenn ein {@link OnDateSetListener} gesetzt ist
     */
    @Override
    public void onClick(View v) {
        if (mOnDateSetListener == null && mOnDateValuesSetListener == null) {
            return;
        }
        if (isFocusable()) {
            DatePickerDialog mDatePickerDialog = new DatePickerDialog(getContext(), this, year, month, day);
            mDatePickerDialog.show();
        }
    }

    /**
     * Wird gerufen, wenn das Datum im Dialog gesetzt wurde. In diesem Fall wird der Listener
     * gerufen, dass sich das Datum geaendert hat.
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        setDate(year, monthOfYear, dayOfMonth);
        if (mOnDateSetListener != null) {
            mOnDateSetListener.onDateChanged(this, cal);
        }
        if (mOnDateValuesSetListener != null) {
            mOnDateValuesSetListener.onDateChanged(this, year, monthOfYear, dayOfMonth);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        super.setOnClickListener(this);
        setFocusable(true);
        setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
    }

    /**
     * Setzt die uebergebenen Werte als Datum
     *
     * @param year  Jahr
     * @param month Monat
     * @param day   Tag des Monats
     */
    public void setDate(int year, int month, int day) {
        Calendar newCal = Calendar.getInstance();
        newCal.set(Calendar.YEAR, year);
        newCal.set(Calendar.MONTH, month);
        newCal.set(Calendar.DAY_OF_MONTH, day);
        setDate(newCal.getTime());
    }

    /**
     * OnHolderClickListener wird nicht beachtet. Stattdessen {@link OnDateSetListener}
     * implementieren
     *
     * @throws IllegalArgumentException bei jedem Aufruf.
     */
    @Override
    public void setOnClickListener(OnClickListener l) {
        throw new IllegalArgumentException("Nicht moeglich");
    }

    public void setOnDateChangedListener(OnDateValuesSetListener listener) {
        mOnDateValuesSetListener = listener;
    }

    /**
     * Registriert einen {@link OnDateSetListener}
     *
     * @param l OnDateSetListener
     */
    public void setOnDateChangedListener(OnDateSetListener l) {
        mOnDateSetListener = l;
    }

    /**
     * Wird mit dem Calendar gerufen, wenn sich das Datum geaendert hat.
     */
    public interface OnDateSetListener {
        /**
         * Wird gerufen, wenn das Datum eingegeben wurde.
         *
         * @param view View
         * @param cal  Calendar mit neuem Datum
         */
        void onDateChanged(AWDateTextView view, Calendar cal);
    }

    /**
     * Wird mit dem Calendar gerufen, wenn sich das Datum geaendert hat.
     */
    public interface OnDateValuesSetListener {
        /**
         * Wird gerufen, wenn das Datum eingegeben wurde.
         *
         * @param view  View
         * @param year  Jahr
         * @param month Monat
         * @param day   Tag
         */
        void onDateChanged(AWDateTextView view, int year, int month, int day);
    }
}
