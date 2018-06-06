/**
 *
 */
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

import android.os.Parcel;
import android.os.Parcelable;

import com.gerwalex.gerwalex.events.AWEvent;
import com.gerwalex.gerwalex.fragments.AWFileChooser;

/**
 * @author alex
 */
public interface AWInterface {
    String
            /**
             *
             */
            ID = "ID"
            /**
             *
             */
            , LAYOUT = "LAYOUT"
            /**
             *
             */
            , VIEWHOLDERLAYOUT = "VIEWHOLDERLAYOUT"
            /**
             *
             */
            , VIEWRESIDS = "VIEWRESIDS"
            /**
             *
             */
            , FROMRESIDS = "FROMRESIDS"
            /**
             *
             */
            , LOGFILE = "LOGFILE"
            /**
             *
             */
            , DIALOGRESULT = "DIALOGRESULT"
            /**
             *
             */
            , NEXTACTIVITY = "NEXTACTIVITY"
            /**
             *
             */
            , DBDEFINITION = "DBDEFINITION"
            /**
             *
             */
            , PROJECTION = "PROJECTION"
            /**
             *
             */
            , SELECTION = "SELECTION"
            /**
             *
             */
            , SELECTIONARGS = "SELECTIONARGS"
            /**
             *
             */
            , GROUPBY = "GROUPBY"
            /**
             *
             */
            , ORDERBY = "ORDERBY"
            /**
             *
             */
            , SELECTEDVIEWHOLDERITEM = "SELECTEDVIEWHOLDERITEM"
            /**
             *
             */
            , LASTSELECTEDPOSITION = "LASTSELECTEDPOSITION"
            /**
             *
             */
            , ACTIONBARTITLE = "ACTIONBARTITLE"
            /**
             *
             */
            , FILENAME = "FILENAME"
            /**
             *
             */
            , ACTIONBARSUBTITLE = "ACTIONBARSUBTITLE"
            /**
             * Events fuer {@link AWEvent}
             */
            , AWLIBEVENT = "AWLIBEVENT"
            /**
             * MainAction
             */
            , AWLIBACTION = "AWLIBACTION"
            /**
             * Filter fuer einen Filenamen. Wird in {@link AWFileChooser} verwendet.
             */
            , FILENAMEFILTER = "FILENAMEFILTER"//
            , REMOTEFILESERVER = "REMOTEFILESERVER"//
            , COLUMS = "COLUMS"//
            , FORMATE = "FORMATE"//
            , FRAGMENTTITLE = "FRAGMENTTITLE"//
            ;
    int
            /**
             *
             */
            REQUEST_PERMISSION_READ_CALENDAR = 100
            /**
             *
             */
            , REQUEST_PICTURE_RESULT = 110
            /**
             *
             */
            , REQUEST_PERMISSION_CALENDAR = 120
            /**
             *
             */
            , REQUEST_PERMISSION_STORAGE = 130
            /**
             *
             */
            , REQUEST_NUMBER = 140
            /**
             *
             */
            , REQUEST_TIME = 150
            /**
             *
             */
            , REQUEST_PERMISSION_CAMERA = 160//
            /**
             *
             */
            , REQUEST_EMAIL = 170
            /**
             *
             */
            , NOLAYOUT = -1
            /**
             *
             */
            , NOID = 0;
    Long
            /**
             *
             */
            NOROWS = -1L;
    String
            /**
             *
             */
            linefeed = System.getProperty("line.separator");

    enum MainAction implements Parcelable {
        ADD, EDIT, SHOW;
        public static final Creator<MainAction> CREATOR = new Creator<MainAction>() {
            public MainAction createFromParcel(Parcel in) {
                return MainAction.values()[in.readInt()];
            }

            public MainAction[] newArray(int size) {
                return new MainAction[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(ordinal());
        }
    }
}

