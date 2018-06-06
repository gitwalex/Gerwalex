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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.database.AWDBConvert;

/**
 * Zeigt einen Betrag in der jeweiligen Waehrung an. Als Defult wird bei negativen Werten der Text
 * in rot gezeigt. Das kann durch {@link AWTextCurrency#setColorMode(boolean)} geaendert werden.
 */
public class AWTextCurrency extends android.support.v7.widget.AppCompatTextView {
    private static final int minCharacters = 10;
    private boolean colorMode = true;
    private long value;

    public AWTextCurrency(Context context) {
        this(context, null);
    }

    public AWTextCurrency(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.TextCurrency);
    }

    public AWTextCurrency(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * @return Liefert den aktuellen Wert zurueck
     */
    public long getValue() {
        return value;
    }

    /**
     * Setzt einen long-Wert als Text. Dieser wird in das entsprechende Currency-Format
     * umformatiert.
     *
     * @param amount
     *         Wert zur Anzeige
     */
    @SuppressLint("SetTextI18n")
    public void setValue(long amount) {
        if (!isInEditMode()) {
            value = amount;
            setText(AWDBConvert.convertCurrency(value));
            if (colorMode && value < 0) {
                setTextColor(Color.RED);
            } else {
                setTextColor(Color.BLACK);
            }
        } else {
            setText("0,00 €");
        }
    }

    /**
     * Initialisieret die Attribute.
     * Wenn nicht anders angegegeben: 'android:gravity=Gravity.End' und 'android:ems=10'
     *
     * @param attrs
     *         Attribute
     */
    private void init(AttributeSet attrs) {
        int gravity = Gravity.END;
        int ems = minCharacters;
        if (!isInEditMode()) {
            TypedArray a = getContext().getTheme()
                    .obtainStyledAttributes(attrs, R.styleable.AWTextCurrency, 0, 0);
            try {
                float val = a.getFloat(R.styleable.AWTextCurrency_value, 0f);
                value = (long) (val * AWDBConvert.mCurrencyDigits);
            } finally {
                if (attrs != null) {
                    gravity =
                            attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android",
                                    "gravity", Gravity.END);
                    ems =
                            attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android",
                                    "ems", minCharacters);
                }
                a.recycle();
            }
        }
        setGravity(gravity);
        setEms(ems);
        this.setValue(value);
    }

    /**
     * Setzt den ColorMode.
     *
     * @param colorMode
     *         colorMode. Wenn true (default), wird ein Negativer Wert rot dargestellt. Bei false
     *         werden alle Werte schwarz dargestellt.
     */
    public void setColorMode(boolean colorMode) {
        this.colorMode = colorMode;
    }
}
