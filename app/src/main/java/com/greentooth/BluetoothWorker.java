package com.greentooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.greentooth.Util.isEnabled;
import static com.greentooth.Util.notConnected;

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
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_name), 0);
        boolean switchedOn = sharedPreferences.getBoolean("isEnabled", false);
        if (isEnabled(bluetoothAdapter) && notConnected(bluetoothAdapter) && switchedOn) {
            boolean disabled = bluetoothAdapter.disable();
            if (disabled) {
                boolean enableNotif = sharedPreferences.getBoolean("enableNotifications", false);
                if (enableNotif) {
                    Util.SendNotification(context, "Bluetooth disabled",
                            "No devices connected.");
                }
                Toast.makeText(context, "Bluetooth disabled.", Toast.LENGTH_SHORT).show();
                return Result.success();
            } else {
                return Result.failure();
            }
        }
        return Result.success();
    }

}
