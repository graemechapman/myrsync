package com.linminitools.mysync;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import static com.linminitools.mysync.MainActivity.PackageName;
import static com.linminitools.mysync.MainActivity.appContext;
import static com.linminitools.mysync.MainActivity.configs;


public class addDaemonConfig extends AppCompatActivity {

    private int id;
    public Button save_config, view_cmd, add_path, execute;
    public ImageButton rsync_help_button;
    public EditText et_srv_ip, et_srv_port, et_rs_user, et_rs_mod;
    public TextView tv_path;
    public LayoutInflater inflater;
    public View vi = null;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public addDaemonConfig() {
    }


    public addDaemonConfig(Context ctx, ViewGroup parent) {

        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vi = inflater.inflate(R.layout.add_daemon_config, parent, false);
        context = ctx;
        tv_path = vi.findViewById(R.id.tv_path);
        add_path = vi.findViewById(R.id.bt_add);
        save_config = vi.findViewById(R.id.bt_save);
        rsync_help_button= vi.findViewById(R.id.bt_help_rsync);
        view_cmd = vi.findViewById(R.id.bt_view);
        save_config.setEnabled(false);
        view_cmd.setEnabled(false);
        et_srv_ip= vi.findViewById(R.id.ed_srv_ip);
        et_srv_port= vi.findViewById(R.id.ed_srv_port);
        et_rs_user= vi.findViewById(R.id.ed_rsync_user);
        et_rs_mod= vi.findViewById(R.id.ed_rsync_mod);
        ImageButton rsync_help_button =vi.findViewById(R.id.bt_help_rsync);

    }


    protected void Update_view(String local_path){

        if (!local_path.isEmpty()) {
            tv_path.setText("Selected Path: "+ local_path);
            tv_path.setVisibility(View.VISIBLE);

            add_path.setText("Change Path");

            save_config.setEnabled(true);
            view_cmd.setEnabled(true);

        }
    }


    protected Map<String,String> processForm(View v){
        if (vi==null) {
            if (inflater==null) inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.add_daemon_config, (ViewGroup) findViewById(android.R.id.content), false);
        }

        Map<String,String> configMap = new HashMap<String,String>();

        String options="-";

        String[] availableOptions={"a","r","z","v","n","p","t","O","q","m","u","g"};
        Log.d("PACKAGENAME",PackageName);
        for(String check_box : availableOptions){

            int resID = appContext.getResources().getIdentifier("cb_" + check_box, "id", PackageName);
            Log.d("RESID",String.valueOf(resID));
            CheckBox cb = vi.findViewById(resID);
            if(cb.isChecked()){
                options=options.concat(check_box);
            }
        }

        if (options=="-"){options="";}

        String rs_user = String.valueOf(this.et_rs_user.getText());
        String rs_ip = String.valueOf(this.et_srv_ip.getText());
        String rs_port = String.valueOf(this.et_srv_port.getText());
        String rs_module = String.valueOf(this.et_rs_mod.getText());
        String local_path = this.context.getSharedPreferences("Rsync_Config_path", MODE_PRIVATE).getString("local_path", "");
        if (rs_port.isEmpty()) {
            rs_port = "873";
        }


        configMap.put("rs_mode", "0");
        configMap.put("rs_ip", rs_ip);
        configMap.put("rs_user", rs_user);
        configMap.put("rs_port", rs_port);
        configMap.put("rs_module", rs_module);
        configMap.put("local_path", local_path);
        configMap.put("rs_options", options);


        return configMap;
    }

    public void viewCommand(View v){

        Map<String,String> configMap=this.processForm(v);

        String options = configMap.get("rs_options");
        String rs_user = configMap.get("rs_user");
        String rs_ip = configMap.get("rs_ip");
        String rs_port = configMap.get("rs_port");
        String rs_module = configMap.get("rs_module");
        String local_path = configMap.get("local_path");


        TextView tv= this.vi.findViewById(R.id.tv_rs_cmd_View);

        String Rsync_command = "rsync "+options+" "+ local_path+" "+"rsync://"+rs_user+"@"+rs_ip+":"+rs_port+"/"+rs_module;


        tv.setText(Rsync_command);

    }


    public boolean saveConfig(View v){
        Map<String,String> configMap=this.processForm(v);

        String rs_options = configMap.get("rs_options");
        String rs_user = configMap.get("rs_user");
        String rs_ip = configMap.get("rs_ip");
        String rs_port = configMap.get("rs_port");
        String rs_module = configMap.get("rs_module");
        String local_path = configMap.get("local_path");


        if (!rs_ip.isEmpty() && !rs_module.isEmpty() && !local_path.isEmpty()) {


            int counter=configs.size()+1;

            RS_Configuration config = new RS_Configuration(counter);
            config.mode=(Integer.parseInt(configMap.get("rs_mode")));
            config.rs_ip = rs_ip;
            config.rs_user = rs_user;
            config.rs_port = rs_port;
            config.rs_options = rs_options;
            config.rs_module = rs_module;
            config.local_path = local_path;
            config.saveToDisk();
            configs.add(config);
            return true;

        }
        else{


            CharSequence text = "Configuration is not Complete!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return false;
        }



    }

    public void rsync_help(View v){
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Rsync Options Help")
                        .setMessage(context.getResources().getString(R.string.rsync_options))
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                ;
        alertDialogBuilder.show();
    }

}
