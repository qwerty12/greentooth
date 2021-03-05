package com.smilla.greentooth;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

import static com.smilla.greentooth.GreenApplication.APP_KEY;
import static com.smilla.greentooth.GreenApplication.ENABLED_KEY;

@RequiresApi(24)
public class OnOffTileService extends TileService {

    @Override
    public void onStartListening() {
        Tile tile = getQsTile();
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(APP_KEY, 0);
        boolean greentoothEnabled = sharedPreferences.getBoolean(ENABLED_KEY, false);
        if (greentoothEnabled) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.setLabel(getString(R.string.app_name)/* + " " + getString(R.string.enabled)*/);
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel(getString(R.string.app_name)/* + " " + getString(R.string.disabled)*/);
        }
        tile.updateTile();
    }

    @Override
    public void onClick() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(APP_KEY, 0);
        boolean greentoothEnabledNew = !sharedPreferences.getBoolean(ENABLED_KEY, false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(ENABLED_KEY, greentoothEnabledNew);
        editor.commit();
        onStartListening();
    }

    @Override
    public void onTileAdded() {
        onStartListening();
    }

    @Override
    public IBinder onBind(Intent intent) {
        TileService.requestListeningState(this, new ComponentName(this, OnOffTileService.class));
        return super.onBind(intent);
    }
}
