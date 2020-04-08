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

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;


public class GreenApplication extends Application {
    public static final String APP_KEY = "Greentooth";
    public static final String ENABLED_KEY = "greentoothEnabled";
    public static final String DELAY_KEY = "waitTime";
    public static final String NOTIFICATIONS_KEY = "notificationsEnabled";
    public static final String TIME_SPINNER_POSITION_KEY = "timeSpinnerPosition";
    public static final String THEME_KEY = "theme";
    public static final String CHANNEL_ID = "greentoothChannel";
    public static final String LAST_NOTIFICATION_ID_KEY = "lastNotificationId";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        SharedPreferences sharedPreferences = getSharedPreferences(APP_KEY, 0);
        int themeItemId = sharedPreferences.getInt(THEME_KEY, R.id.action_default_theme);
        switch (themeItemId) {
            case R.id.action_default_theme:
                //Samsung phones have night mode in Android Pie
                if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.P) || (Build.VERSION.SDK_INT == Build.VERSION_CODES.P
                    && Build.MANUFACTURER.equalsIgnoreCase("samsung"))) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                }
                break;
            case R.id.action_dark_theme:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case R.id.action_light_theme:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
