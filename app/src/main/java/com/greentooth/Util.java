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

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.greentooth.GreenApplication.APP_KEY;
import static com.greentooth.GreenApplication.CHANNEL_ID;
import static com.greentooth.GreenApplication.LAST_NOTIFICATION_ID_KEY;

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

    static void sendNotification(Context context, String title, String body) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(body)
                .setColorized(true)
                .setColor(Color.GREEN)
                .setLights(Color.rgb(31, 85, 244), 200, 200)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int notificationId = getNextNotificationId(context);
        notificationManager.notify(notificationId, builder.build());
    }

    private static int getNextNotificationId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_KEY,0);
        int id = sharedPreferences.getInt(LAST_NOTIFICATION_ID_KEY, 0) + 1;
        if (id == Integer.MAX_VALUE) {
            id = 0;
        }
        sharedPreferences.edit().putInt(LAST_NOTIFICATION_ID_KEY, id).apply();
        return id;
    }

}
