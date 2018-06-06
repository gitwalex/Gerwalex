package com.gerwalex.gerwalex.fragments;

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

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

import com.gerwalex.gerwalex.R;

/**
 * Zeigt ein Image. Der Name des zu ladenden Files wird im Titel angezeigt.Gibt es im Bundle unter
 * 'FRAGMENTTITLE' einen Text, wird dieser als Titel angezeigt. Ansonsten der Letzte Teil des
 * Filenamens
 */
public class AWShowPicture extends AWFragment {
    private static final int layout = R.layout.awlib_zoomableimageview;
    private ImageView imageView;
    private TextView tvFilename;

    /**
     * Neue Instanz
     *
     * @param filename
     *         Filename des Bildes. Dieses Bild wird angezeigt.
     */
    public static AWShowPicture newInstance(@NonNull String filename) {
        AWShowPicture fragment = new AWShowPicture();
        Bundle args = new Bundle();
        args.putString(FILENAME, filename);
        fragment.setArguments(args);
        return fragment;
    }

    private void createPicture() {
        File file = new File(args.getString(FILENAME));
        Glide.with(getContext()).load(file).asBitmap().centerCrop().fitCenter().into(imageView);
        tvFilename.setText(file.getName());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_STORAGE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createPicture();
                }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvFilename = (TextView) view.findViewById(R.id.tvFilename);
        imageView = (ImageView) view.findViewById(R.id.imgView);
        if (ContextCompat
                .checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            createPicture();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_STORAGE);
        }
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
    }
}
