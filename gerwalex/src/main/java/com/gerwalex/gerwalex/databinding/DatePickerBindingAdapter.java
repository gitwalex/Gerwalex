package com.gerwalex.gerwalex.databinding;

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

import android.databinding.BindingAdapter;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Binding-Adpter fuer DatePicker
 */
public class DatePickerBindingAdapter {
    @BindingAdapter({"year", "month", "day", "onDateChanged"})
    public static void setDate(DatePicker view, int year, int month, int day,
                               DatePicker.OnDateChangedListener listener) {
        view.init(year, month, day, listener);
    }

    @BindingAdapter({"date"})
    public static void setDate(DatePicker view, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        view.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
    }

    @BindingAdapter({"date", "onDateChanged"})
    public static void setDate(DatePicker view, Date date,
                               DatePicker.OnDateChangedListener listener) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        view.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                listener);
    }

    @BindingAdapter({"date"})
    public static void setDate(DatePicker view, long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        view.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
    }

    @BindingAdapter({"date", "onDateChanged"})
    public static void setDate(DatePicker view, long timeInMillis,
                               DatePicker.OnDateChangedListener listener) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        view.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                listener);
    }

    @BindingAdapter({"year", "month", "day"})
    public static void setDate(DatePicker view, int year, int month, int day) {
        view.updateDate(year, month, day);
    }

    @BindingAdapter({"year", "month"})
    public static void setYearMonth(DatePicker view, int year, int month) {
        setDate(view, year, month, view.getDayOfMonth());
    }

    @BindingAdapter({"month", "day"})
    public static void setMonthDay(DatePicker view, int month, int day) {
        setDate(view, view.getYear(), month, day);
    }

    @BindingAdapter(value = {"year", "day", "onDateChanged"}, requireAll = false)
    public static void setYearDay(DatePicker view, int year, int day,
                                  DatePicker.OnDateChangedListener listener) {
        setDate(view, year, view.getMonth(), day, listener);
    }

    @BindingAdapter({"year", "month", "onDateChanged"})
    public static void setYearMonth(DatePicker view, int year, int month,
                                    DatePicker.OnDateChangedListener listener) {
        setDate(view, year, month, view.getDayOfMonth(), listener);
    }

    @BindingAdapter({"month", "day", "onDateChanged"})
    public static void setMonthDay(DatePicker view, int month, int day,
                                   DatePicker.OnDateChangedListener listener) {
        setDate(view, view.getYear(), month, day, listener);
    }
}