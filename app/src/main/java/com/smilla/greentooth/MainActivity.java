package com.smilla.greentooth;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.quicksettings.TileService;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import static com.smilla.greentooth.GreenApplication.APP_KEY;
import static com.smilla.greentooth.GreenApplication.ENABLED_KEY;
import static com.smilla.greentooth.GreenApplication.POST_DISABLE_NOTIFICATIONS_KEY;
import static com.smilla.greentooth.GreenApplication.PRE_DISABLE_NOTIFICATIONS_KEY;
import static com.smilla.greentooth.GreenApplication.THEME_KEY;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity implements DelayFragment.DelayInterface {
    private int shortAnimationDuration;
    private SharedPreferences sharedPreferences;
    private SwitchCompat onSwitch;
    private View timeClicker;
    private TextView delayValue;
    private SwitchCompat preDisableNotificationsSwitch;
    private SwitchCompat postDisableNotificationsSwitch;
    private BroadcastReceiver broadcastReceiver;


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
        timeClicker = findViewById(R.id.timeClicker);
        delayValue = findViewById(R.id.delayValue);
        preDisableNotificationsSwitch = findViewById(R.id.preDisableNotificationSwitch);
        postDisableNotificationsSwitch = findViewById(R.id.postDisableNotificationSwitch);

        //Set listeners
        onSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(ENABLED_KEY, isChecked).apply();
            TextView switchText = findViewById(R.id.switchTitle);
            MaterialCardView switchCard = findViewById(R.id.switchCard);
            final int switchCardDisabledColor = ContextCompat.getColor(this, R.color.switchDisabled);
            final int switchCardEnabledColor = ContextCompat.getColor(this, R.color.primaryColorVariant);
            if (isChecked) {
                switchText.setText(R.string.enabled);
                changeCardColor(switchCard, switchCardDisabledColor, switchCardEnabledColor);
            } else {
                switchText.setText(R.string.disabled);
                changeCardColor(switchCard, switchCardEnabledColor, switchCardDisabledColor);
            }
            updateDescription();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                TileService.requestListeningState(this, new ComponentName(this, OnOffTileService.class));
            }
        });
        MaterialCardView switchCard = findViewById(R.id.switchCard);
        switchCard.setOnClickListener(v -> onSwitch.setChecked(!onSwitch.isChecked()));

        timeClicker.setOnClickListener(v -> {
            DelayFragment delayFragment = new DelayFragment();
            delayFragment.show(getSupportFragmentManager(), "CustomDurationFragment");
        });

        preDisableNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean(PRE_DISABLE_NOTIFICATIONS_KEY, isChecked).apply());
        View preDisableNotificationClicker = findViewById(R.id.preDisableNotificationClicker);
        preDisableNotificationClicker.setOnClickListener(v ->
                preDisableNotificationsSwitch.setChecked(!preDisableNotificationsSwitch.isChecked()));

        postDisableNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean(POST_DISABLE_NOTIFICATIONS_KEY, isChecked).apply());
        View postDisableNotificationClicker = findViewById(R.id.postDisableNotificationClicker);
        postDisableNotificationClicker.setOnClickListener(v ->
                postDisableNotificationsSwitch.setChecked(!postDisableNotificationsSwitch.isChecked()));

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                disableOnSwitch();
            }
        };
        this.registerReceiver(broadcastReceiver, new IntentFilter(GreenApplication.ACTION_SWITCH_OFF));

        checkPermission(369);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDelayText();
        onSwitch.setChecked(sharedPreferences.getBoolean(ENABLED_KEY, false));
        preDisableNotificationsSwitch.setChecked(sharedPreferences.getBoolean(PRE_DISABLE_NOTIFICATIONS_KEY, false));
        postDisableNotificationsSwitch.setChecked(sharedPreferences.getBoolean(POST_DISABLE_NOTIFICATIONS_KEY, false));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(broadcastReceiver);
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
                AboutFragment about = new AboutFragment();
                about.show(getSupportFragmentManager(), "com.smilla.greentooth.AboutFragment");
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

    private void checkPermission(int code) {
        if (Shizuku.isPreV11()) {
            // Pre-v11 is unsupported
            return;
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
            return;

        if (Shizuku.shouldShowRequestPermissionRationale())
            return;

        // Request the permission
        Shizuku.requestPermission(code);
    }

    public void disableOnSwitch() {
        onSwitch.setChecked(false);
    }

    public void updateDelayText() {
        updateDelayText(Util.getSaneDelay(sharedPreferences));
    }

    public void updateDelayText(int delay) {
        delayValue.setText(Util.getDelayString(this, delay));
    }
}
