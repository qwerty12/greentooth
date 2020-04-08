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

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {
    private int shortAnimationDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        androidx.appcompat.widget.Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        final SharedPreferences sharedPreferences = this.getSharedPreferences(this.getString(
                R.string.preference_name), 0);
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        SwitchCompat onSwitch = findViewById(R.id.onSwitch);
        onSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("isEnabled", isChecked).apply();
            TextView switchText = findViewById(R.id.switchTitle);
            MaterialCardView switchCard = findViewById(R.id.switchCard);
            final int switchCardDisabledColor = getResources().getColor(R.color.switchDisabled);
            final int switchCardEnabledColor = getResources().getColor(R.color.primaryColorVariant);
            if (isChecked) {
                switchText.setText(R.string.enabled);
                changeCardColor(switchCard, switchCardDisabledColor, switchCardEnabledColor);
            } else {
                switchText.setText(R.string.disabled);
                changeCardColor(switchCard, switchCardEnabledColor, switchCardDisabledColor);
            }
            updateDescription();
        });
        Spinner timeSpinner = findViewById(R.id.timeSpinner);

        MaterialCardView switchCard = findViewById(R.id.switchCard);
        switchCard.setOnClickListener(v -> onSwitch.setChecked(!onSwitch.isChecked()));
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int[] values = getApplicationContext().getResources().getIntArray(R.array.wait_values);
                sharedPreferences.edit().putInt("wait_time", values[position]).apply();
                sharedPreferences.edit().putInt("spinner_position", position).apply();
                updateDescription();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // *tumbleweeds roll by*
            }
        });
        View timeClicker = findViewById(R.id.timeClicker);
        timeClicker.setOnClickListener(v -> timeSpinner.performClick());
        SwitchCompat notifSwitch = findViewById(R.id.notifSwitch);
        notifSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> sharedPreferences.edit().putBoolean(
                "enableNotifications", isChecked).apply());
        View notifClicker = findViewById(R.id.notifClicker);
        notifClicker.setOnClickListener(v -> notifSwitch.setChecked(!notifSwitch.isChecked()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_name), 0);
        Spinner timeSpinner = findViewById(R.id.timeSpinner);
        SwitchCompat onSwitch = findViewById(R.id.onSwitch);
        SwitchCompat notifSwitch = findViewById(R.id.notifSwitch);
        timeSpinner.setSelection(sharedPreferences.getInt("spinner_position", 0));
        onSwitch.setChecked(sharedPreferences.getBoolean("isEnabled", false));
        notifSwitch.setChecked(sharedPreferences.getBoolean("enableNotifications", false));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_name), 0);
        int themeItemId = sharedPreferences.getInt("theme", R.id.action_default_theme);
        menu.findItem(themeItemId).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_name), 0);
        switch (item.getItemId()) {
            case R.id.action_help:
                showHelp();
                return true;
            case R.id.action_about:
                com.greentooth.AboutFragment about = new AboutFragment();
                about.show(getSupportFragmentManager(), "com.greentooth.AboutFragment");
                return true;
            case R.id.action_dark_theme:
                item.setChecked(true);
                sharedPreferences.edit().putInt("theme", item.getItemId()).apply();
                Toast.makeText(this, "Dark theme selected", Toast.LENGTH_SHORT).show();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return true;
            case R.id.action_light_theme:
                item.setChecked(true);
                sharedPreferences.edit().putInt("theme", item.getItemId()).apply();
                Toast.makeText(this, "Light theme selected", Toast.LENGTH_SHORT).show();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return true;
            case R.id.action_default_theme:
                item.setChecked(true);
                sharedPreferences.edit().putInt("theme", item.getItemId()).apply();
                Toast.makeText(this, "Default theme selected", Toast.LENGTH_SHORT).show();
                if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.P) || Build.MODEL.equals("SM-G950F")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateDescription() {
        SwitchCompat onSwitch = findViewById(R.id.onSwitch);
        TextView switchDesc = findViewById(R.id.switchDesc);
        if (onSwitch.isChecked()) {
            switchDesc.setText(getResources().getString(R.string.enabled_desc));
        } else {
            switchDesc.setText(getResources().getString(R.string.disabled_desc));
        }
    }

    private void changeCardColor(final MaterialCardView cardView, int fromColor, int toColor) {
        @SuppressLint("RestrictedApi") ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
        colorAnimation.setDuration(shortAnimationDuration);
        colorAnimation.addUpdateListener(animator -> cardView.setCardBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    private void showHelp() {
        View view = getLayoutInflater().inflate(R.layout.help_sheet, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        Button helpButton = dialog.findViewById(R.id.helpButton);
        helpButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        /*BottomSheetHelpDialogFragment bottomSheetHelpDialogFragment = new BottomSheetHelpDialogFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        bottomSheetHelpDialogFragment.show(fragmentManager, "modalSheetDialog");*/
    }
}
