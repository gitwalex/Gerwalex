package com.gerwalex.gerwalex.security;

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

import android.support.annotation.NonNull;
import android.util.Base64;

import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Klasse fuer Ver-/Entschlueseln von Strings/Bytefolgen
 */
public final class AWAESEncrypter {
    private static final int ITERATION_COUNT = 2048;
    private static final int KEY_LENGTH = 256;
    private static final byte[] SALT =
            {(byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35,
                    (byte) 0xE3, (byte) 0x03};
    private static AWAESEncrypter mDefaultEncrypter;
    private static boolean checked;
    private Cipher dcipher;
    private Cipher ecipher;

    /**
     * Checked, ob das uebergebene Passwort zum Schluessel passt
     *
     * @param passPhrase
     *         Passwort
     * @param mKey
     *         passender Key
     * @return true, wenn Schluesel und key zusammenpassen. Dann kann ueber {@link
     * AWAESEncrypter#getDefault()} der entsprechende Encrypter besorgt werden. Sonst false.
     */
    public static boolean checkPassword(@NonNull String passPhrase, @NonNull byte[] mKey) {
        checked = false;
        try {
            byte[] generatedKey = generateKey(passPhrase);
            if (Arrays.equals(mKey, generatedKey)) {
                mDefaultEncrypter = new AWAESEncrypter(mKey);
                checked = true;
            }
        } catch (Exception e) {
            //TODO Execption bearbeiten
            e.printStackTrace();
        }
        return checked;
    }

    /**
     * Liefert einen Encrypter zu einem Key. Dieser key kann z.B. mittels {@link
     * AWAESEncrypter#generateKey(String)} erstellt werden. Sinnvoll z.B. bei Passwortwechsel
     *
     * @param key
     *         key
     * @return Encrypter passend zum key.
     *
     * @throws Exception
     *         Wenn beim Erstellen eines neuen Encryptors etwas schiefgegangen ist.
     */
    public static AWAESEncrypter createInstance(byte[] key) throws Exception {
        mDefaultEncrypter = new AWAESEncrypter(key);
        return mDefaultEncrypter;
    }

    /**
     * Generiert eiinen Schluessel zu einem Passwort
     *
     * @param password
     *         Passwort
     * @return key
     *
     * @throws Exception
     *         wenn bei der Generierung Fehler aufgetretn sind
     */
    public static byte[] generateKey(String password) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return tmp.getEncoded();
    }

    /**
     * @return Liefert den Default-Encrypter. Wird erstellt, wenn {@link
     * AWAESEncrypter#checkPassword(String, byte[])} mit true beendet wurde
     */
    public static AWAESEncrypter getDefault() {
        return mDefaultEncrypter;
    }

    public static void removeDefault() {
        if (!checked) {
            mDefaultEncrypter = null;
        }
    }

    /**
     * Erstellt einen Encrypter.
     *
     * @param key,
     *         der fuer die Ver-/Entschlueselung gueltig ist
     * @throws IllegalArgumentException
     *         wenn passphrase und mKey nicht zueinander passen.
     * @throws Exception
     *         bei sonstigen Fehlern
     */
    private AWAESEncrypter(byte[] key) throws Exception {
        SecretKey secret = new SecretKeySpec(key, "AES");
        ecipher = Cipher.getInstance("AES");
        ecipher.init(Cipher.ENCRYPT_MODE, secret);
        dcipher = Cipher.getInstance("AES");
        dcipher.init(Cipher.DECRYPT_MODE, secret);
    }

    /**
     * Entschluesselt einen String
     *
     * @param decrypt
     *         String zum entschluesseln
     * @return Entschluesselten String
     *
     * @throws Exception
     *         Wenn beim Entschluesseln Fehler aufgetreten sind.
     */
    public String decrypt(String decrypt) throws Exception {
        byte[] bytes = Base64.decode(decrypt, Base64.NO_WRAP);
        byte[] decrypted = decrypt(bytes);
        return new String(decrypted, "UTF8");
    }

    /**
     * Entschluesselt eine Bytefolge
     *
     * @param decrypt
     *         Bytefolge zum entschluesseln
     * @return Entschluesselte Bytefolge
     *
     * @throws Exception
     *         Wenn beim Entschluesseln Fehler aufgetreten sind.
     */
    public byte[] decrypt(byte[] decrypt) throws Exception {
        return dcipher.doFinal(decrypt);
    }

    /**
     * Verschluesselt einen String
     *
     * @param encrypt
     *         String zum verschluesseln
     * @return Verschluesselten String
     *
     * @throws Exception
     *         Wenn beim Entschluesseln Fehler aufgetreten sind.
     */
    public String encrypt(String encrypt) throws Exception {
        byte[] bytes = encrypt.getBytes("UTF8");
        byte[] encrypted = encrypt(bytes);
        return Base64.encodeToString(encrypted, Base64.NO_WRAP);
    }

    /**
     * Verschluesselt eine Bytefolge
     *
     * @param encrypt
     *         Bytefolge zum verschluesseln
     * @return Verschluesselte Bytefolge
     *
     * @throws Exception
     *         Wenn beim Entschluesseln Fehler aufgetreten sind.
     */
    public byte[] encrypt(byte[] encrypt) throws Exception {
        return ecipher.doFinal(encrypt);
    }
}