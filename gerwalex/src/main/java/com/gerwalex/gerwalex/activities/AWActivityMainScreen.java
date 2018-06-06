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
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;

import com.gerwalex.gerwalex.R;

/**
 * Abstracte Klasse fuer Navigation mit NavigatioView. Sollte von jeder App als Haupteinstieg
 * verwendet werden.
 * <p>
 * Bietet die Moeglichkeit, entweder einen ViewPager darzustellen oder nur in einfaches Fragment.
 * Die Steuerung fuer einen ViewPager efolgt ueber das Ueberschreiben von {@link
 * AWActivityMainScreen#getFragmentPagerAdapter()}. Hier muss dann ein ViewPagerAdapter geliefert
 * werden. Wird hier null zurueckgeliefert (Default), wird der Pager ausgeblendet und es koennen
 * Fragment in container (R.id.container4fragment) eingehaengt werden. Der Titel der Navigation wird
 * ueber {@link AWActivityMainScreen#getNavigationTitel()} festgelegt. Als Default wird 'Kein Titel'
 * angezeigt. Das NavigationMenu wird durch {@link AWActivityMainScreen#getNavigationMenuID()}
 * ermittelt. Als Default wird hier nur 'Einstellungen' zurueckgeliefert, dies bietet dann Infos zur
 * App und Sicherung/Restore der Datenbank als Preferenca an.
 * <p>
 */
@SuppressWarnings("ConstantConditions")
public abstract class AWActivityMainScreen extends AWMainActivity
        implements NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {
    private static final int layout = R.layout.awlib_activity_main_screen;
    protected DrawerLayout mDrawerLayout;
    private DrawerToggle mDrawerToggle;
    private ViewPager pager;

    /**
     * Wird hier ein FragmentPagerAdapter geliefert, wird dieser fuer einen ViewPager benutzt.
     * Fragment keonnen dann nicht mehr ueber den container (R.id.container4fragment) engehaengt
     * werden (sind nicht sichtbar).
     *
     * @return null in der Defaultimplementierung
     */
    protected FragmentPagerAdapter getFragmentPagerAdapter() {
        return null;
    }

    /**
     * Liefert die ID des Navigationsmenues.
     *
     * @return Ein einfaches Menu mit Preferences 'Einstellungen' fue die App und Sichern/Restore
     * der Datenbank
     */
    protected int getNavigationMenuID() {
        return R.menu.awlib_navigationdrawer;
    }

    /**
     * Titel in der App, wenn das Navigationsmenu geoeffnet wird.
     *
     * @return Defualt 'Kein Titel'
     */
    public int getNavigationTitel() {
        return R.string.awlib_noTitle;
    }

    /**
     * @return den genutzten ViewPager oder null, wenn kein Adapter in {@link
     * AWActivityMainScreen#getFragmentPagerAdapter()} geliefert wurde.
     */
    public ViewPager getPager() {
        return pager;
    }

    /**
     * Ist das Navigationsmenu offen, wird es geschlossen
     * <p>
     * oder
     * <p>
     * gibt es einen Pager und der Pager steht nicht auf der ersten Position, wird im Pager die
     * Position um eins vermindert.
     * <p>
     * oder
     * <p>
     * beenden der App.
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            // Drawer ist offen - close
            mDrawerLayout.closeDrawers();
            return;
        } else {
            if (pager != null && pager.getCurrentItem() != 0) {
                //  finish(), if the user is currently looking at the first step on this activity
                pager.setCurrentItem(pager.getCurrentItem() - 1);
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, layout);
        ActionBar bar = getSupportActionBar();
        bar.setHomeAsUpIndicator(R.drawable.ic_drawer);
        bar.setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.awlib_drawer_layout);
        mDrawerToggle = new DrawerToggle(this);
        NavigationView view = (NavigationView) findViewById(R.id.awlib_navigation_view);
        view.inflateMenu(getNavigationMenuID());
        view.setNavigationItemSelectedListener(this);
        FragmentPagerAdapter adapter = getFragmentPagerAdapter();
        if (adapter != null) {
            pager = (ViewPager) findViewById(R.id.awlib_pager);
            pager.setVisibility(View.VISIBLE);
            pager.setAdapter(adapter);
            pager.setOffscreenPageLimit(1);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.awlib_tabhost_main);
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setupWithViewPager(pager);
        } else {
            findViewById(R.id.container4fragment).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Diese Methode kann hier nicht verwendet werden, da die contentView festgelegt ist.
     *
     * @throws UnsupportedOperationException
     *         bei Aufruf
     */
    @Override
    protected void onCreate(Bundle savedInstanceState, int layout) {
        throw new UnsupportedOperationException("Diese Methode kann hier nicht verwendet werden");
    }

    /**
     * Gibt es kein eigenes NavigationsMenu ({@link AWActivityMainScreen#getNavigationMenuID()} ist
     * nicht ueberschrieben), wird eine Einstellung fuer Datanbankaktionen und Infos ueber die App
     * gezeigt.
     */
    @CallSuper
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawers();
        int i = item.getItemId();
        if (i == R.id.awlib_nav_Settings) {
            Intent intent = new Intent();
            intent.setClass(AWActivityMainScreen.this, AWPreferenceActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    /**
     * Ist der NavigationDrawer geoeffent, wird er geschlissen und vice versa.
     */
    @CallSuper
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean consumed;
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
            default:
                consumed = super.onOptionsItemSelected(item);
                break;
        }
        return consumed;
    }

    /**
     * Keine Aktion in der Default-Implementierung
     */
    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * Keine Aktion in der Default-Implementierung
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    /**
     * Keine Aktion in der Default-Implementierung
     */
    @Override
    public void onPageSelected(int position) {
    }

    /**
     * Entfernt den DrawerListener. Gibt es einen Pager, wird der OnPageListener entfernt und die
     * letzte Position gespeichert.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mDrawerLayout.removeDrawerListener(mDrawerToggle);
        if (pager != null) {
            pager.removeOnPageChangeListener(this);
            args.putInt(AWInterface.LASTSELECTEDPOSITION, pager.getCurrentItem());
        }
    }

    /**
     * Synkronisiert den Drawer
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    /**
     * r Setzt den DrawerListener. Gibt es einen Pager, wird die Activity als OnPageListener
     * registriert und die zuletzt gewaehlte Position aufgerufen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        if (pager != null) {
            pager.addOnPageChangeListener(this);
            pager.setCurrentItem(args.getInt(LASTSELECTEDPOSITION, 0));
        }
    }

    /**
     * DrawerToggle.
     */
    private class DrawerToggle extends ActionBarDrawerToggle
            implements DrawerLayout.DrawerListener {
        private CharSequence savedSubtitel;

        DrawerToggle(AWActivityMainScreen activity) {
            super(activity, mDrawerLayout, activity.getNavigationTitel(), R.string.Bearbeiten);
        }

        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            setTitle(AWActivityMainScreen.this.getNavigationTitel());
            ActionBar bar = getSupportActionBar();
            bar.setSubtitle(savedSubtitel);
        }

        /**
         * Called when a drawer has settled in a completely open state.
         */
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            setTitle(R.string.Bearbeiten);
            ActionBar bar = getSupportActionBar();
            savedSubtitel = bar.getSubtitle();
            bar.setSubtitle(null);
        }
    }
}