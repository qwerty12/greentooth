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

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.greentooth.GreenApplication.APP_KEY;
import static com.greentooth.GreenApplication.DELAY_KEY;
import static com.greentooth.GreenApplication.ENABLED_KEY;
import static com.greentooth.GreenApplication.NOTIFICATIONS_KEY;
import static com.greentooth.GreenApplication.THEME_KEY;
import static com.greentooth.GreenApplication.TIME_SPINNER_POSITION_KEY;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    int[] timeSpinnerValues;
    SwitchCompat onSwitch;
    SwitchCompat notifSwitch;
    Spinner timeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        androidx.appcompat.widget.Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        sharedPreferences = this.getSharedPreferences(APP_KEY, 0);
        if (BuildConfig.DEBUG) {
            int[] temp = getResources().getIntArray(R.array.wait_values);
            timeSpinnerValues = new int[temp.length+1];
            System.arraycopy(temp, 0, timeSpinnerValues, 1, temp.length);
            timeSpinnerValues[0] = 0;
        } else {
            timeSpinnerValues = getResources().getIntArray(R.array.wait_values);
        }

        onSwitch = findViewById(R.id.onSwitch);
        onSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean(ENABLED_KEY, isChecked).apply());
        timeSpinner = findViewById(R.id.timeSpinner);
        List<String> wait_entries = new ArrayList<String>(Arrays.asList(
                getResources().getStringArray(R.array.wait_entries)));
        if (BuildConfig.DEBUG) {
            wait_entries.add(0, "None");
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wait_entries);
        timeAdapter.setDropDownViewResource(com.google.android.material.R.layout.support_simple_spinner_dropdown_item);

        timeSpinner.setAdapter(timeAdapter);

        MaterialCardView switchCard = findViewById(R.id.switchCard);
        switchCard.setOnClickListener(v -> onSwitch.setChecked(!onSwitch.isChecked()));
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferences.edit().putInt(DELAY_KEY, timeSpinnerValues[position]).apply();
                sharedPreferences.edit().putInt(TIME_SPINNER_POSITION_KEY, position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // *tumbleweeds roll by*
            }
        });
        MaterialCardView waitCard = findViewById(R.id.timeCard);
        waitCard.setOnClickListener(v -> timeSpinner.performClick());
        notifSwitch = findViewById(R.id.notifSwitch);
        notifSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean(NOTIFICATIONS_KEY, isChecked).apply());
        MaterialCardView notifCard = findViewById(R.id.notifCard);
        notifCard.setOnClickListener(v -> notifSwitch.setChecked(!notifSwitch.isChecked()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        timeSpinner.setSelection(sharedPreferences.getInt(TIME_SPINNER_POSITION_KEY, 0));
        onSwitch.setChecked(sharedPreferences.getBoolean(ENABLED_KEY, false));
        notifSwitch.setChecked(sharedPreferences.getBoolean(NOTIFICATIONS_KEY, false));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        int themeItemId = sharedPreferences.getInt(THEME_KEY, R.id.action_default_theme);
        menu.findItem(themeItemId).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getGroupId() == R.id.theme_group) {
            item.setChecked(true);
            sharedPreferences.edit().putInt(THEME_KEY, item.getItemId()).apply();
        }
        switch (item.getItemId()) {
            case R.id.action_about:
                com.greentooth.AboutFragment about = new AboutFragment();
                about.show(getSupportFragmentManager(), "com.greentooth.AboutFragment");
                return true;
            case R.id.action_dark_theme:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return true;
            case R.id.action_light_theme:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return true;
            case R.id.action_default_theme:
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
}
