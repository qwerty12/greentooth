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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import static com.greentooth.GreenApplication.APP_KEY;
import static com.greentooth.GreenApplication.DELAY_KEY;
import static com.greentooth.GreenApplication.ENABLED_KEY;
import static com.greentooth.GreenApplication.NOTIFICATIONS_KEY;
import static com.greentooth.GreenApplication.THEME_KEY;
import static com.greentooth.GreenApplication.TIME_SPINNER_POSITION_KEY;

public class MainActivity extends AppCompatActivity {
    private int shortAnimationDuration;
    private SharedPreferences sharedPreferences;
    private SwitchCompat onSwitch;
    private Spinner timeSpinner;
    private SwitchCompat notificationsSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MaterialToolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //Set instance variables
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        sharedPreferences = this.getSharedPreferences(APP_KEY, 0);
        onSwitch = findViewById(R.id.onSwitch);
        timeSpinner = findViewById(R.id.timeSpinner);
        notificationsSwitch = findViewById(R.id.notificationsSwitch);

        //Set listeners
        onSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(ENABLED_KEY, isChecked).apply();
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
        MaterialCardView switchCard = findViewById(R.id.switchCard);
        switchCard.setOnClickListener(v -> onSwitch.setChecked(!onSwitch.isChecked()));

        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int[] values = getApplicationContext().getResources().getIntArray(R.array.wait_values);
                sharedPreferences.edit().putInt(DELAY_KEY, values[position]).apply();
                sharedPreferences.edit().putInt(TIME_SPINNER_POSITION_KEY, position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        View timeClicker = findViewById(R.id.timeClicker);
        timeClicker.setOnClickListener(v -> timeSpinner.performClick());

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean(NOTIFICATIONS_KEY, isChecked).apply());
        View notifClicker = findViewById(R.id.notifClicker);
        notifClicker.setOnClickListener(v -> notificationsSwitch.setChecked(!notificationsSwitch.isChecked()));
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
            case R.id.action_help:
                showHelp();
                return true;
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

    private void updateDescription() {
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
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.help_sheet, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        Button helpButton = dialog.findViewById(R.id.helpButton);
        helpButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
