package com.linminitools.mysync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

import static com.linminitools.mysync.MainActivity.appContext;
import static com.linminitools.mysync.MainActivity.configs;
import static com.linminitools.mysync.MainActivity.getPath;

public class editDaemonConfig extends addDaemonConfig {
    public addDaemonConfig d;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup vg = findViewById(android.R.id.content);
        Context ctx = this.getBaseContext();
        d = new addDaemonConfig(ctx,vg);
        setContentView(d.vi);

        Button save = d.save_config;
        Button view = d.view_cmd;
        Button bt_add = d.add_path;
        Button bt_exec = d.vi.findViewById(R.id.bt_execute);
        ImageButton rsync_help = d.rsync_help_button;
        rsync_help.setImageResource(R.drawable.help);

        save.setEnabled(true);
        view.setEnabled(true);
        bt_exec.setEnabled(true);

        bt_add.setText("Change Path");
        bt_exec.setVisibility(View.VISIBLE);

        Intent i = getIntent();
        final int p = i.getIntExtra("pos", 0);
        RS_Configuration config = configs.get(p);

        EditText ed_srv_ip = d.et_srv_ip;
        ed_srv_ip.setText(config.rs_ip);

        EditText ed_rsync_user = d.et_rs_user;
        ed_rsync_user.setText(config.rs_user);

        EditText ed_srv_port = d.et_srv_port;
        ed_srv_port.setText(config.rs_port);

        EditText ed_rsync_mod = d.et_rs_mod;
        ed_rsync_mod.setText(config.rs_module);

        TextView tv_path = d.tv_path;
        tv_path.setVisibility(View.VISIBLE);
        tv_path.setText(config.local_path);

        String rs_options = config.rs_options;

        if (rs_options == "-") {
            rs_options = "";
        }

        if (!rs_options.isEmpty()) {
            String options = rs_options.substring(1);
            for (char x : options.toCharArray()) {
                int resID = getResources().getIdentifier("cb_" + String.valueOf(x), "id", getPackageName());
                CheckBox cb = findViewById(resID);
                cb.setChecked(true);
            }
        }

        final int id = config.id;

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionConfig(v,id,p,1,d);
            }
        });

        bt_exec.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                actionConfig(v, id, p,2,d);
            }
        });

        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionConfig(v,id,p,3,d);
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionConfig(v,id,p,4,d);
            }
        });


    }

    protected void actionConfig(View v, int id, int position, int request,addDaemonConfig d){
        Map<String,String> configMap=d.processForm(v);

        if (!configMap.get("rs_ip").isEmpty() &&
                !configMap.get("rs_module").isEmpty() && !configMap.get("local_path").isEmpty()) {

            for (RS_Configuration c : configs){

                if (c.id==id){
                    c.rs_ip = configMap.get("rs_ip");
                    c.rs_user = configMap.get("rs_user");
                    c.rs_port = configMap.get("rs_port");
                    c.rs_options = configMap.get("rs_options");
                    c.rs_module = configMap.get("rs_module");
                    c.local_path = configMap.get("local_path");


                    if (request==1) {
                        c.saveToDisk();
                        Log.d("Configuration ID",c.rs_user + " " + c.rs_options);
                        CharSequence text = "Configuration saved";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(appContext, text, duration);
                        toast.show();
                        this.finish();
                    }
                    else if (request==2){
                        c.executeConfig(getApplication().getBaseContext());

                        CharSequence text = "Rsync Job Started";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(appContext, text, duration);
                        toast.show();
                        this.finish();
                    }

                    else if (request==3){
                        Uri selectedUri = Uri.parse(Environment.getDataDirectory().toString());
                        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        Intent i2= Intent.createChooser(i,"Choose Directory");
                        startActivityForResult(i2,1);
                    }

                    else if (request==4){
                        d.viewCommand(v);
                    }

                    break;

                }
            }

        }
        else{


            CharSequence text = "Configuration is not Complete!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(appContext, text, duration);
            toast.show();
            this.finish();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected path

                Uri pathUri = data.getData();
                Uri dirUri = DocumentsContract.buildDocumentUriUsingTree(pathUri, DocumentsContract.getTreeDocumentId(pathUri));
                String local_path = getPath(this, dirUri);


                SharedPreferences path_prefs = getSharedPreferences("Rsync_Config_path", MODE_PRIVATE);
                SharedPreferences.Editor path_prefseditor = path_prefs.edit();
                path_prefseditor.putString("local_path", local_path);
                path_prefseditor.apply();

                Log.d("ONACTIVITYRESULT",local_path);


                d.Update_view(local_path);


            }

        }
    }


}
