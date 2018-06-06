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
import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Helper fuer Download einer url im UTF-8 Format.
 */
public class AWHTTPDownLoader extends AsyncTask<URL, Integer, AWHTTPDownLoader.HTTPDownloadResult> {
    private final HTTPDownLoadResultListener mResultListener;
    private final ProgressUpdateListener mProgressUpdateListener;

    /**
     * @param resultListener
     *         Listener fuer das Ergebnis
     */
    public AWHTTPDownLoader(@NonNull HTTPDownLoadResultListener resultListener) {
        this(resultListener, null);
    }

    /**
     * @param resultListener
     *         Listener fuer das Ergebnis
     * @param progresUpdateListener
     *         Listener fuer den Fortschritt
     */
    public AWHTTPDownLoader(@NonNull HTTPDownLoadResultListener resultListener,
                            ProgressUpdateListener progresUpdateListener) {
        mResultListener = resultListener;
        mProgressUpdateListener = progresUpdateListener;
    }

    /**
     * Laedt eine Datei zur Url herunter. Es kann nur eine URL uebergeben werden. Liefert der Server
     * im Header die zu erwartende Groesse, wird regelmaessig der {@link ProgressUpdateListener}
     * aufgerufen.
     *
     * @param urls
     *         eine Url, deren Inhalt heruntergeladen werden soll
     * @return {@link HTTPDownloadResult}
     */
    @Override
    protected HTTPDownloadResult doInBackground(URL... urls) {
        HTTPDownloadResult result = new HTTPDownloadResult();
        HttpURLConnection urlConnection = null;
        BufferedInputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            urlConnection = (HttpURLConnection) urls[0].openConnection();
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            int size = urlConnection.getContentLength();
            outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length, aktsize = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
                aktsize += length;
                if (size > 0) {
                    publishProgress(size, aktsize);
                }
            }
            result.setResult(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            result.setException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }

    /**
     * Nach Beendigung des Prozesses wird der {@link HTTPDownLoadResultListener} gerufen.
     *
     * @param result
     *         {@link HTTPDownloadResult}
     */
    @Override
    protected void onPostExecute(HTTPDownloadResult result) {
        mResultListener.onHTTPDownLoadResult(result);
    }

    /**
     * Liefert der Server im Header die Groesse des Downloads, wird entsprechend der Fortschritt
     * gerechnet und der {@link ProgressUpdateListener} aufgerufen (wenn vorhanden).
     *
     * @param values
     *         values[0]: erwarte Downloadgroesse, values [1]: aktueller Stand.
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mProgressUpdateListener != null) {
            int fortschritt = values[1] * 100 / values[0];
            mProgressUpdateListener.onProgressUpdate(fortschritt);
        }
    }

    /**
     * Listener fuer das Ergebnis eines Downloads.
     */
    public interface HTTPDownLoadResultListener {
        /**
         * Wird mit dem Ergebnis des Downloads gerufen.
         *
         * @param result
         *         Ergebnis des Downloads als String im utf-8 format
         */
        void onHTTPDownLoadResult(HTTPDownloadResult result);
    }

    /**
     * Listener fuer den Fortschritt des Downloads. Wird nur gerufen, wenn der Server die erwartete
     * Groesse des Downloads im Header bekanntgibt.
     */
    public interface ProgressUpdateListener {
        /**
         * Wird mit dem Fortschritt des Downloads gerufen.
         *
         * @param percent
         *         Fortschritt des Downloads in Prozent
         */
        void onProgressUpdate(int percent);
    }

    /**
     * Klasse, die das Ergebnis des Downloads liefert.
     */
    public static class HTTPDownloadResult {
        private boolean erfolgreich;
        private Exception exception;
        private byte[] mBytes;

        /**
         * @return das Ergebnis asl Byte-Array. null, wenn der Download nicht erfolgreich war
         */
        public byte[] getByteResult() {
            return mBytes;
        }

        /**
         * @return wenn {@link HTTPDownloadResult#isErfolgreich()} false liefert eine ggfs.
         * aufgetretene Exception. Kann aber auch null sein, wenn der Fehler undefiniert ist.
         */
        public Exception getException() {
            return exception;
        }

        /**
         * Setzt eine beim Download ggfs. aufgetretende Exception.
         *
         * @param exception
         *         Excepition
         */
        public void setException(Exception exception) {
            this.exception = exception;
            erfolgreich = false;
        }

        /**
         * @return das Ergebnis als String im utf-8 Format. null, wenn kein Ergebnis vorliegt.
         *
         * @throws UnsupportedEncodingException
         */
        public String getStringResult() throws UnsupportedEncodingException {
            if (erfolgreich) {
                return new String(mBytes, Charset.forName("UTF-8"));
            }
            return null;
        }

        /**
         * @return true, wenn der Download erfolgreich war.
         */
        public boolean isErfolgreich() {
            return erfolgreich;
        }

        /**
         * Liefert das Ergebnis des Download ein.
         *
         * @param bytes
         *         Byte-Array, welches heruntergeladen wurde.
         */
        private void setResult(byte[] bytes) {
            mBytes = bytes;
            erfolgreich = true;
        }
    }
}
