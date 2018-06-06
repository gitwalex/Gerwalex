package com.gerwalex.gerwalex.utils;

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

import android.os.AsyncTask;

import com.gerwalex.gerwalex.gv.AWRemoteFileServer;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.gerwalex.gerwalex.activities.AWInterface.linefeed;

/**
 * Handler fuer Zugriffe auf einen FileServer. Alle Transaktionen werden in einem separatem
 * AsyncTask durchgefuehrt.
 */
public class AWRemoteFileServerHandler {
    private final AWRemoteFileServer mRemoteFileServer;
    private final ExecutionListener mExecutionListener;
    private final FTPClient mClient;
    private FTPFile[] mFiles;

    public AWRemoteFileServerHandler(AWRemoteFileServer remoteFileServer,
                                     ExecutionListener executionListener) {
        mRemoteFileServer = remoteFileServer;
        mExecutionListener = executionListener;
        switch (remoteFileServer.getConnectionType()) {
            case SSL:
                mClient = new FTPSClient();
                break;
            case NONSSL:
                mClient = new FTPClient();
                break;
            default:
                mClient = null;
        }
    }

    /**
     * Erstellt ggfs. einen Client entsprechend des {@link ConnectionType}. Gibt es keine
     * Verbindung, wird diese aufgebaut und eingeloggt.
     *
     * @throws ConnectionFailsException,
     *         wenn die Verbindung fehlgeschlagen ist.
     */
    private void connectClient() throws ConnectionFailsException {
        try {
            if (!mClient.isConnected()) {
                mClient.connect(mRemoteFileServer.getURL(), 21);
                mClient.enterLocalPassiveMode();
                if (!mClient.login(mRemoteFileServer.getUserID(),
                        mRemoteFileServer.getUserPassword())) {
                    throw new ConnectionFailsException(mClient);
                }
                if (mClient instanceof FTPSClient) {
                    // Set protection buffer size
                    ((FTPSClient) mClient).execPBSZ(0);
                    // Set data channel protection to private
                    ((FTPSClient) mClient).execPROT("P");
                }
            }
        } catch (IOException e) {
            throw new ConnectionFailsException(mClient);
        }
    }

    /**
     * /** Loescht ein File vom Server
     *
     * @param pathname
     *         Vollstaendiger Pfad zum File
     */
    public void deleteFile(final String pathname) {
        new RemoteFileServerTask() {
            @Override
            protected ConnectionFailsException doInBackground(Void... params) {
                try {
                    if (!mClient.deleteFile(pathname)) {
                        return new ConnectionFailsException("File not found");
                    }
                    return null;
                } catch (IOException e) {
                    return new ConnectionFailsException(mClient);
                }
            }
        }.execute();
    }

    /**
     * Baut die Verbindung zum Server wieder ab.
     */
    private void disconnectClient() {
        if (mClient != null && mClient.isConnected()) {
            try {
                mClient.logout();
                mClient.disconnect();
            } catch (IOException e) {
                //TODO Execption bearbeiten
                e.printStackTrace();
            }
        }
    }

    /**
     * @return Liefert die in {@link AWRemoteFileServerHandler#listFilesInDirectory(String, FTPFileFilter)} oder {@link AWRemoteFileServerHandler#listFiles(FTPFileFilter)} ermittelten
     * Files.
     */
    public FTPFile[] getFiles() {
        return mFiles;
    }

    /**
     * @param files
     *         die nach Abschluss von {@link AWRemoteFileServerHandler#listFilesInDirectory(String, FTPFileFilter)} oder {@link AWRemoteFileServerHandler#listFiles(FTPFileFilter)}
     *         ermittelten Files.
     */
    private void setFiles(FTPFile[] files) {
        mFiles = files;
    }

    /**
     * Listet alle Files im RootDirectory des Servers. Das Ergebnis kann durch {@link
     * AWRemoteFileServerHandler#getFiles()} abgeholt werden.
     *
     * @param filter
     *         FileFilter. Kann null sein
     */
    public void listFiles(FTPFileFilter filter) {
        listFilesInDirectory("/", filter);
    }

    /**
     * Ermittelt alle Files auf dem Remote-Server zu einem Directory. Das Ergebnis kann durch {@link
     * AWRemoteFileServerHandler#getFiles()} abgeholt werden.
     *
     * @param directory
     *         Directory
     * @param filter
     *         FileFilter. Kann null sein
     */
    public void listFilesInDirectory(final String directory, final FTPFileFilter filter) {
        new RemoteFileServerTask() {
            @Override
            protected ConnectionFailsException doInBackground(Void... params) {
                FTPFile[] files;
                try {
                    connectClient();
                    if (filter != null) {
                        files = mClient.listFiles(directory, filter);
                    } else {
                        files = mClient.listFiles(directory);
                    }
                    setFiles(files);
                    return null;
                } catch (IOException e) {
                    return new ConnectionFailsException(mClient);
                } catch (ConnectionFailsException e) {
                    return e;
                } finally {
                    disconnectClient();
                }
            }
        }.execute();
    }

    /**
     * Uebertraegt ein File auf den Server.
     *
     * @param transferFile
     *         das zu uebertragende File. Der Name des File auf dem Server entspriehc dem Namen
     *         dieses Files
     * @param destDirectoryName
     *         Verzeichnis auf dem Server, in dem das File gespeichert werden soll.
     */
    public void transferFile(final File transferFile, final String destDirectoryName) {
        new RemoteFileServerTask() {
            @Override
            protected ConnectionFailsException doInBackground(Void... params) {
                FileInputStream fis = null;
                try {
                    connectClient();
                    fis = new FileInputStream(transferFile);
                    mClient.changeWorkingDirectory(destDirectoryName);
                    mClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
                    mClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                    if (!mClient.storeFile(transferFile.getName(), fis)) {
                        throw new ConnectionFailsException("Filetransfer failed");
                    }
                    return null;
                } catch (IOException e) {
                    return new ConnectionFailsException(mClient);
                } catch (ConnectionFailsException e) {
                    return e;
                } finally {
                    disconnectClient();
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            //TODO Execption bearbeiten
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.execute();
    }

    /**
     * Ubertraegt ein File auf das Backupdirectory des Servers
     *
     * @param transferFile
     *         File, welches Uebertragen werden soll
     */
    public void transferFileToBackup(File transferFile) {
        String mDestDirectoryName = mRemoteFileServer.getBackupDirectory();
        transferFile(transferFile, mDestDirectoryName);
    }

    /**
     * Arten der Verbindungen zu einem Server.
     */
    public enum ConnectionType {
        /**
         * Uebertragung via SSL
         */
        SSL, /**
         * Uebertragung unverschluesselt
         */
        NONSSL
    }

    /**
     * Listener fuer auf dem Server laufende Transaktion
     */
    public interface ExecutionListener {
        /**
         * Wird nach Ende der Transaktion gerufen.
         *
         * @param result
         *         Die {@link ConnectionFailsException}, wenn Fehler
         *         aufgetretn sind. Ansonsten null.
         */
        void onEndFileServerTask(ConnectionFailsException result);

        /**
         * Wird vor Beginn der Transaktion gerufen.
         */
        void onStartFileServerTask();
    }

    /**
     * Exception, wenn bei der  Verbindung zum Server Fehler festgestellt wurden
     */
    public static class ConnectionFailsException extends Throwable {
        private final String[] status;

        /**
         * @param client
         *         der ausloesende Client
         */
        ConnectionFailsException(FTPClient client) {
            super("Fehler bei der Verbindung mit Server");
            this.status = client.getReplyStrings();
        }

        /**
         * @param message
         *         Message der Exception
         */
        ConnectionFailsException(String message) {
            super(message);
            this.status = new String[]{message};
        }

        /**
         * @return Statusmeldungen des Servers bei Fehlern.
         */
        public String[] getStatus() {
            return status;
        }

        /**
         * @return Liefert einen aufbereiteten String mit den StatusMessages. Jede einzelne Zeile
         * des Status + linefeed
         */
        public String getStatusMessage() {
            StringBuilder s = new StringBuilder();
            for (String val : getStatus()) {
                s.append(val).append(linefeed);
            }
            return s.toString();
        }
    }

    /**
     * Template fuer einen AsyncTask zur ausfuehrung des Auftrags
     */
    private abstract class RemoteFileServerTask
            extends AsyncTask<Void, Void, ConnectionFailsException> {
        /**
         * Ruft {@link ExecutionListener#onEndFileServerTask(ConnectionFailsException)}
         */
        @Override
        protected void onPostExecute(ConnectionFailsException result) {
            mExecutionListener.onEndFileServerTask(result);
        }

        /**
         * Ruft {@link ExecutionListener#onStartFileServerTask()}
         */
        @Override
        protected void onPreExecute() {
            mExecutionListener.onStartFileServerTask();
        }
    }
}
