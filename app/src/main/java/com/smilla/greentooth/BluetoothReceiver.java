package com.smilla.greentooth;

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

import static com.smilla.greentooth.GreenApplication.APP_KEY;
import static com.smilla.greentooth.GreenApplication.DEFAULT_DELAY;
import static com.smilla.greentooth.GreenApplication.DELAY_KEY;
import static com.smilla.greentooth.GreenApplication.ENABLED_KEY;
import static com.smilla.greentooth.GreenApplication.NOTIFICATION_TAG;
import static com.smilla.greentooth.GreenApplication.PRE_DISABLE_NOTIFICATIONS_KEY;
import static com.smilla.greentooth.GreenApplication.PRE_DISABLE_NOTIFICATION_ID;
import static com.smilla.greentooth.Util.isBluetoothConnected;
import static com.smilla.greentooth.Util.isBluetoothEnabled;


public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            SharedPreferences sharedPreferences = context.getSharedPreferences(APP_KEY, 0);
            boolean greentoothEnabled = sharedPreferences.getBoolean(ENABLED_KEY, false);
            if (isBluetoothEnabled(bluetoothAdapter) && !isBluetoothConnected(bluetoothAdapter)
                    && !bluetoothAdapter.isDiscovering() && greentoothEnabled) {
                long waitTime = sharedPreferences.getInt(DELAY_KEY, DEFAULT_DELAY);
                OneTimeWorkRequest bluetoothWorkRequest = new OneTimeWorkRequest.Builder(
                        BluetoothWorker.class).setInitialDelay(waitTime, TimeUnit.SECONDS).
                        setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS).build();
                WorkManager.getInstance(context).enqueueUniqueWork("bluetoothJob",
                        ExistingWorkPolicy.KEEP, bluetoothWorkRequest);
                //Send low-priority notification here too allow user to keep Bluetooth on
                boolean preDisableNotification = sharedPreferences.getBoolean(PRE_DISABLE_NOTIFICATIONS_KEY, false);
                if (preDisableNotification) {
                    Util.sendNotification(context, context.getString(R.string.pre_disable_notification_title),
                            context.getString(R.string.pre_disable_notification_body), GreenApplication.NOTIFICATION_TYPE_PRE_DISABLE);
                }
            }
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            //Cancel the pre-disable notification if device becomes connected
            Util.cancelNotification(context, NOTIFICATION_TAG, PRE_DISABLE_NOTIFICATION_ID);
        } else if (GreenApplication.ACTION_TEMP_DISABLE.equals(action)) {
            //User selected button to keep Bluetooth on for now: cancel job to disable it and remove this notification
            WorkManager.getInstance(context).cancelAllWork();
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
            Util.cancelNotification(context, NOTIFICATION_TAG, PRE_DISABLE_NOTIFICATION_ID);
        } else if (GreenApplication.ACTION_DISABLE.equals(action)) {
            //User selected button to disable Greentooth altogether
            WorkManager.getInstance(context).cancelAllWork();
            SharedPreferences sharedPreferences = context.getSharedPreferences(APP_KEY, 0);
            sharedPreferences.edit().putBoolean(ENABLED_KEY, false).apply();
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
            Util.cancelNotification(context, NOTIFICATION_TAG, PRE_DISABLE_NOTIFICATION_ID);
            context.sendBroadcast(new Intent(GreenApplication.ACTION_SWITCH_OFF));
        }
    }
}
