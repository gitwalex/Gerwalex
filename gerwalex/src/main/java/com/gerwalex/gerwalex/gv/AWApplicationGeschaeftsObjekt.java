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
import android.content.res.Resources;
import android.database.Cursor;
import android.databinding.BaseObservable;
import android.os.Parcel;
import android.support.annotation.CallSuper;

import java.text.ParseException;
import java.util.Date;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.activities.AWInterface;
import com.gerwalex.gerwalex.application.AWApplication;
import com.gerwalex.gerwalex.database.AWAbstractDBDefinition;
import com.gerwalex.gerwalex.database.AWDBConvert;
import com.gerwalex.gerwalex.database.AbstractDBHelper;

/**
 * MonMa AWApplicationGeschaeftsObjekt
 * Vorlage fuer die Geschaeftsvorfaelle, z.B. Bankkonto-Buchung, Neues Account etc. Bietet
 * Import-Funktion, die Tabellen werden direkt aus dem Import-File gefuellt.
 * <p/>
 * Fuer Sonderheiten kann die Methode doImport(int resID, String Key, String value) ueberschieben
 * werden. Diese Funktion wird immer aufgerufen, wenn ein neues Praefix beim Import gefunden wurde.
 * <p/>
 * Die erbende Klasse muss durch Aufrufe von update, delete, cancel fuer die Pflege der Datenbank
 * sorgen.
 *
 * @author alex
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class AWApplicationGeschaeftsObjekt extends BaseObservable
        implements AWInterface {
    private final String CLASSNAME = this.getClass().getSimpleName();
    /**
     * Tabellendefinition, fuer die dieser AWApplicationGeschaeftsObjekt gilt. Wird im Konstruktor
     * belegt.
     */
    private final AWAbstractDBDefinition tbd;
    private final AWApplication mContext;
    protected String selection;
    /**
     * ID des AWApplicationGeschaeftsObjekt
     */
    protected Long id;
    protected String[] selectionArgs;
    /**
     * Abbild der jeweiligen Zeile der Datenbank. Werden nicht direkt geaendert.
     */
    private ContentValues currentContent = new ContentValues();
    private boolean isDirty;

    /**
     * Laden eines Geschaeftsvorfalls mit der id aus der Datenbank.
     *
     * @param tbd
     *         Tabelle, der der GV zugeordnet ist.
     * @param id
     *         ID des Geschaeftsvorfalls in der entsprechenden Tabelle.
     *
     * @throws LineNotFoundException
     *         Wenn keine Zeile mit der id gefunden wurde.
     * @throws Resources.NotFoundException
     *         Wenn kein Datensatz mit dieser ID gefunden wurde.
     */
    public AWApplicationGeschaeftsObjekt(Context context, AWAbstractDBDefinition tbd, Long id)
            throws LineNotFoundException {
        this(context, tbd);
        fillContent(mContext.getDBHelper().getApplicationContext(), id);
        id = getID();
        selectionArgs = new String[]{id.toString()};
    }

    public AWApplicationGeschaeftsObjekt(Context context, AWAbstractDBDefinition tbd, Cursor c) {
        this(context, tbd);
        fillContent(c);
    }

    /**
     * Anlegen eines leeren Geschaeftsobjektes
     *
     * @param tbd
     *         AWAbstractDBDefinition
     */
    public AWApplicationGeschaeftsObjekt(Context context, AWAbstractDBDefinition tbd) {
        this.tbd = tbd;
        mContext = (AWApplication) context.getApplicationContext();
        selection = mContext.getString(R.string._id) + " = ?";
    }

    /**
     * Legt ein neues Geschaeftsobject auf Basis eines anderen GO an. Alle Werte, die in dem neuen
     * GO moeglich sind, werden kopiert.
     *
     * @param tbd
     *         AWAbstractDBDefinition
     * @param go
     *         GO, dessen Inhalte kopiert werden sollen.
     */
    protected AWApplicationGeschaeftsObjekt(Context context, AWAbstractDBDefinition tbd,
                                            AWApplicationGeschaeftsObjekt go) {
        this(context, tbd);
        for (int resID : tbd.getTableItems()) {
            Object value = go.currentContent.get(mContext.getDBHelper().columnName(resID));
            if (value != null) {
                put(resID, value);
            }
        }
    }

    protected AWApplicationGeschaeftsObjekt(Context context, Parcel in) {
        this(context, (AWAbstractDBDefinition) in
                .readParcelable(AWAbstractDBDefinition.class.getClassLoader()));
        this.selection = in.readString();
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.selectionArgs = in.createStringArray();
        this.currentContent = in.readParcelable(ContentValues.class.getClassLoader());
        this.isDirty = in.readByte() != 0;
    }

    /**
     * Prueft, ob ein Wert zur ResID vorhanden ist.
     *
     * @param resID
     *         resID des keys
     *
     * @return true, wenn vorhanden. Sonst false
     */
    public boolean IsNull(int resID) {
        String key = mContext.getDBHelper().columnName(resID);
        return currentContent.containsKey(key);
    }

    /**
     * Prueft, ob ein Wert geaendert wurde oder bereits in der DB vorhanden ist.
     *
     * @param resID
     *         resID der Spalte
     *
     * @return true, wenn ein Wert ungleich null enthalten ist
     */
    public final boolean containsValue(int resID) {
        String key = mContext.getDBHelper().columnName(resID);
        return currentContent.get(key) != null;
    }

    protected String convertValue(int resID, Object value) {
        String newValue = null;
        if (value != null) {
            newValue = value.toString();
            char format = mContext.getDBHelper().getFormat(resID);
            switch (format) {
                case 'B':
                    if (AWDBConvert.convertBoolean(newValue)) {
                        newValue = "1";
                    } else {
                        newValue = "0";
                    }
                    break;
                case 'D':
                    if (value instanceof Date) {
                        newValue = AWDBConvert.convertDate2SQLiteDate((Date) value);
                    }
                    break;
                case 'O':
                    throw new IllegalArgumentException("Kann nicht nach String konvertieren");
            }
        }
        return newValue;
    }

    protected void copy(AWApplicationGeschaeftsObjekt source) {
        currentContent = source.getContent();
        id = source.id;
    }


    protected int delete(AbstractDBHelper db) {
        if (id == null) {
            AWApplication
                    .Log("AWApplicationGeschaeftsObjekt noch nicht angelegt! Delete nicht moeglich");
            return 0;
        }
        int result;
        result = db.delete(tbd, selection, selectionArgs);
        isDirty = true;
        if (result != 0) {
            currentContent.putNull(mContext.getDBHelper().columnName(R.string._id));
            id = null;
        }
        return result;
    }

    public int describeContents() {
        return 0;
    }

    /**
     * @param o
     *         zu vergleichendes Object.
     *
     * @return true,  wenn das vergleichende Object ein Geschaeftsobjekte ist, beide auf die gleiche
     * Tabelle zeigen (DBDefiniton)und die gleiche Anzahl Werte mit den gleichen Inhalten in den
     * ContentValues vorhanden sind.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AWApplicationGeschaeftsObjekt that = (AWApplicationGeschaeftsObjekt) o;
        return tbd == that.tbd && currentContent.equals(that.currentContent);
    }

    /**
     * Fuellt currentContent (aus einem Cursor. Cursor kann auch leer sein. Hat der Cursor mehrere
     * Zeilen, wird nur die erste uebernommen. Es werden nur Werte ungleich null uebernommen.
     * <p/>
     * Alle vorigen Werte werden verworfen.
     *
     * @param c
     *         Cursor
     */
    public final void fillContent(Cursor c) throws LineNotFoundException {
        if (c.isBeforeFirst()) {
            if (!c.moveToFirst()) {
                throw new LineNotFoundException("Cursor ist leer!");
            }
        }
        currentContent.clear();
        for (int i = 0; i < c.getColumnCount(); i++) {
            int type = c.getType(i);
            switch (type) {
                case Cursor.FIELD_TYPE_BLOB:
                    byte[] blob = c.getBlob(i);
                    currentContent.put(c.getColumnName(i), blob);
                    break;
                default:
                    String value = c.getString(i);
                    if (value != null) {
                        currentContent.put(c.getColumnName(i), c.getString(i));
                    }
            }
        }
        id = getAsLong(R.string._id);
        selectionArgs = new String[]{id.toString()};
        currentContent.remove(mContext.getDBHelper().columnName(R.string._id));
        isDirty = false;
    }

    /**
     * Fuellt den Content aus der Datenbank in currentContent
     *
     * @param id
     *         in der Tabelle
     *
     * @throws LineNotFoundException
     *         Wenn keine Zeile mit der id gefunden wurde.
     */
    public final void fillContent(Context context, Long id) throws LineNotFoundException {
        selectionArgs = new String[]{id.toString()};
        fillContent(context, id, selection, selectionArgs);
    }

    /**
     * Fuellt das Geschaeftsobjekt anhand der uebergebenen Daten aus der DB
     *
     * @param id
     *         id des Objektes
     * @param selection
     *         selection
     * @param selectionArgs
     *         selectionArgs
     *
     * @throws LineNotFoundException
     *         wenn keine Zeile gefunden wurde.
     */
    public final void fillContent(Context context, Long id, String selection,
                                  String[] selectionArgs) throws LineNotFoundException {
        Cursor c = context.getContentResolver()
                .query(tbd.getUri(), tbd.columnNames(tbd.getTableItems()), selection,
                        selectionArgs, null);
        assert c != null;
        try {
            if (c.moveToFirst()) {
                fillContent(c);
            } else {
                throw new LineNotFoundException(
                        tbd.name() + ": Zeile mit id " + id + " nicht gefunden.");
            }
        } finally {
            if (!c.isClosed()) {
                c.close();
            }
        }
    }

    /**
     * Fuellt contentValues mit den Daten aus selection und selectionArgs. Bereits vorhandene Daten
     * werden ueberschrieben.
     *
     * @param selection
     *         Where-Clause
     * @param selectionArgs
     *         Argumente
     *
     * @throws LineNotFoundException
     *         Wenn keine Zeile mit der zur Selektion gefunden wurde.
     * @throws IllegalStateException
     *         , wenn das Ergebnis mehr als eine Zeile liefert.
     */
    public final void fillContent(Context context, String selection, String[] selectionArgs)
            throws LineNotFoundException {
        String[] projection = tbd.columnNames(tbd.getTableItems());
        Cursor c = context.getContentResolver()
                .query(tbd.getUri(), projection, selection, selectionArgs, null);
        assert c != null;
        try {
            if (c.getCount() > 1) {
                AWApplication.Log("selection" + selection);
                if (selectionArgs != null) {
                    for (String s : selectionArgs) {
                        AWApplication.Log("selectionArg" + s);
                    }
                }
                throw new IllegalStateException("Selection ergab mehr als eine Zeile!");
            }
            if (!c.moveToFirst()) {
                StringBuilder sb = new StringBuilder();
                if (selectionArgs != null) {
                    for (String s : selectionArgs) {
                        sb.append(" [").append(s).append("] ");
                    }
                }
                throw new LineNotFoundException(
                        "Zeile mit Selektion " + selection + " zu Argumenten " + sb.toString() +
                                " nicht gefunden.");
            } else {
                fillContent(c);
            }
        } finally {
            if (!c.isClosed()) {
                c.close();
            }
        }
    }

    protected Resources getApplicationResources() {
        return mContext.getResources();
    }

    /**
     * Liefert den Wert aus dem Geschaeftsobjekt zu Spalte resID als Boolean.
     *
     * @param resID
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte (true ooder false)
     */
    public final Boolean getAsBoolean(int resID) {
        String key = mContext.getDBHelper().columnName(resID);
        String value = currentContent.getAsString(key);
        return AWDBConvert.convertBoolean(value);
    }

    public final byte[] getAsByteArray(int resID) {
        String key = mContext.getDBHelper().columnName(resID);
        return currentContent.getAsByteArray(key);
    }

    /**
     * Konvertiert ein Datum zurueck.
     *
     * @param datum
     *         Datum im SQLite-Format
     *
     * @return Date-Objekt oder null
     */
    public Date getAsDate(String datum) {
        try {
            return new java.sql.Date(AWDBConvert.mSqliteDateFormat.parse(datum).getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Liest das Datum aus dem Geschaeftsobjekt und liefert es als Date zurueck. Siehe {@link
     * AWApplicationGeschaeftsObjekt#getAsDate(String)}
     */
    public Date getAsDate(int resID) {
        return getAsDate(getAsString(resID));
    }

    /**
     * Liefert den aktuellsten Wert aus dem Geschaeftsobjekt zu Spalte resID als Integer.
     *
     * @param resID
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte oder null, wenn nicht vorhanden
     */
    public final Integer getAsInt(int resID) {
        String key = mContext.getDBHelper().columnName(resID);
        return currentContent.getAsInteger(key);
    }

    /**
     * Liefert den aktuellsten Wert aus dem Geschaeftsobjekt zu Spalte resID als Integer.
     *
     * @param resID
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte oder null, wenn nicht vorhanden
     */
    public final int getAsInt(int resID, int defaultValue) {
        String key = mContext.getDBHelper().columnName(resID);
        Integer value = currentContent.getAsInteger(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Liefert den aktuellsten Wert aus dem Geschaeftsobjekt zu Spalte resID als Long.
     *
     * @param resID
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte oder null, wenn nicht vorhanden
     */
    public final Long getAsLong(int resID) {
        String key = mContext.getDBHelper().columnName(resID);
        return currentContent.getAsLong(key);
    }

    /**
     * Wie {@link AWApplicationGeschaeftsObjekt#getAsLong(int)}, liefert aber den Defaultwert
     * zuruck, wenn die Spalte nicht belegt iist.
     */
    public final long getAsLong(int resID, long defaultWert) {
        Long value = getAsLong(resID);
        if (value == null) {
            return defaultWert;
        }
        return value;
    }

    /**
     * Liefert den aktuellsten Wert aus dem Geschaeftsobjekt zu Spalte resID als String.
     *
     * @param resID
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte oder null, wenn nicht vorhanden
     */
    public final String getAsString(int resID) {
        String key = mContext.getDBHelper().columnName(resID);
        return currentContent.getAsString(key);
    }

    /**
     * @return Liefert eine Kopie der akteullen  Werte zurueck.
     */
    public final ContentValues getContent() {
        return new ContentValues(currentContent);
    }

    protected AWAbstractDBDefinition getDBDefinition() {
        return tbd;
    }

    /**
     * @return ID des Geschaeftsvorfalls
     */
    public long getID() {
        if (id == null) {
            throw new IllegalStateException("ID ist Null. Vorher insert() " + "ausfuehren");
        }
        return id;
    }

    /**
     * @return ID als Integer
     */
    public final Integer getIDAsInt() {
        if (id == null) {
            throw new IllegalStateException("ID ist Null. Vorher insert() " + "ausfuehren");
        }
        return id.intValue();
    }

    protected long insert(AbstractDBHelper db) {
        if (isInserted()) {
            throw new IllegalStateException(
                    "AWApplicationGeschaeftsObjekt bereits angelegt! Insert nicht moeglich");
        }
        for (int resID : tbd.getTableItems()) {
            char format = mContext.getDBHelper().getFormat(resID);
            switch (format) {
                case 'B':
                    // Vorbelegung mit Wert 'false'
                    if (!getAsBoolean(resID)) {
                        put(resID, false);
                    }
                    break;
            }
        }
        id = db.insert(tbd, null, currentContent);
        if (id != -1) {
            currentContent.put(mContext.getDBHelper().columnName(R.string._id), id);
        } else {
            AWApplication.Log("Insert in AWApplicationGeschaeftsObjekt " + CLASSNAME +
                    " fehlgeschlagen! Werte: " + currentContent.toString());
            currentContent.clear();
        }
        selectionArgs = new String[]{id.toString()};
        isDirty = false;
        return id;
    }

    /**
     * @return true, wenn Aenderungen vorgenomen wurden.
     */
    public boolean isDirty() {
        return isDirty;
    }

    /**
     * Check, ob das Geschaeftsobjekt schon in die DB eingefuegt wurde
     *
     * @return true, wenn bereits eingefuegt.
     */
    public final boolean isInserted() {
        return id != null;
    }

    /**
     * Aendert oder Fuegt Daten ein. Werden mAccountID oder buchungsID belegt, werden die
     * entsprechenden Variaablen (neu) belegt. Wird catID belegt, wird aucch catname belegt. Wird
     * catname belegt, wird geprueft, ob catID bereits belegt ist. Wenn nicht, wird wird diese dort
     * auch belegt.
     *
     * @param resID
     *         ResID der Spalte, die eingefuegt werden soll.
     * @param value
     *         Wert, der eingefuegt werden soll. Wert wird als String eingefuegt. Ist der Wert
     *         bereits identisch vorhanden (DB oder als geaenderter Wert), wird nichts eingefuegt.
     *         Ist value == null oder ein leerer String, wird ein ggfs. geaenderter Wert in der
     *         Spalte und nach Update dann auch aus der DB entfernt.
     *
     * @return true, wenn die Aktion erfolgreich war, also ein Wert eingefuegt/entfernt wurde.
     * false, wenn der gleiche Wert bereitsvorhanden war.
     *
     * @throws IllegalArgumentException
     *         wenn ResID nicht in der Tabelle vorhanden ist
     */
    public boolean put(int resID, Object value) {
        String newValue;
        String key = mContext.getDBHelper().columnName(resID);
        if (value != null && !value.toString().isEmpty()) {
            newValue = convertValue(resID, value);
            currentContent.put(key, newValue);
        } else {
            currentContent.putNull(key);
        }
        isDirty = true;
        return true;
    }

    /**
     * Aendert oder Fuegt Daten ein. Werden mAccountID oder buchungsID belegt, werden die
     * entsprechenden Variaablen (neu) belegt. Wird catID belegt, wird aucch catname belegt. Wird
     * catname belegt, wird geprueft, ob catID bereits belegt ist. Wenn nicht, wird wird diese dort
     * auch belegt.
     *
     * @param resID
     *         ResID der Spalte, die eingefuegt werden soll.
     * @param value
     *         BlobWert, der eingefuegt werden soll.
     *
     * @return true, wenn die Aktion erfolgreich war, also ein Wert eingefuegt/entfernt wurde.
     * false, wenn der gleiche Wert bereitsvorhanden war.
     *
     * @throws IllegalArgumentException
     *         wenn ResID nicht in der Tabelle vorhanden ist
     */
    public boolean put(int resID, byte[] value) {
        String key = mContext.getDBHelper().columnName(resID);
        if (value == null) {
            currentContent.putNull(key);
        } else {
            currentContent.put(key, value);
        }
        isDirty = true;
        return true;
    }

    /**
     * Aendert oder Fuegt Daten ein.
     *
     * @param resID
     *         ResID der Spalte, die entfernt werden soll.
     *
     * @throws UnsupportedOperationException
     *         wennn _id entfernt werden soll.
     */
    @CallSuper
    public void remove(int resID) {
        if (resID == R.string._id) {
            throw new UnsupportedOperationException("ID entfernen nur mit delete()!");
        }
        currentContent.putNull(mContext.getDBHelper().columnName(resID));
        isDirty = true;
    }

    /**
     * Lesbare Informationen zum Geschaeftvorfall
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        if (id != null) {
            sb.append(", ID: ").append(getID());
        } else {
            sb.append(", Noch nicht eingefuegt");
        }
        sb.append(linefeed).append("Werte: ").append(currentContent.toString());
        return sb.toString();
    }

    protected int update(AbstractDBHelper db) {
        int result = 0;
        if (id == null) {
            throw new IllegalStateException(
                    "AWApplicationGeschaeftsObjekt noch nicht angelegt! Update nicht moeglich");
        }
        if (isDirty) {
            currentContent.put(mContext.getDBHelper().columnName(R.string._id), getID());
            selectionArgs = new String[]{id.toString()};
            result = db.update(tbd, currentContent, selection, selectionArgs);
            if (result != 1) {
                throw new IllegalStateException(
                        "Fehler beim Update. Satz nicht gefunden mit RowID " + id +
                                ", SelectionID = " + selectionArgs[0]);
            }
            isDirty = false;
        }
        return result;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.tbd, flags);
        dest.writeString(this.selection);
        dest.writeValue(this.id);
        dest.writeStringArray(this.selectionArgs);
        dest.writeParcelable(this.currentContent, flags);
        dest.writeByte(this.isDirty ? (byte) 1 : (byte) 0);
    }

    public static class LineNotFoundException extends RuntimeException {
        /**
         *
         */
        private static final long serialVersionUID = -3204185849776637352L;

        public LineNotFoundException(String detailMessage) {
            super(detailMessage);
        }
    }
}
