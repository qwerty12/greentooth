package com.smilla.greentooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.smilla.greentooth.GreenApplication.APP_KEY;
import static com.smilla.greentooth.GreenApplication.ENABLED_KEY;
import static com.smilla.greentooth.GreenApplication.NOTIFICATIONS_KEY;
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
                boolean notificationsEnabled = sharedPreferences.getBoolean(NOTIFICATIONS_KEY, false);
                if (notificationsEnabled) {
                    Util.sendNotification(context, context.getString(R.string.notification_title),
                            context.getString(R.string.notification_body));
                }
                return Result.success();
            } else {
                return Result.failure();
            }
        }
        return Result.success();
    }
}
