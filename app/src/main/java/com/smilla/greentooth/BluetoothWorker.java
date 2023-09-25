package com.smilla.greentooth;

import android.app.IActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.IBluetoothManager;
import android.content.AttributionSource;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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

import java.lang.reflect.Constructor;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

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
        if (greentoothEnabled && isBluetoothEnabled(bluetoothAdapter) && !isBluetoothConnected(bluetoothAdapter)
                && !bluetoothAdapter.isDiscovering()) {
            boolean disabled;

            try {
                final Constructor<?> bluetoothAdapterConstructor = BluetoothAdapter.class.getDeclaredConstructor(IBluetoothManager.class, AttributionSource.class);
                bluetoothAdapterConstructor.setAccessible(true);

                final AttributionSource attributionSource = (new AttributionSource.Builder(Shizuku.getUid())).setPackageName("com.android.shell").build();
                final IBluetoothManager BLUETOOTH_MANAGER = IBluetoothManager.Stub.asInterface(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(BluetoothAdapter.BLUETOOTH_MANAGER_SERVICE)));

                final BluetoothAdapter bluetoothAdapterShell = (BluetoothAdapter) bluetoothAdapterConstructor.newInstance(BLUETOOTH_MANAGER, attributionSource);
                disabled = bluetoothAdapterShell.disable(true);

                if (disabled) {
                    try {
                        final IActivityManager ACTIVITY_MANAGER = IActivityManager.Stub.asInterface(
                                new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.ACTIVITY_SERVICE)));
                        ACTIVITY_MANAGER.forceStopPackage("com.spotify.lite", 0);
                    } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                Log.w("BluetoothWorker", "Error disabling Bluetooth via Shizuku", e);
                disabled = bluetoothAdapter.disable();
            }

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
