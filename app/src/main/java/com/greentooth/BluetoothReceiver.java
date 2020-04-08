package com.greentooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.work.BackoffPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import static com.greentooth.GreenApplication.APP_KEY;
import static com.greentooth.GreenApplication.DELAY_KEY;
import static com.greentooth.GreenApplication.ENABLED_KEY;
import static com.greentooth.Util.isBluetoothConnected;
import static com.greentooth.Util.isBluetoothEnabled;


public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            SharedPreferences sharedPreferences = context.getSharedPreferences(APP_KEY, 0);
            boolean greentoothEnabled = sharedPreferences.getBoolean(ENABLED_KEY, false);
            if (isBluetoothEnabled(bluetoothAdapter) && !isBluetoothConnected(bluetoothAdapter)
                    && !bluetoothAdapter.isDiscovering() && greentoothEnabled) {
                long waitTime = sharedPreferences.getInt(DELAY_KEY, 20);
                OneTimeWorkRequest bluetoothWorkRequest = new OneTimeWorkRequest.Builder(
                        BluetoothWorker.class).setInitialDelay(waitTime, TimeUnit.SECONDS).
                        setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS).build();
                WorkManager.getInstance(context).enqueueUniqueWork("bluetoothJob",
                        ExistingWorkPolicy.KEEP, bluetoothWorkRequest);
            }
        }
    }
}
