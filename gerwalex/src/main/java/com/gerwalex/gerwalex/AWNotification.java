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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import java.util.List;

import com.gerwalex.gerwalex.activities.AWInterface;
import com.gerwalex.gerwalex.application.AWApplication;

/**
 * Erstellt und ersetzt Notifications
 */
public class AWNotification implements AWInterface {
    private static int lastNotifyID = 1;
    protected final Bundle extras = new Bundle();
    private final Context context;
    private final String mChannelID;
    private final NotificationChannel mNotificationChannel;
    private String contentTitle;
    private boolean hasProgressBar;
    private int mNotifyID;
    private int number = NOID;
    private Class startActivity;
    private String ticker;

    /**
     * Siehe {@link AWNotification#AWNotification(Context, String)}
     *
     * @param startActivity Activity, die bei click auf Notification gestartet werden soll. Kann null sein, dann
     *                      wird die AWMainActivity gestartet.
     */
    public AWNotification(@NonNull Context context, @NonNull String contentTitle, Class startActivity) {
        this(context, contentTitle);
        this.startActivity = startActivity;
    }

    /**
     * @param context      Context
     * @param contentTitle 1. Zeile der Notification
     */
    public AWNotification(@NonNull Context context, @NonNull String contentTitle) {
        this.context = context;
        this.contentTitle = contentTitle;
        mNotifyID = lastNotifyID;
        lastNotifyID++;
        mChannelID = ((AWApplication) context.getApplicationContext()).getNotficationChannelID();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationChannel = new NotificationChannel(mChannelID, "Nachricht", NotificationManager.IMPORTANCE_DEFAULT);
        } else {
            mNotificationChannel = null;
        }
    }

    /**
     * @param extras Bundle fuer die zu rufende Activity
     */
    @CallSuper
    public void addExtras(Bundle extras) {
        this.extras.putAll(extras);
    }

    /**
     * Cancelt die Notification
     */
    public void cancel() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) context.getSystemService(ns);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nMgr.createNotificationChannel(mNotificationChannel);
        }
        nMgr.cancel(mNotifyID);
    }

    /**
     * Erstelle Notification.
     *
     * @param contentListHeader Header der Liste.
     * @param contentListTexte  Liste der NotificationTexte
     * @return die NotificationID
     */
    public int createNotification(@NonNull String contentListHeader, @NonNull List<String> contentListTexte) {
        NotificationCompat.Builder mBuilder = getNotification();
        mBuilder.setContentTitle(contentTitle);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setSummaryText(contentListHeader);
        for (String s : contentListTexte) {
            inboxStyle.addLine(s);
        }
        mBuilder.setContentText(contentListHeader);
        mBuilder.setStyle(inboxStyle);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }

        mNotificationManager.notify(mNotifyID, mBuilder.build());
        return mNotifyID;
    }

    /**
     * Erstellt Notification mit  Titel aus Konstruktor und ContentText
     *
     * @param contentText ContentText
     * @return NotifyID
     */
    public int createNotification(@NonNull String contentText) {
        return createNotification(contentTitle, contentText);
    }

    /**
     * Erstellt Notification mit neuem Titel und neuen Text
     *
     * @param contentTitle ContentTitel
     * @param contentText  ContentText
     * @return NotifyID
     */
    public int createNotification(@NonNull String contentTitle, @NonNull String contentText) {
        NotificationCompat.Builder mBuilder = getNotification();
        mBuilder.setContentTitle(contentTitle);
        mBuilder.setContentText(contentText);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }

        mNotificationManager.notify(mNotifyID, mBuilder.build());
        return mNotifyID;
    }

    public Context getContext() {
        return context;
    }

    /**
     * @return Liefert das Icon zuruck, welches neben der Notification gezeigt wird. Default:
     * R.drawable.ic_stat_action_account
     */
    @DrawableRes
    protected int getIconResID() {
        return R.drawable.ic_stat_action_account;
    }

    /**
     * Erstellt einen NotificationBuilder mit Ticker {@link AWNotification#ticker}, hasProgressBar
     * und setzt (wenn gesetzt) startActivity als StartActivity.
     *
     * @return NotificationBuilder
     */
    protected NotificationCompat.Builder getNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, mChannelID);
        mBuilder.setSmallIcon(getIconResID()).setAutoCancel(true);
        if (ticker != null) {
            mBuilder.setTicker(ticker);
        }
        if (number != NOID) {
            mBuilder.setNumber(number);
        }
        mBuilder.setProgress(0, 0, hasProgressBar);
        Intent intent;
        if (startActivity != null) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            intent = new Intent(context, startActivity);
            intent.putExtras(extras);
            stackBuilder.addNextIntent(intent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
        }
        return mBuilder;
    }

    public int getNotifyID() {
        return mNotifyID;
    }

    /**
     * Setzt die Notification mit einem neuen Text
     *
     * @param contentText Text
     * @return NotifyID
     */
    public int replaceNotification(@NonNull String contentText) {
        return replaceNotification(contentTitle, contentText);
    }

    /**
     * Notification mit nur einer Zeile.
     *
     * @param contentTitle Title der Notification
     * @param contentText  Text der Notification
     * @return NotifyID
     */
    public int replaceNotification(@NonNull String contentTitle, @NonNull String contentText) {
        NotificationCompat.Builder mBuilder = getNotification();
        mBuilder.setContentTitle(contentTitle);
        mBuilder.setContentText(contentText);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }

        mNotificationManager.notify(mNotifyID, mBuilder.build());
        return mNotifyID;
    }

    /**
     * Ersetze Notification.
     *
     * @param contentListHeader Header der Liste.
     * @param contentListTexte  Liste der NotificationTexte
     * @return die NotificationID
     */
    public int replaceNotification(@NonNull String contentListHeader, @NonNull List<String> contentListTexte) {
        NotificationCompat.Builder mBuilder = getNotification();
        mBuilder.setContentTitle(contentTitle);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setSummaryText(contentListHeader);
        for (String s : contentListTexte) {
            inboxStyle.addLine(s);
        }
        mBuilder.setContentText(contentListHeader);
        mBuilder.setStyle(inboxStyle);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }
        mNotificationManager.notify(mNotifyID, mBuilder.build());
        return mNotifyID;
    }

    /**
     * Setzt den Ticker fuer die Notification
     *
     * @param contentTitle Titel der Notification
     */
    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    /**
     * @param hasProgressBar wenn true, wird in der Notification eine Progressbar gezeigt
     */
    public void setHasProgressBar(boolean hasProgressBar) {
        this.hasProgressBar = hasProgressBar;
    }

    /**
     * @param number Number, die am Ende der Notification angezeigt werden soll, z.B. die Anzahl der
     *               importierten Umsaetze. Keine Anzeige, wenn NOID
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Setzt den Ticker fuer die Notification
     *
     * @param ticker Tickertext
     */
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public AWNotification startForegroundNotification(@NonNull Service service, @NonNull String contentText) {
        NotificationCompat.Builder mBuilder = getNotification();
        mBuilder.setContentTitle(contentTitle);
        mBuilder.setContentText(contentText);
        service.startForeground(mNotifyID, mBuilder.build());
        return this;
    }
}
