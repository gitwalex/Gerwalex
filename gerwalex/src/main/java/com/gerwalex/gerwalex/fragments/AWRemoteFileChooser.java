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

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.activities.AWInterface;
import com.gerwalex.gerwalex.adapters.AWItemListAdapterTemplate;
import com.gerwalex.gerwalex.adapters.AWSortedItemListAdapter;
import com.gerwalex.gerwalex.application.AWApplication;
import com.gerwalex.gerwalex.gv.AWRemoteFileServer;
import com.gerwalex.gerwalex.recyclerview.AWItemListRecyclerViewFragment;
import com.gerwalex.gerwalex.recyclerview.AWLibViewHolder;
import com.gerwalex.gerwalex.utils.AWRemoteFileServerHandler;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import java.util.ArrayList;

import static android.net.Uri.withAppendedPath;

/**
 * Dialog zur Abfrage von Zugangsdaten fuer externe Sicherung der DB.
 */
public class AWRemoteFileChooser extends AWItemListRecyclerViewFragment<FTPFile>
        implements AWRemoteFileServerHandler.ExecutionListener,
        AWFragment.OnAWFragmentDismissListener, AWFragment.OnAWFragmentCancelListener, AWInterface {
    protected static final String DIRECTORYNAME = "DIRECTORYNAME";
    private static final int layout = R.layout.awlib_remote_filechooser;
    private static final int[] viewResIDs =
            new int[]{R.id.awlib_fileName, R.id.awlib_fileData, R.id.folderImage};
    private static final int viewHolderLayout = R.layout.awlib_filechooser_items;
    private static final FTPFileFilter mFileFilter = new FTPFileFilter() {
        @Override
        public boolean accept(FTPFile file) {
            String name = file.getName();
            // Nur durch den User beschreibbare Dateien,
            if (!file.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION)) {
                return false;
            }
            if (name.startsWith("..")) {
                return true;
            }
            // keine versteckten
            return !name.startsWith(".");
        }
    };
    private static final int BACKTOPARENT = 1;
    private final ArrayList<String> mDirectoyList = new ArrayList<>();
    private AWFragmentActionBar.OnActionFinishListener mOnActionFinishListener;
    private View mProgressServerConnection;
    private AWRemoteFileServer mRemoteFileServer;
    private AWRemoteFileServerHandler mRemoteFileServerHandler;
    private View mServerErrorLayout;
    private TextView mServerErrorTexte;
    private Uri mUri = Uri.parse("/");

    /**
     * Erstellt eine neue Instanz eines FileChooser, zeigt die Daten des uebergebenen
     * Verzeichnisnamen an
     *
     * @return Fragment
     *
     * @throws IllegalStateException
     *         wenn das Verzeichnis kein Directory ist
     */
    public static AWRemoteFileChooser newInstance(AWRemoteFileServer fileServer) {
        Bundle args = new Bundle();
        AWRemoteFileChooser fragment = new AWRemoteFileChooser();
        args.putParcelable(REMOTEFILESERVER, fileServer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected AWItemListAdapterTemplate<FTPFile> createListAdapter() {
        return new AWSortedItemListAdapter<FTPFile>(FTPFile.class, this) {
            @Override
            protected boolean areContentsTheSame(FTPFile item, FTPFile other) {
                return false;
            }

            @Override
            protected boolean areItemsTheSame(FTPFile item, FTPFile other) {
                return item.getName().equals(other.getName());
            }

            @Override
            protected int compare(FTPFile item, FTPFile other) {
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
            protected long getID(@NonNull FTPFile item) {
                return 0;
            }
        };
    }

    private AWRemoteFileServerHandler getExecuter() {
        if (mRemoteFileServerHandler == null) {
            mRemoteFileServerHandler = new AWRemoteFileServerHandler(mRemoteFileServer, this);
        }
        return mRemoteFileServerHandler;
    }

    @Override
    public int getItemViewType(FTPFile file, int position) {
        if (position == 0 && file.getName().equals("..")) {
            return BACKTOPARENT;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mOnActionFinishListener = (AWFragmentActionBar.OnActionFinishListener) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(
                    "Activity muss OnActionFinishedListener implementieren");
        }
    }

    public boolean onBackpressed() {
        return mDirectoyList.size() == 0;
    }

    @Override
    public void onBindViewHolder(AWLibViewHolder holder, FTPFile file, int position) {
        TextView tv;
        switch (holder.getItemViewType()) {
            case BACKTOPARENT:
                for (int resID : viewResIDs) {
                    View view = holder.itemView.findViewById(resID);
                    if (resID == R.id.folderImage) {
                        ImageView img = (ImageView) view;
                        img.setImageResource(R.drawable.ic_open_folder);
                    } else if (resID == R.id.awlib_fileName) {
                        tv = (TextView) view;
                        if (mDirectoyList.size() == 0) {
                            tv.setText(".");
                        } else {
                            tv.setText(file.getName());
                        }
                    } else if (resID == R.id.awlib_fileData) {
                        view.setVisibility(View.GONE);
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
                        view.setVisibility(View.VISIBLE);
                        tv = (TextView) view;
                        tv.setText(Formatter.formatFileSize(getContext(), file.getSize()));
                    }
                }
        }
    }

    @Override
    public void onCancel(int layoutID, DialogInterface dialog) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteFileServer = args.getParcelable(REMOTEFILESERVER);
    }

    @Override
    public void onDismiss(int layoutID, DialogInterface dialog) {
        if (!isCanceled) {
            if (!mRemoteFileServer.isValid()) {
                if (!mRemoteFileServer.isValid()) {
                    AWRemoteServerConnectionData f =
                            AWRemoteServerConnectionData.newInstance(mRemoteFileServer);
                    f.setOnDismissListener(this);
                    f.setOnCancelListener(this);
                    f.show(getFragmentManager(), null);
                }
            } else {
                getExecuter().listFilesInDirectory(mUri.getEncodedPath(), mFileFilter);
            }
        }
    }

    public void onEndFileServerTask(AWRemoteFileServerHandler.ConnectionFailsException result) {
        mProgressServerConnection.setVisibility(View.INVISIBLE);
        if (result == null) {
            FTPFile[] mFiles = mRemoteFileServerHandler.getFiles();
            getAdapter().addAll(mFiles);
            setTitle(mUri.getEncodedPath());
        } else {
            mServerErrorLayout.setVisibility(View.VISIBLE);
            mServerErrorTexte.setText(result.getStatusMessage());
        }
    }

    /**
     * Wird ein Directory ausgwaehlt, wird in dieses Directory gewechselt.
     */
    @Override
    public void onRecyclerItemClick(View v, int position, FTPFile file) {
        if (file.isDirectory()) {
            String filename = file.getName();
            if (filename.equals("..")) {
                if (mDirectoyList.size() != 0) {
                    mDirectoyList.remove(mDirectoyList.size() - 1);
                }
                mUri = Uri.parse("/");
                for (int i = 0; i < mDirectoyList.size(); i++) {
                    String dir = mDirectoyList.get(i);
                    mUri = withAppendedPath(mUri, dir);
                }
            } else {
                mDirectoyList.add(filename);
                mUri = withAppendedPath(mUri, filename);
            }
            getExecuter().listFilesInDirectory(mUri.getEncodedPath(), mFileFilter);
        } else {
            super.onRecyclerItemClick(v, position, file);
        }
    }

    /**
     * Wird ein Dateieintrag lang ausgewaehlt, wird ein Loeschen-Dialog angeboten.
     */
    @Override
    public boolean onRecyclerItemLongClick(View v, int position, FTPFile file) {
        if (file.isDirectory()) {
            AWApplication mAppContext = ((AWApplication) getContext().getApplicationContext());
            mUri = withAppendedPath(mUri, file.getName());
            mRemoteFileServer.setMainDirectory(mAppContext, mUri.getEncodedPath());
            if (mRemoteFileServer.isInserted()) {
                mRemoteFileServer.update(getActivity(), mAppContext.getDBHelper());
            } else {
                mRemoteFileServer.insert(getActivity(), mAppContext.getDBHelper());
            }
            mOnActionFinishListener.onActionFinishClicked(layout);
            return true;
        }
        return super.onRecyclerItemLongClick(v, position, file);
    }

    @Override
    public void onStart() {
        super.onStart();
        getExecuter().listFilesInDirectory(mUri.getEncodedPath(), mFileFilter);
    }

    public void onStartFileServerTask() {
        mProgressServerConnection.setVisibility(View.VISIBLE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mServerErrorLayout = view.findViewById(R.id.awlib_llServerError);
        mServerErrorTexte = (TextView) view.findViewById(R.id.awlib_tvServerError);
        mProgressServerConnection = view.findViewById(R.id.pbDlgServerConnection);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
        args.putIntArray(VIEWRESIDS, viewResIDs);
        args.putInt(VIEWHOLDERLAYOUT, viewHolderLayout);
    }
}

