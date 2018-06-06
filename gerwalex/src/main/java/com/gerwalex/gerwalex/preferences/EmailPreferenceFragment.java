package com.gerwalex.gerwalex.preferences;

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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v14.preference.PreferenceDialogFragment;
import android.view.View;
import android.widget.EditText;

import com.gerwalex.gerwalex.R;
import com.gerwalex.gerwalex.activities.AWInterface;

import static com.gerwalex.gerwalex.AWResultCodes.RESULT_Divers;
import static com.gerwalex.gerwalex.AWResultCodes.RESULT_OK;

/**
 * Fragment zur Eingabe von Mail-Adressen
 */
public class EmailPreferenceFragment extends PreferenceDialogFragment implements AWInterface {
    private EditText mEmailText;

    /**
     * Erstellt einen EditText zur Eingabe von Mail-Adressen
     *
     * @param pref
     *         EMail-Preference. Das {@link PreferenceDialogFragment} erwartet unter dem Tag {@link
     *         PreferenceDialogFragment#ARG_KEY} den key der Preference.
     * @return Fragment
     */
    public static EmailPreferenceFragment newInstance(EmailPreference pref) {
        final EmailPreferenceFragment fragment = new EmailPreferenceFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, pref.getKey());
        fragment.setArguments(b);
        return fragment;
    }

    /**
     * Erstellt den Dialog. View muss ein Element {@link EditText} mit der id 'emailEditText'
     * enthalten
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mEmailText = (EditText) view.findViewById(R.id.emailEditText);
        // Exception when there is no NumberPicker
        if (mEmailText == null) {
            throw new IllegalStateException(
                    "Dialog view must contain a EditText with id 'emailEditText'");
        }
        EmailPreference eMailPreference = (EmailPreference) getPreference();
        mEmailText.setText(eMailPreference.getText());
        mEmailText.selectAll();
    }

    /**
     * Prueft bei positiveResult, ob eine gueltige Mail-Adresse eingegeben wurde. In dem Fall wird
     * die Adresse in der Preferences gespeichert und das TagetFragment mit dem ResultCode
     * 'RESULT_OK' gerufen. Im Intent wird unter dem Tag 'DIALOGRESULT' dann die eingegebene
     * EmailAdresse mitgeliefert.
     * <p>
     * <p>
     * Ansonsten wird ein Dialog angezeigt und das TargetFragment mit 'RESULT_DIVERS' gerufen. Im
     * Intent wird unter dem Tag 'DIALOGRESULT' dann die urspruenglich eingegebene EmailAdresse
     * mitgeliefert.
     * <p>
     *
     * @param positiveResult
     *         true, wenn die Eingabe mit ok abgeschlossen wurd
     */
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // generate value to save
            Fragment targetFragment = getTargetFragment();
            Intent intent = new Intent();
            String text = mEmailText.getText().toString();
            EmailPreference eMailPreference = (EmailPreference) getPreference();
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                // Save the value
                eMailPreference.setText(text);
                intent.putExtra(DIALOGRESULT, text);
                targetFragment.onActivityResult(getTargetRequestCode(), RESULT_OK, intent);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.pkEmpfaenger);
                builder.setMessage(R.string.noValidEmailAdress);
                builder.setPositiveButton(R.string.awlib_btnAccept,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                builder.create().show();
                intent.putExtra(DIALOGRESULT, eMailPreference.getText());
                targetFragment.onActivityResult(getTargetRequestCode(), RESULT_Divers, intent);
            }
        }
    }
}