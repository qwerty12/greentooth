package com.smilla.greentooth;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View dialogView = layoutInflater.inflate(R.layout.delay_dialog, null);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.okay, null)
                .create();
        dialog.setView(dialogView);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(APP_KEY, 0);
        AlertDialog dialog = (AlertDialog) getDialog();
        Button okButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
        Button cancelButton = dialog.getButton(Dialog.BUTTON_NEGATIVE);
        NumberPicker minutesPicker = dialog.findViewById(R.id.minutesPicker);
        NumberPicker secondsPicker = dialog.findViewById(R.id.secondsPicker);

        minutesPicker.setMaxValue(GreenApplication.MAX_MINUTE_DELAY);
        minutesPicker.setMinValue(GreenApplication.MIN_MINUTE_DELAY);
        secondsPicker.setMaxValue(GreenApplication.MAX_SECOND_DELAY);
        secondsPicker.setMinValue(GreenApplication.MIN_SECOND_DELAY);

        //Show 01 instead of 1, etc...
        NumberPicker.Formatter formatter = value -> String.format(Locale.getDefault(), "%02d", value);
        minutesPicker.setFormatter(formatter);
        secondsPicker.setFormatter(formatter);

        //Get child EditTexts
        int textId = Resources.getSystem().getIdentifier("numberpicker_input", "id", "android");
        EditText minutesText = minutesPicker.findViewById(textId);
        EditText secondsText = secondsPicker.findViewById(textId);

        okButton.setOnClickListener((dialogInterface) -> {
            minutesPicker.setValue(Integer.parseInt(minutesText.getText().toString()));
            secondsPicker.setValue(Integer.parseInt(secondsText.getText().toString()));
            int delay = minutesPicker.getValue() * 60 + secondsPicker.getValue();
            sharedPreferences.edit().putInt(DELAY_KEY, delay).apply();
            delayInterface.updateDelayText();
            dialog.dismiss();
        });
        cancelButton.setOnClickListener((dialogInterface) -> {
            dialog.dismiss();
        });

        int delay = Util.getSaneDelay(sharedPreferences);
        if (delay > 0) {
            int minutes = delay / 60;
            int seconds = delay % 60;
            minutesPicker.setValue(minutes);
            secondsPicker.setValue(seconds);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            delayInterface = (DelayInterface) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString() + " must implement DelayInterface");
        }
    }
}
