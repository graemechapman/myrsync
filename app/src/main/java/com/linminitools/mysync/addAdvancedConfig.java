package com.linminitools.mysync;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Base64;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import static com.linminitools.mysync.MainActivity.appContext;
import static com.linminitools.mysync.MainActivity.configs;
import static com.linminitools.mysync.MainActivity.getPath;
import static java.nio.CharBuffer.wrap;

public class addAdvancedConfig extends AppCompatActivity {

    EditText options_field, arg1_field, arg2_field;
    String options;
    TextView view_cmd;
    CheckBox cb_useLogFile;
    Button save, generate_ssh, view_cmd_bt, execute_bt;
    ImageButton info, ssh_keys_help, arg1_path_selector, arg2_path_selector;
    LayoutInflater inflater;
    View vi = null;
    Context context;
    RS_Configuration config;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public addAdvancedConfig(){
    }

    public addAdvancedConfig(Context ctx, ViewGroup parent){
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vi = inflater.inflate(R.layout.add_advanced_config, parent, false);
        context = ctx;

        options_field=vi.findViewById(R.id.et_options_field);
        arg1_field=vi.findViewById(R.id.et_arg1_field);
        arg2_field=vi.findViewById(R.id.et_arg2_field);

        arg1_path_selector=vi.findViewById(R.id.ib_arg1_path_selector);
        arg2_path_selector=vi.findViewById(R.id.ib_arg2_path_selector);
        arg1_path_selector.setTag(R.id.ib_arg1_path_selector,"arg1");
        arg2_path_selector.setTag(R.id.ib_arg2_path_selector,"arg2");

        cb_useLogFile=vi.findViewById(R.id.cb_logfile);

        info = vi.findViewById(R.id.ib_info);
        save= vi.findViewById(R.id.bt_save);
        view_cmd_bt=vi.findViewById(R.id.bt_view);
        ssh_keys_help=vi.findViewById(R.id.ib_keys_help);

        view_cmd=vi.findViewById(R.id.tv_rs_cmd_View);

        generate_ssh = vi.findViewById(R.id.bt_generate_ssh_keys);


        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info_message(v);
            }
        });

        ssh_keys_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ssh_help_dialog(v);
            }
        });


    }

       protected Map<String,String> processForm(View v){
        if (vi==null) {
            if (inflater==null) inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.add_advanced_config, (ViewGroup) findViewById(android.R.id.content), false);
        }

        Map<String,String> configMap = new HashMap<String,String>();

        configMap.put("rs_arg1", String.valueOf(arg1_field.getText()));
        configMap.put("rs_arg2", String.valueOf(arg2_field.getText()));
        configMap.put("rs_options", String.valueOf(options_field.getText()));
        configMap.put("rs_mode","2");

        return configMap;
    }

    public boolean saveConfig(View v){
        Map<String,String> configMap=this.processForm(v);
        Log.d("SAVE_ADVANCED", "TRUE");
        String rs_options = configMap.get("rs_options");
        String arg1 = configMap.get("rs_arg1");
        String arg2 = configMap.get("rs_arg2");
        String rs_logfile="";

        final SharedPreferences prefs =  context.getSharedPreferences("Rsync_Command_build", MODE_PRIVATE);
        String log_path= prefs.getString("log",context.getApplicationInfo().dataDir+"/logfile.log");

        if (cb_useLogFile.isChecked()) rs_logfile="--log-file="+log_path;



        if (!arg1.isEmpty() && !arg2.isEmpty() && !rs_options.isEmpty()) {

            int counter=configs.size()+1;

            RS_Configuration config = new RS_Configuration(counter);
            config.mode=2;
            config.arg1=arg1;
            config.arg2=arg2;
            config.rs_options=rs_options;
            config.rs_logfile=rs_logfile;
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

    public void viewCommand(View v){

        Map<String,String> configMap=this.processForm(v);

        String options = configMap.get("rs_options");
        String arg1 = configMap.get("rs_arg1");
        String arg2 = configMap.get("rs_arg2");
        String rs_logfile = "";

        TextView tv= this.vi.findViewById(R.id.tv_rs_cmd_View);

        final SharedPreferences prefs =  context.getSharedPreferences("Rsync_Command_build", MODE_PRIVATE);
        String log_path= prefs.getString("log",context.getApplicationInfo().dataDir+"/logfile.log");

        if (cb_useLogFile.isChecked()) rs_logfile="--log-file="+log_path;

        String Rsync_command = "rsync "+"-"+options+" "+rs_logfile+" "+arg1+" "+arg2;
        tv.setText(Rsync_command);
    }

    public void info_message(View v) {
        final SpannableString s = new SpannableString(context.getResources().getString(R.string.adv_config_help));
        Linkify.addLinks(s, Linkify.WEB_URLS);

        AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                .setTitle("Advanced Configuration Help")
                .setMessage(s)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
        TextView textView = dialog.findViewById(android.R.id.message);

        textView.setTextSize(12);
        textView.setMovementMethod(LinkMovementMethod.getInstance());


    }

    public void ssh_help_dialog(View v){
        final SpannableString s = new SpannableString(context.getResources().getString(R.string.ssh_help));
        Linkify.addLinks(s, Linkify.ALL);

        AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                .setTitle("SSH Keys Help")
                .setMessage(s)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }







}
