package com.queatz.sparkleme;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.queatz.beetleconnect.Beetle;

/**
 * Created by jacob on 9/7/17.
 */

public class Func {
    public static void sendColor(@ColorInt int color) {
        Beetle.getBeetleManager().send("+c" +
                Color.red(color) +
                ":" +
                Color.green(color) +
                ":" +
                Color.blue(color) + "=\r\n"
        );
    }
}
