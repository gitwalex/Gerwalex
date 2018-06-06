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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.application.AWApplication;
import com.gerwalex.gerwalex.database.AbstractDBHelper;
import com.gerwalex.gerwalex.gv.AWRemoteFileServer;
import com.gerwalex.gerwalex.utils.AWRemoteFileServerHandler;

import static com.gerwalex.gerwalex.utils.AWRemoteFileServerHandler.ConnectionType.SSL;

/**
 * Dialog fuer Abfrage von Zugangsdaten zu einem FileServer.
 */
public class AWRemoteServerConnectionData extends AWFragment {
    private static final int layout = R.layout.awlib_dialog_remote_fileserver;
    private static final int[] viewResIDs =
            new int[]{R.id.awlib_etDBServerName, R.id.awlib_etDBUserName};
    private static final int[] fromResIDs =
            new int[]{R.string.column_serverurl, R.string.column_userID};
    private EditText mPasswortEditText;
    private AWRemoteFileServer mRemoteFileServer;
    private EditText mUIDEditText;
    private EditText mURLEditText;

    public static AWRemoteServerConnectionData newInstance(AWRemoteFileServer fileServer) {
        Bundle args = new Bundle();
        AWRemoteServerConnectionData fragment = new AWRemoteServerConnectionData();
        args.putParcelable(REMOTEFILESERVER, fileServer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteFileServer = args.getParcelable(REMOTEFILESERVER);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View childView = inflater.inflate(layout, null);
        onViewCreated(childView, savedInstanceState);
        builder.setView(childView);
        Dialog dialog = builder.setPositiveButton(R.string.awlib_btnAccept,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mRemoteFileServer
                                .put(getContext(), R.string.column_serverurl,
                                        mURLEditText.getText().toString());
                        mRemoteFileServer
                                .put(getContext(), R.string.column_userID,
                                        mUIDEditText.getText().toString());
                        mRemoteFileServer.setUserPassword(mPasswortEditText.getText().toString());
                        if (mRemoteFileServer.isValid()) {
                            AWApplication mAppConfig =
                                    (AWApplication) getContext().getApplicationContext();
                            AbstractDBHelper db = mAppConfig.getDBHelper();
                            if (mRemoteFileServer.isInserted()) {
                                mRemoteFileServer.update(getContext(), db);
                            } else {
                                mRemoteFileServer.insert(getContext(), db);
                            }
                            View view = getView();
                            if (view != null) {
                                Snackbar.make(view, getString(R.string.awlib_datensatzSaved),
                                        Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    }
                }).setNegativeButton(R.string.awlib_btnCancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Nix tun
                    }
                }).setView(childView).create();
        // Wenn das Dialogfenster teilweise von der eingeblendeten Tatstatur
        // ueberlappt wird, resize des Fensters zulassen.
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setTitle(R.string.titleFileServerKonfigurieren);
        setRetainInstance(true);
        return dialog;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mURLEditText = (EditText) view.findViewById(R.id.awlib_etDBServerName);
        mUIDEditText = (EditText) view.findViewById(R.id.awlib_etDBUserName);
        mPasswortEditText = (EditText) view.findViewById(R.id.awlib_etDBUserPW);
        mPasswortEditText.setText(mRemoteFileServer.getUserPassword());
        CheckBox mConnectionTypeCheckBox = (CheckBox) view.findViewById(R.id.cbConnectionType);
        mConnectionTypeCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (buttonView.isChecked()) {
                            mRemoteFileServer
                                    .put(getContext(), R.string.column_connectionType, SSL.name());
                        } else {
                            mRemoteFileServer.put(getContext(), R.string.column_connectionType,
                                    AWRemoteFileServerHandler.ConnectionType.NONSSL.name());
                        }
                    }
                });
        mConnectionTypeCheckBox.setChecked(mRemoteFileServer.getConnectionType() == SSL);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
        args.putIntArray(VIEWRESIDS, viewResIDs);
        args.putIntArray(FROMRESIDS, fromResIDs);
    }
}
