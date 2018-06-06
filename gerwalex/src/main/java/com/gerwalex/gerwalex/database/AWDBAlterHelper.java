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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.application.AWApplication;

/**
 * Helper-Klasse fuer Aenderungen/Neuanlagen in der DB
 */
public final class AWDBAlterHelper {
    private final SQLiteDatabase database;
    private final AbstractDBHelper dbhelper;
    private final String idColumn;

    /**
     * Initialisiert AWDBAlterHelper. Die letzte vergebene indexNummer/uniqueIndexNummer wird aus
     * Preferences gelesen.
     *
     * @param dbhelper
     *         AbstractDBHelper
     */
    public AWDBAlterHelper(AbstractDBHelper dbhelper, SQLiteDatabase database) {
        this.dbhelper = dbhelper;
        this.database = database;
        idColumn = dbhelper.getApplicationResources().getString(R.string._id);
    }

    /**
     * Aendert einen Index
     *
     * @param tbd
     *         AWAbstractDBDefinition
     * @param indexName
     *         Name des Index
     * @param indexItems
     *         Items des Index
     */
    public void alterIndex(AWAbstractDBDefinition tbd, String indexName, int[] indexItems) {
        if (!tbd.isView()) {
            dropIndex(tbd);
            String sql = getCreateIndexSQL(tbd, indexName, indexItems, false);
            database.execSQL(sql);
        }
    }

    /**
     * Legt einen Index an.
     *
     * @param tbd
     *         Tabelle
     * @param indexName
     *         Name des Index
     * @param indexColumns
     *         Spalten des Index, Komme getrennt
     */
    public void alterIndex(AWAbstractDBDefinition tbd, String indexName, String indexColumns) {
        dropIndex(tbd);
        String sql = "CREATE ";
        sql = sql + "INDEX IF NOT EXISTS " + indexName + " on " + tbd.name() + " (" + indexColumns +
                ")";
        database.execSQL(sql);
    }

    /**
     * Legt einen Index an.
     *
     * @param tbd
     *         Tabelle
     * @param indexName
     *         Name des Index
     * @param indexColumns
     *         Spalten des Index, Komme getrennt
     * @param selection
     *         Selection fuer den Index
     */
    public void alterIndex(AWAbstractDBDefinition tbd, String indexName, String indexColumns,
                           String selection) {
        dropIndex(tbd);
        String sql = "CREATE ";
        sql = sql + "INDEX IF NOT EXISTS " + indexName + " on " + tbd.name() + " (" + indexColumns +
                ")" + selection;
        database.execSQL(sql);
    }

    /**
     * Legt eine Tabelle mit neuen Spalten an. Inhalte werden kopiert, die zu kopierenden Felder
     * werden ueber fromColumn vorgegeben
     *
     * @param tbd
     *         AWAbstractDBDefinition
     * @param fromColumns
     *         Spalten, die kopiert werden sollen
     */
    public void alterTable(AWAbstractDBDefinition tbd, int[] fromColumns) {
        String colums = dbhelper.getCommaSeperatedList(fromColumns);
        String tempTableName = "temp" + tbd.name();
        String createTempTable = "CREATE TABLE " + tempTableName + getCreateTableSQL(tbd);
        String copyValuesSQL = "INSERT INTO temp" + tbd.name() + " (" + colums + ") SELECT " +
                colums + " FROM " + tbd.name();
        database.execSQL(createTempTable);
        database.execSQL(copyValuesSQL);
        dropTable(tbd.name());
        renameTable(tempTableName, tbd.name());
        tbd.createDatabase(this);
    }

    /**
     * Aendert eine Tabelle und haengt eine neuen Column hinten an. Indices werden neu angelegt.
     * Nacharbeiten durch die Tabelle selbst werden ermoeglicht durch Aufruf {@link
     * AWAbstractDBDefinition#createDatabase(AWDBAlterHelper)}
     *
     * @param tbd
     *         AWAbstractDBDefinition
     * @param newColumn
     *         neue Column
     */
    public void alterTableAddColumn(AWAbstractDBDefinition tbd, int newColumn) {
        String colName = dbhelper.columnName(newColumn);
        String format = dbhelper.getSQLiteFormat(newColumn);
        String sql = "ALTER TABLE " + tbd.name() + " ADD " + colName + " " + format;
        database.execSQL(sql);
        tbd.createDatabase(this);
    }

    /**
     * Aendert eine Tabelle und haengt eine neuen Column hinten an. Indices werden neu angelegt.
     * Nacharbeiten durch die Tabelle selbst werden ermoeglicht durch Aufruf {@link
     * AWAbstractDBDefinition#createDatabase(AWDBAlterHelper)}. Der defualtWert wird in die neue
     * Column eingetragen
     *
     * @param tbd
     *         AWAbstractDBDefinition
     * @param newColumn
     *         neue Column
     */
    public void alterTableAddColumn(AWAbstractDBDefinition tbd, int newColumn, String defaultWert) {
        alterTableAddColumn(tbd, newColumn);
        String colName = dbhelper.columnName(newColumn);
        String sql = "UPDATE " + tbd.name() + " SET  " + colName + " = " + defaultWert;
        database.execSQL(sql);
    }

    /**
     * Aendert eine Tabelle und haengt eine neuen Column hinten an. Indices werden neu angelegt.
     * Nacharbeiten durch die Tabelle selbst werden ermoeglicht durch Aufruf {@link
     * AWAbstractDBDefinition#createDatabase(AWDBAlterHelper)}
     *
     * @param tbd
     *         AWAbstractDBDefinition
     * @param newColumn
     *         neue Column
     */
    public void alterTableAddColumn(AWAbstractDBDefinition tbd, String newColumn) {
        String sql = "ALTER TABLE " + tbd.name() + " ADD " + newColumn + " TEXT";
        database.execSQL(sql);
        tbd.createDatabase(this);
    }

    public void alterTableDistinct(AWAbstractDBDefinition tbd, String distinctColumn) {
        String tempTableName = "temp" + tbd.name();
        String createTempTable = "CREATE TABLE " + tempTableName + getCreateTableSQL(tbd);
        String oldColumnNames = dbhelper.getCommaSeperatedList(tbd.getTableItems());
        String copyValuesSQL =
                "INSERT INTO temp" + tbd.name() + " (" + oldColumnNames + ") SELECT " +
                        oldColumnNames +
                        " FROM " +
                        tbd.name() + " GROUP BY " + distinctColumn;
        database.execSQL(createTempTable);
        database.execSQL(copyValuesSQL);
        dropTable(tbd.name());
        renameTable(tempTableName, tbd.name());
        tbd.createDatabase(this);
    }

    /**
     * Aendert eine Tabelle. Die neue Tabelle hat keine geaenderten Columns, nur die gleichen oder
     * weniger! Alle Indices werden wieder angelegt. Nacharbeiten durch die Tabelle selbst werden
     * ermoeglicht durch Aufruf {@link AWAbstractDBDefinition#createDatabase(AWDBAlterHelper)}
     *
     * @param tbd
     *         AWAbstractDBDefinition
     */
    public void alterTableOnlyDeletedColumns(AWAbstractDBDefinition tbd) {
        AWApplication.Log("Alter Table: " + tbd.name());
        String tempTableName = "temp" + tbd.name();
        String createTempTable = "CREATE TABLE " + tempTableName + getCreateTableSQL(tbd);
        String oldColumnNames = dbhelper.getCommaSeperatedList(tbd.getTableItems());
        String copyValuesSQL =
                "INSERT INTO temp" + tbd.name() + " (" + oldColumnNames + ") SELECT " +
                        oldColumnNames +
                        " FROM " +
                        tbd.name();
        database.execSQL(createTempTable);
        database.execSQL(copyValuesSQL);
        dropTable(tbd.name());
        renameTable(tempTableName, tbd.name());
        tbd.createDatabase(this);
    }

    /**
     * Aendert eine UniqueIndex
     *
     * @param tbd
     *         tbd
     * @param indexName
     *         IndexName
     * @param uniqueIndexItems
     *         IndexItems
     */
    public void alterUniqueIndex(AWAbstractDBDefinition tbd, String indexName,
                                 int[] uniqueIndexItems) {
        dropIndex(indexName);
        String sql = getCreateIndexSQL(tbd, indexName, uniqueIndexItems, true);
        database.execSQL(sql);
    }

    /**
     * Aendert eine View. Create wird aus {@link AWAbstractDBDefinition#getCreateViewSQL()} geholt
     *
     * @param tbd
     *         AWAbstractDBDefinition
     */
    public void alterView(AWAbstractDBDefinition tbd) {
        String viewSQL = tbd.getCreateViewSQL();
        if (viewSQL != null) {
            dropView(tbd);
            String sql = ("CREATE VIEW " + tbd.name() + " AS " + viewSQL);
            database.execSQL(sql);
            tbd.createDatabase(this);
            checkView(tbd);
        }
    }

    /**
     * Versorgt die Statistiktabellen
     */
    public void analyze() {
        database.execSQL("analyze");
    }

    private boolean checkView(AWAbstractDBDefinition tbd) {
        ContentValues cv = new ContentValues();
        List<String> fehler = new ArrayList<>();
        String[] projection = tbd.columnNames(tbd.getTableItems());
        Cursor c = database.query(tbd.name(), projection, null, null, null, null, null);
        try {
            for (String column : c.getColumnNames()) {
                cv.put(column, "test");
            }
        } finally {
            c.close();
        }
        for (String column : projection) {
            if (cv.get(column) == null) {
                fehler.add("In " + tbd.name() + " column nicht gefunden: " + column);
            }
        }
        for (String value : fehler) {
            AWApplication.Log(value);
        }
        return fehler.size() == 0;
    }

    public void copyValues(AWAbstractDBDefinition from, int[] columns, AWAbstractDBDefinition to) {
        String mColumns = dbhelper.getCommaSeperatedList(columns);
        String sql = "INSERT INTO " + to.name() + " (" + mColumns + ") SELECT " +
                mColumns +
                " FROM " +
                from.name();
        database.execSQL(sql);
    }

    /**
     * Legt eine neue Tabelle an. Eine vorhandene Tabelle mit diesem Namen wird geloescht. ein
     * eventuell vorhandener Index bzw. UniqueIndex wird mit angelegt.
     *
     * @param tbd
     *         AWAbstractDBDefinition
     */
    public void createTable(AWAbstractDBDefinition tbd) {
        dropTable(tbd.name());
        String sql = "CREATE TABLE IF NOT EXISTS " + tbd.name() + " " + getCreateTableSQL(tbd);
        database.execSQL(sql);
        tbd.createDatabase(this);
    }

    /**
     * Legt eine Tabelle mit Namen und Spalten an
     *
     * @param tablename
     *         Tabellenname
     * @param colums
     *         Spaltennamen
     * @param formate
     *         Formate der Spalte
     */
    public void createTable(String tablename, String[] colums, char[] formate) {
        String[] format = new String[formate.length];
        for (int i = 0; i < formate.length; i++) {
            format[i] = dbhelper.getSQLiteFormat(formate[i]);
        }
        dropTable(tablename);
        StringBuilder sql = new StringBuilder(" ( ");
        sql.append("_id INTEGER PRIMARY KEY ");
        for (int i = 0; i < formate.length; i++) {
            String colName = colums[i];
            String f = format[i];
            sql.append(", ").append(colName).append(" ").append(f);
        }
        sql.append(")");
        String createSQL = "CREATE TABLE IF NOT EXISTS " + tablename + sql.toString();
        database.execSQL(createSQL);
    }

    /**
     * Dropt alle Indices der Datenbank.
     */
    public void dropAllIndices() {
        String selection = "SELECT name FROM sqlite_master WHERE type == 'index'";
        Cursor c = database.rawQuery(selection, null);
        if (c.moveToFirst()) {
            String name = c.getString(0);
            dropIndex(name);
        }
        c.close();
    }

    /**
     * Dropt einen Index
     *
     * @param name
     *         Name
     */
    public void dropIndex(String name) {
        String sql = "DROP INDEX IF EXISTS " + name;
        database.execSQL(sql);
    }

    /**
     * Loescht einen (Unique-) Index ersatzlos
     *
     * @param tbd
     *         AWAbstractDBDefinition
     */
    public void dropIndex(AWAbstractDBDefinition tbd) {
        String sql = "DROP INDEX IF EXISTS " + tbd.name();
        database.execSQL(sql);
    }

    /**
     * Loescht eine Tabelle ersatzlos
     *
     * @param tablename
     *         Name
     */
    public void dropTable(String tablename) {
        String sql = "DROP TABLE IF EXISTS " + tablename;
        database.execSQL(sql);
    }

    /**
     * Loescht eine View ersatzlos
     *
     * @param tbd
     *         AWAbstractDBDefinition
     */
    public void dropView(AWAbstractDBDefinition tbd) {
        dropView(tbd.name());
    }

    /**
     * Dropt eine View mit Namen
     *
     * @param s
     *         Name der View
     */
    public void dropView(String s) {
        String sql = "DROP VIEW IF EXISTS " + s;
        database.execSQL(sql);
    }

    /**
     * Liefert die Tabellenspalten der Tabelle ohne _id zuruck.
     *
     * @param tbd
     *         tbd
     * @param tableindex
     *         Tableindex
     *
     * @return commaseperated List der Spalten
     */
    public String getCommaSeperatedListNoID(@NonNull AWAbstractDBDefinition tbd,
                                            @NonNull int[] tableindex) {
        List<String> columns = new ArrayList<>();
        for (int resID : tableindex) {
            columns.add(dbhelper.columnName(resID));
        }
        columns.remove(idColumn);
        StringBuilder indexSQL = new StringBuilder(columns.get(0));
        for (int j = 1; j < columns.size(); j++) {
            String column = columns.get(j);
            indexSQL.append(", ").append(column);
        }
        return indexSQL.toString();
    }

    public String getCreateIndexSQL(AWAbstractDBDefinition tbd, String indexName, int[] columns,
                                    boolean uniqueIndex) {
        String sql = "CREATE ";
        if (uniqueIndex) {
            sql = sql + "UNIQUE ";
        }
        sql = sql + "INDEX IF NOT EXISTS " + indexName + " on " + tbd.name() + " (" +
                dbhelper.getCommaSeperatedList(columns) + ")";
        return sql;
    }

    /**
     * @return den String fuer den Aubau eine Tabelle (ohne CREATE TABLE AS name)
     */
    public String getCreateTableSQL(AWAbstractDBDefinition tbd) {
        StringBuilder sql = new StringBuilder(" ( ");
        boolean id = true;
        for (int resID : tbd.getTableItems()) {
            String colName = dbhelper.columnName(resID);
            String format = dbhelper.getSQLiteFormat(resID);
            if (id) {
                sql.append(colName).append(" INTEGER PRIMARY KEY ");
                id = false;
            } else {
                sql.append(", ").append(colName).append(" ");
                sql.append(format);
            }
        }
        sql.append(")");
        return sql.toString();
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    /**
     * Umbenennung einer Tabelle
     *
     * @param oldName
     *         alter Name der Tabelle
     * @param newName
     *         neuer Name der Tabelle
     */
    public void renameTable(String oldName, String newName) {
        String renameTableSQL = "ALTER TABLE " + oldName + " RENAME TO " + newName;
        database.execSQL(renameTableSQL);
    }

    /**
     * Komprimiert die Datenbank
     */
    public void vacuum() {
        database.execSQL("vacuum");
    }
}
