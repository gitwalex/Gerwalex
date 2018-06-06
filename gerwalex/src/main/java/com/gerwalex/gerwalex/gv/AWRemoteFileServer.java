package com.gerwalex.gerwalex.gv;

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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.database.AbstractDBHelper;
import com.gerwalex.gerwalex.utils.AWRemoteFileServerHandler.ConnectionType;

import static com.gerwalex.gerwalex.activities.AWInterface.NOID;
import static com.gerwalex.gerwalex.utils.AWRemoteFileServerHandler.ConnectionType.SSL;

/**
 * Stammdaten fuer einen AWRemoteFileServer.
 */
public class AWRemoteFileServer implements Parcelable {
    public static final Creator<AWRemoteFileServer> CREATOR =
            new Creator<AWRemoteFileServer>() {
                @Override
                public AWRemoteFileServer createFromParcel(Parcel source) {
                    return new AWRemoteFileServer(source);
                }

                @Override
                public AWRemoteFileServer[] newArray(int size) {
                    return new AWRemoteFileServer[size];
                }
            };
    private static final AbstractDBHelper.AWDBDefinition tbd =
            AbstractDBHelper.AWDBDefinition.RemoteServer;
    private final String mSelection = tbd.columnName(R.string._id) + " = ?";
    private ContentValues currentContent = new ContentValues();
    private ConnectionType mConnectionType;
    private long mID = NOID;
    private String mMainDirectory;
    private String[] mSelectionArgs;
    private String mURL;
    private String mUserID;
    private String mUserPassword;

    public AWRemoteFileServer(Context context) {
        put(context, R.string.column_connectionType, SSL.name());
    }

    public AWRemoteFileServer(Context context, long id)
            throws AWApplicationGeschaeftsObjekt.LineNotFoundException {
        fillContent(context, id);
        mSelectionArgs = new String[]{String.valueOf(id)};
        mURL = currentContent.getAsString(context.getString(R.string.column_serverurl));
        mUserID = currentContent.getAsString(context.getString(R.string.column_userID));
        mMainDirectory =
                currentContent.getAsString(context.getString(R.string.column_maindirectory));
        this.mConnectionType = ConnectionType.valueOf(
                currentContent.getAsString(context.getString(R.string.column_connectionType)));
        if (mURL != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            mUserPassword = prefs.getString(mURL, null);
        }
    }

    protected AWRemoteFileServer(Parcel in) {
        int tmpMConnectionType = in.readInt();
        this.mConnectionType =
                tmpMConnectionType == -1 ? null : ConnectionType.values()[tmpMConnectionType];
        this.mMainDirectory = in.readString();
        this.mURL = in.readString();
        this.mUserID = in.readString();
        this.mUserPassword = in.readString();
        this.currentContent = in.readParcelable(ContentValues.class.getClassLoader());
        this.mSelectionArgs = in.createStringArray();
        this.mID = in.readLong();
    }

    public int delete(Context context, AbstractDBHelper db) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int result = db.delete(tbd.getUri(), mSelection, mSelectionArgs);
        if (result != 0) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(mURL).apply();
        }
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Fuellt das Geschaeftsobjekt anhand der uebergebenen Daten aus der DB
     *
     * @param id
     *         id des Objektes
     * @throws AWApplicationGeschaeftsObjekt.LineNotFoundException
     *         wenn keine Zeile gefunden wurde.
     */
    public final void fillContent(Context context, Long id)
            throws AWApplicationGeschaeftsObjekt.LineNotFoundException {
        mSelectionArgs = new String[]{id.toString()};
        Cursor c = context.getContentResolver()
                          .query(tbd.getUri(), tbd.columnNames(tbd.getTableItems()), mSelection,
                                  mSelectionArgs, null);
        try {
            if (c.moveToFirst()) {
                currentContent.clear();
                for (int i = 0; i < c.getColumnCount(); i++) {
                    String value = c.getString(i);
                    if (value != null) {
                        currentContent.put(c.getColumnName(i), c.getString(i));
                    }
                }
                currentContent.remove(context.getString(R.string._id));
            } else {
                throw new AWApplicationGeschaeftsObjekt.LineNotFoundException(
                        tbd.name() + ": Zeile mit id " + id + " nicht gefunden.");
            }
        } finally {
            if (!c.isClosed()) {
                c.close();
            }
        }
    }

    public String getBackupDirectory() {
        return mMainDirectory;
    }

    public ConnectionType getConnectionType() {
        return mConnectionType;
    }

    /**
     * @return URL des Servers
     */
    public String getURL() {
        return mURL;
    }

    /**
     * @return UserID
     */
    public String getUserID() {
        return mUserID;
    }

    /**
     * @return Passwort
     */
    public String getUserPassword() {
        return mUserPassword;
    }

    /**
     * setzt Passwort
     */
    public void setUserPassword(String password) {
        mUserPassword = password;
    }

    public long insert(Context context, AbstractDBHelper db) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long id = -1;
        if (isValid()) {
            id = db.insert(tbd, null, currentContent);
            if (id != -1) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(mURL, mUserPassword).apply();
            }
        }
        mID = id;
        mSelectionArgs = new String[]{String.valueOf(id)};
        return id;
    }

    public boolean isInserted() {
        return mID != NOID;
    }

    public boolean isValid() {
        return mURL != null && mUserID != null && mUserPassword != null && mConnectionType != null;
    }

    public void put(Context context, int resID, Object value) {
        if (resID == R.string.column_serverurl) {
            mURL = (String) value;
        } else if (resID == R.string.column_userID) {
            mUserID = (String) value;
        } else if (resID == R.string.column_maindirectory) {
            mMainDirectory = (String) value;
        } else if (resID == R.string.column_connectionType) {
            mConnectionType = ConnectionType.valueOf((String) value);
        }
        currentContent.put(context.getString(resID), (String) value);
    }

    public void setMainDirectory(Context context, String mMainDirectory) {
        put(context, R.string.column_maindirectory, mMainDirectory);
    }

    public int update(Context context, AbstractDBHelper db) {
        if (!isValid()) {
            return 0;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(mURL, mUserPassword).apply();
        return db.update(tbd, currentContent, mSelection, mSelectionArgs);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mConnectionType == null ? -1 : this.mConnectionType.ordinal());
        dest.writeString(this.mMainDirectory);
        dest.writeString(this.mURL);
        dest.writeString(this.mUserID);
        dest.writeString(this.mUserPassword);
        dest.writeParcelable(this.currentContent, flags);
        dest.writeStringArray(this.mSelectionArgs);
        dest.writeLong(this.mID);
    }
}

