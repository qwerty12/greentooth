package com.smilla.greentooth;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Locale;

import static com.smilla.greentooth.GreenApplication.APP_KEY;
import static com.smilla.greentooth.GreenApplication.DELAY_KEY;

public class DelayFragment extends DialogFragment {

    interface DelayInterface {
        void updateDelayText(int delay);
        void updateDelayText();
    }
    DelayInterface delayInterface;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view = layoutInflater.inflate(R.layout.delay_dialog, null);
        NumberPicker minutesPicker = view.findViewById(R.id.minutesPicker);
        NumberPicker secondsPicker = view.findViewById(R.id.secondsPicker);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(APP_KEY, 0);
        minutesPicker.setMaxValue(GreenApplication.MAX_MINUTE_DELAY);
        minutesPicker.setMinValue(GreenApplication.MIN_MINUTE_DELAY);
        secondsPicker.setMaxValue(GreenApplication.MAX_SECOND_DELAY);
        secondsPicker.setMinValue(GreenApplication.MIN_SECOND_DELAY);
        //Show 01 instead of 1, etc...
        NumberPicker.Formatter formatter = value -> String.format(Locale.getDefault(), "%02d", value);
        minutesPicker.setFormatter(formatter);
        secondsPicker.setFormatter(formatter);
        int currentDelay = Util.getSaneDelay(sharedPreferences);
        if (currentDelay > 0) {
            int minutes = currentDelay / 60;
            int seconds = currentDelay % 60;
            minutesPicker.setValue(minutes);
            secondsPicker.setValue(seconds);
        }
        /* Getting the EditText children and extracting the text from them allows the user to not have to
           press "done" on the software keyboard to save their changes. */
        int textId = Resources.getSystem().getIdentifier("numberpicker_input", "id", "android");
        EditText minutesText = minutesPicker.findViewById(textId);
        EditText secondsText = secondsPicker.findViewById(textId);

        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setPositiveButton(R.string.okay, (dialog, which) -> {
                    minutesPicker.setValue(Integer.parseInt(minutesText.getText().toString()));
                    secondsPicker.setValue(Integer.parseInt(secondsText.getText().toString()));
                    int newDelay = minutesPicker.getValue() * 60 + secondsPicker.getValue();
                    sharedPreferences.edit().putInt(DELAY_KEY, newDelay).apply();
                    delayInterface.updateDelayText();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        alertDialog.setView(view);
        return alertDialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            delayInterface = (DelayInterface) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString() + " must implement DelayInterface");
        }
    }
}
