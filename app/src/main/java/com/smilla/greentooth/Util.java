//TODO: CREATE WRAPPER FUNCTION FOR GETTING DELAY INT FROM SHAREDPREFERENCES
package com.smilla.greentooth;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.smilla.greentooth.GreenApplication.APP_KEY;
import static com.smilla.greentooth.GreenApplication.DEFAULT_DELAY;
import static com.smilla.greentooth.GreenApplication.DELAY_KEY;
import static com.smilla.greentooth.GreenApplication.LAST_NOTIFICATION_ID_KEY;
import static com.smilla.greentooth.GreenApplication.MAX_MINUTE_DELAY;
import static com.smilla.greentooth.GreenApplication.MAX_SECOND_DELAY;
import static com.smilla.greentooth.GreenApplication.NOTIFICATION_TYPE_PRE_DISABLE;
import static com.smilla.greentooth.GreenApplication.POST_DISABLE_CHANNEL_ID;
import static com.smilla.greentooth.GreenApplication.PRE_DISABLE_CHANNEL_ID;
import static com.smilla.greentooth.GreenApplication.PRE_DISABLE_NOTIFICATION_ID;

class Util {

    private static int[] bluetoothProfiles;

    public static void setBluetoothProfiles(int[] bluetoothProfiles) {
        Util.bluetoothProfiles = bluetoothProfiles;
    }

    public static void setBluetoothProfiles(int sdkInt) {
        Util.bluetoothProfiles = getBuildBluetoothProfiles(sdkInt);
    }

    public static int[] getBluetoothProfiles() {
        if (Util.bluetoothProfiles == null) {
            Util.bluetoothProfiles = getBuildBluetoothProfiles(Build.VERSION.SDK_INT);
        }
        return Util.bluetoothProfiles;
    }

    @SuppressLint("InlinedApi")
    @SuppressWarnings("deprecation")
    private static int[] getBuildBluetoothProfiles(int sdkInt) {
        int[] profiles;
        if (sdkInt >= 29) {
            profiles = new int[] {BluetoothProfile.HEADSET, BluetoothProfile.A2DP,
                    BluetoothProfile.GATT, BluetoothProfile.GATT_SERVER, BluetoothProfile.SAP,
                    BluetoothProfile.HID_DEVICE, BluetoothProfile.HEARING_AID};
        } else if (sdkInt >= 28) {
            profiles = new int[] {BluetoothProfile.HEADSET, BluetoothProfile.A2DP,
                    BluetoothProfile.HEALTH, BluetoothProfile.GATT, BluetoothProfile.GATT_SERVER,
                    BluetoothProfile.SAP, BluetoothProfile.HID_DEVICE};
        } else if (sdkInt >= 23) {
            profiles = new int[] {BluetoothProfile.HEADSET, BluetoothProfile.A2DP,
                    BluetoothProfile.HEALTH, BluetoothProfile.GATT, BluetoothProfile.GATT_SERVER,
                    BluetoothProfile.SAP};
        } else if (sdkInt >= 18) {
            profiles = new int[] {BluetoothProfile.HEADSET, BluetoothProfile.A2DP,
                    BluetoothProfile.HEALTH, BluetoothProfile.GATT, BluetoothProfile.GATT_SERVER};
        } else {
            profiles = new int[] {BluetoothProfile.HEADSET, BluetoothProfile.A2DP,
                    BluetoothProfile.HEALTH};
        }
        return profiles;
    }

    static boolean isBluetoothEnabled(BluetoothAdapter bluetoothAdapter) {
        int state = bluetoothAdapter.getState();
        return state != BluetoothAdapter.STATE_OFF && state != BluetoothAdapter.STATE_TURNING_OFF;
    }

    static boolean isBluetoothConnected(BluetoothAdapter bluetoothAdapter) {
        int state;
        for (int profile : getBluetoothProfiles()) {
            state = bluetoothAdapter.getProfileConnectionState(profile);
            if (state == BluetoothProfile.STATE_CONNECTED || state == BluetoothProfile.STATE_CONNECTING) {
                return true;
            }
        }
        return false;
    }

    static void cancelNotification(Context context, String tag, int notificationId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(tag, notificationId);
    }

    static void sendNotification(Context context, String title, String body, int notificationType) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, intent, 0);
        String channelID;
        if (notificationType == NOTIFICATION_TYPE_PRE_DISABLE) {
            channelID = PRE_DISABLE_CHANNEL_ID;
        } else {
            channelID = POST_DISABLE_CHANNEL_ID;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                channelID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        int notificationId;
        if (notificationType == NOTIFICATION_TYPE_PRE_DISABLE) {
            notificationId = PRE_DISABLE_NOTIFICATION_ID;
            builder.setPriority(NotificationCompat.PRIORITY_LOW);

            Intent tempDisableIntent = new Intent(context, BluetoothReceiver.class);
            tempDisableIntent.setAction(GreenApplication.ACTION_TEMP_DISABLE);
            PendingIntent tempDisablePendingEvent = PendingIntent.getBroadcast(context, 0, tempDisableIntent, 0);
            builder.addAction(0, context.getString(R.string.abort_job_button), tempDisablePendingEvent);

            Intent disableIntent = new Intent(context, BluetoothReceiver.class);
            disableIntent.setAction(GreenApplication.ACTION_DISABLE);
            PendingIntent disablePendingEvent = PendingIntent.getBroadcast(context, 0, disableIntent, 0);
            builder.addAction(0, context.getString(R.string.disable_button), disablePendingEvent);

        } else {
            notificationId = getNextNotificationId(context);
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify("TAG", notificationId, builder.build());
    }

    private static int getNextNotificationId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_KEY,0);
        int id = sharedPreferences.getInt(LAST_NOTIFICATION_ID_KEY, 0) + 1;
        if (id == Integer.MAX_VALUE) {
            id = 1;
        }
        sharedPreferences.edit().putInt(LAST_NOTIFICATION_ID_KEY, id).apply();
        return id;
    }

    public static String getDelayString(Context context, int delay) {
        Resources res = context.getResources();
        if (delay == 0) {
            return res.getString(R.string.none);
        }
        int minutes = delay / 60;
        int seconds = delay % 60;
        String minutesString = res.getQuantityString(R.plurals.minutes_delay, minutes, minutes);
        String secondsString = res.getQuantityString(R.plurals.seconds_delay, seconds, seconds);
        String resultString;
        if ((minutes > 0) && (seconds > 0)) {
            resultString = String.format("%s %s", minutesString, secondsString);
        } else if (minutes > 0) {
            resultString = String.format("%s", minutesString);
        } else {
            resultString = String.format("%s", secondsString);
        }
        return resultString;
    }

    public static int getSaneDelay(SharedPreferences sharedPreferences) {
        int delay = sharedPreferences.getInt(DELAY_KEY, DEFAULT_DELAY);
        int maxDelay = MAX_MINUTE_DELAY * 60 + MAX_SECOND_DELAY;
        if (delay > maxDelay) {
            delay = maxDelay;
        } else if (delay < 0) {
            delay = 0;
        }
        return delay;
    }

}
