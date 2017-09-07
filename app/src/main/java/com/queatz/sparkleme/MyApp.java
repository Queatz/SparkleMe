package com.queatz.sparkleme;

import android.app.Application;

import com.queatz.beetleconnect.*;

/**
 * Created by jacob on 9/7/17.
 */

public class MyApp extends Application {

    private Config config;

    @Override
    public void onCreate() {
        super.onCreate();

        config = new Config(this);
        Beetle.setFindFirst(true);
        Beetle.initialize(getApplicationContext());

        Beetle.stayConnected(config.autoOnOff());

        Beetle.subscribe(new BeetleListener() {
            @Override
            public void onDisconnected() {

            }

            @Override
            public void onRead(String string) {
                if ("+a=".equals(string)) {
                    Beetle.getBeetleManager().send("+a=\r\n");
                }
            }

            @Override
            public void onConnected() {
                if (config.autoOnOff()) {
                    Func.sendColor(config.myColor());
                }
            }
        });
    }
}
