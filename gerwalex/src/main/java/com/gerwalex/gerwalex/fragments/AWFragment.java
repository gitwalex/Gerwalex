package com.gerwalex.gerwalex.fragments;

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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gerwalex.gerwalex.activities.AWActivityActions;
import com.gerwalex.gerwalex.activities.AWBasicActivity;
import com.gerwalex.gerwalex.activities.AWInterface;
import com.gerwalex.gerwalex.activities.AWMainActivity;
import com.gerwalex.gerwalex.application.AWApplication;

import static com.gerwalex.gerwalex.events.AWEvent.ShowPicture;

/**
 * Template fuer MonMaFragmente
 * <p/>
 * Folgende Funktionen:
 * Bereitstellung eines Bundle 'args' fuer alle abgeleiteten Klassen
 */
public abstract class AWFragment extends DialogFragment
        implements AWInterface, AWFragmentInterface {
    /**
     * TAG Fuer die Fragmente
     */
    public final String TAG = this.getClass().getSimpleName();
    /**
     * Bundle fuer ein Fragment. Wird in onCreate() wiederhergestellt und in OnSaveStateInstance()
     * gesichert.
     */
    protected final Bundle args = new Bundle();
    /**
     * Layout des Fragments
     */
    protected int layout = NOLAYOUT;
    /**
     * SharedPreferences werden allen abgeleiteten Fragmenten bereitgestellt
     */
    protected SharedPreferences prefs;
    protected boolean isCanceled;
    private int containerID;
    /**
     * Merker, ob der ActionBarSubtitle ueberschrieben wurde.
     */
    private boolean isSavedActionBarSubtitle;
    private OnAWFragmentCancelListener mOnCancelListener;
    /**
     *
     */
    private OnAWFragmentDismissListener mOnDismissListener;
    /**
     * Gemerkter ActionBarSubTitle. Wird in onPause() wiederhergestellt.
     */
    private CharSequence mSavedActionBarSubtitle;
    private MainAction mainAction;
    /**
     * Dient zur Berechnung der Startdauer
     */
    private long timer;

    public AWFragment() {
        timer = System.currentTimeMillis();
    }

    protected final void Log(String message) {
        AWApplication.Log(message);
    }

    protected final void LogError(String message) {
        AWApplication.LogError(message);
    }

    public MainAction getMainAction() {
        return mainAction;
    }

    @Override
    public final String getTAG() {
        return TAG;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String title = args.getString(ACTIONBARTITLE);
        if (title != null) {
            setTitle(title);
        }
        String subTitle = args.getString(ACTIONBARSUBTITLE);
        if (subTitle != null) {
            setSubTitle(subTitle);
        }
    }

    /**
     * Die aktuellen Preferences werden ermittelt und in prefs gespeichert. Ausserdem werden die
     * Argumente aus {@link Fragment#getArguments()} gelesen und in args gespeichert. Danach wird
     * {@link AWFragment#setInternalArguments(Bundle)} } gerufen.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Bundle argumente = getArguments();
        if (argumente != null) {
            args.putAll(argumente);
        }
        setInternalArguments(args);
    }

    /**
     * Wird ein Dialog gecancelt, wird der mOnCancelListener gerufen (wenn vorhanden)
     * Ausserdem ist dann isCanceled true.
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        isCanceled = true;
        if (mOnCancelListener != null) {
            mOnCancelListener.onCancel(layout, dialog);
        }
    }

    /**
     * Setzen der durch setArguments(args) erhaltenen bzw. Ruecksichern der Argumente im Bundle
     * args.
     * Gibt es keine MainAction unter AWLIBACTION, wird MainAction.SHOW verwendet.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            args.putAll(savedInstanceState);
        }
        layout = args.getInt(LAYOUT, NOLAYOUT);
        mainAction = args.getParcelable(AWLIBACTION);
        if (mainAction == null) {
            mainAction = MainAction.SHOW;
        }
    }

    /**
     * Handelt es sich um einen Dialog, wird null zurueckgegeben. Ansoonsten wird die View erstellt
     * und zurueckgegeben.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container != null) {
            this.containerID = container.getId();
        }
        if (!getShowsDialog()) {
            return layout == NOLAYOUT ? null : inflater.inflate(layout, container, false);
        }
        return null;
    }

    /**
     * Nur loggen, dass Fragment destroeyed wurde.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log("Fragment " + getClass().getSimpleName() + " destroyed");
    }

    /**
     * Der Dailog verschwindet bei eine Aenderung der Configuration (z.B. drehen). Wenn das Fragment
     * retainInstance(true) gesetz hat, wird dies mit diesem Code verhindert. @see <a
     * href="https://code.google.com/p/android/issues/detail?id=17423">Hinweis</a>
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    /**
     * Bei Dismiss wird ein OnDismissListener informiert(wenn vorhanden).
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (!isCanceled && mOnDismissListener != null) {
            mOnDismissListener.onDismiss(layout, dialog);
        }
    }

    /**
     * Deregistrierung als OnSharedPreferenceListener, wenn die Klasse eine Instanz von
     * OnSharedPreferenceChangeListener ist.
     * Wiederherstellen eines Subtitles, wenn vorhanden.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (this instanceof SharedPreferences.OnSharedPreferenceChangeListener) {
            prefs.unregisterOnSharedPreferenceChangeListener(
                    (SharedPreferences.OnSharedPreferenceChangeListener) this);
        }
        if (isSavedActionBarSubtitle) {
            setSubTitle(mSavedActionBarSubtitle);
        }
    }

    /**
     * Ist die Klasse eine Instanz von OnSharedPreferenceChangeListener, wird diese als {@link
     * SharedPreferences#registerOnSharedPreferenceChangeListener} registriert.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (this instanceof SharedPreferences.OnSharedPreferenceChangeListener) {
            prefs.registerOnSharedPreferenceChangeListener(
                    (SharedPreferences.OnSharedPreferenceChangeListener) this);
        }
        CharSequence title = args.getCharSequence(ACTIONBARTITLE);
        if (title != null) {
            setTitle(title);
        }
    }

    /**
     * Sichern des bereitgestellten Bundles args. Wird in onCreate() wiederhergestellt.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putAll(args);
        super.onSaveInstanceState(outState);
    }

    /**
     * Ausgabe Dauer des Starts des Fragments
     */
    @Override
    public void onStart() {
        super.onStart();
        if (timer != 0) {
            Log("Fragment " + getClass().getSimpleName() + " Startdauer: " +
                    String.valueOf(System.currentTimeMillis() - timer));
            timer = 0;
        }
    }

    /**
     * Ersetzt das aktuelle Fragment durch das uebergebene Fragment und fuegt es dem BackStacj
     * hinzu.
     *
     * @param f
     *         neues Fragment
     *
     * @throws IllegalStateException,
     *         wenn nicht super.onCreate(...) gerufen wurde..
     */
    protected void replaceFragment(AWFragment f) {
        if (containerID == NOID) {
            throw new IllegalStateException(
                    "Container nicht bekannt. Wurde super.onCreateView(...) gerufen?");
        }
        getActivity().getSupportFragmentManager().beginTransaction().replace(containerID, f)
                .addToBackStack(null).commit();
    }

    /**
     * Uebernimmt Argumente zusaetzlich(!) zu bereits vorhandenen
     *
     * @see Fragment#setArguments(Bundle)
     */
    public final void setArguments(Bundle args) {
        Bundle oldArgs = getArguments();
        if (oldArgs != null) {
            if (args != null) {
                oldArgs.putAll(args);
            }
        } else {
            super.setArguments(args);
        }
    }

    /**
     * Setzt die Sichtbarkkeit des DefaultFloatingActionButton
     *
     * @param visible
     *         true: Visible
     *
     * @throws ClassCastException
     *         wenn Activity nicht Instance von {@link AWMainActivity}
     */
    protected void setDefaultFabVisibility(boolean visible) {
        FloatingActionButton fab = ((AWMainActivity) getActivity()).getDefaultFAB();
        if (visible) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
    }

    /**
     * Methode wird aus onAttach gerufen. Im uebergebenen Bundle sind alle Argumente gespeichert,
     * die ggfs. in newInstance(...) belegt wurden.
     * Zweckmaessigerweise wird zuerst super.setInternalArguments(args) gerufen. Danach sind in args
     * die Argumente der Vererbungshirache vorhanden, welche auch ueberschrieben werden koennen. Es
     * koennen weitere Argumente zum Initialisieren genau dieses Fragments gesetzt werden.
     * Argumente, die von einem vererbten Fragment gesetzt werden, sind aber noch nicht vorhanden.
     * Werden diese benoetigt, sollten diese fruehestens in onCreate(saveStateInstance) aus args
     * geholt werden.
     *
     * @param args
     *         Bundle mit Argumenten.
     */
    @CallSuper
    protected void setInternalArguments(Bundle args) {
    }

    /**
     * @param listener
     *         erweiterter {@link OnAWFragmentCancelListener}
     */
    public void setOnCancelListener(OnAWFragmentCancelListener listener) {
        mOnCancelListener = listener;
    }

    /**
     * @param listener
     *         erweiterter {@link OnAWFragmentDismissListener}
     */
    public void setOnDismissListener(OnAWFragmentDismissListener listener) {
        mOnDismissListener = listener;
    }

    /**
     * Setzt den SubTitle in der SupportActionBar. Rettet vorher den aktuellen Subtitle, der wird
     * dann in onPause() wiederhergestellt.
     *
     * @param subTitle
     *         Text des Subtitles
     */
    public void setSubTitle(CharSequence subTitle) {
        ActionBar bar = ((AWBasicActivity) getActivity()).getSupportActionBar();
        if (bar != null) {
            if (!isSavedActionBarSubtitle) {
                mSavedActionBarSubtitle = bar.getSubtitle();
                isSavedActionBarSubtitle = true;
            }
            bar.setSubtitle(subTitle);
        }
        args.putCharSequence(ACTIONBARSUBTITLE, subTitle);
    }

    /**
     * Setzt den SubTitle in der SupportActionBar Rettet vorher den aktuellen Subtitle, der wird
     * dann in onPause() wiederhergestellt.
     *
     * @param subTitleResID
     *         resID des Subtitles
     */
    public void setSubTitle(int subTitleResID) {
        setSubTitle(getString(subTitleResID));
    }

    /**
     * Setzt den Title in der SupportActionBar
     *
     * @param title
     *         Text des STitles
     */
    public void setTitle(CharSequence title) {
        ActionBar bar = ((AWBasicActivity) getActivity()).getSupportActionBar();
        if (bar != null) {
            bar.setTitle(title);
        }
        args.putCharSequence(ACTIONBARTITLE, title);
    }

    /**
     * Setzt den Title in der SupportActionBar
     *
     * @param titleResID
     *         resID des Titles
     */
    public void setTitle(int titleResID) {
        setTitle(getString(titleResID));
    }

    /**
     * Startet eine Activity zum anzeigen eines Bildes.
     *
     * @param filename
     *         Absoluter Path zum File. File muss ein Bild enthalten
     */
    protected void showPictureFrom(String filename) {
        Intent intent = new Intent(getActivity(), AWActivityActions.class);
        intent.putExtra(FILENAME, filename);
        intent.putExtra(AWLIBEVENT, ShowPicture);
        getActivity().startActivity(intent);
    }

    /**
     * Erweiterter Dialog-Cancel-Listener. Liefert zusaetzlich zum Dialog auch die layoutID des
     * Fragments mit.
     */
    public interface OnAWFragmentCancelListener {
        /**
         * Wird gerufen, wenn ein Dialog gecancelt wurde.Liefert zusaetzlich zum Dialog auch die
         * layoutID des Fragments mit.
         *
         * @param layoutID
         *         layout des Fragments
         * @param dialog
         *         Dialog
         */
        void onCancel(@LayoutRes int layoutID, DialogInterface dialog);
    }

    /**
     * Erweiterter Dialog-Dismiss-Listener. Liefert zusaetzlich zum Dialog auch die layoutID des
     * Fragments mit.
     */
    public interface OnAWFragmentDismissListener {
        /**
         * Wird gerufen, wenn ein Dialog beendet wurde.Liefert zusaetzlich zum Dialog auch die
         * layoutID des Fragments mit.
         *
         * @param layoutID
         *         layout des Fragments
         * @param dialog
         *         Dialog
         */
        void onDismiss(@LayoutRes int layoutID, DialogInterface dialog);
    }
}