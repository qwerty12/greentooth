package com.smilla.greentooth;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;


public class AboutFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Greentooth version ");
        //In case something goes wrong
        String version = "1.0";
        try {
            version = getContext().getPackageManager().getPackageInfo("com.smilla.greentooth", 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        stringBuilder.append(version);
        stringBuilder.append(getContext().getText(R.string.about_string));
        final SpannableString message =  new SpannableString(stringBuilder.toString());
        Linkify.addLinks(message, Linkify.ALL);
        builder.setMessage(message)
                .setCancelable(false)
                .setTitle("About")
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton(R.string.okay, (dialogInterface, id) -> dialogInterface.dismiss());
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((TextView) getDialog().findViewById(android.R.id.message))
                .setMovementMethod(LinkMovementMethod.getInstance());
    }
}