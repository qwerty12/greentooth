package com.smilla.greentooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.smilla.greentooth.GreenApplication.APP_KEY;
import static com.smilla.greentooth.GreenApplication.ENABLED_KEY;
import static com.smilla.greentooth.GreenApplication.NOTIFICATION_TAG;
import static com.smilla.greentooth.GreenApplication.POST_DISABLE_NOTIFICATIONS_KEY;
import static com.smilla.greentooth.GreenApplication.PRE_DISABLE_NOTIFICATION_ID;
import static com.smilla.greentooth.Util.isBluetoothConnected;
import static com.smilla.greentooth.Util.isBluetoothEnabled;

public class BluetoothWorker extends Worker {

    public BluetoothWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public @NonNull
    Result doWork() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_KEY, 0);
        boolean greentoothEnabled = sharedPreferences.getBoolean(ENABLED_KEY, false);
        if (isBluetoothEnabled(bluetoothAdapter) && !isBluetoothConnected(bluetoothAdapter)
                && !bluetoothAdapter.isDiscovering() && greentoothEnabled) {
            boolean disabled = bluetoothAdapter.disable();
            if (disabled) {
                //Remove pre-disable notification if there is one
                Util.cancelNotification(context, NOTIFICATION_TAG, PRE_DISABLE_NOTIFICATION_ID);
                boolean postDisableNotificationsEnabled = sharedPreferences.getBoolean(POST_DISABLE_NOTIFICATIONS_KEY, false);
                if (postDisableNotificationsEnabled) {
                    Util.sendNotification(context, context.getString(R.string.post_disable_notification_title),
                            context.getString(R.string.post_disable_notification_body), GreenApplication.NOTIFICATION_TYPE_POST_DISABLE);
                }
                return Result.success();
            } else {
                return Result.failure();
            }
        }
        return Result.success();
    }
}
