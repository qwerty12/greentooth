package com.greentooth;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.card.MaterialCardView;

import static com.greentooth.GreenApplication.APP_KEY;
import static com.greentooth.GreenApplication.DELAY_KEY;
import static com.greentooth.GreenApplication.ENABLED_KEY;
import static com.greentooth.GreenApplication.NOTIFICATIONS_KEY;
import static com.greentooth.GreenApplication.THEME_KEY;
import static com.greentooth.GreenApplication.TIME_SPINNER_POSITION_KEY;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private SwitchCompat onSwitch;
    private Spinner timeSpinner;
    private SwitchCompat notificationsSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        androidx.appcompat.widget.Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //Set instance variables
        sharedPreferences = this.getSharedPreferences(APP_KEY, 0);
        onSwitch = findViewById(R.id.onSwitch);
        timeSpinner = findViewById(R.id.timeSpinner);
        notificationsSwitch = findViewById(R.id.notificationsSwitch);

        //Set listeners for controls
        onSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean(ENABLED_KEY, isChecked).apply());
        int[] timeSpinnerValues = getResources().getIntArray(R.array.wait_values);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferences.edit().putInt(DELAY_KEY, timeSpinnerValues[position]).apply();
                sharedPreferences.edit().putInt(TIME_SPINNER_POSITION_KEY, position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean(NOTIFICATIONS_KEY, isChecked).apply());

        //Declare and set parent card views
        MaterialCardView switchCard = findViewById(R.id.switchCard);
        MaterialCardView waitCard = findViewById(R.id.timeCard);
        MaterialCardView notificationsCard = findViewById(R.id.notifications_card);

        //Set parent listeners to pass clicks to children
        switchCard.setOnClickListener(v -> onSwitch.setChecked(!onSwitch.isChecked()));
        waitCard.setOnClickListener(v -> timeSpinner.performClick());
        notificationsCard.setOnClickListener(v -> notificationsSwitch.setChecked(!notificationsSwitch.isChecked()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        timeSpinner.setSelection(sharedPreferences.getInt(TIME_SPINNER_POSITION_KEY, 0));
        onSwitch.setChecked(sharedPreferences.getBoolean(ENABLED_KEY, false));
        notificationsSwitch.setChecked(sharedPreferences.getBoolean(NOTIFICATIONS_KEY, false));
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
                //Samsung phones have night mode in Android Pie
                if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.P) || (Build.VERSION.SDK_INT == Build.VERSION_CODES.P
                && Build.MANUFACTURER.equalsIgnoreCase("samsung"))) {
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
