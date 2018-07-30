package com.linminitools.mysync;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import static com.linminitools.mysync.MainActivity.appContext;
import static com.linminitools.mysync.MainActivity.configs;
import static com.linminitools.mysync.MainActivity.getPath;


public class addDaemonConfig extends AppCompatActivity {

    private int id;
    public Button save_config,view_cmd,add_path;
    public EditText ed_user;
    public LayoutInflater inflater;
    public View vi =null;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.add_daemon_config);
        //inflater=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //vi=inflater.inflate(R.layout.add_daemon_config,null);
        Button save = findViewById(R.id.bt_save);
        Button view = findViewById(R.id.bt_view);
        save.setEnabled(false);
        view.setEnabled(false);
    }

    public addDaemonConfig(){};

    public addDaemonConfig(Context ctx, ViewGroup parent){

        Log.d("Parent" , String.valueOf(parent));
        Log.d("View", String.valueOf(vi));
        inflater=(LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vi=inflater.inflate(R.layout.add_daemon_config,parent,false);
        context=ctx;
        add_path=vi.findViewById(R.id.bt_add);
        add_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPath(view,context);
            }
        });
        /*
        save_config = vi.findViewById(R.id.bt_save);
        view_cmd = vi.findViewById(R.id.bt_view);
        add_path = vi.findViewById(R.id.bt_add);
                vi=inflater.inflate(R.layout.add_daemon_config,null);

        ed_user = vi.findViewById(R.id.ed_rsync_user);
        save_config.setEnabled(false);
        view_cmd.setEnabled(false);
        */
    };


    public void addPath(View v, Context context){
        Uri selectedUri = Uri.parse(Environment.getDataDirectory().toString());
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE,selectedUri,context, Activity.class);

        startActivityForResult(Intent.createChooser(i,"Choose Directory"),1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected path

                Uri pathUri = data.getData();
                Uri dirUri = DocumentsContract.buildDocumentUriUsingTree(pathUri,
                        DocumentsContract.getTreeDocumentId(pathUri));
                String local_path = getPath(this, dirUri);


                SharedPreferences path_prefs = appContext.getSharedPreferences("Rsync_Config_path", MODE_PRIVATE);
                SharedPreferences.Editor path_prefseditor = path_prefs.edit();
                path_prefseditor.putString("local_path",local_path);
                path_prefseditor.apply();

                TextView tv_path= findViewById(R.id.tv_path);
                if (!local_path.isEmpty()) {
                    tv_path.setText(path_prefs.getString("local_path", ""));
                    tv_path.setVisibility(View.VISIBLE);

                    Button tv_addpath = findViewById(R.id.bt_add);
                    tv_addpath.setText("Change Path");

                    Button save = findViewById(R.id.bt_save);
                    Button view = findViewById(R.id.bt_view);
                    save.setEnabled(true);
                    view.setEnabled(true);

                }

            }
        }
    }



    protected Map<String,String> processForm(View v){

        Map<String,String> configMap = new HashMap<String,String>();

        EditText et_srv_ip= findViewById(R.id.ed_srv_ip);
        EditText et_srv_port= findViewById(R.id.ed_srv_port);
        EditText et_rs_user= findViewById(R.id.ed_rsync_user);
        EditText et_rs_mod= findViewById(R.id.ed_rsync_mod);


        String options="-";

        String[] availableOptions={"a","r","z","v","n","p","t","O","q","m","u","g"};

        for(String check_box : availableOptions){
            int resID = getResources().getIdentifier("cb_" + check_box, "id", getPackageName());
            CheckBox cb = findViewById(resID);
            if(cb.isChecked()){
                options=options.concat(check_box);
            }
        }
        if (options=="-"){options="";}

        String log="";
        String rs_user=String.valueOf(et_rs_user.getText()) ;
        String rs_ip=String.valueOf(et_srv_ip.getText());
        String rs_port=String.valueOf(et_srv_port.getText());
        String rs_module=String.valueOf(et_rs_mod.getText());
        String local_path=appContext.getSharedPreferences("Rsync_Config_path", MODE_PRIVATE).getString("local_path","");
        if (rs_port.isEmpty()) { rs_port="873";}

        configMap.put("rs_ip",rs_ip);
        configMap.put("rs_user",rs_user);
        configMap.put("rs_port",rs_port);
        configMap.put("rs_module",rs_module);
        configMap.put("local_path",local_path);
        configMap.put("rs_options",options);

        return configMap;
    }



    public void viewCommand(View v){

        Map<String,String> configMap=processForm(v);

        String options = configMap.get("rs_options");
        String rs_user = configMap.get("rs_user");
        String rs_ip = configMap.get("rs_ip");
        String rs_port = configMap.get("rs_port");
        String rs_module = configMap.get("rs_module");
        String local_path = configMap.get("local_path");


        TextView tv= findViewById(R.id.tv_rs_cmd_View);

        String Rsync_command = "rsync "+options+" "+ local_path+" "+"rsync://"+rs_user+"@"+rs_ip+":"+rs_port+"/"+rs_module;


        tv.setText(Rsync_command);

    }


    public void saveConfig(View v){
        Map<String,String> configMap=processForm(v);

        String rs_options = configMap.get("rs_options");
        String rs_user = configMap.get("rs_user");
        String rs_ip = configMap.get("rs_ip");
        String rs_port = configMap.get("rs_port");
        String rs_module = configMap.get("rs_module");
        String local_path = configMap.get("local_path");



        if (!rs_ip.isEmpty() && !rs_module.isEmpty() && !local_path.isEmpty()) {


            int counter=configs.size()+1;

            RS_Configuration config = new RS_Configuration(counter);

            config.rs_ip = rs_ip;
            config.rs_user = rs_user;
            config.rs_port = rs_port;
            config.rs_options = rs_options;
            config.rs_module = rs_module;
            config.local_path = local_path;
            config.saveToDisk();
            configs.add(config);

        }
        else{

            Context context = getApplicationContext();
            CharSequence text = "Configuration is not Complete!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }


        this.finish();
    }

    public void rsync_help(View v){
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Rsync Options Help")
                        .setMessage(getResources().getString(R.string.rsync_options))
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                ;
        alertDialogBuilder.show();
    }
}
