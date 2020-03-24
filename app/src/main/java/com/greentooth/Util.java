/*
   Copyright 2020 Nicklas Bergman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.greentooth;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

class Util {

    private static final int[] profiles = getBuildProfiles();

    private static int[] getBuildProfiles() {
        int[] profiles;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            profiles = new int[] {BluetoothProfile.HEADSET, BluetoothProfile.A2DP,
                    BluetoothProfile.GATT, BluetoothProfile.GATT_SERVER, BluetoothProfile.SAP,
                    BluetoothProfile.HID_DEVICE, BluetoothProfile.HEARING_AID};
        } else if (Build.VERSION.SDK_INT >= 28) {
            profiles = new int[] {BluetoothProfile.HEADSET, BluetoothProfile.A2DP,
                    BluetoothProfile.HEALTH, BluetoothProfile.GATT, BluetoothProfile.GATT_SERVER,
                    BluetoothProfile.SAP, BluetoothProfile.HID_DEVICE};
        } else if (Build.VERSION.SDK_INT >= 23) {
            profiles = new int[] {BluetoothProfile.HEADSET, BluetoothProfile.A2DP,
                    BluetoothProfile.HEALTH, BluetoothProfile.GATT, BluetoothProfile.GATT_SERVER,
                    BluetoothProfile.SAP};
        } else if (Build.VERSION.SDK_INT >= 18) {
            profiles = new int[] {BluetoothProfile.HEADSET, BluetoothProfile.A2DP,
                    BluetoothProfile.HEALTH, BluetoothProfile.GATT, BluetoothProfile.GATT_SERVER};
        } else {
            profiles = new int[] {BluetoothProfile.HEADSET, BluetoothProfile.A2DP,
                    BluetoothProfile.HEALTH};
        }
        return profiles;
    }

    static void SendNotification(Context context, String title, String body) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                context.getString(R.string.channel_id))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(body)
                .setColorized(true)
                .setColor(Color.GREEN)
                .setLights(Color.rgb(31, 85, 244), 200, 200)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        int notificationId = getNextNotifId(context);
        notificationManager.notify(notificationId, builder.build());
    }

    static boolean isEnabled(BluetoothAdapter bluetoothAdapter) {
        int state = bluetoothAdapter.getState();
        return state != BluetoothAdapter.STATE_OFF && state != BluetoothAdapter.STATE_TURNING_OFF;
    }

    static boolean notConnected(BluetoothAdapter bluetoothAdapter) {
        int state;
        for (int profile : profiles) {
            state = bluetoothAdapter.getProfileConnectionState(profile);
            if (state == BluetoothProfile.STATE_CONNECTED || state == BluetoothProfile.STATE_CONNECTING) {
                return false;
            }
        }
        return true;
    }

    private static final String PREFERENCE_LAST_NOTIF_ID = "PREFERENCE_LAST_NOTIF_ID";

    private static int getNextNotifId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(
                R.string.preference_name),0);
        int id = sharedPreferences.getInt(PREFERENCE_LAST_NOTIF_ID, 0) + 1;
        if (id == Integer.MAX_VALUE) { id = 0; } // isn't this over kill ??? hahaha!!  ^_^
        sharedPreferences.edit().putInt(PREFERENCE_LAST_NOTIF_ID, id).apply();
        return id;
    }

    /**
     * Get activity instance from desired context.
     */
    public static Activity getActivity(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) return (Activity) context;
        if (context instanceof ContextWrapper) return getActivity(((ContextWrapper)context).getBaseContext());
        return null;
    }
}
