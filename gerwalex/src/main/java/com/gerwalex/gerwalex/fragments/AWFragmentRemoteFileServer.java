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

import android.os.Bundle;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.database.AbstractDBHelper;
import com.gerwalex.gerwalex.recyclerview.AWCursorRecyclerViewFragment;

/**
 * Fragment zur Anzeige der bisher konfigurierten RemoteFileServer
 */
public class AWFragmentRemoteFileServer extends AWCursorRecyclerViewFragment {
    private static final AbstractDBHelper.AWDBDefinition tbd =
            AbstractDBHelper.AWDBDefinition.RemoteServer;
    private static final int[] fromResIDs =
            new int[]{R.string.column_serverurl, R.string.column_userID,
                    R.string.column_connectionType, R.string.column_maindirectory};
    private static final int viewHolderLayout = R.layout.awlib_remote_fileserver;
    private static final int[] viewResIDs =
            new int[]{R.id.tvRemoteFileServerName, R.id.tvUserName, R.id.tvConnectionType,
                    R.id.tvBackupVerzeichnis};

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putParcelable(DBDEFINITION, tbd);
        args.putInt(VIEWHOLDERLAYOUT, viewHolderLayout);
        args.putIntArray(VIEWRESIDS, viewResIDs);
        args.putIntArray(FROMRESIDS, fromResIDs);
    }
}
