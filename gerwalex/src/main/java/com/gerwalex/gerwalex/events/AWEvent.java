package com.gerwalex.gerwalex.events;

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
 * Definiert moegliche Events innerhalb AWLib.
 */
public interface AWEvent {
    int
            /**
             * Event fuer Sicherung der Datenbank
             */
            DoDatabaseSave = 1,//
    /**
     * Event zur kompriemierung Datenbank
     */
    doVaccum = 2,//
    /**
     * Event restore Datenbank
     */
    showBackupFiles = 3,//
    /**
     *
     */
    showRemoteFileServer = 4,//
    /**
     *
     */
    configRemoteFileServer = 5,//
    /**
     *
     */
    copyAndDebugDatabase = 6,//
    /**
     * Zeigt ein Image. Der Filename (absolut) muss  als String
     * geliefert werden. Der Name des zu ladenden Files wird im Titel angezeigt.Gibt es im Bundle
     * unter 'FRAGMENTTITLE' einen Text, wird dieser als Titel angezeigt. Ansonsten der Letzte Teil
     * des Filenamens
     */
    ShowPicture = 7,//
            AWLibDailyEvent = 8;
}
