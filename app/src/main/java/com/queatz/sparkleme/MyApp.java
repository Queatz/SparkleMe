package com.queatz.sparkleme;

import android.support.multidex.MultiDexApplication;

import com.queatz.beetleconnect.Beetle;
import com.queatz.beetleconnect.BeetleListener;

/**
 * Created by jacob on 9/7/17.
 */

public class MyApp extends MultiDexApplication {

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
