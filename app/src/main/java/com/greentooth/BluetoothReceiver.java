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

import static com.greentooth.Util.isEnabled;
import static com.greentooth.Util.notConnected;


public class BluetoothReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_name), 0);
            boolean switchedOn = sharedPreferences.getBoolean("isEnabled", false);
            if (isEnabled(bluetoothAdapter) && notConnected(bluetoothAdapter) && switchedOn) {
                long waitTime = sharedPreferences.getInt("wait_time", 20);
                OneTimeWorkRequest bluetoothWorkRequest = new OneTimeWorkRequest.Builder(
                        BluetoothWorker.class).setInitialDelay(waitTime, TimeUnit.SECONDS).
                        setBackoffCriteria(BackoffPolicy.LINEAR,
                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS).build();
                WorkManager.getInstance(context).enqueueUniqueWork("Bluetooth_Job",
                        ExistingWorkPolicy.KEEP, bluetoothWorkRequest);
            }
        }
    }
}
