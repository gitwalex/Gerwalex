package com.gerwalex.gerwalex.database;

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

import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteQuery;

/**
 * Cursor, der die Zeit zum Ende der Abfrage festhaelt. Diese Zeit kann dann mittels {@link
 * AWSQLiteCursor#getFinishTime()} abgefragt werden.
 */
public class AWSQLiteCursor extends SQLiteCursor {
    private final long finishTime;

    {
        finishTime = System.nanoTime();
    }

    public AWSQLiteCursor(SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
        super(driver, editTable, query);
    }

    @Override
    protected void finalize() {
        if (!isClosed()) {
            close();
        }
        super.finalize();
    }

    public long getFinishTime() {
        return finishTime;
    }
}
