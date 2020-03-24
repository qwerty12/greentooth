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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        androidx.appcompat.widget.Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        final SharedPreferences sharedPreferences = this.getSharedPreferences(this.getString(
                R.string.preference_name), 0);
        SwitchCompat onSwitch = findViewById(R.id.onSwitch);

        onSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean("isEnabled", isChecked).apply();
            }
        });

        Spinner timeSpinner = findViewById(R.id.timeSpinner);

        MaterialCardView switchCard = findViewById(R.id.switchCard);
        switchCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwitchCompat onSwitch = findViewById(R.id.onSwitch);
                onSwitch.setChecked(!onSwitch.isChecked());
            }
        });

        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int[] values = getApplicationContext().getResources().getIntArray(R.array.wait_values);
                sharedPreferences.edit().putInt("wait_time", values[position]).apply();
                sharedPreferences.edit().putInt("spinner_position", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // *tumbleweeds roll by*
            }
        });

        MaterialCardView waitCard = findViewById(R.id.timeCard);
        waitCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner timeSpinner = findViewById(R.id.timeSpinner);
                timeSpinner.performClick();
            }
        });

        SwitchCompat notifSwitch = findViewById(R.id.notifSwitch);
        notifSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean("enableNotifications", isChecked).apply();
            }
        });
        MaterialCardView notifCard = findViewById(R.id.notifCard);
        notifCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwitchCompat notifSwitch = findViewById(R.id.notifSwitch);
                notifSwitch.setChecked(!notifSwitch.isChecked());
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                com.greentooth.AboutFragment about = new AboutFragment();
                about.show(getSupportFragmentManager(), "com.greentooth.AboutFragment");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }




}
