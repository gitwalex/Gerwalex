package com.gerwalex.gerwalex.enums;

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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.database.AWDBConvert;

/**
 * Auswahl des Datums fuer Reports
 */
public enum AWDateSelector {
    AktJhr(R.string.AktJhr) {
        @Override
        public VonBisDate getDateSelection() {
            Calendar bis = Calendar.getInstance();
            bis.set(Calendar.MONTH, Calendar.DECEMBER);
            bis.set(Calendar.DAY_OF_MONTH, 31);
            Calendar vonDate = getActualDate();
            vonDate.set(Calendar.MONTH, Calendar.JANUARY);
            vonDate.set(Calendar.DAY_OF_MONTH, 1);
            return new VonBisDate(vonDate.getTime(), bis.getTime());
        }
    }//
    , AktMnt(R.string.AktMnt) {
        @Override
        public VonBisDate getDateSelection() {
            Calendar bisDate = getUltimoDate(getActualDate());
            Calendar vonDate = getActualDate();
            vonDate.set(Calendar.DAY_OF_MONTH, 1);
            return new VonBisDate(vonDate.getTime(), bisDate.getTime());
        }
    }//
    , AktQuart(R.string.AktQuart) {
        @Override
        public VonBisDate getDateSelection() {
            Calendar bisDate = getUltimoDate(getActualDate());
            Calendar von = getQuartalBeginn();
            return new VonBisDate(von.getTime(), bisDate.getTime());
        }
    }//
    , BisHeuteJhr(R.string.BisHeuteJhr) {
        @Override
        public VonBisDate getDateSelection() {
            Calendar bisDate = getActualDate();
            Calendar vonDate = getActualDate();
            vonDate.set(Calendar.MONTH, Calendar.JANUARY);
            vonDate.set(Calendar.DAY_OF_MONTH, 1);
            return new VonBisDate(vonDate.getTime(), bisDate.getTime());
        }
    }//
    , Heute(R.string.Heute) {
        @Override
        public VonBisDate getDateSelection() {
            Calendar bisDate = getActualDate();
            Calendar vonDate = getActualDate();
            return new VonBisDate(vonDate.getTime(), bisDate.getTime());
        }
    }//
    , BisHeuteMnt(R.string.BisHeuteMnt) {
        @Override
        public VonBisDate getDateSelection() {
            Calendar bisDate = getActualDate();
            Calendar vonDate = getActualDate();
            vonDate.set(Calendar.DAY_OF_MONTH, 1);
            return new VonBisDate(vonDate.getTime(), bisDate.getTime());
        }
    }//
    , BisHeuteQuart(R.string.BisHeuteQuart) {
        @Override
        public VonBisDate getDateSelection() {
            Calendar bisDate = getActualDate();
            Calendar von = getQuartalBeginn();
            return new VonBisDate(von.getTime(), bisDate.getTime());
        }
    }//
    , LetzMnt(R.string.LetzMnt) {
        @Override
        public VonBisDate getDateSelection() {
            Calendar bisDate = getUltimoDate(getActualDate());
            Calendar vonDate = getActualDate();
            bisDate.add(Calendar.MONTH, -1);
            vonDate.add(Calendar.MONTH, -1);
            vonDate.set(Calendar.DAY_OF_MONTH, 1);
            return new VonBisDate(vonDate.getTime(), bisDate.getTime());
        }
    }//
    , LetztQuart(R.string.LetztQuart) {
        @Override
        public VonBisDate getDateSelection() {
            Calendar bisDate = getUltimoDate(getQuartalBeginn());
            bisDate.add(Calendar.MONTH, -1);
            Calendar von = getQuartalBeginn(bisDate);
            return new VonBisDate(von.getTime(), bisDate.getTime());
        }
    }//
    , LztJhr(R.string.LztJhr) {
        @Override
        public VonBisDate getDateSelection() {
            Calendar bisDate = getActualDate();
            bisDate.add(Calendar.YEAR, -1);
            bisDate.set(Calendar.MONTH, Calendar.DECEMBER);
            bisDate.set(Calendar.DAY_OF_MONTH, 31);
            Calendar vonDate = getActualDate();
            vonDate.set(Calendar.YEAR, bisDate.get(Calendar.YEAR));
            vonDate.set(Calendar.MONTH, Calendar.JANUARY);
            vonDate.set(Calendar.DAY_OF_MONTH, 1);
            return new VonBisDate(vonDate.getTime(), bisDate.getTime());
        }
    }//
    , Ltz12Mnt(R.string.Ltz12Mnt) {
        @Override
        public VonBisDate getDateSelection() {
            Calendar bisDate = getActualDate();
            Calendar vonDate = getActualDate();
            vonDate.add(Calendar.YEAR, -1);
            vonDate.add(Calendar.DAY_OF_MONTH, 1);
            return new VonBisDate(vonDate.getTime(), bisDate.getTime());
        }
    }//
    , EigDatum(R.string.EigDatum) {
        /**
         * @throws UnsupportedOperationException, da hier kein
         * automatisierter Auswertungszeitraum ermittelbar ist
         */
        @Override
        public VonBisDate getDateSelection() {
            // Keine Standarauswahl moeglich
            return null;
        }
    }//
    ;
    /**
     * Texte der Aufzaehlung als Array
     */
    private static String[] dateTexte;
    /**
     * ResID des Klartextes fuer die jeweilige Enum
     */
    public final int textResID;

    /**
     * Texte der Aufzaehlung als Array
     */
    public static String[] getDateTexte(Context context) {
        if (dateTexte == null) {
            dateTexte = new String[AWDateSelector.values().length];
            for (AWDateSelector ds : AWDateSelector.values()) {
                dateTexte[ds.ordinal()] = context.getString(ds.textResID);
            }
        }
        return dateTexte;
    }

    /**
     * Setzt ein Datum auf Ultimo des Monats in cal
     *
     * @param cal
     *         Datum
     * @return Ultimo des Monats in Date
     */
    private static Calendar getUltimoDate(Calendar cal) {
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal;
    }

    /**
     * @param textResID
     *         ResID des Klartextes fuer die jeweilige Enum
     */
    AWDateSelector(int textResID) {
        this.textResID = textResID;
    }

    /**
     * @return Aktuelles Datum mit Stunde, Minute, Secunde, Milisekunde = 0
     */
    public Calendar getActualDate() {
        return Calendar.getInstance();
    }

    /**
     * Muss von jeder Enum implementiert werden.
     *
     * @return Liefert eine Klasse {@link VonBisDate} zuruck.
     */
    public abstract VonBisDate getDateSelection();

    /**
     * @return Liefert den Quartalsbeginn zum aktuellen Datum
     */
    public Calendar getQuartalBeginn() {
        Calendar quartalBeginn = Calendar.getInstance();
        int quartal = (quartalBeginn.get(Calendar.MONTH) / 4) * 3;
        quartalBeginn.set(Calendar.MONTH, quartal);
        quartalBeginn.set(Calendar.DAY_OF_MONTH, 1);
        return quartalBeginn;
    }

    /**
     * @param cal
     *         Basisdatum
     * @return Liefert den Quartalsbeginn zum Basisdatum cal
     */
    public Calendar getQuartalBeginn(Calendar cal) {
        Calendar quartalBeginn = (Calendar) cal.clone();
        int quartal = (cal.get(Calendar.MONTH) / 4) * 3;
        quartalBeginn.set(Calendar.MONTH, quartal);
        quartalBeginn.set(Calendar.DAY_OF_MONTH, 1);
        return quartalBeginn;
    }

    /**
     * Klasse zum festlegen des Auswertungszeitraumes
     */
    public static class VonBisDate implements Serializable {
        private volatile java.sql.Date startDate, endDate;

        public VonBisDate(Date von, Date bis) {
            startDate = new java.sql.Date(von.getTime());
            endDate = new java.sql.Date(bis.getTime());
        }

        /**
         * Vergleicht das uebergebene Datum mit dem aktuellen.
         *
         * @param vonBisDate
         *         zu vr
         * @return
         */
        public boolean equals(VonBisDate vonBisDate) {
            if (vonBisDate == null) {
                return false;
            }
            return vonBisDate.startDate.toString().equals(startDate.toString()) |
                    vonBisDate.endDate.equals(endDate);
        }

        /**
         * @return Liefert das EndeDatum zur Anzeige in View
         */
        public String getDisplayEndDate() {
            return AWDBConvert.convertDate(endDate);
        }

        /**
         * @return Liefert das StartDatum zur Anzeige in View
         */
        public String getDisplayStartDate() {
            return AWDBConvert.convertDate(startDate);
        }

        /**
         * @return Liefert das EndeDatum
         */
        public Date getEndDate() {
            return endDate;
        }

        /**
         * @return EndDatum im Format YYYY-MM-DD
         */
        public String getSQLEndDate() {
            return endDate.toString();
        }

        /**
         * @return StartDatum im Format YYYY-MM-DD
         */
        public String getSQLStartDate() {
            return startDate.toString();
        }

        /**
         * @return Liefert das Startdatum
         */
        public Date getStartDate() {
            return startDate;
        }
    }
}