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
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.adapters.AWItemListAdapterTemplate;
import com.gerwalex.gerwalex.adapters.AWSortedItemListAdapter;
import com.gerwalex.gerwalex.recyclerview.AWItemListRecyclerViewFragment;
import com.gerwalex.gerwalex.recyclerview.AWLibViewHolder;

/**
 * FileChooser fuer Dateien. Ermittelt vor Anzeige die Berechtigung, wenn erforderlich.
 */
public class AWFileChooser extends AWItemListRecyclerViewFragment<File> {
    protected static final String DIRECTORYNAME = "DIRECTORYNAME";
    private static final int[] viewResIDs =
            new int[]{R.id.awlib_fileName, R.id.awlib_fileData, R.id.folderImage};
    private static final int viewHolderLayout = R.layout.awlib_filechooser_items;
    private static final int HASPARENTFOLDER = 1;
    protected boolean hasParent;
    protected String mDirectoy;
    private File mFile;
    private FilenameFilter mFilenameFilter;

    /**
     * Erstellt eine neue Instanz eines FileChooser, zeigt die Daten des uebergebenen
     * Verzeichnisnamen an
     *
     * @param directoryAbsolutPathName
     *         Absoluter Pafd des Directory
     * @return Fragment
     *
     * @throws IllegalStateException
     *         wenn das Verzeichnis kein Directory ist
     */
    public static AWFileChooser newInstance(@NonNull String directoryAbsolutPathName) {
        Bundle args = new Bundle();
        File file = new File(directoryAbsolutPathName);
        if (!file.isDirectory()) {
            throw new IllegalStateException("File ist kein Directory");
        }
        args.putString(DIRECTORYNAME, directoryAbsolutPathName);
        AWFileChooser fragment = new AWFileChooser();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Erstellt eine neue Instanz eines FileChooser, zeigt die Daten des uebergebenen
     * Verzeichnisnamen an
     *
     * @param directoryAbsolutPathName
     *         Absoluter Pafd des Directory
     * @param filterExtension
     *         Dateiextension, die gewaehlt werden soll.
     * @return Fragment
     *
     * @throws IllegalStateException
     *         wenn das Verzeichnis kein Directory ist
     */
    public static AWFileChooser newInstance(@NonNull String directoryAbsolutPathName,
                                            @NonNull String filterExtension) {
        Bundle args = new Bundle();
        args.putString(FILENAMEFILTER, filterExtension);
        AWFileChooser fragment = newInstance(directoryAbsolutPathName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Erstellt eine Liste der Files innerhalb eines Directories. Ist das File ungleich dem in
     * {@link AWFileChooser#newInstance(String)} angegebenen Directory, wird am Anfang der Liste der
     * Parent des uebergebenen Files eingefuegt. Damit kann eine Navigation erfolgen.
     * <p>
     * Die erstellte Liste wird direkt in den Adapter einestellt.
     * <p>
     * Ausserdem wird im Subtitle der Toolbar der Name des akuellten Verzeichnisses eingeblendet.
     *
     * @param file
     *         File, zu dem die Liste erstellt werden soll
     */
    private void createFileList(File file) {
        File[] files = file.listFiles(mFilenameFilter);
        List<File> mFiles = Arrays.asList(files);
        ArrayList<File> value = new ArrayList<>(mFiles);
        hasParent = !mDirectoy.toLowerCase().equals(file.getAbsolutePath().toLowerCase());
        if (hasParent) {
            value.add(0, file.getParentFile());
        }
        mFile = file;
        setTitle(file.getAbsolutePath());
        getAdapter().addAll(value);
    }

    @Override
    protected AWItemListAdapterTemplate<File> createListAdapter() {
        return new AWSortedItemListAdapter<File>(File.class, this) {
            @Override
            protected boolean areContentsTheSame(File item, File other) {
                return false;
            }

            @Override
            protected boolean areItemsTheSame(File item, File other) {
                return item.getAbsolutePath().equals(other.getAbsolutePath());
            }

            @Override
            protected int compare(File item, File other) {
                if (item.isDirectory() && !other.isDirectory()) {
                    // Directory before File
                    return -1;
                } else if (!item.isDirectory() && other.isDirectory()) {
                    // File after directory
                    return 1;
                } else {
                    // Otherwise in Alphabetic order...
                    return item.getName().compareTo(other.getName());
                }
            }

            @Override
            protected long getID(@NonNull File item) {
                return 0;
            }
        };
    }

    /**
     * Prueft, ob an Position 0 eine View eingefuegt werden muss, damit in das uebergeordnete
     * Verzeichnis gewechselt werden kann. Ansonsten wird der Default zuruckgeliefert.
     */
    @Override
    public int getItemViewType(File file, int position) {
        if (position == 0 && hasParent) {
            return HASPARENTFOLDER;
        }
        return super.getItemViewType(position);
    }

    /**
     * Sollte gerufen werden, bevor das Fragment beendet wird.
     *
     * @return true, wenn innerhalb der Verzeichnishierachie eine Stufe nach oben gegangen werden
     * konnte. Dann wird auch gleich eine neue Fileliste angezeigt.
     * <p>
     * Ansonsten false.
     */
    public boolean onBackPressed() {
        if (hasParent) {
            createFileList(mFile.getParentFile());
            return true;
        }
        return false;
    }

    @Override
    public void onBindViewHolder(AWLibViewHolder holder, File file, int position) {
        TextView tv;
        switch (holder.getItemViewType()) {
            case HASPARENTFOLDER:
                for (int resID : viewResIDs) {
                    View view = holder.itemView.findViewById(resID);
                    if (resID == R.id.folderImage) {
                        ImageView img = (ImageView) view;
                        img.setImageResource(R.drawable.ic_open_folder);
                    } else if (resID == R.id.awlib_fileName) {
                        tv = (TextView) view;
                        tv.setText("..");
                    } else if (resID == R.id.awlib_fileData) {
                        tv = (TextView) view;
                        tv.setText(file.getAbsolutePath());
                    }
                }
                break;
            default:
                for (int resID : viewResIDs) {
                    View view = holder.itemView.findViewById(resID);
                    if (resID == R.id.folderImage) {
                        ImageView img = (ImageView) view;
                        if (file.isDirectory()) {
                            img.setImageResource(R.drawable.ic_closed_folder);
                        } else {
                            img.setImageResource(R.drawable.ic_file_generic);
                        }
                    } else if (resID == R.id.awlib_fileName) {
                        tv = (TextView) view;
                        tv.setText(file.getName());
                    } else if (resID == R.id.awlib_fileData) {
                        tv = (TextView) view;
                        tv.setText(Formatter.formatFileSize(getContext(), file.length()));
                    }
                }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDirectoy = args.getString(DIRECTORYNAME);
        final String filenameFilter = args.getString(FILENAMEFILTER);
        mFilenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filenameFilter == null ||
                        filename.toLowerCase().endsWith("." + filenameFilter);
            }
        };
    }

    /**
     * Wird ein Directory ausgewaehlt, wird in dieses Directory gewechselt.
     */
    @Override
    public void onRecyclerItemClick(View v, int position, File item) {
        if (item.isDirectory()) {
            createFileList(item);
        } else {
            super.onRecyclerItemClick(v, position, item);
        }
    }

    /**
     * Wird ein Dateieintrag lang ausgewaehlt, wird ein Loeschen-Dialog angeboten.
     */
    @Override
    public boolean onRecyclerItemLongClick(View v, int position, final File file) {
        if (!file.isDirectory()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(R.string.awlib_btnAccept,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            File parent = file.getParentFile();
                            file.delete();
                            createFileList(parent);
                        }
                    });
            builder.setTitle(R.string.awlib_deleteFile);
            Dialog dlg = builder.create();
            dlg.show();
            return true;
        }
        return super.onRecyclerItemLongClick(v, position, file);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createFileList(new File(mDirectoy));
                }
        }
    }

    /**
     * Sobald die View erstellt wurde erste Liste zur Verfuegung stellen.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (ContextCompat
                .checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            createFileList(new File(mDirectoy));
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_STORAGE);
        }
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putIntArray(VIEWRESIDS, viewResIDs);
        args.putInt(VIEWHOLDERLAYOUT, viewHolderLayout);
    }
}

