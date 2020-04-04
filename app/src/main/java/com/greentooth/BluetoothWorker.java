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

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;

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
        if (isEnabled(bluetoothAdapter) && notConnected(bluetoothAdapter) && !bluetoothAdapter.isDiscovering()
                && switchedOn) {
            boolean disabled = bluetoothAdapter.disable();
            if (disabled) {
                boolean enableNotif = sharedPreferences.getBoolean("enableNotifications", false);
                if (enableNotif) {
                    Util.SendNotification(context, "Bluetooth disabled",
                            "No devices connected.");
                }
                return Result.success();
            } else {
                return Result.failure();
            }
        }
        return Result.success();
    }
}
