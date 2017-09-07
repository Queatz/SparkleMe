package com.queatz.sparkleme;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * Created by jacob on 9/6/17.
 */

public class Config {

    private static final String PREFS = "config";
    private static final String AUTO_ON_OFF = "auto_on_off";
    private static final String MY_COLOR = "my_color";

    private SharedPreferences sharedPreferences;

    public Config(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean autoOnOff() {
        return sharedPreferences.getBoolean(AUTO_ON_OFF, false);
    }

    public void autoOnOff(boolean value) {
        sharedPreferences.edit().putBoolean(AUTO_ON_OFF, value).apply();
    }

    public @ColorInt int myColor() {
        return sharedPreferences.getInt(MY_COLOR, Color.WHITE);
    }

    public void myColor(@ColorInt int color) {
        sharedPreferences.edit().putInt(MY_COLOR, color).apply();
    }
}
