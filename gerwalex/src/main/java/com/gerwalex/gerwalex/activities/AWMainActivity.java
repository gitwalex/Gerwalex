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
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gerwalex.gerwalex.R;

/**
 * Template fuer Activities. Implementiert das globale Menu sowie die entsprechenden Reaktionen
 * darauf. Ausserdem wird dafuer gesorgt, dass bei Auswahl des MenuButtons des Geraetes der
 * OverFlow-Butten angezeigt wird.
 */
public abstract class AWMainActivity extends AWBasicActivity implements View.OnClickListener {
    /**
     * Layout fuer alle Activities. Beinhaltet ein FrameLayout als container ("container") und einen
     * DetailLayout ("containerDetail").
     */
    private static final int layout = R.layout.awlib_activity_main;
    /**
     * Default FloatingActionButton. Rechts unten, Icon ist 'Add', standardmaessig View.GONE
     */
    private FloatingActionButton mDefaultFAB;
    private MainAction mainAction;

    public FloatingActionButton getDefaultFAB() {
        return mDefaultFAB;
    }

    /**
     * Als Default wird hier nohelp zurueckgeliefert.
     *
     * @return ein Helpfile unter /assets/html
     */
    protected String getHelpFile() {
        return "nohelp.html";
    }

    public MainAction getMainAction() {
        return mainAction;
    }

    @Override
    public void onClick(View v) {
    }

    /**
     * Allgemeine Aufgaben fuer onCreate: - rufen von onCreate(Bundle, layout) mit Standardlayout. -
     * ContentView ist container in activity_container
     */
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onCreate(savedInstanceState, layout);
    }

    /**
     * - Initialisiert IntermediateProgress in ActionBar -  ermitteln der gesicherten Argumente -
     * HomeButton intialisieren - Ist der DetailContainer Visible, wird er auch (wieder) angezeigt.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState, int layout) {
        super.onCreate(savedInstanceState, layout);
        mainAction = args.getParcelable(AWLIBACTION);
        mDefaultFAB = findViewById(R.id.awlib_defaultFAB);
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
        bar.setHomeButtonEnabled(true);
        supportInvalidateOptionsMenu();
    }

    /**
     * Installiert ein Menu mit folgenden Items:
     * <p>
     * Rechner
     * <p>
     * Hilfe
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.awlib_activity_menu, menu);
        return true;
    }

    /**
     * Reagiert auf die MenuItems.
     * <p>
     * Bie Rechner wird ein BottomSheet mit einem Rechner gezeigt, bei Hilfe startet eine WebView
     * mit einem Hilfetext. Die ID des Hilfetextes wird ueber
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean isConsumed = false;
        Intent intent;
        int i = item.getItemId();
        if (i == R.id.awlib_menu_item_hilfe) {
            intent = new Intent(this, AWWebViewActivity.class);
            intent.putExtra(ID, getHelpFile());
            startActivity(intent);
            isConsumed = true;
        } else if (i == android.R.id.home) {
            setResult(RESULT_OK);
        }
        return isConsumed;
    }
}