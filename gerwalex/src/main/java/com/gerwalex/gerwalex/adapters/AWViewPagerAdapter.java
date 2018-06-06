package com.gerwalex.gerwalex.adapters;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Adapter fuer ViewPager
 */
public abstract class AWViewPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT;
    private final String tabtitles[];

    public AWViewPagerAdapter(Context context, FragmentManager fm, int[] tableTitlesResIDs) {
        super(fm);
        PAGE_COUNT = tableTitlesResIDs.length;
        tabtitles = new String[PAGE_COUNT];
        int i = 0;
        for (int resID : tableTitlesResIDs) {
            tabtitles[i] = context.getString(resID);
            i++;
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public abstract Fragment getItem(int position);

    @Override
    public CharSequence getPageTitle(int position) {
        return tabtitles[position];
    }
}