package com.queatz.sparkleme;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.queatz.beetleconnect.Beetle;
import com.queatz.beetleconnect.BeetleListener;
import com.queatz.sparkleme.ui.ColorChooser;

public class MainActivity extends Activity {

    private static final int LOCATION = 12;

    private Config config;
    private BeetleListener beetleListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        config = new Config(this);
        Beetle.setFindFirst(true);
        Beetle.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        Beetle.getBeetleManager().setHighPower(true);
        Beetle.stayConnected(config.autoOnOff());

        final TextView status = findViewById(R.id.status);
        final ColorChooser colorChooser = findViewById(R.id.color);
        final SeekBar seekBar = findViewById(R.id.brightness);
        final CheckBox autoOnOff = findViewById(R.id.autoOnOff);

        seekBar.setProgress(50);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                colorChooser.setBrightness((float) progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        colorChooser.setListener(new ColorChooser.OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                if (Beetle.getBeetleManager() == null) {
                    return;
                }

                if (config.autoOnOff()) {
                    config.myColor(color);
                }

                Func.sendColor(color);
            }
        });

        autoOnOff.setChecked(config.autoOnOff());

        autoOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                config.autoOnOff(value);
                Beetle.stayConnected(value);
                sendConfig();
            }
        });

        beetleListener = new BeetleListener() {
            @Override
            public void onDisconnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status.setText(R.string.connection_lost);
                    }
                });
            }

            @Override
            public void onRead(String string) {
            }

            @Override
            public void onConnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status.setText(R.string.connected);
                    }
                });
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        Beetle.subscribe(beetleListener);
        enable();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Beetle.unsubscribe(beetleListener);
        disable();
    }

    private void sendConfig() {
        Beetle.getBeetleManager().send("+xd" + (config.autoOnOff() ? "1" : "0") + "=");
    }

    private void enable() {
        final TextView status = findViewById(R.id.status);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION);

            status.setText(R.string.starting);
            return;
        }

        Beetle.getBeetleManager().enable();
    }

    private void disable() {
        if (!config.autoOnOff()) {
            Beetle.getBeetleManager().disable();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        final TextView status = findViewById(R.id.status);

        for (int i = 0; i < permissions.length; i++) {
            if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[i])) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    enable();
                } else {
                    status.setText(R.string.needs_permisison);
                }
            }
        }
    }
}
