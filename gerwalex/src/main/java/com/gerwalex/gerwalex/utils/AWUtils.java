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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.gerwalex.gerwalex.application.AWApplication;

import static com.gerwalex.gerwalex.AWResultCodes.RESULT_Divers;
import static com.gerwalex.gerwalex.AWResultCodes.RESULT_FILE_ERROR;
import static com.gerwalex.gerwalex.AWResultCodes.RESULT_FILE_NOTFOUND;
import static com.gerwalex.gerwalex.AWResultCodes.RESULT_OK;

/**
 * Utility-Klasse
 */
public final class AWUtils {
    private final static int BUFFERSIZE = 8192;

    /**
     * Erstellt ein Zip-Archiv und fuegt die Files eines Directories dem Zip-Archiv hinzu.
     *
     * @param target
     *         File, in dem das Archiv gespeichert werden soll
     * @param fileToZip
     *         Name des zu zippenden Verzeichnisses/Datei
     * @return Ergebnis der Operation
     */
    public static int addToZipArchive(File target, String fileToZip) {
        int ergebnis = RESULT_OK;
        ZipOutputStream zos = null;
        try {
            FileOutputStream fout = new FileOutputStream(target);
            zos = new ZipOutputStream(new BufferedOutputStream(fout));
            addDirToZipArchive(zos, fileToZip);
        } catch (IOException e) {
            ergebnis = RESULT_FILE_ERROR;
        } catch (Exception e) {
            ergebnis = RESULT_Divers;
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                ergebnis = RESULT_FILE_ERROR;
            }
        }
        return ergebnis;
    }

    /**
     * Kopiert ein File. Die Pruefung, ob das Zielfile z.B. wegen Berechtigungen erstellt werden
     * kann obliegt der aufrufenden Klasse.
     *
     * @param context
     *         Context
     * @param src
     *         File, welches kopiert werden soll
     * @param dest
     *         TargetFile
     * @throws IOException
     *         Bei Fehlern.
     */
    public static void copyFile(Context context, File src, File dest) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dest).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    /**
     * Prueft, ob Internetverbindung vorhanden ist.
     *
     * @param context
     *         Context
     * @return true, wenn irgendeine Internetverbindung vorhanden ist.
     */
    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Prueft, ob Internet via WiFi erreichbar ist
     *
     * @param context
     *         Context
     * @return true, wenn WiFi-Verbindung.
     */
    public static boolean isInternetWifi(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * @param context
     *         Context
     * @param prefix
     *         Name des Ordners, in dem das Photo abgelegt werden soll. Gibt es den Ordner noch
     *         nicht, wird er in {@link AWApplication#getApplicationPicturePath()} angelegt
     * @return einen Intent zum Starten der Camera oder null
     */
    public static Intent prepareNewPhoto(Context context, String prefix) {
        // the intent is my camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Setting the file Uri to my photo
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            String mPath =
                    ((AWApplication) context.getApplicationContext()).getApplicationPicturePath();
            File folder = new File(mPath + "/" + prefix);
            //if it doesn't exist the folder will be created
            if (!folder.exists()) {
                folder.mkdirs();
            }
            //getting uri of the file
            @SuppressLint("SimpleDateFormat") String timeStamp =
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File imageFile = new File(folder + File.separator + prefix + "_" + timeStamp + ".jpg");
            Uri file = Uri.fromFile(imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
            return intent;
        }
        return null;
    }

    /**
     * Liest eine Map aus Parcel.
     * <p/>
     * Usage: MyClass1 and MyClass2 must extend Parcelable Map<MyClass1, MyClass2> map;
     * <p/>
     * readParcelableMap(parcel, MyClass1.class, MyClass2.class);
     *
     * @param parcel
     *         parcel aus Kontruktor Object(Parcel parcel)
     * @param kClass
     *         Key-Klasse
     * @param vClass
     *         Value-Klasse
     * @param <K>
     *         Key. Parceable
     * @param <V>
     *         Value. Parceable
     * @return Map
     */
    public static <K extends Parcelable, V extends Parcelable> Map<K, V> readParcelableMap(
            Parcel parcel, Class<K> kClass, Class<V> vClass) {
        int size = parcel.readInt();
        Map<K, V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(kClass.cast(parcel.readParcelable(kClass.getClassLoader())),
                    vClass.cast(parcel.readParcelable(vClass.getClassLoader())));
        }
        return map;
    }

    public static int restoreZipArchivToFile(String target, File inputFile) {
        int result = RESULT_Divers;
        ZipInputStream is = null;
        try {
            is = new ZipInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
            while (is.getNextEntry() != null) {
                extractEntry(is, target);
            }
            result = RESULT_OK;
        } catch (FileNotFoundException e) {
            result = RESULT_FILE_NOTFOUND;
        } catch (IOException e) {
            result = RESULT_FILE_ERROR;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                //TODO Execption bearbeiten
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Schreibt Map nach Parcel
     * <p/>
     * Usage: MyClass1 and MyClass2 must extend Parcelable Map<MyClass1, MyClass2> map;
     * <p/>
     * // Writing to a parcel
     * <p/>
     * writeParcelableMap(parcel, flags, map); *
     *
     * @param parcel
     *         parcel aus {@link Parcelable#writeToParcel(Parcel, int)}
     * @param flags
     *         flags aus {@link Parcelable#writeToParcel(Parcel, int)}
     * @param map
     *         Map, die geschrieben werden soll
     * @param <K>
     *         Key der Map. Parceable
     * @param <V>
     *         Value der Map. Parceable
     */
    public static <K extends Parcelable, V extends Parcelable> void writeParcelableMap(
            Parcel parcel, int flags, Map<K, V> map) {
        parcel.writeInt(map.size());
        for (Map.Entry<K, V> e : map.entrySet()) {
            parcel.writeParcelable(e.getKey(), flags);
            parcel.writeParcelable(e.getValue(), flags);
        }
    }

    /**
     * Erstellt ein Zip-Archiv und fuegt die Files eines Directories dem Zip-Archiv hinzu.
     *
     * @param zos
     *         ZipOutputStraem
     * @param parrentDirectoryName
     *         Name des ParentDirectories.
     * @throws IOException
     *         Bei Fehlern
     */
    private static void addDirToZipArchive(ZipOutputStream zos, String parrentDirectoryName)
            throws IOException {
        File directoryToZip = new File(parrentDirectoryName);
        if (!directoryToZip.isDirectory()) {
            addFileToZipArchive(zos, directoryToZip);
            return;
        }
        for (File fileToZip : directoryToZip.listFiles()) {
            String zipEntryName;
            if (fileToZip.isDirectory()) {
                zipEntryName = fileToZip.getName();
                System.out.println("+" + zipEntryName);
                addDirToZipArchive(zos, zipEntryName);
            } else {
                addFileToZipArchive(zos, fileToZip);
            }
        }
    }

    /**
     * Fuegt die Files dem Zip-Archiv hinzu.
     *
     * @param zos
     *         ZipOutputStraem
     * @param fileToZip
     *         File, welches gezipt werden soll
     * @throws IOException
     *         Bei Fehlern
     */
    private static void addFileToZipArchive(ZipOutputStream zos, File fileToZip)
            throws IOException {
        if (fileToZip == null || !fileToZip.exists()) {
            return;
        }
        String zipEntryName = fileToZip.getName();
        System.out.println("   " + zipEntryName);
        byte[] buffer = new byte[BUFFERSIZE];
        FileInputStream fis = new FileInputStream(fileToZip);
        zos.putNextEntry(new ZipEntry(zipEntryName));
        int length;
        while ((length = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, length);
        }
        zos.closeEntry();
        fis.close();
    }

    /**
     * Utility method to read data from InputStream
     */
    private static void extractEntry(InputStream is, String extractTo) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(extractTo);
            final byte[] buf = new byte[BUFFERSIZE];
            int length;
            while ((length = is.read(buf, 0, buf.length)) >= 0) {
                fos.write(buf, 0, length);
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
}

