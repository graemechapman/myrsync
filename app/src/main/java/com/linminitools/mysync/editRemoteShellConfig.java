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
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Map;

import static com.linminitools.mysync.MainActivity.appContext;
import static com.linminitools.mysync.MainActivity.configs;
import static com.linminitools.mysync.MainActivity.getPath;

public class editRemoteShellConfig extends addRemoteShellConfig {

    public addRemoteShellConfig r;

@Override
    protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);


    ViewGroup vg = findViewById(android.R.id.content);
    Context ctx = this.getBaseContext();
    r = new addRemoteShellConfig(ctx,vg);
    setContentView(r.vi);

    Intent i = getIntent();
    final int p = i.getIntExtra("pos", -1);
    RS_Configuration config = configs.get(p);

    r.user.setText(config.rs_user);
    r.remote_host_ip.setText(config.rs_ip);
    r.destination.setText(config.rs_dest);
    r.tv_path.setVisibility(View.VISIBLE);
    r.tv_path.setText("Selected Path: "+ config.local_path);
    Button execute = r.vi.findViewById(R.id.bt_execute);

    ImageButton rsync_help = r.rsync_help;
    ImageButton ssh_help = r.ssh_keys_help;
    rsync_help.setImageResource(android.R.drawable.ic_menu_help);
    ssh_help.setImageResource(android.R.drawable.ic_menu_help);

    r.options=(config.rs_options) ;

    if (r.options == "-") {
        r.options = "";
    }

    if (!r.options.isEmpty()) {
        String rs_options = r.options.substring(1);
        for (char x : rs_options.toCharArray()) {
            int resID = getResources().getIdentifier("cb_" + String.valueOf(x), "id", getPackageName());
            CheckBox cb = findViewById(resID);
            cb.setChecked(true);
        }
    }

    r.save_rsh.setEnabled(true);
    r.view_cmd_bt.setEnabled(true);
    execute.setVisibility(View.VISIBLE);

    final int id=config.id;

    r.save_rsh.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            action2Config(v,id,p,1,r);
        }
    });

    execute.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            action2Config(v, id, p,2,r);
        }
    });

    r.add_path.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            action2Config(v,id,p,3,r);
        }
    });

    r.view_cmd_bt.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            action2Config(v,id,p,4,r);
        }
    });


}


    protected void action2Config(View v, int id, int position, int request,addRemoteShellConfig r){
        Map<String,String> configMap=r.processForm(v);

        if (!configMap.get("rs_ip").isEmpty() && !configMap.get("rs_user").isEmpty() &&
                !configMap.get("rs_dest").isEmpty() && !configMap.get("local_path").isEmpty()) {

            for (RS_Configuration c : configs){

                if (c.id==id){
                    c.rs_ip = configMap.get("rs_ip");
                    c.rs_user = configMap.get("rs_user");
                    c.rs_options = configMap.get("rs_options");
                    c.local_path = configMap.get("local_path");
                    c.rs_dest = configMap.get("rs_dest");
                    c.mode = Integer.parseInt(configMap.get("rs_mode"));


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
                        r.viewCommand(v);
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


                r.Update_view(local_path);


            }

        }
    }



}
