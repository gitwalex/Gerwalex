package com.gerwalex.gerwalex;

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

/**
 * Verschiedene ResultCodes fuer Ergebnisse von Aktivitaeten
 */
public interface AWResultCodes {
    /**
     * Alles OK
     */
    int RESULT_OK = -1,//
    /**
     * TimeOut
     */
    RESULT_TimeOut = -3,//
    /**
     * Fehler bei Dateibearbeitung
     */
    RESULT_FILE_ERROR = -4,//
    /**
     * Fehler, wenn Eine TBD nicht gefunden werden kann
     */
    RESULT_FEHLER_TBD = -5,//
    /**
     * Fehlr, wenn eine gesuchte Splate in TBD nicht vohanden ist.
     */
    RESULT_SPALTE_IN_TBD_NICHT_VORHANDEN = -7,//
    /**
     * File not found
     */
    RESULT_FILE_NOTFOUND = -6,//
    /**
     * Sonstiger Fehler
     */
    RESULT_Divers = -99//
            ;
}
