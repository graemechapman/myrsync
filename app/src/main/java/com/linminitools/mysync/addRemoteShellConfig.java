package com.linminitools.mysync;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

public class addRemoteShellConfig extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_daemon_config);
        Button save = findViewById(R.id.bt_save);
        Button view = findViewById(R.id.bt_view);
        save.setEnabled(false);
        view.setEnabled(false);

    }
}
