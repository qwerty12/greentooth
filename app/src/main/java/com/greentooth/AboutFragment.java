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

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.NotNull;


public class AboutFragment extends DialogFragment {
    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Greentooth version ");
        //In case something goes wrong
        String version = "1.0";
        try {
            version = getContext().getPackageManager().getPackageInfo(
                    "com.greentooth", 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        stringBuilder.append(version);
        stringBuilder.append(getContext().getText(R.string.about_string));
        builder.setMessage(stringBuilder.toString())
                .setCancelable(false)
                .setTitle("About")
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton(R.string.about_button_positive, (dialogInterface, id) -> dialogInterface.dismiss());
        return builder.create();
    }
}