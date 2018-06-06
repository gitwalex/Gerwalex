package com.gerwalex.gerwalex.activities;

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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.gerwalex.gerwalex.R;

/**
 * Basis-Activity fuer AWLib mit einem simplen Layout
 * Stellt ein Bundle args mit den extras aus Intent bereit
 * Erwartet eine Toolbar unter R.id.awlib_toolbar
 */
public abstract class AWBasicActivity extends AppCompatActivity implements AWInterface {
    /**
     * ID fuer Fragment-Container. Hier koennen Fragmente eingehaengt werden
     */
    protected static final int container = R.id.container4fragment;
    private static final int layout = R.layout.awlib_activity_simple;
    /**
     * Bundle fuer Argumente. Wird in SaveStateInstance gesichert und in onCreate
     * wiederhergestellt.
     */
    protected final Bundle args = new Bundle();
    private Toolbar mToolbar;

    /**
     * @return Liefert die Toolbar der View zurueck.
     */
    public Toolbar getToolbar() {
        return mToolbar;
    }

    /**
     * Hides a Keyboard
     *
     * @see "stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard"
     */
    public void hide_keyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        onCreate(savedInstanceState, layout);
    }

    /**
     * Bereitstellen der Argumente aus Intent bzw savedStateInstance in args.
     * Ist in den Args ein String unter {@link AWInterface#NEXTACTIVITY} vorhanden, wird diiese
     * Activity nachgestartet
     * Setzen der View
     *
     * @param savedInstanceState
     *         SavedState
     * @param layout
     *         layout fuer content
     */
    protected void onCreate(Bundle savedInstanceState, @LayoutRes int layout) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            args.putAll(extras);
        }
        if (savedInstanceState != null) {
            args.putAll(savedInstanceState);
            setSubTitle(args.getCharSequence(ACTIONBARSUBTITLE));
        }
        String nextActivity = args.getString(NEXTACTIVITY);
        if (nextActivity != null) {
            try {
                Intent intent = new Intent(this, Class.forName(nextActivity));
                args.remove(NEXTACTIVITY);
                Bundle newArgs = new Bundle(args);
                intent.putExtras(newArgs);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                //TODO Execption bearbeiten
                e.printStackTrace();
            }
        }
        setContentView(layout);
        mToolbar = (Toolbar) findViewById(R.id.awlib_toolbar);
        setSupportActionBar(mToolbar);
    }

    /**
     * Setzt den Subtitle aus args neu
     */
    @Override
    protected void onResume() {
        super.onResume();
        setSubTitle(args.getString(ACTIONBARSUBTITLE));
    }

    /**
     * Sicherung aller Argumente
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putAll(args);
        super.onSaveInstanceState(outState);
    }

    /**
     * Setzt den SubTitle in der SupportActionBar
     *
     * @param subTitleResID
     *         resID des Subtitles
     */
    public void setSubTitle(int subTitleResID) {
        setSubTitle(getString(subTitleResID));
    }

    /**
     * Setzt den SubTitle in der SupportActionBar, wenn vorhanden
     *
     * @param subTitle
     *         Text des Subtitles
     */
    public void setSubTitle(CharSequence subTitle) {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setSubtitle(subTitle);
        }
        args.putCharSequence(ACTIONBARSUBTITLE, subTitle);
    }
}
