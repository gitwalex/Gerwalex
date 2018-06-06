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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileFilter;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.application.AWApplication;
import com.gerwalex.gerwalex.fragments.AWShowPicture;

import static com.gerwalex.gerwalex.activities.AWInterface.FILENAME;

/**
 * Zeigt eine Gallery von Bildern im jpg-Format in einem ViewPager.
 */
public class AWGalleryActivity extends AppCompatActivity {
    private static int layout = R.layout.awactivity_gallery;

    @SuppressLint("MissingSuperCall")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout);
        ViewPager pager = (ViewPager) findViewById(R.id.GalleryPager);
        Bundle args = getIntent().getExtras();
        if (args != null) {
            String mDirectory = args.getString(FILENAME);
            if (mDirectory == null) {
                AWApplication.Log("Kein Verzeichnis in Intent unter 'FILENAME'");
                finish();
            } else {
                File directory = new File(mDirectory);
                if (directory.isDirectory()) {
                    File[] pictures = directory.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return pathname.getName().endsWith("jpg");
                        }
                    });
                    if (pictures != null) {
                        pager.setAdapter(
                                new ScreenSlidePagerAdapter(getSupportFragmentManager(), pictures));
                    } else {
                        AWApplication.Log("Kein Zugriff");
                    }
                } else {
                    AWApplication.Log(mDirectory + " ist kein Verzeichnis");
                    finish();
                }
            }
        } else {
            AWApplication.Log("Kein Verzeichnis in Intent unter 'FILENAME'");
            finish();
        }
    }

    /**
     * Adapter fuer Pictures
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private final File[] mPictures;

        /**
         * @param fm
         *         FragmentManager
         * @param pictures
         *         Picture-Files
         */
        ScreenSlidePagerAdapter(FragmentManager fm, File[] pictures) {
            super(fm);
            mPictures = pictures;
        }

        @Override
        public int getCount() {
            return mPictures == null ? 0 : mPictures.length;
        }

        @Override
        public Fragment getItem(int position) {
            return AWShowPicture.newInstance(mPictures[position].getAbsolutePath());
        }
    }
}
