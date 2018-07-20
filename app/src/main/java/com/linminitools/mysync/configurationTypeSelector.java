package com.linminitools.mysync;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;

public class configurationTypeSelector extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_types);

        ArrayList<String> Headers = new ArrayList();

        Headers.add("Rsync Daemon");
        Headers.add("Rsync Remote Shell");

        ExpandableListView lv_config_types = findViewById(R.id.lv_config_types);
        customAdapter adapter = new customAdapter(getApplicationContext(),Headers,21);


        lv_config_types.setAdapter((ExpandableListAdapter) adapter);

    }

}
